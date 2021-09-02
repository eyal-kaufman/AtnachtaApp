package com.example.atnachta
import android.os.Bundle
import android.os.Environment
import android.util.Log
import android.view.*
import android.widget.PopupMenu
import androidx.databinding.DataBindingUtil
import androidx.fragment.app.Fragment
import androidx.navigation.findNavController
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.atnachta.data.Profile
import com.example.atnachta.databinding.FragmentRecycleSearchBinding
import com.firebase.ui.firestore.FirestoreRecyclerOptions
import com.google.firebase.firestore.CollectionReference
import com.google.firebase.firestore.DocumentSnapshot
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase
import org.apache.commons.csv.CSVFormat
import org.apache.commons.csv.CSVPrinter
import java.io.*


// TODO: Rename parameter arguments, choose names that match
// the fragment initialization parameters, e.g. ARG_ITEM_NUMBER
private const val ARG_PARAM1 = "param1"
private const val ARG_PARAM2 = "param2"

private const val PROFILES_COLLECTION = "profiles"
private const val REFERENCE_COLLECTION = "References"
private const val ATTENDANCE_COLLECTION = "attendance"



/**
 * A simple [Fragment] subclass.
 * Use the [RecycleSearch.newInstance] factory method to
 * create an instance of this fragment.
 */
class RecycleSearch : Fragment(), ProfileAdapter.OnProfileSelectedListener,
    MenuItem.OnMenuItemClickListener {
    // TODO: 1. Check if the recyclerView gets data
    // TODO: 2. Add onClickListeners to the Search button and search results

    private var param1: String? = null
    private var param2: String? = null

    lateinit var binding : FragmentRecycleSearchBinding
    lateinit var firestore: FirebaseFirestore
    lateinit var profileCollection: CollectionReference
    lateinit var adapter: ProfileAdapter
    lateinit var docRefID: String
    lateinit var initialSearchInput: String
    val TAG : String = "RecycleView"
    var fileName =""
    var filepath = ""
    ///samples


    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
//        activity?.menuInflater.inflate(R.menu.menu_main)
        firestore = Firebase.firestore

        profileCollection = firestore.collection(PROFILES_COLLECTION)
        initialSearchInput = RecycleSearchArgs.fromBundle(requireArguments()).searchInput
        setUpRecyclerView()

        // onClickListener for the search button - update result list
        binding.searchButton.setOnClickListener {
            updateQuery(binding.searchInput.text.toString()) }

        binding.newProfileButton.setOnClickListener{ v : View ->
            v.findNavController().navigate(
            RecycleSearchDirections.actionRecycleSearchToNewReference(true,""))}
        if (!isFileExists()){
            binding.newProfileButton.isEnabled = false
        }


    }
    override fun onCreateOptionsMenu(menu: Menu, inflater: MenuInflater) {
        inflater.inflate(R.menu.menu_recycle_fragment, menu)
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {

        return when (item.itemId) {
            R.id.export_to_mail -> {

//                FireStoreHandler.createReport(firestore.collection(ATTENDANCE_COLLECTION), 8,2021)
                sendProfilesByEmail()
                true
            }
            R.id.export_all_ref_email ->{
//                sendReferenceByEmail()
                true
            }

            else -> super.onOptionsItemSelected(item)
        }
    }
    /**
     * Click handler for the search button.
     * Updates the query the adapter works with, according to what the user searched
     * @param searchInput the text the user typed at the search bar
     */
    private fun updateQuery(searchInput: String) {
        val newQuery : Query = if (searchInput.isBlank()){ // if blank just show everything
            profileCollection.orderBy("firstName", Query.Direction.DESCENDING)
        } else{ // query by input
            profileCollection.whereArrayContains("searchList", searchInput)
        }
        val newOptions : FirestoreRecyclerOptions<Profile> = FirestoreRecyclerOptions.Builder<Profile>()
            .setQuery(newQuery, Profile::class.java)
            .build()

        // Change options of adapter.
        adapter.updateOptions(newOptions)

    }

    private fun setUpRecyclerView() {
        // set up a query which gets all of the profiles in the database
        val query : Query = if (initialSearchInput.isBlank()){ // if blank just show everything
            profileCollection.orderBy("firstName", Query.Direction.DESCENDING)
        } else{ // query by input
            profileCollection.whereArrayContains("searchList", initialSearchInput)
        }

        // define the options object (firebaseUI class), that gets the query into the adapter
        val firestoreRecyclerOptions : FirestoreRecyclerOptions<Profile> = FirestoreRecyclerOptions.Builder<Profile>()
            .setQuery(query, Profile::class.java)
            .build()

        adapter = ProfileAdapter(firestoreRecyclerOptions,this)
        binding.resultList.layoutManager = LinearLayoutManager(activity) // still not sure what that is, but is needed
        binding.resultList.adapter = adapter

    }
    fun isFileExists(): Boolean {
        val extra = Environment.getExternalStorageState()
        if (extra.equals(Environment.MEDIA_MOUNTED) ){
            return true
        }
        return false
    }

    fun sendReferenceByEmail(){
        fileName ="referenceReport.csv"
        filepath = "reports"
        val file = File(context?.getExternalFilesDir(filepath), fileName)

        try{
            profileCollection.get().addOnSuccessListener { profileDocs ->
                var headers : MutableList<String> = mutableListOf()
                val writer = file.bufferedWriter()

                writer.write("\ufeff")//important for input in hebrew
                val csvPrinter = CSVPrinter(writer, CSVFormat.DEFAULT)
                for (profile in profileDocs){
                    profileCollection.document(profile.id).collection("References").get().addOnSuccessListener { referenceDocs ->
                        var referenceMap: MutableMap<String, Any> = mutableMapOf()
                        for (reference in referenceDocs){
                            referenceMap = reference.data
                            referenceMap["profileName"] = profile.data["firstName"].toString() + " "+ profile.data["lastName"].toString()
//                            if (headers.size == 0) {
//
//                                headers = referenceMap.keys.toMutableList()
//                                headers.sort()
//
//                                DataToCSV.mapFields(headers, DataToCSV.referenceFieldMap)
//                                csvPrinter.printRecord(headers)
//
//                            }
                            csvPrinter.printRecord(referenceMap.toSortedMap().values)
                        }

                    }

                }
                DataToCSV.sendEmail(requireContext(), file, "Profile Report",csvPrinter)

            }
        } catch (e :FileNotFoundException ){
            e.printStackTrace()
        } catch (e : IOException){
            e.printStackTrace()
        }
    }
    fun sendProfilesByEmail(){

        fileName ="profileReport.csv"
        filepath = "reports"
//        filename ="myFile.csv"
//        filepath = "MyFileDir"
        val myextrnal = File(context?.getExternalFilesDir(filepath), fileName)


//        var headers : MutableList<String> = mutableListOf()
        try{
            profileCollection.orderBy("firstName", Query.Direction.DESCENDING)
                .get()
                .addOnSuccessListener{profileDocuments->

                    val csvPrinter = DataToCSV.writeCSV(profileDocuments, myextrnal, DataToCSV.fieldsMap)
//                    val writer = myextrnal.bufferedWriter()
//                    val csvPrinter = CSVPrinter(writer, CSVFormat.DEFAULT)
//                    writer.write("\ufeff")//important for input in hebrew
//
//                    var profileMap: MutableMap<String,Any?> = mutableMapOf()
//                    for (doc in profileDocuments){
//                        profileMap = doc.data
//                        profileMap.remove("searchList")
//                        if (headers.size==0) {
//
//                            headers = profileMap.keys.toMutableList()
//                            headers.sort()
//                            Log.d(TAG, "${doc.id} BEFORE => ${headers}")
//                            DataToCSV.mapFields(headers)
//                            Log.d(TAG, "${doc.id} AFTER => ${headers}")
//                            csvPrinter.printRecord(headers)
//                        }
//                        csvPrinter.printRecord(profileMap.toSortedMap().values)
//
//                        Log.d(TAG, "${doc.id} => ${profileMap.toSortedMap()}")
//
//                    }
                    DataToCSV.sendEmail(requireContext(), myextrnal, "Profile Report",csvPrinter)
//                    val path: Uri =
//                        FileProvider.getUriForFile(requireContext(), "com.example.atnachta.fileprovider", myextrnal)
//
//                    val fileIntent = Intent(Intent.ACTION_SEND)
//                    fileIntent.type = "text/csv"
//                    fileIntent.putExtra(Intent.EXTRA_SUBJECT, "Data")
//                    fileIntent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
//                    fileIntent.putExtra(Intent.EXTRA_STREAM, path)
//                    startActivity(fileIntent)
//
//                    csvPrinter.flush()
//                    csvPrinter.close()
                }

        } catch (e :FileNotFoundException ){
            e.printStackTrace()
        } catch (e : IOException){
            e.printStackTrace()
        }


    }

    fun handleDeleteProfile(){
        val profileDocReference = profileCollection.document(docRefID)
        val referenceCollection = profileDocReference.collection(REFERENCE_COLLECTION)
//        val referencesList : MutableCollection<String> = mutableListOf()
//        referenceCollection.get()
//            .addOnSuccessListener { documents ->
//                for (doc in documents) {
//                    referencesList.add(doc.id)
//                }
//            }
        FireStoreHandler.deleteProfile(profileDocReference, referenceCollection, null, firestore.collection(ATTENDANCE_COLLECTION))
    }
    /**
     * the click handler for pressing a result item
     * @param snapshot docSnapshot of the selcted profile. We get the snapshot from the viewHolder
     * (specifically in onBindViewHolder), which gets using firebaseUI methods
     */
    override fun onProfileSelected(view : View, snapshot: DocumentSnapshot, longPress: Boolean) {
//        val docId : String = snapshot.id
        docRefID = snapshot.id
        Log.d(TAG, docRefID)
        if (longPress){
            PopupMenu(context,view).apply {
                setOnMenuItemClickListener(PopupMenu.OnMenuItemClickListener{menuItem->
                    when (menuItem?.itemId){
                    R.id.add_reference ->{
                        Log.d(TAG, "long press>add reference @@@@@@@@@@@@")
                        findNavController().navigate(RecycleSearchDirections.actionRecycleSearchToNewReference(false, docRefID))
                        true
                    }
                    R.id.delete_profile ->{
                        handleDeleteProfile()
                        true
                    }
                    else -> {
                        Log.d(TAG, "else@@@@@@@@@@@@@@@@@@")
                        false
                    }
                }})
                inflate(R.menu.menu_profile_fragment)
                show()
            }
//            val popup = PopupMenu(context,view)
//            val inflater: MenuInflater = popup.menuInflater
//            inflater.inflate(R.menu.menu_profile_fragment,popup.menu)
//            popup.show()
//            findNavController().navigate(RecycleSearchDirections.actionRecycleSearchToNewReference(false, docId))
        }
        else {
            val action = RecycleSearchDirections.actionRecycleSearchToProfileFragment(docRefID)
            findNavController().navigate(action)
        }


    }

    // the documentation said to implement it like that
    override fun onStart() {
        super.onStart()
        adapter.startListening()
    }

    // the documentation said to implement it like that
    override fun onStop() {
        super.onStop()
        adapter.stopListening()
    }

    //todo not sure if this one is necessary
    override fun onDestroy() {
        super.onDestroy()
        adapter.stopListening()
    }


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        arguments?.let {
            param1 = it.getString(ARG_PARAM1)
            param2 = it.getString(ARG_PARAM2)
        }
        setHasOptionsMenu(true)
    }

    override fun onCreateView(

        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        // Inflate the layout for this fragment
        binding = DataBindingUtil.inflate(inflater,R.layout.fragment_recycle_search,container,false)
        return binding.root
    }

    companion object {
        /**
         * Use this factory method to create a new instance of
         * this fragment using the provided parameters.
         *
         * @param param1 Parameter 1.
         * @param param2 Parameter 2.
         * @return A new instance of fragment RecycleSearch.
         */
        // TODO: Rename and change types and number of parameters
        @JvmStatic
        fun newInstance(param1: String, param2: String) =
            RecycleSearch().apply {
                arguments = Bundle().apply {
                    putString(ARG_PARAM1, param1)
                    putString(ARG_PARAM2, param2)
                }
            }
    }

    override fun onMenuItemClick(item: MenuItem?): Boolean {
        return when (item?.itemId){
            R.id.add_reference ->{
                Log.d(TAG, "long press>add reference @@@@@@@@@@@@")
                findNavController().navigate(RecycleSearchDirections.actionRecycleSearchToNewReference(false, docRefID))
                true
            }
            R.id.delete_profile ->{
                Log.d(TAG, "long press>delete profile@@@@@@@@@")
                true
            }
            else -> {
                Log.d(TAG, "else@@@@@@@@@@@@@@@@@@")
                false
            }
        }
    }


}
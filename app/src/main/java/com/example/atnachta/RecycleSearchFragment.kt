package com.example.atnachta
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.os.Environment
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.core.content.FileProvider
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
import com.google.firebase.firestore.ktx.toObject
import com.google.firebase.ktx.Firebase
import org.apache.commons.csv.CSVFormat
import org.apache.commons.csv.CSVPrinter
import java.io.*
import java.util.*



// TODO: Rename parameter arguments, choose names that match
// the fragment initialization parameters, e.g. ARG_ITEM_NUMBER
private const val ARG_PARAM1 = "param1"
private const val ARG_PARAM2 = "param2"

private const val PROFILES_COLLECTION = "profiles"



/**
 * A simple [Fragment] subclass.
 * Use the [RecycleSearch.newInstance] factory method to
 * create an instance of this fragment.
 */
class RecycleSearch : Fragment(), ProfileAdapter.OnProfileSelectedListener {
    // TODO: 1. Check if the recyclerView gets data
    // TODO: 2. Add onClickListeners to the Search button and search results

    private var param1: String? = null
    private var param2: String? = null

    lateinit var binding : FragmentRecycleSearchBinding
    lateinit var firestore: FirebaseFirestore
    lateinit var collectionReference: CollectionReference
    lateinit var adapter: ProfileAdapter

    lateinit var initialSearchInput: String
    val TAG : String = "RecycleView"
    var filename =""
    var filepath = ""
    ///samples


    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        firestore = Firebase.firestore

        collectionReference = firestore.collection(PROFILES_COLLECTION)
        initialSearchInput = RecycleSearchArgs.fromBundle(requireArguments()).searchInput
        setUpRecyclerView()

        // onClickListener for the search button - update result list
        binding.searchButton.setOnClickListener { updateQuery(binding.searchInput.text.toString()) }

        binding.newProfileButton.setOnClickListener{ v : View -> v.findNavController().navigate(
            RecycleSearchDirections.actionRecycleSearchToNewReference(true))}

        if (!isFileExists()){
            binding.button3.isEnabled = false
        }
        binding.button3.setOnClickListener { sendProfilesByEmail() }

    }

    /**
     * Click handler for the search button.
     * Updates the query the adapter works with, according to what the user searched
     * @param searchInput the text the user typed at the search bar
     */
    private fun updateQuery(searchInput: String) {
        val newQuery : Query = if (searchInput.isBlank()){ // if blank just show everything
            collectionReference.orderBy("firstName", Query.Direction.DESCENDING)
        } else{ // query by input
            collectionReference.whereArrayContains("searchList", searchInput)
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
            collectionReference.orderBy("firstName", Query.Direction.DESCENDING)
        } else{ // query by input
            collectionReference.whereArrayContains("searchList", initialSearchInput)
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


    fun sendProfilesByEmail(){


        filename ="myFile.csv"
        filepath = "MyFileDir"
        val myextrnal = File(context?.getExternalFilesDir(filepath), filename)
        var fos : FileOutputStream? = null
//        val tmp = Profile.l;
        var headers : SortedSet<String> = sortedSetOf()
        try{
            collectionReference.orderBy("firstName", Query.Direction.DESCENDING)
                .get().addOnSuccessListener { documents->
                    val writer = myextrnal.bufferedWriter()
                    writer.write("\ufeff")
                    val csvPrinter = CSVPrinter(writer, CSVFormat.DEFAULT)

                    for (doc in documents){
                        if (headers.size==0) {
                            headers = doc.data.keys.toSortedSet()
                            csvPrinter.printRecord(headers)
                        }
                        csvPrinter.printRecord(doc.data.toSortedMap().values)


                        Log.d(TAG, "${doc.id} => ${doc.data}")

                    }
                    val path: Uri =
                        FileProvider.getUriForFile(requireContext(), "com.example.atnachta.fileprovider", myextrnal)

                    val fileIntent = Intent(Intent.ACTION_SEND)
                    fileIntent.type = "text/csv"
                    fileIntent.putExtra(Intent.EXTRA_SUBJECT, "Data")
                    fileIntent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
                    fileIntent.putExtra(Intent.EXTRA_STREAM, path)
                    startActivity(fileIntent)
                    csvPrinter.flush();
                    csvPrinter.close();
                }

        } catch (e :FileNotFoundException ){
            e.printStackTrace()
        } catch (e : IOException){
            e.printStackTrace()
        }


    }
    /**
     * the click handler for pressing a result item
     * @param snapshot docSnapshot of the selcted profile. We get the snapshot from the viewHolder
     * (specifically in onBindViewHolder), which gets using firebaseUI methods
     */
    override fun onProfileSelected(snapshot: DocumentSnapshot) {
        val docId : String = snapshot.id
        Log.d(TAG, docId)
        val action = RecycleSearchDirections.actionRecycleSearchToProfileFragment(docId)
        findNavController().navigate(action)
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



}
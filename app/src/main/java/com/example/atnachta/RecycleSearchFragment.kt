package com.example.atnachta
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.os.Environment
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.EditText
import androidx.core.content.ContextCompat
import androidx.core.content.FileProvider
import androidx.databinding.DataBindingUtil
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.atnachta.data.Girl
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
import org.json.*
import java.io.BufferedReader
import java.io.File
import java.io.FileWriter
import java.io.IOException


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
    lateinit var map_lst : MutableList<Map<String, Any>>
    var data_STR = ""
    val TAG : String = "RecycleView"
    ///samples


    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        firestore = Firebase.firestore

        collectionReference = firestore.collection(PROFILES_COLLECTION)
        setUpRecyclerView()

        // onClickListener for the search button - update result list
        binding.searchButton.setOnClickListener { updateQuery(binding.searchInput.text.toString()) }


        // TODO: Eyals code, may want to remove
//        var girlsList : MutableList<Map<String,Any>> = mutableListOf()
//        var str = ""
//        val splitText = "איל".toString().split(" ")
//        val docRef = firestore.collection("profiles").orderBy("firstName", Query.Direction.DESCENDING)
//            .get()
//            .addOnSuccessListener { documents ->
//                for (document in documents) {
//                    girlsList.add(document.data)
//                    Log.d(TAG, "${document.id} => ${document.data}")
//                    str += document.data.toString()
//                    str += "aa"
//
//                }
//
//            }
//            .addOnFailureListener { exception ->
//                Log.w(TAG, "Error getting documents: ", exception)
//            }
//        Log.d("@@@@@", girlsList.toString())
//        Log.d("@@@@@STR", str)
//        val girlsList : MutableList<Girl> = mutableListOf()
//        val adapter = PersonItemAdapter(girlsData = girlsList,)
//        binding.resultList.adapter = adapter
        binding.button3.setOnClickListener { foo() }
//        docRef.get()

//    print(docRef.get())
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
            val splitText = searchInput.split(" ")
            // NOTICE - this wont find old test profiles without a searchList field
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
        val query : Query = collectionReference.orderBy("firstName", Query.Direction.DESCENDING)

        // define the options object (firebaseUI class), that gets the query into the adapter
        val firestoreRecyclerOptions : FirestoreRecyclerOptions<Profile> = FirestoreRecyclerOptions.Builder<Profile>()
            .setQuery(query, Profile::class.java)
            .build()

//        foo()
        adapter = ProfileAdapter(firestoreRecyclerOptions,this)
        binding.resultList.layoutManager = LinearLayoutManager(activity) // still not sure what that is, but is needed
        binding.resultList.adapter = adapter

    }

//    TODO DELETE:

    fun foo(){
//        val docs = JSONObject(jsonArrayString).getJSONArray("profiles")

//        val fileName = "myfile3.csv"
//
//        val myfile = File(fileName)
//        myfile.createNewFile()
        val content = "Toda,snow ,is ,falling."
        map_lst = mutableListOf()

//        myfile.writeText(content)
        try {

//            val filelocation =
//                File(context.getExternalFilesDir(), "data.csv")
            val pathFile = File(context?.filesDir,"data.csv")
            val writer = FileWriter(pathFile)
//            var data_str : String = ""
//            val docRef = firestore.collection("profiles").whereIn("firstName", "איל".split(" "))
//                .get()
//                .addOnSuccessListener { documents ->
//                    for (document in documents) {
//                        map_lst.add(document.data)
//                        Log.d(TAG, "${document.id} => ${document.data}")
//                        data_STR += document.data.toString() + "\n"
////
//                    }
//
//                }
            collectionReference.orderBy("firstName", Query.Direction.DESCENDING)
                .get().addOnSuccessListener { documents->
                    for (doc in documents){
                        Log.d(TAG, "${doc.id} => ${doc.data}")
//                        map_lst.add(doc.data)
//                        writer.use { it.write(doc.data.toString()) }
//                        pathFile.printWriter().use { out -> out.println(doc.data.toString()) }
                        data_STR += doc.data.toString() + "\n"

                    }
                }
            Log.d(TAG, data_STR)
//            Log.e("errr",context?.getExternalFilesDir(null).toString())
            writer.use { it.write(data_STR) }

            val bufferedReader: BufferedReader = File(context?.filesDir,"data.csv").bufferedReader()
            val inputString = bufferedReader.use { it.readText() }
            println(inputString)
            Log.e("errr inputString",inputString)
            Log.e("errr dataSTR",data_STR)
//            val file= File(context?.getExternalFilesDir(null),"data.csv")
//            val uri = Uri.fromFile(file)

//            val intent = Intent(Intent.ACTION_SEND).apply {
//                type = "*/*"
//                putExtra(Intent.EXTRA_EMAIL, "eeyalhod@gmail.com")
//                putExtra(Intent.EXTRA_SUBJECT, "what what try try")
////                putExtra(Intent.EXTRA_STREAM, uri)
//            }
//            startActivity(Intent.createChooser(intent,"aaaaa"))

//            val filelocation: File = File(context?.filesDir, "data.csv")
            val path: Uri =
                FileProvider.getUriForFile(requireContext(), "com.example.atnachta.fileprovider", pathFile)
            val fileIntent = Intent(Intent.ACTION_SEND)
            fileIntent.type = "text/csv"
            fileIntent.putExtra(Intent.EXTRA_SUBJECT, "Data")
            fileIntent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
            fileIntent.putExtra(Intent.EXTRA_STREAM, path)
//            startActivity(fileIntent)
//            ContextCompat.startActivity(Intent.createChooser(fileIntent, "Send mail"))
//            startActivity(Intent.createChooser(fileIntent,"aaaaa"))
//            if (context?.let { intent.resolveActivity(it.packageManager) } != null) {
//                startActivity(intent)
//            }
        } catch (e : IOException){
            Log.e("errr","not fn", e)
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

    // TODO: remove this function
    fun queryProfiles(searchInput: EditText) : MutableList<Girl>{
        val girlsList : MutableList<Girl> = mutableListOf()
        val splitText = searchInput.text.toString().split(" ")
        val docRef = firestore.collection("profiles").whereIn("firstName", splitText)
                .get()
                .addOnSuccessListener { documents ->
                    for (document in documents) {
                        girlsList.add(document.toObject<Girl>())
                        Log.d(TAG, "${document.id} => ${document.data}")

                    }

                }
                .addOnFailureListener { exception ->
                    Log.w(TAG, "Error getting documents: ", exception)
                }
//        val query: String = searchInput.text.toString()
//        return girlsList.filter{ query in it.firstName || query in it.lastName} as MutableList<Girl>
        return girlsList
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
//        binding.searchButton.setOnClickListener { adapter.editResultList(getGirlsList(girlsList,binding.searchInput)) }
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
package com.example.atnachta

import android.os.Bundle
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.EditText
import android.widget.TextView
import androidx.databinding.DataBindingUtil
import androidx.recyclerview.widget.RecyclerView
import com.example.atnachta.data.Girl
import com.example.atnachta.databinding.FragmentRecycleSearchBinding
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.firestore.ktx.toObject
import com.google.firebase.ktx.Firebase

// TODO: Rename parameter arguments, choose names that match
// the fragment initialization parameters, e.g. ARG_ITEM_NUMBER
private const val ARG_PARAM1 = "param1"
private const val ARG_PARAM2 = "param2"



/**
 * A simple [Fragment] subclass.
 * Use the [RecycleSearch.newInstance] factory method to
 * create an instance of this fragment.
 */
class RecycleSearch : Fragment() {
    // TODO: Rename and change types of parameters
    private var param1: String? = null
    private var param2: String? = null

    lateinit var binding : FragmentRecycleSearchBinding
    lateinit var firestore: FirebaseFirestore
    val TAG : String = "RecycleView"
    ///samples


    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        firestore = Firebase.firestore
        val girlsList : MutableList<Girl> = mutableListOf()
        val adapter = PersonItemAdapter(girlsData = girlsList,)


        binding.resultList.adapter = adapter

        binding.searchButton.setOnClickListener { adapter.editResultList(queryProfiles(binding.searchInput)) }
//        docRef.get()

//    print(docRef.get())
    }


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
//      TODO: delete
//        for (i in 1..20){
//            girlsList.add(Girl("Girl num ",i.toString(), i+10))
//
//        }


    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
//        val adapter = PersonItemAdapter(girlsData = girlsList,)
        binding = DataBindingUtil.inflate(inflater,R.layout.fragment_recycle_search,container,false)
//        binding.resultList.adapter = adapter
//
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
class TextItemViewHolder(val textView: TextView): RecyclerView.ViewHolder(textView)

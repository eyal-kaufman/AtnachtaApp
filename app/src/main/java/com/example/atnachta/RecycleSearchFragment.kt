package com.example.atnachta

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.core.view.get
import androidx.databinding.DataBindingUtil
import androidx.recyclerview.widget.RecyclerView
import com.example.atnachta.data.Girl
import com.example.atnachta.databinding.FragmentRecycleSearchBinding
import java.util.*

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
    var exampleList : MutableList<String> = mutableListOf()
    var girlsList : MutableList<Girl> = mutableListOf()
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        arguments?.let {
            param1 = it.getString(ARG_PARAM1)
            param2 = it.getString(ARG_PARAM2)
        }
        for (i in 1..20){
            exampleList.add("Word "+i)
            girlsList.add(Girl("Girl num ",i.toString(), i+10))

        }

    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        val adapter = PersonItemAdapter(girlsData = girlsList,)
        binding = DataBindingUtil.inflate(inflater,R.layout.fragment_recycle_search,container,false)
        binding.resultList.adapter = adapter
//        adapter.girlsData = girlsList
//        adapter.girlsData = exampleList
//        adapter.get
//        binding.resultList.get()
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

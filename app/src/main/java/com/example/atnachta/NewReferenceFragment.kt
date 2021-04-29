package com.example.atnachta

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.databinding.DataBindingUtil
import androidx.navigation.findNavController
import com.example.atnachta.data.Girl
import com.example.atnachta.databinding.FragmentNewReferenceBinding
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase

// TODO: Rename parameter arguments, choose names that match
// the fragment initialization parameters, e.g. ARG_ITEM_NUMBER
private const val ARG_PARAM1 = "param1"
private const val ARG_PARAM2 = "param2"

private const val PROFILES_COLLECTION = "profiles"


/**
 * A simple [Fragment] subclass.
 * Use the [NewReference.newInstance] factory method to
 * create an instance of this fragment.
 */
class NewReference : Fragment() {
    // TODO: Rename and change types of parameters
    private var param1: String? = null
    private var param2: String? = null

    lateinit var binding : FragmentNewReferenceBinding
    lateinit var firestore : FirebaseFirestore
    lateinit var girlDocId : String

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        arguments?.let {
            param1 = it.getString(ARG_PARAM1)
            param2 = it.getString(ARG_PARAM2)
        }
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?,
                              savedInstanceState: Bundle?): View {
        // Inflate the layout for this fragment
        binding = DataBindingUtil.inflate(inflater,R.layout.fragment_new_reference,container,false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        // ActionBar title text
        activity?.setTitle(R.string.ReferenceDetails)

        // getting Firestore instance
        firestore = Firebase.firestore

        // getting girlDocId
        girlDocId = NewReferenceArgs.fromBundle(requireArguments()).girlDocId
        firestore.collection(PROFILES_COLLECTION).document(girlDocId).update("age", 123123123)

        // continue button setup
        binding.continueButton.setOnClickListener { continueButtonHandler()}
        /*TODO Continue button should:
        *  1. Create a Reference object from data in TextViews
        *  2. Add the new reference to the reference nested-collection in the girl Firestore doc */

    }

    private fun continueButtonHandler(){

    }

    companion object {
        /**
         * Use this factory method to create a new instance of
         * this fragment using the provided parameters.
         *
         * @param param1 Parameter 1.
         * @param param2 Parameter 2.
         * @return A new instance of fragment newReference.
         */
        // TODO: Rename and change types and number of parameters
        @JvmStatic
        fun newInstance(param1: String, param2: String) =
                NewReference().apply {
                    arguments = Bundle().apply {
                        putString(ARG_PARAM1, param1)
                        putString(ARG_PARAM2, param2)
                    }
                }
    }
}
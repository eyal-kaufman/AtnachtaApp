package com.example.atnachta

import android.os.Bundle
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.databinding.DataBindingUtil
import androidx.navigation.findNavController
import com.example.atnachta.data.Girl
import com.example.atnachta.data.Profile
import com.example.atnachta.databinding.FragmentNewProfileBinding
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase

// TODO: Rename parameter arguments, choose names that match
// the fragment initialization parameters, e.g. ARG_ITEM_NUMBER
private const val ARG_PARAM1 = "param1"
private const val ARG_PARAM2 = "param2"

private const val PROFILES_COLLECTION = "profiles"
private const val TAG = "DocSnippets" // not sure what this means, was copied from Firestore documentation


/**
 * A simple [Fragment] subclass.
 * Use the [NewProfileFragment.newInstance] factory method to
 * create an instance of this fragment.
 */
class NewProfileFragment : Fragment() {
    // TODO: Rename and change types of parameters
    private var param1: String? = null
    private var param2: String? = null



    lateinit var binding : FragmentNewProfileBinding
    lateinit var firestore : FirebaseFirestore



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
        binding = DataBindingUtil.inflate(inflater,R.layout.fragment_new_profile,container,false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        // continue button setup
        binding.continueButton.setOnClickListener { v : View -> continueButtonHandler(v) }
        /*TODO Continue button should:
        *  1. Create a Girl object from data in TextViews
        *  2. Create a Girl document in Firestore
        *  3. Pass the doc id to the next fragment (NewReference)*/

        // setting action bar title
        activity?.setTitle(R.string.basicDetails)

        // getting Firestore instance
        firestore = Firebase.firestore
    }

    private fun continueButtonHandler(view: View){

        val profile : Profile = createProfile()


        val profileDocRef = firestore.collection(PROFILES_COLLECTION).document()

        profileDocRef.set(profile)

        profileDocRef.get().addOnSuccessListener { document ->
            if (document != null){
                Log.d(TAG, "DocumentSnapshot data: ${document.data}")
                view.findNavController().navigate(
                    NewProfileFragmentDirections.actionNewProfileFragmentToNewReferenceFragment(document.id))
            } else {
                Log.d(TAG, "No such document")
            }
        }
            .addOnFailureListener { exception ->
                Log.d(TAG, "get failed with ", exception)
            }

//        val girl: Girl = createGirl()

//        val girlDocRef = firestore.collection(PROFILES_COLLECTION).document()

//        girlDocRef.set(girl)

//        girlDocRef.get().addOnSuccessListener { document ->
//            if (document != null) {
//                Log.d(TAG, "DocumentSnapshot data: ${document.data}")
//                view.findNavController().navigate(
//                    NewProfileFragmentDirections.actionNewProfileFragmentToNewReferenceFragment(document.id))
//            } else {
//                Log.d(TAG, "No such document")
//            }
//        }
//            .addOnFailureListener { exception ->
//                Log.d(TAG, "get failed with ", exception)
//            }


    }



    fun setCardExpansion(view : View){
        when(view.visibility){
            View.GONE -> view.visibility = View.VISIBLE
            else -> view.visibility = View.GONE
        }
    }

    private fun createProfile() : Profile{
        return Profile(binding.firstName.text.toString(),
            binding.familyName.text.toString(),
            binding.editTextProfilePhone.text.toString())
    }
    private fun createGirl() : Girl{
        return Girl(binding.firstName.text.toString(),
                binding.familyName.text.toString(),
                Integer.parseInt(binding.editTextProfilePhone.text.toString()))
    }

    companion object {
        /**
         * Use this factory method to create a new instance of
         * this fragment using the provided parameters.
         *
         * @param param1 Parameter 1.
         * @param param2 Parameter 2.
         * @return A new instance of fragment NewProfileFragment.
         */
        // TODO: Rename and change types and number of parameters
        @JvmStatic
        fun newInstance(param1: String, param2: String) =
            NewProfileFragment().apply {
                arguments = Bundle().apply {
                    putString(ARG_PARAM1, param1)
                    putString(ARG_PARAM2, param2)
                }
            }
    }
}
package com.example.atnachta

import android.os.Bundle
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.databinding.DataBindingUtil
import androidx.navigation.findNavController
import com.example.atnachta.data.Profile
import com.example.atnachta.data.Reference
import com.example.atnachta.databinding.FragmentNewReferenceBinding
import com.google.firebase.firestore.DocumentReference
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase

// TODO: Rename parameter arguments, choose names that match
// the fragment initialization parameters, e.g. ARG_ITEM_NUMBER
private const val ARG_PARAM1 = "param1"
private const val ARG_PARAM2 = "param2"

private const val TAG = "DocSnippets" // not sure what this means, was copied from Firestore documentation
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
//    private lateinit var profileDocId : String
//    private lateinit var profileDocRef : DocumentReference


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
//        profileDocId = NewReferenceArgs.fromBundle(requireArguments()).profileDocId
//        profileDocRef = firestore.collection(PROFILES_COLLECTION).document(profileDocId)
//        firestore.collection(PROFILES_COLLECTION).document(girlDocId).update("age", 123123123)

        // continue button setup
        binding.continueButton.setOnClickListener {v : View -> continueButtonHandler(v)}
    }

    private fun continueButtonHandler(view: View){
        val profile : Profile = createProfile()
        val profileDocRef = firestore.collection(PROFILES_COLLECTION).document()
        profileDocRef.set(profile)
        val ref: Reference = createReference()
        profileDocRef.collection("References").add(ref)
                .addOnSuccessListener { documentReference ->
                    Log.d(TAG, "DocumentSnapshot written with ID: ${documentReference.id}")
                }
                .addOnFailureListener { e ->
                    Log.w(TAG, "Error adding document", e)
                }
        profileDocRef.get().addOnSuccessListener { document ->
            if (document != null){
                Log.d(TAG, "DocumentSnapshot data: ${document.data}")
                view.findNavController().navigate(
                    NewReferenceDirections.actionNewReferenceToProfileFragment(document.id))
            } else {
                Log.d(TAG, "No such document")
            }
        }
            .addOnFailureListener { exception ->
                Log.d(TAG, "get failed with ", exception)
            }
    }

    private fun createProfile() : Profile{
        // todo change this according to the updated profile constructor
        val age : Int? = if (binding.girlAge.text.toString().isBlank()){
            null
        } else{
            binding.girlAge.text.toString().toInt()
        }
        return Profile(binding.girlFirstName.text.toString(),age)
//        return Profile(binding.firstName.text.toString(),
//            binding.familyName.text.toString(),
//            binding.editTextProfilePhone.text.toString())
    }

    private fun createReference(): Reference {
        return Reference(binding.receiverName.text.toString(),
                        binding.editTextDate.text.toString(),
                        binding.editTextTime.text.toString(),
                        binding.referenceReason.text.toString(),
                        binding.refererName.text.toString(),
                        binding.refererJob.text.toString(),
                        binding.editTextPhone.text.toString()
        )
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
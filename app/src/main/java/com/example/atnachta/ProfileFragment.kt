package com.example.atnachta

import android.os.Bundle
import android.transition.AutoTransition
import android.transition.TransitionManager
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.AdapterView
import android.widget.ArrayAdapter
import androidx.appcompat.app.AppCompatActivity
import androidx.databinding.DataBindingUtil
import androidx.fragment.app.Fragment
import androidx.navigation.findNavController
import com.example.atnachta.data.Girl
import com.example.atnachta.databinding.FragmentProfileBinding
import com.google.firebase.firestore.DocumentReference
import com.google.firebase.firestore.DocumentSnapshot
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.firestore.ktx.toObject
import com.google.firebase.ktx.Firebase
import kotlinx.android.synthetic.main.fragment_profile.*


// TODO: Rename parameter arguments, choose names that match
// the fragment initialization parameters, e.g. ARG_ITEM_NUMBER
private const val ARG_PARAM1 = "param1"
private const val ARG_PARAM2 = "param2"

/**
 * A simple [Fragment] subclass.
 * Use the [profileFragment.newInstance] factory method to
 * create an instance of this fragment.
 */
class profileFragment : Fragment(), AdapterView.OnItemSelectedListener {
    // TODO: Rename and change types of parameters
    private var param1: String? = null
    private var param2: String? = null
    lateinit var profile: Girl
    lateinit var firestore: FirebaseFirestore
    lateinit var binding: FragmentProfileBinding
    lateinit var girlDocId: String
    lateinit var girlDocRef: DocumentReference
    val TAG: String = "profile"

//    val _edit_text_array = arrayOf(binding.editName, binding.editId, binding.editPhone ,
//        binding.editFatherName, binding.editMotherName, binding.editFatherPhone,
//        binding.editMotherPhone, binding.editAddress, binding.editBirthDate,
//        binding.editKupach, binding.editSchool, binding.editCivil)
//
//    val _edited_text_array = arrayOf(binding.editedName, binding.editedId, binding.editedPhone ,
//        binding.editedFatherName, binding.editedMotherName, binding.editedFatherPhone,
//        binding.editedMotherPhone, binding.editedAddress, binding.editedBirthDate,
//        binding.editedKupach, binding.editedSchool, binding.editedCivil)

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        arguments?.let {
            param1 = it.getString(ARG_PARAM1)
            param2 = it.getString(ARG_PARAM2)
        }
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        girlDocRef.update("firstName", "hey!").addOnSuccessListener {
            Log.d(TAG, "success!")
//            displayMode(view)
        }
            .addOnFailureListener { exception ->
                Log.d(TAG, "get failed with ", exception)
            }
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        var profile: Girl
        // Inflate the layout for this fragment
        binding = DataBindingUtil.inflate(inflater, R.layout.fragment_profile, container, false)
        binding.button4.setOnClickListener { view: View ->
            view.findNavController().navigate(R.id.action_profileFragment_to_newReferenceFragment)
        }
        binding.examples.setOnClickListener { view: View ->
            view.findNavController().navigate(R.id.action_ProfileFragment_to_ReferenceFragment)
        }

//        girlDocId = "r2m6NpHUQX4WZOpcjJCC"
//        firestore = Firebase.firestore
//        girlDocRef = firestore.collection("profiles").document(girlDocId)

        activity?.setTitle(R.string.basicDetails)

        binding.editButton.setOnClickListener {
            editMode(it)
        }
//        binding.button5.setOnClickListener { view ->
//
//        }


        binding.parentsTitle.setOnClickListener {
            if (parentsData.visibility == View.GONE) {
                TransitionManager.beginDelayedTransition(parentsData, AutoTransition())
                parentsData.visibility = View.VISIBLE;
            } else {
                parentsData.visibility = View.GONE;
            }
        }
        binding.generalTitle.setOnClickListener {
            if (generalData.visibility == View.GONE) {
                TransitionManager.beginDelayedTransition(generalData, AutoTransition())
                generalData.visibility = View.VISIBLE;
            } else {
                generalData.visibility = View.GONE;
            }
        }
        binding.referenceTitle.setOnClickListener {
            if (referenceData.visibility == View.GONE) {
                TransitionManager.beginDelayedTransition(referenceData, AutoTransition())
                referenceData.visibility = View.VISIBLE;
            } else {
                referenceData.visibility = View.GONE;
            }
        }
        binding.educationTitle.setOnClickListener {
            if (educationData.visibility == View.GONE) {
                TransitionManager.beginDelayedTransition(educationData, AutoTransition())
                educationData.visibility = View.VISIBLE;
            } else {
                educationData.visibility = View.GONE;
            }
        }
        binding.healthTitle.setOnClickListener {
            if (healthData.visibility == View.GONE) {
                TransitionManager.beginDelayedTransition(healthData, AutoTransition())
                healthData.visibility = View.VISIBLE;
            } else {
                healthData.visibility = View.GONE;
            }
        }


        girlDocId = "ncq4qmlp3LZGxp9iZliB"
        firestore = Firebase.firestore
        girlDocRef = firestore.collection("profiles").document(girlDocId)
        girlDocRef.get()
            .addOnSuccessListener { document ->
                if (document != null) {
                    retrieveProfileData(document)

                } else {
                    Log.d(TAG, "No such document")
                }
            }
            .addOnFailureListener { exception ->
                Log.d(TAG, "get failed with ", exception)
            }

        return binding.root
    }

    private fun retrieveProfileData(document: DocumentSnapshot) {
        Log.d(TAG, "DocumentSnapshot data: ${document.data}")
        edited_name.text = document.getString("firstName")
        edited_id.text = document.getString("lastName")
        edited_phone.text = document.getString("age")
    }

    private fun updateProfileData(document: DocumentSnapshot) {
        girlDocRef.update("lastName", edit_id.text)
        girlDocRef.update("age", edit_phone.text)
        girlDocRef.update("firstName", edit_name.text)
        Log.d(TAG, "DocumentSnapshot update: ${document.data}")
    }

    private fun update(view: View) {

    }


    private fun displayMode(view: View) {
//        for(edit_text in _edit_text_array){
//            edit_text.visibility = View.GONE
//        }
//        for(edited_text in _edited_text_array){
//            edited_text.visibility = View.VISIBLE
//        }
        edited_name.visibility = View.VISIBLE
        edit_name.visibility = View.GONE

        edited_id.visibility = View.VISIBLE
        edit_id.visibility = View.GONE

        edited_phone.visibility = View.VISIBLE
        edit_phone.visibility = View.GONE

        edited_father_name.text = edit_father_name.text
        edited_father_name.visibility = View.VISIBLE
        edited_mother_name.visibility = View.VISIBLE

        edited_mother_name.text = edit_mother_name.text
        edit_father_name.visibility = View.GONE
        edit_mother_name.visibility = View.GONE

        edited_father_phone.text = edit_father_phone.text
        edited_father_phone.visibility = View.VISIBLE
        edited_mother_phone.visibility = View.VISIBLE

        edited_mother_phone.text = edit_mother_phone.text
        edit_father_phone.visibility = View.GONE
        edit_mother_phone.visibility = View.GONE

        edited_address.text = edit_address.text
        edited_address.visibility = View.VISIBLE
        edit_address.visibility = View.GONE

        edited_birth_date.text = edit_birth_date.text
        edited_birth_date.visibility = View.VISIBLE
        edit_birth_date.visibility = View.GONE

        edited_kupach.text = edit_kupach.text
        edited_kupach.visibility = View.VISIBLE
        edit_kupach.visibility = View.GONE

        edited_school.text = edit_school.text
        edited_school.visibility = View.VISIBLE
        edit_school.visibility = View.GONE

        edited_civil.text = edit_civil.text
        edited_civil.visibility = View.VISIBLE
        edit_civil.visibility = View.GONE

        view.visibility = View.VISIBLE
        edit_button.visibility = View.VISIBLE
        button5.visibility = View.GONE
    }


    private fun editMode(view: View) {
//        for(edit_text in _edit_text_array){
//            edit_text.visibility = View.VISIBLE
//        }
//        for(edited_text in _edited_text_array){
//            edited_text.visibility = View.GONE
//        }
        view.visibility = View.GONE
        button5.visibility = View.VISIBLE
//        for(edit_text in _edit_text_array){
//            edit_text.visibility = View.VISIBLE
//        }
//        for(edited_text in _edited_text_array){
//            edited_text.visibility = View.GONE
//        }
        edited_name.visibility = View.GONE
        edit_name.visibility = View.VISIBLE

        edited_id.visibility = View.GONE
        edit_id.visibility = View.VISIBLE

        edited_phone.visibility = View.GONE
        edit_phone.visibility = View.VISIBLE

        edited_father_name.visibility = View.GONE
        edited_mother_name.visibility = View.GONE

        edit_father_name.visibility = View.VISIBLE
        edit_mother_name.visibility = View.VISIBLE

        edited_father_phone.visibility = View.GONE
        edited_mother_phone.visibility = View.GONE

        edit_father_phone.visibility = View.VISIBLE
        edit_mother_phone.visibility = View.VISIBLE

        edited_address.visibility = View.GONE
        edit_address.visibility = View.VISIBLE

        edited_birth_date.visibility = View.GONE
        edit_birth_date.visibility = View.VISIBLE

        edited_kupach.visibility = View.GONE
        edit_kupach.visibility = View.VISIBLE

        edited_school.visibility = View.GONE
        edit_school.visibility = View.VISIBLE

        edited_civil.visibility = View.GONE
        edit_civil.visibility = View.VISIBLE


    }

    companion object {
        /**
         * Use this factory method to create a new instance of
         * this fragment using the provided parameters.
         *
         * @param param1 Parameter 1.
         * @param param2 Parameter 2.
         * @return A new instance of fragment profileFragment.
         */
        /**
         * Use this factory method to create a new instance of
         * this fragment using the provided parameters.
         *
         * @param param1 Parameter 1.
         * @param param2 Parameter 2.
         * @return A new instance of fragment profileFragment.
         */

        // TODO: Rename and change types and number of parameters
        @JvmStatic
        fun newInstance(param1: String, param2: String) =
            profileFragment().apply {
                arguments = Bundle().apply {
                    putString(ARG_PARAM1, param1)
                    putString(ARG_PARAM2, param2)
                }
            }
    }

    override fun onItemSelected(parent: AdapterView<*>?, view: View?, position: Int, id: Long) {
        TODO("Not yet implemented")
    }

    override fun onNothingSelected(parent: AdapterView<*>?) {
        TODO("Not yet implemented")
    }
}
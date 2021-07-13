package com.example.atnachta

import android.app.DatePickerDialog
import android.app.TimePickerDialog
import android.os.Bundle
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.constraintlayout.widget.ConstraintSet
import androidx.databinding.DataBindingUtil
import androidx.navigation.findNavController
import com.example.atnachta.data.Profile
import com.example.atnachta.data.Reference
import com.example.atnachta.databinding.FragmentNewReferenceBinding
import com.google.firebase.firestore.DocumentReference
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase
import java.text.SimpleDateFormat
import java.util.*

// TODO: Rename parameter arguments, choose names that match
// the fragment initialization parameters, e.g. ARG_ITEM_NUMBER
private const val ARG_PARAM1 = "isNewProfile"
private const val ARG_PARAM2 = "referenceID"
private const val ARG_PARAM3 = "docID"

private const val TAG = "DocSnippets" // not sure what this means, was copied from Firestore documentation
private const val PROFILES_COLLECTION = "profiles"


/**
 * A simple [Fragment] subclass.
 * Use the [NewReference.newInstance] factory method to
 * create an instance of this fragment.
 */
class NewReference : Fragment() {
    // TODO: Rename and change types of parameters
    private var isNewProfile: Boolean = false
    private var referenceID: String = ""
    private var docID: String = ""

    private lateinit var binding : FragmentNewReferenceBinding
    private lateinit var firestore : FirebaseFirestore

    private val formatDate = SimpleDateFormat("dd/MM/yyyy", Locale.ITALY) // a random eu state
    private val formatTime = SimpleDateFormat("kk:mm", Locale.ITALY) // a random eu state

//    private lateinit var profileDocId : String
//    private lateinit var profileDocRef : DocumentReference


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        arguments?.let {
            isNewProfile = it.getBoolean(ARG_PARAM1)
            referenceID = it.getString(ARG_PARAM2)!!
            docID = it.getString(ARG_PARAM3)!!
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

        // setting UI according to existence of the profile
        val isNewProfile = NewReferenceArgs.fromBundle(requireArguments()).isNewProfile
        configureUI(isNewProfile)



        // continue button setup
        binding.continueButton.setOnClickListener {v : View -> continueButtonHandler(v)}

        // setting current date and time in views
        binding.dateTextView.text = formatDate.format(Calendar.getInstance().time)
        binding.timeTextView.text = formatTime.format(Calendar.getInstance().time)

        // setting listeners for picking date and time
        binding.dateTextView.setOnClickListener{v:View -> setDatePicker(v)}
        binding.timeTextView.setOnClickListener{v:View -> setTimePicker(v)}

    }

    private fun configureUI(isNewProfile: Boolean) {
        if (isNewProfile){
            return // default layout is for a  new profile
        }
        binding.personalDetailsTitleView.visibility = View.GONE
        binding.textInputLayoutFirstName.visibility = View.GONE
        binding.textViewAgeTitle.visibility = View.GONE
        binding.girlAge.visibility = View.GONE
        binding.divider.visibility = View.GONE

        // updating the title constraint so the title is constrained to the top of the screen
        val constraintSet = ConstraintSet()
        constraintSet.clone(binding.constraintLayout)
        constraintSet.clear(binding.referenceDetailsTitleView.id, ConstraintSet.TOP)
        constraintSet.connect(binding.referenceDetailsTitleView.id,ConstraintSet.TOP, ConstraintSet.PARENT_ID, ConstraintSet.TOP,24)
        constraintSet.applyTo(binding.constraintLayout)
    }

    private fun setDatePicker(v : View) {

        val c = Calendar.getInstance()
        val year = c.get(Calendar.YEAR)
        val month = c.get(Calendar.MONTH)
        val day = c.get(Calendar.DAY_OF_MONTH)
        val datePicker = DatePickerDialog(
            v.context, DatePickerDialog.OnDateSetListener{ view, chosenYear, monthOfYear, dayOfMonth ->
                // Display Selected date in textbox)
                val selectedDate = Calendar.getInstance()
                selectedDate.set(Calendar.YEAR,chosenYear)
                selectedDate.set(Calendar.MONTH,monthOfYear)
                selectedDate.set(Calendar.DAY_OF_MONTH,dayOfMonth)
                val date = formatDate.format(selectedDate.time)
                binding.dateTextView.text = date
            }, year, month, day)
        datePicker.show()
    }

    private fun setTimePicker(v:View){
        val c = Calendar.getInstance()
        val hour = c.get(Calendar.HOUR_OF_DAY)
        val minute = c.get(Calendar.MINUTE)
        val timePicker = TimePickerDialog(
            v.context, TimePickerDialog.OnTimeSetListener{ view, chosenHour, chosenMinute ->
                // Display Selected time in textbox)
                val selectedTime = Calendar.getInstance()
                selectedTime.set(Calendar.HOUR_OF_DAY,chosenHour)
                selectedTime.set(Calendar.MINUTE,chosenMinute)
                val time = formatTime.format(selectedTime.time)
                binding.timeTextView.text = time
            }, hour, minute,true)
        timePicker.show()
    }

    private fun continueButtonHandler(view: View){
        val profile : Profile
        val profileDocRef : DocumentReference
        if (isNewProfile){
            profile = createProfile()
            profileDocRef = firestore.collection(PROFILES_COLLECTION).document()
            profileDocRef.set(profile)
        }
        else{
            profileDocRef = firestore.collection(PROFILES_COLLECTION).document(docID)
        }
//        val profile : Profile = createProfile()

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

    }

    private fun createReference(): Reference {
        return Reference(binding.receiverName.text.toString(),
                        binding.dateTextView.text.toString(),
                        binding.timeTextView.text.toString(),
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
         * @param isNewProfile Parameter 1.
         * @param referenceID Parameter 2.
         * @param docID Parameter 2.
         * @return A new instance of fragment newReference.
         */
        // TODO: Rename and change types and number of parameters
        @JvmStatic
        fun newInstance(isNewProfile: String, referenceID: String, docID: String) =
                NewReference().apply {
                    arguments = Bundle().apply {
                        putString(ARG_PARAM1, isNewProfile)
                        putString(ARG_PARAM2, referenceID)
                        putString(ARG_PARAM3, docID)
                    }
                }
    }
}
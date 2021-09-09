package com.example.atnachta

import android.app.DatePickerDialog
import android.app.TimePickerDialog
import android.os.Bundle
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.constraintlayout.widget.ConstraintSet
import androidx.databinding.DataBindingUtil
import androidx.navigation.findNavController
import com.example.atnachta.data.Profile
import com.example.atnachta.data.Reference
import com.example.atnachta.databinding.FragmentNewReferenceBinding
import com.google.firebase.Timestamp
import com.google.firebase.firestore.DocumentReference
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase
import java.text.DateFormat
//import com.google.firebase.storage.ktx.storage
import java.text.SimpleDateFormat
import java.time.LocalDate
import java.time.Month
import java.util.*

// TODO: Rename parameter arguments, choose names that match
// the fragment initialization parameters, e.g. ARG_ITEM_NUMBER
private const val ARG_PARAM1 = "param1"

private const val ARG_PARAM2 = "param2"

private const val TAG = "DocSnippets" // not sure what this means, was copied from Firestore documentation
private const val PROFILES_COLLECTION = "profiles"
private const val ATTENDANCE_COLLECTION = "attendance"
private const val REFERENCE_STATUS_ARRIVED = ""
private const val DATE_FORMAT = "dd/MM/yyyy"
private const val FILE_SELECT_CODE = 0


/**
 * A simple [Fragment] subclass.
 * Use the [NewReference.newInstance] factory method to
 * create an instance of this fragment.
 */
class NewReference : Fragment() {
    // TODO: Rename and change types of parameters
    private var param1: String? = null
    private var param2: String? = null

    private lateinit var binding : FragmentNewReferenceBinding
    private lateinit var firestore : FirebaseFirestore

    private lateinit var profileDocID : String
    private lateinit var refDocID: String
    private lateinit var calendarDate :Calendar
    private var referenceDate : Date? = null
    private val formatDate = SimpleDateFormat(DATE_FORMAT, Locale.ITALY) // a random eu state
    private val formatTime = SimpleDateFormat("kk:mm", Locale.ITALY) // a random eu state

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

        // getting profile docID (will be empty string if we are creating a new profile)
        profileDocID = NewReferenceArgs.fromBundle(requireArguments()).profileDocID
        refDocID = ""
        // setting UI and DocID according to existence of the profile
        val isNewProfile = NewReferenceArgs.fromBundle(requireArguments()).isNewProfile
        configureUI(isNewProfile)

        // continue button setup
        binding.continueButton.setOnClickListener {v : View -> continueButtonHandler(v,isNewProfile)}

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
        calendarDate = Calendar.getInstance()
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
                calendarDate = selectedTime
            }, hour, minute,true)
        timePicker.show()
    }

    private fun continueButtonHandler(view: View, isNewProfile: Boolean){
        binding.continueButton.isEnabled = false
        binding.progressIndicator.visibility = View.VISIBLE
        val profileDocRef : DocumentReference
        val referenceDocRef : DocumentReference
//        val attendanceReference : DocumentReference
//        var refDocID: String = ""
        if (isNewProfile){
            val profile : Profile = createProfile()
            profileDocRef = firestore.collection(PROFILES_COLLECTION).document()
            profileDocID = profileDocRef.id
            profileDocRef.set(profile)
        }
        else{
            profileDocRef = firestore.collection(PROFILES_COLLECTION).document(profileDocID)
        }
        val ref: Reference = createReference()
        referenceDocRef = profileDocRef.collection("References").document()
        referenceDocRef.set(ref)
        refDocID = referenceDocRef.id
//                .addOnSuccessListener { documentReference ->
//
//                    Log.d(TAG, "DocumentSnapshot written with ID: ${documentReference.id}")
//                }
//                .addOnFailureListener { e ->
//                    Log.w(TAG, "Error adding reference", e)
//                }
//        creating an attendance document, to store the data about this new reference if needed:
//TODO set what is the right refStatus value to trigger the process of adding reference to attendance collection
//        val selectedDate = Date(95,12,15,6,30,55)
        val selectedDate = Calendar.getInstance()
        selectedDate.set(Calendar.YEAR,2012)
        selectedDate.set(Calendar.MONTH,12)
        selectedDate.set(Calendar.DAY_OF_MONTH,11)
        selectedDate.set(Calendar.HOUR_OF_DAY,10)
        selectedDate.set(Calendar.MINUTE,30)
        selectedDate.set(Calendar.SECOND,30)

        if (ref.refStatus==REFERENCE_STATUS_ARRIVED){
            val attend = hashMapOf("endTime" to ref.leavingDate,
//                                    "startTime" to Timestamp(selectedDate.time),
                                    "startTime" to Timestamp(formatDate.parse(binding.dateTextView.text.toString())),
                                    "reference" to refDocID,
                                    "profile" to profileDocID)
            firestore.collection(ATTENDANCE_COLLECTION).add(attend)
                .addOnSuccessListener { attendanceRef ->
                    Log.d(TAG, "attendance document written with ID: ${attendanceRef.id}")
                }
                .addOnFailureListener { e ->
                    Log.w(TAG, "Error adding attendance document", e)
                }

        }
//        TODO: why needed another query?
        profileDocRef.get().addOnSuccessListener { document ->
            if (document != null){

                Log.d(TAG, "DocumentSnapshot data: ${document.data}")
                view.findNavController().navigate(
                    NewReferenceDirections.actionNewReferenceToProfileFragment(document.id))
            } else {
                Log.d(TAG, "No such document")
                Toast.makeText(context, getString(R.string.profileCreationError),
                    Toast.LENGTH_SHORT).show()
                binding.progressIndicator.visibility = View.INVISIBLE
            }
        }
            .addOnFailureListener { exception ->
                Log.d(TAG, "get failed with ", exception)
                Toast.makeText(context, getString(R.string.profileCreationError),
                    Toast.LENGTH_SHORT).show()
                binding.progressIndicator.visibility = View.INVISIBLE
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
                        binding.dateTextView.text.toString(),
//                        Calendar.getInstance().time,
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
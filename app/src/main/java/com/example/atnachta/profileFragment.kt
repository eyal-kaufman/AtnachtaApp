package com.example.atnachta

import android.app.DatePickerDialog
import android.os.Bundle
import android.transition.AutoTransition
import android.transition.TransitionManager
import android.util.Log
import android.view.*
import android.widget.*
import androidx.databinding.DataBindingUtil
import androidx.fragment.app.Fragment
import androidx.navigation.findNavController
import androidx.navigation.fragment.findNavController
import com.example.atnachta.databinding.FragmentProfileBinding
import com.google.android.material.textfield.TextInputLayout
import com.google.firebase.firestore.CollectionReference
import com.google.firebase.firestore.DocumentReference
import com.google.firebase.firestore.DocumentSnapshot
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase
import kotlinx.android.synthetic.main.activity_main.*
import kotlinx.android.synthetic.main.fragment_main_screen.*
import kotlinx.android.synthetic.main.fragment_profile.*
import kotlinx.android.synthetic.main.fragment_profile.edit_button
import kotlinx.android.synthetic.main.fragment_reference.*
import kotlinx.android.synthetic.main.reference_row_table.view.*
import java.text.SimpleDateFormat
import java.util.*


// TODO: Rename parameter arguments, choose names that match
// the fragment initialization parameters, e.g. ARG_ITEM_NUMBER
private const val ARG_PARAM1 = "parma1"
private const val ARG_PARAM2 = "param2"
private const val PROFILES_COLLECTION = "profiles"
private const val FILE_SELECT_CODE = 0 // added for file uploads

/**
 * A simple [Fragment] subclass.
 * Use the [profileFragment.newInstance] factory method to
 * create an instance of this fragment.
 */
class profileFragment : Fragment(), AdapterView.OnItemSelectedListener , View.OnClickListener{
    // TODO: Rename and change types of parameters
    private var param1: String? = null
    private var param2: String? = null
    val TAG: String = "profile"

    lateinit var firestore: FirebaseFirestore
    lateinit var collectionReference: CollectionReference
    lateinit var binding: FragmentProfileBinding
    private lateinit var docID: String // firestore doc ID of the profile
    private lateinit var profileRef: DocumentReference
    lateinit var _edit_text_array: Array<TextView?>
    lateinit var _map_of_views: Map<String, TextView>
    lateinit var titles_map: Map<TextView, ViewGroup>
    lateinit var referenceList : MutableMap<Int, String>

    private lateinit var religiosityOptions : Array<String> // all of the possible religiosity values
    private lateinit var religiositySpinner: Spinner

    private lateinit var parentStatusOptions : Array<String> // all of the possible parent status values
    private lateinit var parentStatusSpinnersMap: Map<String,Spinner>

    private lateinit var textInputLayoutArray: Array<TextInputLayout> // all of the fragments textInputLayouts

    private val formatDate = SimpleDateFormat("dd/MM/yyyy", Locale.ITALY) // italy is a random eu state


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        arguments?.let {
            param1 = it.getString(ARG_PARAM1)
            param2 = it.getString(ARG_PARAM2)
        }
        setHasOptionsMenu(true)
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {

        // Inflate the layout for this fragment
        binding = DataBindingUtil.inflate(inflater, R.layout.fragment_profile, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        // ActionBar title text
        activity?.titleTextView?.text = getString(R.string.profileFragmentTitle)

        firestore = Firebase.firestore
        docID = profileFragmentArgs.fromBundle(requireArguments()).docID
        profileRef = firestore.collection(PROFILES_COLLECTION).document(docID)

        initialData()
        setUpTitles()
        setUpSpinners()
        displayMode(view)

        // edit and submit button listeners
        binding.editButton.setOnClickListener {
            editMode(it)
        }
        binding.submitChanges.setOnClickListener {
            updateProfile(view)
        }

        // date of birth click listener
        binding.editBirthDate.setOnClickListener{v:View -> setDatePicker(v)}

        // references table
        referenceList = mutableMapOf()
        collectionReference = firestore.collection(PROFILES_COLLECTION).document(docID).collection("References")
        collectionReference.get()
            .addOnSuccessListener { documents->
                for (doc in documents){
                    val tr = layoutInflater.inflate(R.layout.reference_row_table, binding.referenceTable, false)
                    tr.setOnClickListener(this)
                    referenceList[tr.id] = doc.id
                    tr.reference_date.text = doc.data["dateOfRef"].toString()
                    tr.referer_name.text = doc.data["receiverName"].toString()
                    tr.reference_status.text = doc.data["refStatus"].toString()
                    binding.referenceTable.addView(tr,1)
                }
            }
        binding.addReferenceBtn.setOnClickListener {
            navigateToNewReference()
        }
    }

    /**
     * set up the subsections titles - the listeners that open the subsections card, and move the
     * arrow icons etc.
     */
    private fun setUpTitles() {
        for ((k, v) in titles_map) {
            k.setOnClickListener {
                if (v.visibility == View.GONE) {
                    TransitionManager.beginDelayedTransition(v, AutoTransition())
                    k.setCompoundDrawablesWithIntrinsicBounds(
                        R.drawable.ic_baseline_keyboard_arrow_up_24,
                        0,
                        0,
                        0
                    )
                    v.visibility = View.VISIBLE;
                    if (k == referenceTitle) {
                        binding.scrollView.post {
                            binding.scrollView.fullScroll(View.FOCUS_DOWN) // scrolls up to the first-name view
                            // needed because reference card is the last card
                            // and this makes it more comfortable when opening
                        }
                    }
                } else {
                    k.setCompoundDrawablesWithIntrinsicBounds(
                        R.drawable.ic_baseline_keyboard_arrow_down_24,
                        0,
                        0,
                        0
                    )
                    v.visibility = View.GONE;
                }
            }
        }
        // sets up the checkbox for the other guardian
        hasOtherGuardian.setOnCheckedChangeListener { buttonView, isChecked ->
            if (isChecked) {
                guardianCard.visibility = View.VISIBLE
            } else {
                guardianCard.visibility = View.GONE
            }
        }
    }

    /**
     * updates the profile on firestore according to all of the values that are currently in the
     * fragments views
     */
    private fun updateProfile(view: View) {
        for ((k, v) in _map_of_views) {
            profileRef.update(k, v.text.toString()).addOnSuccessListener {}
                .addOnFailureListener { exception -> Log.d(TAG, "get failed with ", exception) }
        }
        // the next section is for data that appears in views that are not regular text views, or
        // for values that are not string.
        profileRef.update(
            "religiosity",
            getDataFromSpinners(religiositySpinner, religiosityOptions)
        )
        for ((field, spinner) in parentStatusSpinnersMap) {
            profileRef.update(field, getDataFromSpinners(spinner, parentStatusOptions))
        }
        profileRef.update("otherGuardian", hasOtherGuardian.isChecked)
        profileRef.update("activeStudent", isActiveStudent.isChecked)
        profileRef.update("searchList", createSearchList())
        displayMode(view)

        // todo chage when changing DateOfBirth to date from string
        if (edit_birth_date.text.toString() == getString(R.string.please_choose)){
            profileRef.update("dateOfBirth", "")
            }else{
            profileRef.update("dateOfBirth", edit_birth_date.text.toString())
        }
    }

    /**
     * generates a searchList according to the current values that are in the views.
     * searchList is a field of the profile, of type List<String>, that allows the user to search the profile
     */
    private fun createSearchList(): List<String> {
        val currFirstName = binding.editFirstName.text.toString()
        val currLastName = binding.editLastName.text.toString()
        val currID = binding.editId.text.toString()
        return listOf(currFirstName,currLastName,"$currFirstName $currLastName", currID)
    }

    /**
     * this function builds and display a date picker for the date of birth field
     */
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
                binding.editBirthDate.text = date
            }, year, month, day)
        datePicker.show()
    }

    /**
     * this function initialized all of the spinners in the fragment. Each spinner is initialized with
     * its respective string array, containing the spinner values, the default spinner layout, and
     * a custom spinner drowpdown item layout that makes sure that all of the items are RTL
     */
    private fun setUpSpinners() {
        // initializes references to the string arrays of the spinners
        religiosityOptions = resources.getStringArray(R.array.religious_status_array)
        parentStatusOptions = resources.getStringArray(R.array.family_status_array)
        // Create an ArrayAdapter using the string array and a spinner layout
        religiositySpinner = binding.religiositySpinner
        parentStatusSpinnersMap = mapOf(
            "fatherStatus" to fatherStatusSpinner,"motherStatus" to motherStatusSpinner,
            "guardianStatus" to guardianStatusSpinner)
        ArrayAdapter.createFromResource(
            requireActivity(),
            R.array.religious_status_array,
            android.R.layout.simple_spinner_item
        ).also { adapter ->
            // apply the custom rtl layout to use when the list of choices appears
            adapter.setDropDownViewResource(R.layout.rtl_spinner_dropdown_item)
            // Apply the adapter to the spinner
            religiositySpinner.adapter = adapter
        }
        for((field,spinner) in parentStatusSpinnersMap){
            ArrayAdapter.createFromResource(
                requireActivity(),
                R.array.family_status_array,
                android.R.layout.simple_spinner_item
            ).also { adapter ->
                // apply the custom rtl layout to use when the list of choices appears
                adapter.setDropDownViewResource(R.layout.rtl_spinner_dropdown_item)
                // Apply the adapter to the spinner
                spinner.adapter = adapter
            }
        }
    }

    /**
     * gets a spinner and the array that contains the spinner options, and returns the value that
     * the user has chosen in the spinner
     */
    private fun getDataFromSpinners(spinner: Spinner, options: Array<String>): String{
        val chosenSpinnerIndex = spinner.selectedItemPosition
        if( chosenSpinnerIndex == 0){
            return "" // the first index is not a value (its a string like "please choose...")
        }
        return options[chosenSpinnerIndex]
    }

    /**
     * initializes all of the data structures that the fragment needs
     */
    private fun initialData() {
        _edit_text_array = arrayOf(
            binding.editFirstName, binding.editLastName, binding.editId,
            binding.editPhone, binding.editAddress,
            binding.editBirthDate, binding.editAge, binding.editOriginCountry, binding.editYearOfAliyah,
            binding.fatherName,binding.fatherAddress,binding.fatherPhone,
            binding.motherName,binding.motherAddress,binding.motherPhone,
            binding.guardianName,binding.guardianAddress,binding.guardianPhone,
            binding.hasOtherGuardian,
            binding.editCivil,
            binding.editMedication, binding.editMedicalProblems, binding.editGrade,
            binding.editLastSchool, binding.isActiveStudent)

        _map_of_views = mapOf("firstName" to edit_first_name, "lastName" to edit_last_name,
            "id" to edit_id, "phone" to edit_phone,"homeAddress" to edit_address,
            "dateOfBirth" to edit_birth_date, "OriginCountry" to edit_origin_country,
            "yearOfAliyah" to edit_year_of_aliyah,
            "fatherName" to fatherName, "fatherAddress" to fatherAddress, "fatherPhone" to fatherPhone,            "fatherName" to fatherName, "fatherAddress" to fatherAddress, "fatherPhone" to fatherPhone,
            "motherName" to motherName, "motherAddress" to motherAddress, "motherPhone" to motherPhone,
            "guardianName" to guardianName, "guardianAddress" to guardianAddress, "guardianPhone" to guardianPhone,
            "citizenshipStatus" to edit_civil)
        titles_map = mapOf(generalTitle to generalData, parentsTitle to parentsData,
            healthTitle to healthData, educationTitle to educationData, referenceTitle to referenceData)

        textInputLayoutArray = arrayOf(
            textInputLayoutFatherName,textInputLayoutFatherAddress,textInputLayoutFatherPhone,
            textInputLayoutMotherName,textInputLayoutMotherAddress,textInputLayoutMotherPhone,
            textInputLayoutGuardianName,textInputLayoutGuardianAddress,textInputLayoutGuardianPhone,
            )
    }

    /**
     * reads the data from the profile firestore document and puts it in the views
     */
    private fun retrieveProfileData(document: DocumentSnapshot) {
        Log.d(TAG, "DocumentSnapshot data: ${document.data}")
        for ((k, v) in _map_of_views) {
            v.text = document.getString(k)
        }
        // the next section is for data that appears in views that are not regular text views, or
        // for values that are not string.
        val currReligiosity= document.getString("religiosity")
        // todo boolean fields might become nullable soon, so we'll need to handle them differently
        val hasOtherGuardian = document.getBoolean("otherGuardian")!! //currently never null, init as false
        val isActiveStudent = document.getBoolean("activeStudent")!! //currently never null, init as false
        val currParentStatusMap= mapOf(
            "fatherStatus" to document.getString("fatherStatus"),"motherStatus" to document.getString("motherStatus"),
            "guardianStatus" to document.getString("guardianStatus")
        )
        // date of birth should become date we will need to add a special case
        val dateOfBirth = document.getString("dateOfBirth")

        if (currReligiosity != null) { // religiosity is never null
            if(currReligiosity.isNotBlank()){
                religiositySpinner.setSelection(religiosityOptions.indexOf(currReligiosity))
            }
        }
        for ((parent,currStatus) in currParentStatusMap){
            if (currStatus!= null) { // status is never null
                if(currStatus.isNotBlank()){
                    parentStatusSpinnersMap[parent]?.setSelection(parentStatusOptions.indexOf(currStatus))
                }
            }
        }
        // todo change checkboxes to spinners
        if (hasOtherGuardian){
            binding.hasOtherGuardian.isChecked = true
        }
        if(isActiveStudent){
            binding.isActiveStudent.isChecked = true
        }
        if (dateOfBirth != null) { // todo change when changing DOB to date
            if (dateOfBirth.isBlank())
                binding.editBirthDate.text = getString(R.string.please_choose)
        }
    }

    /**
     * sets the mode of the fragment to "display" - the user can't edit the profile data
     */
    private fun displayMode(view: View) {
        for (edit_text in _edit_text_array) {
            if (edit_text != null) {
                edit_text.isEnabled = false
            }
        }
        for(layout in textInputLayoutArray){
            layout.isEnabled = false
        }
        religiositySpinner.isEnabled = false
        for ((field,spinner) in parentStatusSpinnersMap){
            spinner.isEnabled = false
        }

        profileRef.get()
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

        view.visibility = View.VISIBLE // todo ask shira what this line is for
        edit_button.visibility = View.VISIBLE
        submit_changes.visibility = View.GONE
    }

    /**
     * sets the mode of the fragment to "edit" - the user can edit the profile data
     */
    private fun editMode(view: View) {
        for (edit_text in _edit_text_array) {
            if (edit_text != null) {
                edit_text.isEnabled = true
            }
        }
        for(layout in textInputLayoutArray){
            layout.isEnabled = true
        }
        religiositySpinner.isEnabled = true
        for ((field,spinner) in parentStatusSpinnersMap){
            spinner.isEnabled = true
        }
        view.visibility = View.GONE
        submit_changes.visibility = View.VISIBLE
    }


    override fun onCreateOptionsMenu(menu: Menu, inflater: MenuInflater) {
        inflater.inflate(R.menu.menu_profile_fragment, menu)
    }
    private fun navigateToNewReference(){
        findNavController().navigate(profileFragmentDirections.actionProfileFragmentToNewReference(false,docID))
    }
    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        return when (item.itemId) {
            R.id.add_reference-> {
                navigateToNewReference()
                true
            }
            R.id.delete_profile-> {
                true
            }

            else -> super.onOptionsItemSelected(item)
        }
    }
    override fun onClick(v: View?) {
        v?.findNavController()?.navigate(
            profileFragmentDirections.actionProfileFragmentToReferenceFragment(docID,referenceList[v.id].toString()))
    }

    override fun onItemSelected(parent: AdapterView<*>?, view: View?, position: Int, id: Long) {
//        textView_msg!!.text = "Selected : "+languages[position]
    }

    override fun onNothingSelected(parent: AdapterView<*>?) {
        TODO("Not yet implemented")
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
}


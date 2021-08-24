package com.example.atnachta

import android.os.Bundle
import android.transition.AutoTransition
import android.transition.TransitionManager
import android.util.Log
import android.view.*
import android.widget.*
import androidx.databinding.DataBindingUtil
import androidx.fragment.app.Fragment
import androidx.navigation.NavController
import androidx.navigation.findNavController
import com.example.atnachta.databinding.FragmentProfileBinding
import com.google.firebase.firestore.CollectionReference
import com.google.firebase.firestore.DocumentReference
import com.google.firebase.firestore.DocumentSnapshot
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase
import kotlinx.android.synthetic.main.fragment_main_screen.*
import kotlinx.android.synthetic.main.fragment_profile.*
import kotlinx.android.synthetic.main.fragment_profile.edit_button
import kotlinx.android.synthetic.main.fragment_reference.*
import kotlinx.android.synthetic.main.reference_row_table.view.*


// TODO: Rename parameter arguments, choose names that match
// the fragment initialization parameters, e.g. ARG_ITEM_NUMBER
private const val ARG_PARAM1 = "parma1"
private const val ARG_PARAM2 = "param2"
private const val PROFILES_COLLECTION = "profiles"

/**
 * A simple [Fragment] subclass.
 * Use the [profileFragment.newInstance] factory method to
 * create an instance of this fragment.
 */
class profileFragment : Fragment(), AdapterView.OnItemSelectedListener , View.OnClickListener{
    // TODO: Rename and change types of parameters
    private var param1: String? = null
    private var param2: String? = null
    lateinit var firestore: FirebaseFirestore
    lateinit var collectionReference: CollectionReference
    lateinit var binding: FragmentProfileBinding
    lateinit var girlDocRef: DocumentReference
    private lateinit var docID: String
    val TAG: String = "profile"
    var _edit_text_array: Array<TextView?> = arrayOfNulls(20)
    lateinit var _map_of_views: Map<String, TextView>
    lateinit var titles_map: Map<TextView, ViewGroup>
    lateinit var referenceList : MutableMap<Int, String>
    lateinit var navController: NavController

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        arguments?.let {
            param1 = it.getString(ARG_PARAM1)
            param2 = it.getString(ARG_PARAM2)
        }
        setHasOptionsMenu(true)

    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        firestore = Firebase.firestore
        docID = profileFragmentArgs.fromBundle(requireArguments()).docID
        girlDocRef = firestore.collection(PROFILES_COLLECTION).document(docID.toString())

        initialData(view)
        displayMode(view)

        binding.editButton.setOnClickListener {
            editMode(it)
        }

        binding.submitChanges.setOnClickListener { view ->
            for ((k, v) in _map_of_views) {
                girlDocRef.update(k, v.text.toString()).addOnSuccessListener {}
                    .addOnFailureListener { exception -> Log.d(TAG, "get failed with ", exception) }
            }
            girlDocRef.update("id", edit_id.text.toString()).addOnSuccessListener {}
                .addOnFailureListener { exception -> Log.d(TAG, "get failed with ", exception) }
            displayMode(view)
        }

        activity?.setTitle(R.string.basicDetails)

        //movement of the titles
        for((k,v) in titles_map){
            k.setOnClickListener {
                if (v.visibility == View.GONE) {
                    TransitionManager.beginDelayedTransition(v, AutoTransition())
                    v.visibility = View.VISIBLE;
                } else {
                    v.visibility = View.GONE;
                }
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
        referenceList = mutableMapOf()
        collectionReference = firestore.collection(PROFILES_COLLECTION).document(docID.toString()).collection("References")
        collectionReference.get()
            .addOnSuccessListener { documents->
                for (doc in documents){

                    val tr = layoutInflater.inflate(R.layout.reference_row_table, binding.referenceTable, false)
                    tr.setOnClickListener(this)

                    referenceList[tr.id] = doc.id
                    tr.reference_date.text = doc.data["dateOfRef"].toString()
                    tr.referer_name.text = doc.data["receiverName"].toString()
                    binding.referenceTable.addView(tr,1)
                }
            }

        binding.addReferenceBtn.setOnClickListener {
            navigateToNewReference()
        }
    }

    private fun initialData(view: View) {
        _edit_text_array = arrayOf(
            binding.editFirstName, binding.editLastName, binding.editId,
            binding.editPhone,
//            binding.editFatherName, binding.editMotherName,
//            binding.editFatherPhone, binding.editMotherPhone,
            binding.editAddress,
            binding.editBirthDate, binding.editOriginCountry, binding.editYearOfAliyah,
            binding.editReligiosity, binding.editCivil,
            binding.editMedication, binding.editMedicalProblems, binding.editAge, binding.editGrade,
            binding.editLastSchool)

        _map_of_views = mapOf("firstName" to edit_first_name, "lastName" to edit_last_name,
            "id" to edit_id, "phone" to edit_phone,"homeAddress" to edit_address,
            "dateOfBirth" to edit_birth_date, "OriginCountry" to edit_origin_country,
            "yearOfAliyah" to edit_year_of_aliyah, "religiosity" to edit_religiosity,
            "citizenshipStatus" to edit_civil)
        titles_map = mapOf(generalTitle to generalData, parentsTitle to parentsData,
            healthTitle to healthData, educationTitle to educationData)
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {

        // Inflate the layout for this fragment
        binding = DataBindingUtil.inflate(inflater, R.layout.fragment_profile, container, false)
        return binding.root
    }

    private fun retrieveProfileData(document: DocumentSnapshot) {
        Log.d(TAG, "DocumentSnapshot data: ${document.data}")
        for ((k, v) in _map_of_views) {
            v.text = document.getString(k)
        }
    }

    private fun displayMode(view: View) {
//        test.isEnabled = false
//        test.setTextColor(-16777216)
        for (edit_text in _edit_text_array) {
            if (edit_text != null) {
                edit_text.isEnabled = false
            }
        }
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

        view.visibility = View.VISIBLE
        edit_button.visibility = View.VISIBLE
        submit_changes.visibility = View.GONE
    }

    private fun editMode(view: View) {
        for (edit_text in _edit_text_array) {
            if (edit_text != null) {
                edit_text.isEnabled = true
            }
        }
        view.visibility = View.GONE
        submit_changes.visibility = View.VISIBLE
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
    override fun onCreateOptionsMenu(menu: Menu, inflater: MenuInflater) {
        inflater.inflate(R.menu.menu_profile_fragment, menu)
    }
    private fun navigateToNewReference(){
        navController.navigate(profileFragmentDirections.actionProfileFragmentToNewReference(false,docID))
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
            profileFragmentDirections.actionProfileFragmentToReferenceFragment(docID.toString(),referenceList[v.id].toString()))
    }

    override fun onItemSelected(parent: AdapterView<*>?, view: View?, position: Int, id: Long) {
//        textView_msg!!.text = "Selected : "+languages[position]
    }

    override fun onNothingSelected(parent: AdapterView<*>?) {
        TODO("Not yet implemented")
    }
}


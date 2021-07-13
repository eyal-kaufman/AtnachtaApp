package com.example.atnachta

import android.os.Bundle
import android.transition.AutoTransition
import android.transition.TransitionManager
import android.util.Log
import android.view.*
import android.widget.*
import androidx.appcompat.app.AppCompatActivity
import androidx.databinding.DataBindingUtil
import androidx.fragment.app.Fragment
import androidx.navigation.NavController
import androidx.navigation.NavHostController
import androidx.navigation.findNavController
import androidx.navigation.fragment.NavHostFragment.findNavController
import androidx.navigation.fragment.findNavController
import androidx.navigation.ui.onNavDestinationSelected
import kotlinx.android.synthetic.main.fragment_profile.*
import com.example.atnachta.databinding.FragmentProfileBinding
import com.google.firebase.firestore.CollectionReference
import com.google.firebase.firestore.DocumentReference
import com.google.firebase.firestore.DocumentSnapshot
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.firestore.ktx.toObject
import com.google.firebase.ktx.Firebase
import kotlinx.android.synthetic.main.fragment_main_screen.*
import kotlinx.android.synthetic.main.fragment_profile.*
import kotlinx.android.synthetic.main.fragment_profile.edit_button
import kotlinx.android.synthetic.main.fragment_profile.edit_id
import kotlinx.android.synthetic.main.fragment_profile.edit_phone
import kotlinx.android.synthetic.main.fragment_profile.edited_id
import kotlinx.android.synthetic.main.fragment_profile.edited_phone
import kotlinx.android.synthetic.main.fragment_reference.*
import kotlinx.android.synthetic.main.reference_row_table.view.*


// TODO: Rename parameter arguments, choose names that match
// the fragment initialization parameters, e.g. ARG_ITEM_NUMBER
private const val ARG_PARAM1 = "docID"
private const val ARG_PARAM2 = "param2"
private const val PROFILES_COLLECTION = "profiles"

/**
 * A simple [Fragment] subclass.
 * Use the [profileFragment.newInstance] factory method to
 * create an instance of this fragment.
 */
class profileFragment : Fragment(), AdapterView.OnItemSelectedListener , View.OnClickListener{
    // TODO: Rename and change types of parameters
    private var docID: String? = null
    private var param2: String? = null
    lateinit var firestore: FirebaseFirestore
    lateinit var collectionReference: CollectionReference
    lateinit var binding: FragmentProfileBinding
    lateinit var girlDocRef: DocumentReference
    val TAG: String = "profile"
    var _edit_text_array: Array<TextView?> = arrayOfNulls(20)
    var _edited_text_array: Array<TextView?> = arrayOfNulls(20)
    lateinit var _map_of_views: Map<String, ArrayList<TextView>>
    lateinit var _map_of_titles: Map<TextView, TableLayout>
    lateinit var referenceList : MutableMap<Int, String>
    lateinit var navController: NavController

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        arguments?.let {
            docID = it.getString(ARG_PARAM1)
            param2 = it.getString(ARG_PARAM2)
        }
        navController = findNavController(this)

        setHasOptionsMenu(true)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
//        girlDocId = profileFragmentArgs.fromBundle(requireArguments()).docID
        firestore = Firebase.firestore
        girlDocRef = firestore.collection(PROFILES_COLLECTION).document(docID.toString())

        initialData(view)
        displayMode(view)

        binding.editButton.setOnClickListener {
            editMode(it)
        }

        binding.submitChanges.setOnClickListener { view ->
            for ((k, v) in _map_of_views) {
                girlDocRef.update(k, v[0].text.toString()).addOnSuccessListener {}
                    .addOnFailureListener { exception -> Log.d(TAG, "get failed with ", exception) }
            }
            girlDocRef.update("id", edit_id.text.toString()).addOnSuccessListener {}
                .addOnFailureListener { exception -> Log.d(TAG, "get failed with ", exception) }
            displayMode(view)
        }

        activity?.setTitle(R.string.basicDetails)

        //movement of the titles
        for((k,v) in _map_of_titles){
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
            binding.editPhone, binding.editFatherName, binding.editMotherName,
            binding.editFatherPhone, binding.editMotherPhone, binding.editAddress,
            binding.editBirthDate, binding.editKupach, binding.editSchool, binding.editCivil
        )
        _edited_text_array = arrayOf(
            binding.editedFirstName, binding.editedLastName,
            binding.editedId, binding.editedPhone, binding.editedFatherName,
            binding.editedMotherName, binding.editedFatherPhone, binding.editedMotherPhone,
            binding.editedAddress, binding.editedBirthDate,
            binding.editedKupach, binding.editedSchool, binding.editedCivil
        )
        _map_of_views = mapOf("firstName" to arrayListOf(edit_first_name, edited_first_name),
            "lastName" to arrayListOf(edit_last_name, edited_last_name),
            "phone" to arrayListOf(edit_phone, edited_phone),
            "homeAddress" to arrayListOf(edit_address, edited_address)
        )
        _map_of_titles = mapOf(generalTitle to generalData, parentsTitle to parentsData,
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
            v[1].text = document.getString(k)
            v[0].setText(document.getString(k))
        }
        edited_id.text = document.get("id").toString()
        edit_id.setText(document.get("id").toString())
    }

    private fun displayMode(view: View) {
        for (edit_text in _edit_text_array) {
            if (edit_text != null) {
                edit_text.visibility = View.GONE
            }
        }
        for (edited_text in _edited_text_array) {
            if (edited_text != null) {
                edited_text.visibility = View.VISIBLE
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
                edit_text.visibility = View.VISIBLE
            }
        }
        for (edited_text in _edited_text_array) {
            if (edited_text != null) {
                edited_text.visibility = View.GONE
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
        navController.navigate(profileFragmentDirections.actionProfileFragmentToNewReference(false, "",docID))
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
        TODO("Not yet implemented")
    }

    override fun onNothingSelected(parent: AdapterView<*>?) {
        TODO("Not yet implemented")
    }
}

//--------- useful things ----------//

//                binding.button4.setOnClickListener { view: View ->
//            view.findNavController().navigate(R.id.action_profileFragment_to_newReferenceFragment)
//        }
//        binding.examples.setOnClickListener { view: View ->
//            view.findNavController().navigate(R.id.action_ProfileFragment_to_ReferenceFragment)
//        }
//
//        for(i in 0..fields.size){
//            val field = fields.get(i)
//            _map_of_views.plus(field, arrayListOf(_edit_text_array[i], _edited_text_array[i]))
//        }

//------------------------------------------------------------//

//private fun updateProfileData(document: DocumentSnapshot) {
//        girlDocRef.update("firstName", edit_first_name.text.toString())
//        girlDocRef.update("lastName", edit_last_name.text.toString())
//        girlDocRef.update("id", edit_id.text.toString())
//        girlDocRef.update("phone", edit_phone)
//        girlDocRef.update("homeAddress", edit_address.text.toString())
//        Log.d(TAG, "DocumentSnapshot update: ${document.data}")
//    }

//        edited_first_name.visibility = View.VISIBLE
//        edit_first_name.visibility = View.GONE

//        edited_last_name.visibility = View.VISIBLE
//        edit_last_name.visibility = View.GONE
//
//        edited_id.visibility = View.VISIBLE
//        edit_id.visibility = View.GONE
//
//        edited_phone.visibility = View.VISIBLE
//        edit_phone.visibility = View.GONE
//
////        edited_father_name.text = edit_father_name.text
//        edited_father_name.visibility = View.VISIBLE
//        edited_mother_name.visibility = View.VISIBLE
//
////        edited_mother_name.text = edit_mother_name.text
//        edit_father_name.visibility = View.GONE
//        edit_mother_name.visibility = View.GONE
//
////        edited_father_phone.text = edit_father_phone.text
//        edited_father_phone.visibility = View.VISIBLE
//        edited_mother_phone.visibility = View.VISIBLE
//
////        edited_mother_phone.text = edit_mother_phone.text
//        edit_father_phone.visibility = View.GONE
//        edit_mother_phone.visibility = View.GONE
//
////        edited_address.text = edit_address.text
//        edited_address.visibility = View.VISIBLE
//        edit_address.visibility = View.GONE
//
////        edited_birth_date.text = edit_birth_date.text
//        edited_birth_date.visibility = View.VISIBLE
//        edit_birth_date.visibility = View.GONE
//
////        edited_kupach.text = edit_kupach.text
//        edited_kupach.visibility = View.VISIBLE
//        edit_kupach.visibility = View.GONE
//
////        edited_school.text = edit_school.text
//        edited_school.visibility = View.VISIBLE
//        edit_school.visibility = View.GONE
//
////        edited_civil.text = edit_civil.text
//        edited_civil.visibility = View.VISIBLE
//        edit_civil.visibility = View.GONE


//        for(edit_text in _edit_text_array){
//            edit_text.visibility = View.VISIBLE
//        }
//        for(edited_text in _edited_text_array){
//            edited_text.visibility = View.GONE
//        }
//
//        edited_first_name.visibility = View.GONE
//        edit_first_name.visibility = View.VISIBLE
//
//        edited_last_name.visibility = View.GONE
//        edit_last_name.visibility = View.VISIBLE
//
//        edited_id.visibility = View.GONE
//        edit_id.visibility = View.VISIBLE
//
//        edited_phone.visibility = View.GONE
//        edit_phone.visibility = View.VISIBLE
//
//        edited_address.visibility = View.GONE
//        edit_address.visibility = View.VISIBLE
//
//         //--------------------------------//
//
//        edited_father_name.visibility = View.GONE
//        edited_mother_name.visibility = View.GONE
//
//        edit_father_name.visibility = View.VISIBLE
//        edit_mother_name.visibility = View.VISIBLE
//
//        edited_father_phone.visibility = View.GONE
//        edited_mother_phone.visibility = View.GONE
//
//        edit_father_phone.visibility = View.VISIBLE
//        edit_mother_phone.visibility = View.VISIBLE
//
//        edited_birth_date.visibility = View.GONE
//        edit_birth_date.visibility = View.VISIBLE
//
//        edited_kupach.visibility = View.GONE
//        edit_kupach.visibility = View.VISIBLE
//
//        edited_school.visibility = View.GONE
//        edit_school.visibility = View.VISIBLE
//
//        edited_civil.visibility = View.GONE
//        edit_civil.visibility = View.VISIBLE
package com.example.atnachta

import android.os.Bundle
import android.transition.AutoTransition
import android.transition.TransitionManager
import android.util.Log
import android.view.*
import androidx.fragment.app.Fragment
import androidx.databinding.DataBindingUtil
import androidx.navigation.NavController
import androidx.navigation.NavHostController
import androidx.navigation.findNavController
import androidx.navigation.fragment.NavHostFragment.findNavController
import androidx.navigation.fragment.findNavController
import androidx.navigation.ui.onNavDestinationSelected
import kotlinx.android.synthetic.main.fragment_profile.*
import com.example.atnachta.databinding.FragmentProfileBinding
import com.google.firebase.firestore.CollectionReference
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase
import kotlinx.android.synthetic.main.fragment_profile.view.*
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
class profileFragment : Fragment(), View.OnClickListener{
    // TODO: Rename and change types of parameters
    private var docID: String? = null
    private var param2: String? = null

    lateinit var binding: FragmentProfileBinding
    lateinit var firestore: FirebaseFirestore
    lateinit var collectionReference: CollectionReference
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


    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        binding = DataBindingUtil.inflate(inflater, R.layout.fragment_profile, container, false)

//        binding.button4.setOnClickListener { view : View -> view.findNavController().navigate(R.id.action_profileFragment_to_newReferenceFragment)}
        activity?.setTitle(R.string.basicDetails)
        referenceList = mutableMapOf()
        firestore = Firebase.firestore
        collectionReference = firestore.collection(PROFILES_COLLECTION).document(docID.toString()).collection("References")
        collectionReference.get()
            .addOnSuccessListener { documents->
                for (doc in documents){

                    val tr = inflater.inflate(R.layout.reference_row_table, binding.referenceTable, false)
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
        binding.editButton.setOnClickListener {
            editMode(it)
//            var tableRow = TableRow(context)
//            var b = Button (context)
//            b.setText(R.string.basicDetails)
//            tableRow.addView(b)


        }

        binding.button5.setOnClickListener {
            displayMode(it)
        }
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
        return binding.root
    }

    private fun displayMode(view: View) {
        edited_name.text = edit_name.text
        edited_name.visibility = View.VISIBLE
        edit_name.visibility = View.GONE

        edited_id.text = edit_id.text
        edited_id.visibility = View.VISIBLE
        edit_id.visibility = View.GONE

        edited_phone.text = edit_phone.text
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

        view.visibility = View.GONE
        button5.visibility = View.VISIBLE
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
//                val navController =findNavController()

//                val navController = findNavController(R.id.)
//                navController.na
//                item.findNavController()?.navigate(RecycleSearchDirections.actionRecycleSearchToNewReference(true,"",""))
//            item.onNavDestinationSelected(navController)
//                navController.navigate(profileFragmentDirections.actionProfileFragmentToNewReference(false, "",docID))
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
            profileFragmentDirections.actionProfileFragmentToNewReference(false, referenceList[v.id],docID))
    }
}
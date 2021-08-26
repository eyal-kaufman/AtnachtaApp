package com.example.atnachta


import androidx.navigation.findNavController
import android.os.Bundle
import android.transition.AutoTransition
import android.transition.TransitionManager
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.CheckBox
import android.widget.TextView
import androidx.databinding.DataBindingUtil
import androidx.fragment.app.Fragment
import androidx.navigation.NavController
import com.example.atnachta.databinding.FragmentProfileBinding
import kotlinx.android.synthetic.main.fragment_profile.*
import kotlinx.android.synthetic.main.fragment_reference.*
import com.example.atnachta.databinding.FragmentReferenceBinding
import com.google.firebase.firestore.CollectionReference
import com.google.firebase.firestore.DocumentReference
import com.google.firebase.firestore.DocumentSnapshot
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase
import kotlinx.android.synthetic.main.activity_main.*
import kotlinx.android.synthetic.main.reference_row_table.view.*


// TODO: Rename parameter arguments, choose names that match
// the fragment initialization parameters, e.g. ARG_ITEM_NUMBER
private const val ARG_PARAM1 = "param1"
private const val ARG_PARAM2 = "param2"
private const val REFERENCES_COLLECTION = "references"

/**
 * A simple [Fragment] subclass.
 * Use the [ReferenceFragment.newInstance] factory method to
 * create an instance of this fragment.
 */
class ReferenceFragment : Fragment() {
    // TODO: Rename and change types of parameters
    private var param1: String? = null
    private var param2: String? = null
    lateinit var binding: FragmentReferenceBinding
    lateinit var firestore: FirebaseFirestore
    lateinit var collectionReference: CollectionReference
    lateinit var girlDocRef: DocumentReference
    val TAG: String = "reference"
    var _edit_text_array: Array<TextView?> = arrayOfNulls(20)
    lateinit var _map_of_views: Map<String, TextView>
    lateinit var referenceList: MutableMap<Int, String>
    lateinit var navController: NavController


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

        binding = DataBindingUtil.inflate(inflater, R.layout.fragment_reference, container, false)
        var checkBoxWelfare: CheckBox = binding.ifWelfare
        var checkBoxLeft: CheckBox = binding.isLeft
        checkBoxWelfare.setOnCheckedChangeListener { buttonView, isChecked ->
            if (isChecked) {
                welfare_data.visibility = View.VISIBLE
            } else {
                welfare_data.visibility = View.GONE
            }
        }
        checkBoxLeft.setOnCheckedChangeListener { buttonView, isChecked ->
            if (isChecked) {
                leaving_data.visibility = View.VISIBLE
            } else {
                leaving_data.visibility = View.GONE
            }
        }
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        // ActionBar title text
        activity?.titleTextView?.text = getString(R.string.referenceFragmentTitle)

        firestore = Firebase.firestore
        girlDocRef = firestore.collection(REFERENCES_COLLECTION).document(param1.toString())

        initialData(view)
        displayMode(view)

        binding.refEditButton.setOnClickListener {
            editMode(it)
        }

        binding.refSubmitChanges.setOnClickListener {
            for ((k, v) in _map_of_views) {
                girlDocRef.update(k, v.text.toString()).addOnSuccessListener {}
                    .addOnFailureListener { exception -> Log.d(TAG, "get failed with ", exception) }
            }
            displayMode(view)
        }
        binding.FilesTitle.setOnClickListener {
            val details = binding.FilesData
            if (details.visibility == View.GONE){
                TransitionManager.beginDelayedTransition(details, AutoTransition())
                binding.FilesData.visibility = View.VISIBLE
            }
            else{
              details.visibility = View.GONE
            }
         }
    }

    private fun initialData(view: View) {
        _edit_text_array = arrayOf(
            binding.editRefDate, binding.editRefLeavingDate, binding.editRefLeavingDest,
            binding.editRefLeavingMsg, binding.editRefLeavingReason, binding.editRefLeavingTime,
            binding.editRefDate, binding.editRefName, binding.editRefPhone, binding.editRefReason,
            binding.editRefRule, binding.editRefStatus, binding.editRefTime, binding.editRefWelfareName,
            binding.editRefWelfarePhone, binding.editRefWelfareRule, binding.editReferrerName
        )

        _map_of_views = mapOf(
            "receiverName" to edit_ref_name, "dateOfRef" to edit_ref_date,
            "timeOfRef" to edit_ref_time, "welfareName" to edit_ref_welfare_name,
            "welfarePhone" to edit_ref_welfare_phone, "welfarePosition" to edit_ref_welfare_rule,
            "referrerName" to edit_referrer_name, "referrerJob" to edit_ref_rule,
            "referrerPhone" to edit_ref_phone, "refReason" to edit_ref_reason,
            "leavingDate" to edit_ref_leaving_date, "leavingTime" to edit_ref_leaving_time,
            "leavingReason" to edit_ref_leaving_reason,"leavingDestination" to edit_ref_leaving_dest,
            "messageSentTo" to edit_ref_leaving_msg
        )
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
        ref_edit_button.visibility = View.VISIBLE
        ref_submit_changes.visibility = View.GONE
    }

    private fun editMode(view: View) {
        for (edit_text in _edit_text_array) {
            if (edit_text != null) {
                edit_text.isEnabled = true
            }
        }
        view.visibility = View.GONE
        ref_submit_changes.visibility = View.VISIBLE
    }

    companion object {
        /**
         * Use this factory method to create a new instance of
         * this fragment using the provided parameters.
         *
         * @param param1 Parameter 1.
         * @param param2 Parameter 2.
         * @return A new instance of fragment ReferenceFragment.
         */
        // TODO: Rename and change types and number of parameters
        @JvmStatic
        fun newInstance(param1: String, param2: String) =
            ReferenceFragment().apply {
                arguments = Bundle().apply {
                    putString(ARG_PARAM1, param1)
                    putString(ARG_PARAM2, param2)
                }
            }
    }
}
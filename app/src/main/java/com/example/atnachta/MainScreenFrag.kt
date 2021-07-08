package com.example.atnachta

import android.os.Bundle
import android.util.Log
import android.view.*
import androidx.fragment.app.Fragment
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.databinding.DataBindingUtil
import androidx.navigation.findNavController
import com.example.atnachta.databinding.FragmentLoginBinding
import com.example.atnachta.databinding.FragmentMainScreenBinding
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.ktx.auth
import com.google.firebase.ktx.Firebase

// TODO: Rename parameter arguments, choose names that match
// the fragment initialization parameters, e.g. ARG_ITEM_NUMBER
private const val ARG_PARAM1 = "param1"
private const val ARG_PARAM2 = "param2"

private const val TAG = "MainFragment"

// TODO: before send to atnachta - change this to desired "manager" UID, and "instructor" email
private const val SUPER_USER_ID = "LJZYsju9L1StY1NEddRBo3pMLX72"
private const val INSTRUCTOR_USER_EMAIL = "hoplomdim@gmail.com"
/**
 * A simple [Fragment] subclass.
 * Use the [MainScreen.newInstance] factory method to
 * create an instance of this fragment.
 */
class MainScreen : Fragment() {
    // TODO: Rename and change types of parameters
    private var param1: String? = null
    private var param2: String? = null

    private lateinit var binding: FragmentMainScreenBinding
    private lateinit var auth: FirebaseAuth
    private lateinit var userID: String

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
        // init binding
        binding = DataBindingUtil.inflate(inflater,R.layout.fragment_main_screen,container,false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        // ActionBar title
        activity?.setTitle(R.string.mainScreenFragmentTitle)

        // getting Firebase Auth instance
        auth = Firebase.auth

        // getting userID from args
        userID = MainScreenArgs.fromBundle(requireArguments()).userID

        if(userID == SUPER_USER_ID){
            binding.instructorPasswordChangeButton.visibility = View.VISIBLE
        }

        // setup buttons
        binding.mainScreenSearchButton.setOnClickListener {
            val searchInput = binding.initialSearchInput.text.toString()
            view.findNavController().navigate(
            MainScreenDirections.actionMainScreenToRecycleSearch(searchInput))}

        binding.instructorPasswordChangeButton.setOnClickListener{
            auth.sendPasswordResetEmail(INSTRUCTOR_USER_EMAIL)
                .addOnCompleteListener(requireActivity()) { task ->
                    if (task.isSuccessful) {
                        Log.d(TAG, "Email sent.")
                        Toast.makeText(context, getString(R.string.passordResetMsg),
                            Toast.LENGTH_SHORT).show()
                    } else{
                        Log.w(TAG, "UserPasswordReset:failure", task.exception)
                        Toast.makeText(context, getString(R.string.generalPasswordResetError),
                            Toast.LENGTH_SHORT).show()
                    }
                }
        }

    }
    companion object {
        /**
         * Use this factory method to create a new instance of
         * this fragment using the provided parameters.
         *
         * @param param1 Parameter 1.
         * @param param2 Parameter 2.
         * @return A new instance of fragment mainScreen.
         */
        // TODO: Rename and change types and number of parameters
        @JvmStatic
        fun newInstance(param1: String, param2: String) =
            MainScreen().apply {
                arguments = Bundle().apply {
                    putString(ARG_PARAM1, param1)
                    putString(ARG_PARAM2, param2)
                }
            }
    }
}
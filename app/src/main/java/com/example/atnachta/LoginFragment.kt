package com.example.atnachta

import android.os.Bundle
import android.text.TextUtils
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.databinding.DataBindingUtil
import androidx.navigation.NavController
import androidx.navigation.findNavController
import com.example.atnachta.databinding.FragmentLoginBinding
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.ktx.auth
import com.google.firebase.ktx.Firebase

// TODO: Rename parameter arguments, choose names that match
// the fragment initialization parameters, e.g. ARG_ITEM_NUMBER
private const val ARG_PARAM1 = "param1"
private const val ARG_PARAM2 = "param2"

private const val TAG = "LoginFragment"

// TODO: before send to atnachta - change this to desired "manager" email
private const val SUPER_USER_EMAIL = "itaybenyo@gmail.com"

// TODO: please delete this line before sending to atnachta....
private const val DEV_EMAIL = "itaybenyo@gmail.com"
private const val DEV_PASSWORD = "123456"


/**
 * A simple [Fragment] subclass.
 * Use the [LoginFragment.newInstance] factory method to
 * create an instance of this fragment.
 */
class LoginFragment : Fragment() {
    // TODO: Rename and change types of parameters
    private var param1: String? = null
    private var param2: String? = null

    lateinit var auth : FirebaseAuth
    lateinit var binding : FragmentLoginBinding

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

        binding = DataBindingUtil.inflate(inflater,R.layout.fragment_login,container,false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        // ActionBar title text
        activity?.setTitle(R.string.loginFragmentTitle)

        // getting Firebase Auth instance
        auth = Firebase.auth

        // login button setup
        binding.loginButton.setOnClickListener {
            val email = binding.fieldEmail.text.toString()
            val password = binding.fieldPassword.text.toString()
            signIn(email, password,it)
        }


        // password reset link setup
        binding.passwordResetLink.setOnClickListener{
            auth.sendPasswordResetEmail(SUPER_USER_EMAIL)
                .addOnCompleteListener(requireActivity()) { task ->
                    if (task.isSuccessful) {
                        Log.d(TAG, "Email sent.")
                    Toast.makeText(context, getString(R.string.passordResetMsg),
                        Toast.LENGTH_SHORT).show()
                    }
                }
        }

        // login button setup
        binding.devButton.setOnClickListener {
            val email = DEV_EMAIL
            val password = DEV_PASSWORD
            binding.fieldEmail.setText(email)
            binding.fieldPassword.setText(password)
            signIn(email, password,it)
        }
    }

    private fun signIn(email: String, password: String, view: View) {
        Log.d(TAG, "signIn:$email")
        if (!validateForm()) {
            return
        }

        binding.progressIndicator.visibility = View.VISIBLE

        auth.signInWithEmailAndPassword(email, password)
            .addOnCompleteListener(requireActivity()) { task ->
                if (task.isSuccessful) {
                    // Sign in success, update UI with the signed-in user's information
                    Log.d(TAG, "signInWithEmail:success")
                    val user = auth.currentUser
                    if (user != null){
                        val uid = user.uid
                        binding.fieldPassword.setText("")
                        view.findNavController().navigate(
                            LoginFragmentDirections.actionLoginFragmentToMainScreen(uid))
                    }
                    else{
                        Log.w(TAG, "signInWithEmail:failure", task.exception)
                        binding.loginErrorMsg.visibility = View.VISIBLE
                    }
                } else {
                    // If sign in fails, display a message to the user.
                    Log.w(TAG, "signInWithEmail:failure", task.exception)
                    binding.loginErrorMsg.visibility = View.VISIBLE
                }
                binding.progressIndicator.visibility = View.INVISIBLE
            }
    }

    private fun validateForm(): Boolean {
        var valid = true
        val email = binding.fieldEmail.text.toString()
        if (TextUtils.isEmpty(email)) {
            binding.fieldEmail.error = getString(R.string.emptyEmailError)
            valid = false
        } else {
            binding.fieldEmail.error = null
        }

        val password = binding.fieldPassword.text.toString()
        if (TextUtils.isEmpty(password)) {
            binding.fieldPassword.error = getString(R.string.emptyPasswordError)
            valid = false
        } else {
            binding.fieldPassword.error = null
        }

        return valid
    }

    companion object {
        /**
         * Use this factory method to create a new instance of
         * this fragment using the provided parameters.
         *
         * @param param1 Parameter 1.
         * @param param2 Parameter 2.
         * @return A new instance of fragment LoginFragment.
         */
        // TODO: Rename and change types and number of parameters
        @JvmStatic
        fun newInstance(param1: String, param2: String) =
                LoginFragment().apply {
                    arguments = Bundle().apply {
                        putString(ARG_PARAM1, param1)
                        putString(ARG_PARAM2, param2)
                    }
                }
    }
}
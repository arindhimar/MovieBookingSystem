package com.example.combined_loginregister

import android.os.Bundle
import android.text.Editable
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.example.combined_loginregister.databinding.FragmentCommonProfileBinding
import com.google.firebase.auth.EmailAuthProvider
import com.google.firebase.auth.FirebaseAuth

// TODO: Rename parameter arguments, choose names that match
// the fragment initialization parameters, e.g. ARG_ITEM_NUMBER
private const val ARG_PARAM1 = "param1"
private const val ARG_PARAM2 = "param2"

/**
 * A simple [Fragment] subclass.
 * Use the [CommonProfileFragment.newInstance] factory method to
 * create an instance of this fragment.
 */
class CommonProfileFragment : Fragment() {
    lateinit var binding: FragmentCommonProfileBinding
    // TODO: Rename and change types of parameters
    private var param1: String? = null
    private var param2: String? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        arguments?.let {
            param1 = it.getString(ARG_PARAM1)
            param2 = it.getString(ARG_PARAM2)
        }
    }

    private fun ToggleControls(){
        if(binding.enableControls.text=="Disable Controls"){
           binding.enableControls.text = "Enable Controls"
            binding.textInputLayout1.isEnabled=false
            binding.textInputLayout2.isEnabled=false
            binding.textInputLayout3.isEnabled=false
            binding.textInputLayout4.isEnabled=false
        }
        else{
            binding.enableControls.text= "Disable Controls"
            binding.textInputLayout1.isEnabled=true
            binding.textInputLayout2.isEnabled=true
            binding.textInputLayout3.isEnabled=true
            binding.textInputLayout4.isEnabled=true
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        // Inflate the layout for this fragment
        binding = FragmentCommonProfileBinding.inflate(layoutInflater,container,false)

        init()
        ToggleControls()
        return binding.root
    }

    private fun init() {
        val encryption = Encryption(requireContext())

        val userId = encryption.decrypt("userId")



        if (userId != "null") {
            val firebaseRestManager = FirebaseRestManager<UserTb>()
            firebaseRestManager.getSingleItem(
                UserTb::class.java,
                "moviedb/usertb",
                userId
            ) { user ->
                binding.textInputLayout1.editText!!.text =
                    Editable.Factory.getInstance().newEditable(user!!.uname.toString())
                binding.textInputLayout2.editText!!.text =
                    Editable.Factory.getInstance().newEditable(user.uemail.toString())
                binding.textInputLayout4.editText!!.text =
                    Editable.Factory.getInstance().newEditable(user.umobile.toString())
            }


            binding.enableControls.setOnClickListener{
                ToggleControls()
            }

            binding.updateBtn.setOnClickListener {
                FirebaseAuth.getInstance().currentUser?.let { user ->
                    val newEmail = binding.textInputLayout3.editText?.text.toString()
                    val newPassword = "Dhimar@99"

                    user.email?.let { email ->
                        val credential = EmailAuthProvider.getCredential(email, newPassword)

                        user.reauthenticate(credential).addOnCompleteListener { reauthTask ->
                            if (reauthTask.isSuccessful) {
                                user.updateEmail(newEmail).addOnCompleteListener { emailUpdateTask ->
                                    if (emailUpdateTask.isSuccessful) {
                                        user.updatePassword(newPassword).addOnCompleteListener { passwordUpdateTask ->
                                            if (passwordUpdateTask.isSuccessful) {
                                                Log.d("TAG", "User email and password updated successfully.")
                                            } else {
                                                Log.d("TAG", "Failed to update password: ${passwordUpdateTask.exception?.message}")
                                            }
                                        }
                                    } else {
                                        Log.d("TAG", "Failed to update email address: ${emailUpdateTask.exception?.message}")
                                    }
                                }
                            } else {
                                Log.d("TAG", "Reauthentication failed: ${reauthTask.exception?.message}")
                            }
                        }
                    } ?: Log.d("TAG", "User's email is null.")
                } ?: Log.d("TAG", "User not logged in.")
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
         * @return A new instance of fragment CommonProfileFragment.
         */
        // TODO: Rename and change types and number of parameters
        @JvmStatic
        fun newInstance(param1: String, param2: String) =
            CommonProfileFragment().apply {
                arguments = Bundle().apply {
                    putString(ARG_PARAM1, param1)
                    putString(ARG_PARAM2, param2)
                }
            }
    }
}
package com.example.combined_loginregister

import android.os.Bundle
import android.os.Handler
import android.text.Editable
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.Toast
import com.example.combined_loginregister.databinding.FragmentCommonProfileBinding
import com.google.firebase.Firebase
import com.google.firebase.auth.EmailAuthProvider
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.auth
import com.google.firebase.database.FirebaseDatabase

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
    lateinit var auth: FirebaseAuth

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

    private fun ToggleControls() {
        if (binding.enableControls.text == "Disable Controls") {
            binding.enableControls.text = "Enable Controls"
            binding.textInputLayout1.isEnabled = false
            binding.textInputLayout2.isEnabled = false

            binding.textInputLayout4.isEnabled = false
        } else {
            binding.enableControls.text = "Disable Controls"
            binding.textInputLayout1.isEnabled = true
            binding.textInputLayout2.isEnabled = true

            binding.textInputLayout4.isEnabled = true
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        // Inflate the layout for this fragment
        binding = FragmentCommonProfileBinding.inflate(layoutInflater, container, false)

        init()
        ToggleControls()
        return binding.root
    }

    private fun fetchAndSetUserData() {
        auth = Firebase.auth
        val userData = auth.currentUser
        val userId = userData!!.uid

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
                    Editable.Factory.getInstance().newEditable(userData.email.toString())
                binding.textInputLayout4.editText!!.text =
                    Editable.Factory.getInstance().newEditable(user.umobile.toString())
            }

        }
    }

    private fun init() {

        fetchAndSetUserData()


        binding.enableControls.setOnClickListener {
            ToggleControls()
        }

        binding.updateBtn.setOnClickListener {
            val loadingDialogHelper = LoadingDialogHelper()
            loadingDialogHelper.showLoadingDialog(requireContext())

            val userData = auth.currentUser
            val userId = userData!!.uid

            val firebaseRestManager = FirebaseRestManager<UserTb>()
            firebaseRestManager.getSingleItem(
                UserTb::class.java,
                "moviedb/usertb",
                userId
            ) { user ->
                if (user!!.uname!= binding.textInputLayout1.editText!!.text.toString() ||
                    user.umobile!= binding.textInputLayout4.editText!!.text.toString() ||
                    userData.email!= binding.textInputLayout2.editText!!.text.toString()) {

                    if (userData.email!= binding.textInputLayout2.editText!!.text.toString()) {
                        loadingDialogHelper.dismissLoadingDialog()
                        val newEmail = binding.textInputLayout2.editText!!.text.toString()
                        //detected change in email

                        val warningLoadingHelper = WarningLoadingHelper()
                        warningLoadingHelper.showLoadingDialog(requireContext())
                        warningLoadingHelper.hideButtons()
                        warningLoadingHelper.updateText("Change in email detected!!\nNeed password to verify the change.\nA link will be sent to the new email to verify, the email will be only updated after that!!\nRest details will be updated directly!!")

                        val passwordConfirmationLoadingHelper = PasswordConfirmationLoadingHelper()

                        val handler = Handler()
                        handler.postDelayed({
                            warningLoadingHelper.dismissLoadingDialog()
                            passwordConfirmationLoadingHelper.showLoadingDialog(requireContext())
                            ToggleControls()
                            val view = passwordConfirmationLoadingHelper.getView()
                            val btn = view.findViewById<Button>(R.id.passwordconfirmbutton)
                            btn.setOnClickListener {
                                val password = passwordConfirmationLoadingHelper.getPassword()
                                val credential = EmailAuthProvider.getCredential(userData.email!!, password)

                                userData.reauthenticate(credential)
                                    .addOnCompleteListener { task ->
                                        if (task.isSuccessful) {
                                            userData.verifyBeforeUpdateEmail(newEmail)
                                                .addOnCompleteListener { task ->
                                                    if (task.isSuccessful) {
                                                        val database = FirebaseDatabase.getInstance()
                                                        val dbRef = database.getReference("moviedb/usertb")

                                                        val newData = UserTb(
                                                            userId,
                                                            binding.textInputLayout1.editText!!.text.toString(),
                                                            binding.textInputLayout4.editText!!.text.toString(),
                                                            user.utype
                                                        )

                                                        firebaseRestManager.updateItem(dbRef, userId, newData) { success, error ->
                                                            if (success) {
                                                                Log.d("TAG", "Data updated successfully!")
                                                                loadingDialogHelper.dismissLoadingDialog()
                                                                val successLoadingHelper = SuccessLoadingHelper()
                                                                successLoadingHelper.showLoadingDialog(requireContext())
                                                                successLoadingHelper.hideButtons()
                                                                successLoadingHelper.updateText("User Data has been Updated!!")

                                                                passwordConfirmationLoadingHelper.dismissLoadingDialog()

                                                                fetchAndSetUserData()

                                                                val handler = Handler()
                                                                handler.postDelayed({
                                                                    successLoadingHelper.dismissLoadingDialog()
                                                                    ToggleControls()
                                                                }, 2000)

                                                            } else {
                                                                Log.e("TAG", "Failed to update data: $error")
                                                                passwordConfirmationLoadingHelper.dismissLoadingDialog()

                                                            }
                                                        }
                                                    } else {
                                                        Log.e("TAG", "Error updating user email.", task.exception)
                                                        passwordConfirmationLoadingHelper.dismissLoadingDialog()

                                                    }
                                                }
                                        } else {
                                            Log.e("TAG", "Error re-authenticating user.", task.exception)
                                            passwordConfirmationLoadingHelper.dismissLoadingDialog()

                                        }
                                    }
                            }
                        }, 5000)

                    } else {
                        //if email is the same
                        val database = FirebaseDatabase.getInstance()
                        val dbRef = database.getReference("moviedb/usertb")

                        val newData = UserTb(
                            userId,
                            binding.textInputLayout1.editText!!.text.toString(),
                            binding.textInputLayout4.editText!!.text.toString(),
                            user.utype
                        )

                        firebaseRestManager.updateItem(dbRef, userId, newData) { success, error ->
                            if (success) {
                                Log.d("TAG", "Data updated successfully!")
                                loadingDialogHelper.dismissLoadingDialog()
                                val successLoadingHelper = SuccessLoadingHelper()
                                successLoadingHelper.showLoadingDialog(requireContext())
                                successLoadingHelper.hideButtons()
                                successLoadingHelper.updateText("User Data has been Updated!!")

                                fetchAndSetUserData()

                                val handler = Handler()
                                handler.postDelayed({
                                    successLoadingHelper.dismissLoadingDialog()
                                    ToggleControls()
                                }, 2000)

                            } else {
                                Log.e("TAG", "Failed to update data: $error")
                            }
                        }
                    }
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
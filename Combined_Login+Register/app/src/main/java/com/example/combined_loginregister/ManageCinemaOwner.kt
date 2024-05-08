package com.example.applicaitionowner

import android.app.AlertDialog
import android.os.Bundle
import android.os.Handler
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.Toast
import androidx.core.view.isVisible
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.combined_loginregister.Encryption
import com.example.combined_loginregister.FirebaseRestManager
import com.example.combined_loginregister.ListAllUserAdapter
import com.example.combined_loginregister.LoadingDialogHelper
import com.example.combined_loginregister.MoviePosterTb
import com.example.combined_loginregister.R
import com.example.combined_loginregister.SuccessLoadingHelper
import com.example.combined_loginregister.UserTb

import com.example.combined_loginregister.databinding.FragmentManageCinemaownerBinding
import com.google.android.material.textfield.TextInputLayout
import com.google.firebase.FirebaseApp
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.FirebaseDatabase

// TODO: Rename parameter arguments, choose names that match
// the fragment initialization parameters, e.g. ARG_ITEM_NUMBER
private const val ARG_PARAM1 = "param1"
private const val ARG_PARAM2 = "param2"

/**
 * A simple [Fragment] subclass.
 * Use the [ManageCinemaOwner.newInstance] factory method to
 * create an instance of this fragment.
 */
class ManageCinemaOwner : Fragment() {
    // TODO: Rename and change types of parameters
    private var param1: String? = null
    private var param2: String? = null
    lateinit var binding: FragmentManageCinemaownerBinding
    lateinit var dialogView:View
    lateinit var alertDialog:AlertDialog


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
        // Inflate the layout for this fragment
        binding = FragmentManageCinemaownerBinding.inflate(layoutInflater,container,false)

        displayCinemaOwner()

        // Set onClickListener for the register button
        binding.registerOwnerButton.setOnClickListener {
            // Inflate the custom layout
            dialogView = layoutInflater.inflate(R.layout.owner_registeration_dialog, null)

            // Create the dialog
            val dialogBuilder = AlertDialog.Builder(requireContext())
            dialogBuilder.setView(dialogView)

            // Set custom window animations
            alertDialog = dialogBuilder.create()
            alertDialog.window?.attributes?.windowAnimations = R.style.DialogAnimation

            val btn = dialogView.findViewById<Button>(R.id.loginbtn3)
            btn.setOnClickListener {

                if(validateRegisterPage()){
                    registerUser()
                }
            }


            // Show the dialog
            alertDialog.show()
        }


        return binding.root

    }

    private fun registerUser() {
        val firebaseAuth = FirebaseAuth.getInstance()
        val firebaseDatabase = FirebaseDatabase.getInstance()

        // Validating user input
        val textInputLayout1 = dialogView.findViewById<TextInputLayout>(R.id.textInputLayout1)
        val textInputLayout2 = dialogView.findViewById<TextInputLayout>(R.id.textInputLayout2)
        val textInputLayout3 = dialogView.findViewById<TextInputLayout>(R.id.textInputLayout3)
        val textInputLayout4 = dialogView.findViewById<TextInputLayout>(R.id.textInputLayout4)

        val username = textInputLayout1.editText!!.text
        val email = textInputLayout2.editText!!.text
        val password = textInputLayout3.editText!!.text
        val mobile = textInputLayout4.editText!!.text

        if (username.isBlank() || email.isBlank() || password.isBlank() || mobile.isBlank()) {
            Toast.makeText(requireContext(), "Empty fields!", Toast.LENGTH_SHORT).show()
            return
        }

        val loadingDialogHelper = LoadingDialogHelper()
        loadingDialogHelper.showLoadingDialog(requireContext())



        // Check if the email is already registered
        firebaseAuth.fetchSignInMethodsForEmail(email.toString())
            .addOnCompleteListener { fetchTask ->
                if (fetchTask.isSuccessful) {
                    val signInMethods = fetchTask.result?.signInMethods
                    if (signInMethods != null && signInMethods.isNotEmpty()) {
                        // Email is already registered, handle accordingly
                        loadingDialogHelper.dismissLoadingDialog()
                        Log.e("TAG", "Email already registered")
                        Toast.makeText(requireContext(), "Email already registered", Toast.LENGTH_SHORT).show()
                    } else {
                        // Email is not registered, proceed with user registration
                        // Perform user registration with Firebase Authentication
                        firebaseAuth.createUserWithEmailAndPassword(email.toString(),
                            password.toString()
                        )
                            .addOnCompleteListener { authTask ->
                                if (authTask.isSuccessful) {
                                    // Use the user ID from Firebase Authentication as the key for database storage
                                    val userId = firebaseAuth.currentUser!!.uid

                                    // Create a UserTb object with the obtained user ID
                                    val tempUser = UserTb(userId, username.toString(),mobile.toString(), "cinemaowner")
                                    val dbRef = FirebaseDatabase.getInstance().getReference("moviedb/usertb")
                                    val firebaseRestManager = FirebaseRestManager<UserTb>()

                                    firebaseRestManager.addItemWithCustomId(tempUser, userId, dbRef) { success, error ->

                                        if (success) {
                                            firebaseAuth.currentUser!!.sendEmailVerification()
                                            loadingDialogHelper.dismissLoadingDialog()
                                            val successLoadingHelper = SuccessLoadingHelper()
                                            successLoadingHelper.showLoadingDialog(requireContext())
                                            successLoadingHelper.hideButtons()
                                            successLoadingHelper.updateText("User bas been registered!!")


                                            val handler = Handler()
                                            handler.postDelayed({
                                                successLoadingHelper.dismissLoadingDialog()
                                                displayCinemaOwner()
                                                alertDialog.dismiss()
                                                val encryption = Encryption(requireContext())
                                                firebaseAuth.signInWithEmailAndPassword(encryption.decrypt("userEmail"),encryption.decrypt("userPassword"))
                                            }, 2000)
                                        } else {
                                            // Handle failure to add user data to the database
                                            loadingDialogHelper.dismissLoadingDialog()
                                            Log.e("TAG", "Error adding user data to the database: $error")
                                            // Display error message or take appropriate action
                                        }
                                    }
                                } else {
                                    // Registration failed
                                    loadingDialogHelper.dismissLoadingDialog()
                                    Log.e("TAG", "User registration failed: ${authTask.exception}")
                                    Toast.makeText(requireContext(), "User registration failed", Toast.LENGTH_SHORT).show()
                                }
                            }
                    }
                } else {
                    // Error fetching sign-in methods
                    loadingDialogHelper.dismissLoadingDialog()
                    Log.e("TAG", "Error fetching sign-in methods: ${fetchTask.exception}")
                    Toast.makeText(requireContext(), "Error fetching sign-in methods", Toast.LENGTH_SHORT).show()
                }
            }
    }

    private fun validateRegisterPage(): Boolean {
        // Validation for name
        val txtName = dialogView.findViewById<TextInputLayout>(R.id.textInputLayout1)
        val txtEmail = dialogView.findViewById<TextInputLayout>(R.id.textInputLayout2)
        val txtPass = dialogView.findViewById<TextInputLayout>(R.id.textInputLayout3)
        val txtMobile = dialogView.findViewById<TextInputLayout>(R.id.textInputLayout4)

        if (txtName.editText!!.text.isEmpty()) {
            txtName.error = "Name cannot be empty"
        } else if (!txtName.editText!!.text.matches("[a-zA-Z ]+".toRegex())) {
            txtName.error = "Invalid characters in name"
        } else {
            txtName.error = null
        }

        // Validation for Email
        if (txtEmail.editText!!.text.toString().trim().isEmpty()) {
            txtEmail.error = "Email cannot be empty"
        } else if (!android.util.Patterns.EMAIL_ADDRESS.matcher(txtEmail.editText!!.text.toString().trim()).matches()) {
            txtEmail.error = "Invalid email format"
        } else {
            txtEmail.error = null
        }

        // Validation for Password
        if (txtPass.editText!!.text.toString().trim().isEmpty()) {
            txtPass.error = "Password cannot be empty"
        } else if (txtPass.editText!!.text.toString().trim().length < 8) {
            txtPass.error = "Password must be at least 8 characters long"
        } else if (!txtPass.editText!!.text.toString().trim().matches("[a-zA-Z0-9@#$%^&+=]+".toRegex())) {
            txtPass.error = "Password must contain letters, numbers, and special characters"
        } else {
            txtPass.error = null
        }

        // Validation for Mobile
        if (txtMobile.editText!!.text.isEmpty()) {
            txtMobile.error = "Mobile number cannot be empty"
        } else if (!txtMobile.editText!!.text.matches("[6-9]\\d{9}".toRegex())) {
            txtMobile.error = "Invalid mobile number"
        } else {
            txtMobile.error = null
        }

        // Check if there are no errors in any field
        return txtName.error == null && txtEmail.error == null && txtPass.error == null && txtMobile.error == null
    }

    private fun displayCinemaOwner(){
        val loadingScreen = LoadingDialogHelper()
        loadingScreen.showLoadingDialog(requireContext())

        //get data of all the move in movietb
        val userClass = UserTb::class.java
        val node = "moviedb/usertb"

        val firebaseRestManager = FirebaseRestManager<MoviePosterTb>()
        firebaseRestManager.getAllItems(userClass, node) { items ->
            requireActivity().runOnUiThread {
                loadingScreen.dismissLoadingDialog()
                val userList = ArrayList<UserTb>()

                if (items.isNotEmpty()) {

                    for(item in items){
                        if(item.utype=="cinemaowner") {
                            userList.add(item)
                        }
                    }

                    val adapter = ListAllUserAdapter(userList)
                    Log.d("TAG", "displayCinemaOwner: $userList")
                    adapter.setOnItemClickListener(object : ListAllUserAdapter.OnItemClickListener {
                        override fun onItemClick(user: UserTb) {
                            Log.d("TAG", "onItemClick:asdkhasdkashkjdh ")
                        }
                    })


                    binding.CardsHere.adapter = adapter
                    binding.CardsHere.layoutManager = LinearLayoutManager(requireContext())
                } else {
                    Toast.makeText(requireContext(), "No movies found", Toast.LENGTH_SHORT).show()
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
         * @return A new instance of fragment ManageScreenFragment.
         */
        // TODO: Rename and change types and number of parameters
        @JvmStatic
        fun newInstance(param1: String, param2: String) =
            ManageCinemaOwner().apply {
                arguments = Bundle().apply {
                    putString(ARG_PARAM1, param1)
                    putString(ARG_PARAM2, param2)
                }
            }
    }
}
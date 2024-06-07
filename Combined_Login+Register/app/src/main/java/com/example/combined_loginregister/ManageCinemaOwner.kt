package com.example.applicaitionowner

import android.app.AlertDialog
import android.os.Bundle
import android.os.Handler
import android.text.Editable
import android.text.TextWatcher
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.Toast
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.combined_loginregister.*
import com.example.combined_loginregister.databinding.FragmentManageCinemaownerBinding
import com.google.android.material.textfield.TextInputLayout
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.FirebaseDatabase

private const val ARG_PARAM1 = "param1"
private const val ARG_PARAM2 = "param2"

class ManageCinemaOwner : Fragment() {
    private var param1: String? = null
    private var param2: String? = null
    lateinit var binding: FragmentManageCinemaownerBinding
    lateinit var dialogView: View
    lateinit var alertDialog: AlertDialog
    private var cinemaOwnerList = ArrayList<UserTb>()
    private lateinit var adapter: ListAllUserAdapter

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
        binding = FragmentManageCinemaownerBinding.inflate(layoutInflater, container, false)

        displayCinemaOwner()

        binding.registerOwnerButton.setOnClickListener {
            dialogView = layoutInflater.inflate(R.layout.owner_registeration_dialog, null)
            val dialogBuilder = AlertDialog.Builder(requireContext())
            dialogBuilder.setView(dialogView)
            alertDialog = dialogBuilder.create()
            alertDialog.window?.attributes?.windowAnimations = R.style.DialogAnimation

            val btn = dialogView.findViewById<Button>(R.id.loginbtn3)
            btn.setOnClickListener {
                if (validateRegisterPage()) {
                    registerUser()
                }
            }

            alertDialog.show()
        }

        // Add TextWatcher to the search bar
        binding.searchBar.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
                filterCinemaOwners(s.toString())
            }
            override fun afterTextChanged(s: Editable?) {}
        })

        return binding.root
    }

    private fun filterCinemaOwners(query: String) {
        val filteredList = cinemaOwnerList.filter { owner ->
            owner.uname!!.contains(query, ignoreCase = true)
        }
        adapter.updateList(filteredList)
    }

    private fun registerUser() {
        val firebaseAuth = FirebaseAuth.getInstance()
        val firebaseDatabase = FirebaseDatabase.getInstance()

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

        firebaseAuth.fetchSignInMethodsForEmail(email.toString())
            .addOnCompleteListener { fetchTask ->
                if (fetchTask.isSuccessful) {
                    val signInMethods = fetchTask.result?.signInMethods
                    if (signInMethods != null && signInMethods.isNotEmpty()) {
                        loadingDialogHelper.dismissLoadingDialog()
                        Log.e("TAG", "Email already registered")
                        Toast.makeText(requireContext(), "Email already registered", Toast.LENGTH_SHORT).show()
                    } else {
                        firebaseAuth.createUserWithEmailAndPassword(email.toString(), password.toString())
                            .addOnCompleteListener { authTask ->
                                if (authTask.isSuccessful) {
                                    val userId = firebaseAuth.currentUser!!.uid
                                    val tempUser = UserTb(userId, username.toString(), mobile.toString(), "cinemaowner")
                                    val dbRef = FirebaseDatabase.getInstance().getReference("moviedb/usertb")
                                    val firebaseRestManager = FirebaseRestManager<UserTb>()

                                    firebaseRestManager.addItemWithCustomId(tempUser, userId, dbRef) { success, error ->
                                        if (success) {
                                            firebaseAuth.currentUser!!.sendEmailVerification()
                                            loadingDialogHelper.dismissLoadingDialog()
                                            val successLoadingHelper = SuccessLoadingHelper()
                                            successLoadingHelper.showLoadingDialog(requireContext())
                                            successLoadingHelper.hideButtons()
                                            successLoadingHelper.updateText("User has been registered!!")

                                            Handler().postDelayed({
                                                successLoadingHelper.dismissLoadingDialog()
                                                displayCinemaOwner()
                                                alertDialog.dismiss()
                                                val encryption = Encryption(requireContext())
                                                firebaseAuth.signInWithEmailAndPassword(encryption.decrypt("userEmail"), encryption.decrypt("userPassword"))
                                            }, 2000)
                                        } else {
                                            loadingDialogHelper.dismissLoadingDialog()
                                            Log.e("TAG", "Error adding user data to the database: $error")
                                        }
                                    }
                                } else {
                                    loadingDialogHelper.dismissLoadingDialog()
                                    Log.e("TAG", "User registration failed: ${authTask.exception}")
                                    Toast.makeText(requireContext(), "User registration failed", Toast.LENGTH_SHORT).show()
                                }
                            }
                    }
                } else {
                    loadingDialogHelper.dismissLoadingDialog()
                    Log.e("TAG", "Error fetching sign-in methods: ${fetchTask.exception}")
                    Toast.makeText(requireContext(), "Error fetching sign-in methods", Toast.LENGTH_SHORT).show()
                }
            }
    }

    private fun validateRegisterPage(): Boolean {
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

        if (txtEmail.editText!!.text.toString().trim().isEmpty()) {
            txtEmail.error = "Email cannot be empty"
        } else if (!android.util.Patterns.EMAIL_ADDRESS.matcher(txtEmail.editText!!.text.toString().trim()).matches()) {
            txtEmail.error = "Invalid email format"
        } else {
            txtEmail.error = null
        }

        if (txtPass.editText!!.text.toString().trim().isEmpty()) {
            txtPass.error = "Password cannot be empty"
        } else if (txtPass.editText!!.text.toString().trim().length < 8) {
            txtPass.error = "Password must be at least 8 characters long"
        } else if (!txtPass.editText!!.text.toString().trim().matches("[a-zA-Z0-9@#$%^&+=]+".toRegex())) {
            txtPass.error = "Password must contain letters, numbers, and special characters"
        } else {
            txtPass.error = null
        }

        if (txtMobile.editText!!.text.isEmpty()) {
            txtMobile.error = "Mobile number cannot be empty"
        } else if (!txtMobile.editText!!.text.matches("[6-9]\\d{9}".toRegex())) {
            txtMobile.error = "Invalid mobile number"
        } else {
            txtMobile.error = null
        }

        return txtName.error == null && txtEmail.error == null && txtPass.error == null && txtMobile.error == null
    }

    private fun displayCinemaOwner() {
        val loadingScreen = LoadingDialogHelper()
        loadingScreen.showLoadingDialog(requireContext())

        val userClass = UserTb::class.java
        val node = "moviedb/usertb"

        val firebaseRestManager = FirebaseRestManager<MoviePosterTb>()
        firebaseRestManager.getAllItems(userClass, node) { items ->
            requireActivity().runOnUiThread {
                loadingScreen.dismissLoadingDialog()
                cinemaOwnerList.clear()

                if (items.isNotEmpty()) {
                    for (item in items) {
                        if (item.utype == "cinemaowner") {
                            cinemaOwnerList.add(item)
                        }
                    }

                    adapter = ListAllUserAdapter(cinemaOwnerList)
                    Log.d("TAG", "displayCinemaOwner: $cinemaOwnerList")
                    adapter.setOnItemClickListener(object : ListAllUserAdapter.OnItemClickListener {
                        override fun onItemClick(user: UserTb) {
                            Log.d("TAG", "onItemClick: User clicked")
                        }
                    })

                    binding.CardsHere.adapter = adapter
                    binding.CardsHere.layoutManager = LinearLayoutManager(requireContext())
                } else {
                    Toast.makeText(requireContext(), "No cinema owners found", Toast.LENGTH_SHORT).show()
                }
            }
        }
    }

    companion object {
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

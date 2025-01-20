package com.example.combined_loginregister

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
import android.widget.EditText
import android.widget.Toast
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.combined_loginregister.databinding.FragmentCinemaOwnerManageCInemaAdminBinding
import com.google.android.material.textfield.TextInputLayout
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener

// TODO: Rename parameter arguments, choose names that match
// the fragment initialization parameters, e.g. ARG_ITEM_NUMBER
private const val ARG_PARAM1 = "param1"
private const val ARG_PARAM2 = "param2"

/**
 * A simple [Fragment] subclass.
 * Use the [CinemaOwnerManageCInemaAdmin.newInstance] factory method to
 * create an instance of this fragment.
 */
class CinemaOwnerManageCInemaAdmin : Fragment() {
    // TODO: Rename and change types of parameters
    private var param1: String? = null
    private var param2: String? = null
    lateinit var binding: FragmentCinemaOwnerManageCInemaAdminBinding
    lateinit var dialogView: View
    lateinit var alertDialog: AlertDialog
    private lateinit var cinemaList: List<CinemaTb>
    private lateinit var allCinemaAdmins: List<CinemaAdminTb> // To keep the original list

    private lateinit var adapter: CinemaOwnerCinemaListAdapter

    lateinit var cinemaAdminAdapter: CinemaAdminDisplayAdpater


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
        binding = FragmentCinemaOwnerManageCInemaAdminBinding.inflate(
            layoutInflater,
            container,
            false
        )


        binding.btnOpenAddCinemaAdminDialog.setOnClickListener {
            // Inflate the custom layout
            // Inflate the custom layout
            dialogView = layoutInflater.inflate(R.layout.displaycinemadialog, null)

            // Create the dialog
            val dialogBuilder = AlertDialog.Builder(requireContext())
            dialogBuilder.setView(dialogView)

            // Show the dialog
            alertDialog = dialogBuilder.create()
            alertDialog.show()

            // Initialize the recyclerView object
            val recyclerView = dialogView.findViewById<RecyclerView>(R.id.CinemaCardsHere)

            val dialogSearchBar = dialogView.findViewById<EditText>(R.id.search_bar)
            dialogSearchBar.addTextChangedListener(object : TextWatcher {
                override fun afterTextChanged(s: Editable?) {
                    filterDialogCinema(s.toString())
                }

                override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}

                override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {}
            })


            // Set custom window animations
            alertDialog.window?.attributes?.windowAnimations = R.style.DialogAnimation

            // Display the movies
            getOwnedCinemaList(recyclerView)

        }

        getCinemaAdminsForCurrentOwner(binding.cinemaAdminHereForCO)

        // Add TextWatcher for search functionality
        binding.searchBar.addTextChangedListener(object : TextWatcher {
            override fun afterTextChanged(s: Editable?) {
                filterCinemaAdmins(s.toString())
            }

            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}

            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {}
        })



        return binding.root
    }






    fun setUpRealTimeDatabase(){
        val firebaseDatabase = FirebaseDatabase.getInstance().getReference("moviedb/cinemaadmintb")
        firebaseDatabase.addValueEventListener(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                getCinemaAdminsForCurrentOwner(binding.cinemaAdminHereForCO)

            }

            override fun onCancelled(error: DatabaseError) {

            }


        })
    }

    private fun filterDialogCinema(query: String) {
             cinemaList = ArrayList<CinemaTb>() // Initialize cinemaList here

            val filteredList = cinemaList.filter { cinema ->
                cinema.cinemaName!!.contains(query, ignoreCase = true) || cinema.cinemaID!!.contains(query, ignoreCase = true)
            }
            adapter.updateList(filteredList)

    }
    private fun getCinemaAdminsForCurrentOwner(cinemaAdminHereForCO: RecyclerView) {
        val loadingDialogHelper = LoadingDialogHelper()
        loadingDialogHelper.showLoadingDialog(requireContext())
        val firebaseRestManager = FirebaseRestManager<CinemaOwnerTb>()
        val currentUserUid = FirebaseAuth.getInstance().currentUser?.uid

        firebaseRestManager.getAllItems(CinemaOwnerTb::class.java, "moviedb/CinemaOwnerTb") { cinemaOwnerItems ->
            val currentOwner:List<CinemaOwnerTb> = cinemaOwnerItems.filter { it.uid == currentUserUid }
            if (currentOwner == null) {
                loadingDialogHelper.dismissLoadingDialog()
                return@getAllItems
            }

            firebaseRestManager.getAllItems(CinemaAdminTb::class.java, "moviedb/cinemaadmintb") { cinemaAdminItems ->

                val cinemaAdminsForCurrentOwner  = cinemaAdminItems.filter { cinemaAdminItem -> currentOwner.any { it.cinemaOwnerId == cinemaAdminItem.cinemaOwnerId } }
                loadingDialogHelper.dismissLoadingDialog()

                if (cinemaAdminsForCurrentOwner.isEmpty()) {
                    return@getAllItems
                }

                Log.d("TAG", "getCinemaAdminsForCurrentOwner: ${cinemaAdminsForCurrentOwner.size}")
                cinemaAdminAdapter = CinemaAdminDisplayAdpater(cinemaAdminsForCurrentOwner.toMutableList())
                allCinemaAdmins = cinemaAdminsForCurrentOwner // Store the original list
                cinemaAdminHereForCO.adapter = cinemaAdminAdapter
                cinemaAdminHereForCO.layoutManager = LinearLayoutManager(requireContext())
            }
        }
    }


    private fun filterCinemaAdmins(query: String) {


        getAllUsers { users ->
            val filteredUsers = users.filter { it.uname!!.contains(query, ignoreCase = true) }
            val filteredUserIds = filteredUsers.map { it.uid }

            val filteredList = if (query.isEmpty()) {
                allCinemaAdmins // Reset to original list when query is empty

            } else {
                allCinemaAdmins.filter {
                    it.userId in filteredUserIds || it.cinemaadminid!!.contains(query, ignoreCase = true)
                }
            }

            cinemaAdminAdapter.updateList(filteredList)
        }
    }


    private fun getAllUsers(onComplete: (List<UserTb>) -> Unit) {
        val firebaseRestManager = FirebaseRestManager<UserTb>()
        firebaseRestManager.getAllItems(UserTb::class.java, "moviedb/usertb") { users ->
            onComplete(users)
        }
    }

    private fun getOwnedCinemaList(cinemaAdminHereForCO: RecyclerView) {
        val firebaseRestManager = FirebaseRestManager<CinemaOwnerTb>()
        val uid = FirebaseAuth.getInstance().currentUser!!.uid
        cinemaList = ArrayList<CinemaTb>()
        adapter = CinemaOwnerCinemaListAdapter(cinemaList)

        adapter.setOnItemClickListener(object : CinemaOwnerCinemaListAdapter.OnItemClickListener {
            override fun onItemClick(cinema: CinemaTb) {
                val yesOrNoLoadingHelper = YesOrNoLoadingHelper()
                yesOrNoLoadingHelper.showLoadingDialog(requireContext())
                yesOrNoLoadingHelper.hideCheckBox()
                yesOrNoLoadingHelper.updateText("Are you sure you want to add Cinema Admin for \nCinemaId : ${cinema.cinemaID}\nCinema Name : ${cinema.cinemaName}")

                val view = yesOrNoLoadingHelper.getView()

                val yesBtn = view.findViewById<Button>(R.id.btn_yes)
                yesBtn.setOnClickListener {
                    yesOrNoLoadingHelper.dismissLoadingDialog()
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
                            registerUser(cinema)
                        }
                    }


                    alertDialog.show()
                }

                val noBtn = view.findViewById<Button>(R.id.btn_no)
                noBtn.setOnClickListener {
                    yesOrNoLoadingHelper.dismissLoadingDialog()
                }
            }
        })

        cinemaAdminHereForCO.adapter = adapter
        cinemaAdminHereForCO.layoutManager = LinearLayoutManager(requireContext())

        firebaseRestManager.getAllItems(CinemaOwnerTb::class.java, "moviedb/CinemaOwnerTb") { items ->
            if (items.isNotEmpty()) {
                for (item in items) {
                    if (item.uid == uid) {
                        val cinemaId = item.cinemaId!!
                        firebaseRestManager.getSingleItem(CinemaTb::class.java, "moviedb/cinematb", cinemaId) { tempCinemaItem ->
                            tempCinemaItem?.let { (cinemaList as ArrayList<CinemaTb>).add(it) }
                            adapter.notifyDataSetChanged()
                            Log.d("TAG", "getOwnedCinemaList: works!!")
                        }
                    }
                }
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


    private fun registerUser(cinema: CinemaTb) {
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

        // Save current user's email and password before registering the new user
        val currentUser = firebaseAuth.currentUser
        val encryption = Encryption(requireContext())
        val currentEmail = encryption.decrypt("userEmail")
        val currentPassword = encryption.decrypt("userPassword")

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
                        firebaseAuth.createUserWithEmailAndPassword(email.toString(), password.toString())
                            .addOnCompleteListener { authTask ->
                                if (authTask.isSuccessful) {
                                    // Use the user ID from Firebase Authentication as the key for database storage
                                    val newUserId = firebaseAuth.currentUser!!.uid

                                    // Create a UserTb object with the obtained user ID
                                    val tempUser = UserTb(newUserId, username.toString(), mobile.toString(), "cinemaadmin")
                                    val dbRef = firebaseDatabase.getReference("moviedb/usertb")
                                    val firebaseRestManager = FirebaseRestManager<UserTb>()

                                    firebaseRestManager.addItemWithCustomId(tempUser, newUserId, dbRef) { success, error ->
                                        if (success) {
                                            val firebaseRestManager3 = FirebaseRestManager<CredentialsTb>()
                                            val tempCredentials = CredentialsTb(newUserId,
                                                email.toString(), password.toString() )

                                            firebaseRestManager3.addItemWithCustomId(tempCredentials,newUserId,FirebaseDatabase.getInstance().getReference("moviedb/credentialstb")){success,error->{

                                            }}

                                            firebaseAuth.currentUser!!.sendEmailVerification()

                                            // Re-authenticate with the original user's credentials
                                            firebaseAuth.signInWithEmailAndPassword(currentEmail, currentPassword)
                                                .addOnCompleteListener { signInTask ->
                                                    if (signInTask.isSuccessful) {
                                                        // Original user signed in successfully
                                                        val firebaseRestManager2 = FirebaseRestManager<CinemaAdminTb>()
                                                        val dbRef2 = firebaseDatabase.getReference("moviedb/cinemaadmintb")
                                                        val id = dbRef2.push().key

                                                        firebaseRestManager2.getAllItems(CinemaOwnerTb::class.java, "moviedb/CinemaOwnerTb") { cinemaOwnerItems ->
                                                            for (tempItem in cinemaOwnerItems) {
                                                                if (cinema.cinemaID == tempItem.cinemaId && currentUser!!.uid == tempItem.uid) {
                                                                    val tempData = CinemaAdminTb(id, newUserId, tempItem.cinemaOwnerId)
                                                                    firebaseRestManager2.addItemWithCustomId(tempData, id!!, dbRef2) { success2, error2 ->
                                                                        if (success2) {
                                                                            loadingDialogHelper.dismissLoadingDialog()
                                                                            val successLoadingHelper = SuccessLoadingHelper()
                                                                            successLoadingHelper.showLoadingDialog(requireContext())
                                                                            successLoadingHelper.hideButtons()
                                                                            successLoadingHelper.updateText("Cinema Admin has been registered!!\nAlso, a verification mail has been sent!!")

                                                                            getCinemaAdminsForCurrentOwner(binding.cinemaAdminHereForCO)

                                                                            val handler = Handler()
                                                                            handler.postDelayed({
                                                                                successLoadingHelper.dismissLoadingDialog()
                                                                                alertDialog.dismiss()
                                                                            }, 2000)
                                                                        } else {
                                                                            loadingDialogHelper.dismissLoadingDialog()
                                                                            Log.e("TAG", "Error adding user data to the database: $error2")
                                                                            Toast.makeText(requireContext(), "Error adding user data to the database", Toast.LENGTH_SHORT).show()
                                                                        }
                                                                    }
                                                                    break
                                                                }
                                                            }
                                                        }
                                                    } else {
                                                        loadingDialogHelper.dismissLoadingDialog()
                                                        Log.e("TAG", "Error signing in with original user credentials: ${signInTask.exception}")
                                                        Toast.makeText(requireContext(), "Error signing in with original user credentials", Toast.LENGTH_SHORT).show()
                                                    }
                                                }
                                        } else {
                                            loadingDialogHelper.dismissLoadingDialog()
                                            Log.e("TAG", "Error adding user data to the database: $error")
                                            Toast.makeText(requireContext(), "Error adding user data to the database", Toast.LENGTH_SHORT).show()
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



    companion object {
        /**
         * Use this factory method to create a new instance of
         * this fragment using the provided parameters.
         *
         * @param param1 Parameter 1.
         * @param param2 Parameter 2.
         * @return A new instance of fragment CinemaOwnerManageCInemaAdmin.
         */
        // TODO: Rename and change types and number of parameters
        @JvmStatic
        fun newInstance(param1: String, param2: String) =
            CinemaOwnerManageCInemaAdmin().apply {
                arguments = Bundle().apply {
                    putString(ARG_PARAM1, param1)
                    putString(ARG_PARAM2, param2)
                }
            }
    }
}
package com.example.myapplication

import android.R
import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.example.myapplication.databinding.ActivityLoginBinding
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.android.gms.auth.api.signin.GoogleSignInAccount
import com.google.android.gms.auth.api.signin.GoogleSignInClient
import com.google.android.gms.auth.api.signin.GoogleSignInOptions
import com.google.android.gms.common.api.ApiException
import com.google.android.gms.tasks.Task
import com.google.firebase.FirebaseApp
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.GoogleAuthProvider
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener


class LoginActivity : AppCompatActivity() {
    lateinit var binding: ActivityLoginBinding
    private lateinit var DbRef:DatabaseReference
    private lateinit var loadingDialog: LoadingDialog

    lateinit var mGoogleSignInClient: GoogleSignInClient
    val Req_Code: Int = 123
    private lateinit var firebaseAuth: FirebaseAuth
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityLoginBinding.inflate(layoutInflater)
        setContentView(binding.root)

        DbRef = FirebaseDatabase.getInstance().getReference("moviedb/usertb")
        loadingDialog = LoadingDialog()
        loadingDialog.dialog(this)


        FirebaseApp.initializeApp(this)

        val gso = GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
            .requestIdToken("390229249723-kgf51fevhonod7sf18vnd5ga6tnna0ed.apps.googleusercontent.com")
            .requestEmail()
            .build()

        mGoogleSignInClient = GoogleSignIn.getClient(this, gso)
        firebaseAuth = FirebaseAuth.getInstance()




    }

    fun OpenRegister(view: View) {
        val intent = Intent(this,RegisterActivity::class.java)
        startActivity(intent)
        finish()
    }

    fun loginUser(view: View) {
        loadingDialog.startLoadingdialog()

        //Validating for empty fields!!
        if(binding.txtemail.text.isEmpty() || binding.txtpass.text.isEmpty()) {
            loadingDialog.InvalidFieldIconShow()
            loadingDialog.startClosingWithDelay(2000)
            return // Exit the function to prevent further execution
        }

        DbRef.addListenerForSingleValueEvent(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                //checking if data exists
                if (snapshot.exists()) {
                    //traverse data
                    var loginSuccess = false
                    for (data in snapshot.children) {
                        val temp = data.getValue(UserTb::class.java)
                        if (temp != null) {
                            if (temp.uemail == binding.txtemail.text.toString() ||
                                temp.umobile == binding.txtemail.text.toString() ||
                                temp.uname == binding.txtemail.text.toString()) {
                                if (temp.upassword == binding.txtpass.text.toString()) {
                                    Log.d("TAG", "onDataChange:${temp.uid} ")
                                    loadingDialog.successIconShow()
                                    loginSuccess = true
                                    break // Exit the loop once login is successful
                                }
                            }
                        }
                    }

                    if (!loginSuccess) {
                        loadingDialog.failIconShow()
                    }
                    // Close the dialog after 2 seconds regardless of login success or failure
                    loadingDialog.startClosingWithDelay(2000)
                }
            }

            override fun onCancelled(error: DatabaseError) {
                Log.d("Login Error", "Error in Firebase query: ${error.message}")
            }
        })
    }

    fun LoginWithGoogle(view: View) {
        Toast.makeText(this, "Logging In", Toast.LENGTH_SHORT).show()
        signInGoogle()
    }

    private fun signInGoogle() {
        val signInIntent: Intent = mGoogleSignInClient.signInIntent
        startActivityForResult(signInIntent, Req_Code)
    }

    // onActivityResult() function : this is where
    // we provide the task and data for the Google Account
    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == Req_Code) {
            val task: Task<GoogleSignInAccount> = GoogleSignIn.getSignedInAccountFromIntent(data)
            handleResult(task)
        }
    }

    private fun handleResult(completedTask: Task<GoogleSignInAccount>) {
        try {
            val account: GoogleSignInAccount? = completedTask.getResult(ApiException::class.java)
            if (account != null) {
                UpdateUI(account)
            }
        } catch (e: ApiException) {
            Toast.makeText(this, e.toString(), Toast.LENGTH_SHORT).show()
        }
    }

    // this is where we update the UI after Google signin takes place
    private fun UpdateUI(account: GoogleSignInAccount) {
        val email = account.email

        Toast.makeText(this,email,Toast.LENGTH_SHORT).show()

        if (email != null) {
            // Check if the user exists in the Firebase database
            DbRef.orderByChild("uemail").equalTo(email).addListenerForSingleValueEvent(object : ValueEventListener {
                override fun onDataChange(snapshot: DataSnapshot) {
                    if (snapshot.exists()) {

                        Toast.makeText(this@LoginActivity, "HO Gya MKC Finally ", Toast.LENGTH_SHORT).show()



                        // User exists in the database, sign them in using Firebase authentication
//                        firebaseAuth.signInWithEmailAndPassword(email, "b")
//                            .addOnCompleteListener { signInTask ->
//                                if (signInTask.isSuccessful) {
//                                    // Successfully signed in
//                                    val user = firebaseAuth.currentUser
//                                    if (user != null) {
//                                        // Perform actions after successful sign-in
//                                        // For example, navigate to another activity
//                                        Toast.makeText(this@LoginActivity, "HO Gya MKC Finally pr naya hau!!", Toast.LENGTH_SHORT).show()
//                                    }
//                                } else {
//                                    // Sign-in failed
//                                    Toast.makeText(this@LoginActivity, "Sign-in failed: ${signInTask.exception?.message} Ye locha hai re baba", Toast.LENGTH_SHORT).show()
//                                }
//                            }
//                    } else {
//                        Toast.makeText(this@LoginActivity, "Glt account se try kr raha haix`", Toast.LENGTH_SHORT).show()
////                        // User does not exist in the database, create a new entry
////                        val newUser = UserTb(
////                            uid = "", // Generate a unique user ID or use Firebase's push() method
////                            uemail = email,
////                            // Set other user details if needed
////                        )
////                        DbRef.push().setValue(newUser)
////                            .addOnCompleteListener { createTask ->
////                                if (createTask.isSuccessful) {
////                                    // User entry created successfully, sign them in using Firebase authentication
////                                    firebaseAuth.signInWithEmailAndPassword(email, "dummyPassword")
////                                        .addOnCompleteListener { signInTask ->
////                                            if (signInTask.isSuccessful) {
////                                                // Successfully signed in
////                                                val user = firebaseAuth.currentUser
////                                                if (user != null) {
////                                                    // Perform actions after successful sign-in
////                                                    // For example, navigate to another activity
////                                                    Toast.makeText(this@LoginActivity, "HO Gya MKC Finally pr naya hau!!", Toast.LENGTH_SHORT).show()
////                                                }
////                                            } else {
////                                                // Sign-in failed
////                                                Toast.makeText(this@LoginActivity, "Kuch to locha hai re baba", Toast.LENGTH_SHORT).show()
////                                            }
////                                        }
////                                } else {
////                                    // User entry creation failed
////                                    Toast.makeText(this@LoginActivity, "User creation failed: ${createTask.exception?.message}", Toast.LENGTH_SHORT).show()
////                                }
////                            }
//                    }
                    }
                    else{
                        Toast.makeText(this@LoginActivity, "HO Gya MKC Finally pr naya hai!!", Toast.LENGTH_SHORT).show()

                    }
                }

                override fun onCancelled(error: DatabaseError) {
                    // Error occurred while checking for user existence
                    Log.d("Login Error", "Error in Firebase query: ${error.message}")
                }
            })
        } else {
            // Email address is null
            Toast.makeText(this, "Email address is null", Toast.LENGTH_SHORT).show()
        }
    }

    override fun onStart() {
        super.onStart()
        if (GoogleSignIn.getLastSignedInAccount(this) != null) {
            Toast.makeText(this,"HO Gya bhai mkc 22",Toast.LENGTH_SHORT).show()

        }
    }

}
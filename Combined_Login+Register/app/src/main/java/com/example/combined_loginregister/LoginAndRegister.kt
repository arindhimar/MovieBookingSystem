package com.example.combined_loginregister

import android.animation.AnimatorInflater
import android.animation.AnimatorSet
import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.isEmpty
import androidx.core.view.isVisible
import com.example.combined_loginregister.databinding.ActivityLoginAndRegisterBinding
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.android.gms.auth.api.signin.GoogleSignInAccount
import com.google.android.gms.auth.api.signin.GoogleSignInClient
import com.google.android.gms.auth.api.signin.GoogleSignInOptions
import com.google.android.gms.common.api.ApiException
import com.google.android.gms.tasks.Task
import com.google.firebase.FirebaseApp
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener

class LoginAndRegister : AppCompatActivity() {
    private lateinit var binding: ActivityLoginAndRegisterBinding
    private lateinit var frontAnimation: AnimatorSet
    private var isFront = true

    lateinit var mGoogleSignInClient: GoogleSignInClient
    val Req_Code: Int = 123
    private lateinit var firebaseAuth: FirebaseAuth

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        // Inflate layout using view binding
        binding = ActivityLoginAndRegisterBinding.inflate(layoutInflater)
        setContentView(binding.root)

        FirebaseApp.initializeApp(this)

        val gso = GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
            .requestIdToken("390229249723-kgf51fevhonod7sf18vnd5ga6tnna0ed.apps.googleusercontent.com")
            .requestEmail()
            .build()

        mGoogleSignInClient = GoogleSignIn.getClient(this, gso)
        firebaseAuth = FirebaseAuth.getInstance()



        // Initialize front card and flip button
        val scale = applicationContext.resources.displayMetrics.density
        val front = binding.cardFront
        val flip = binding.loginbtn

        // Set camera distance for card flip effect
        front.cameraDistance = 8000 * scale

        // Load the rotate_360 animation for the front card
        frontAnimation = AnimatorInflater.loadAnimator(applicationContext, R.animator.animation360) as AnimatorSet

        // Set the event listener for the flip button
        flip.setOnClickListener {
            // Set the animation target to the front card
            frontAnimation.setTarget(front)

            // Start the animation
            frontAnimation.start()

            // Toggle the card state
            isFront = !isFront

            // Switch visibility of text input layouts and buttons
            switchControls()
        }

        binding.loginbtn2.setOnClickListener {
            // Set the animation target to the front card
            frontAnimation.setTarget(front)

            // Start the animation
            frontAnimation.start()

            // Toggle the card state
            isFront = !isFront

            // Switch visibility of text input layouts and buttons
            switchControls()
        }

        // Trigger the animation once to show the back of the card initially
        binding.CloseIcon.performClick()
    }

    private fun switchControls() {
        val currentVisibility = binding.textInputLayout1.isVisible

        binding.textInputLayout1.isVisible = !currentVisibility
        binding.textInputLayout2.isVisible = !currentVisibility
        binding.textInputLayout3.isVisible = !currentVisibility
        binding.textInputLayout4.isVisible = currentVisibility
        binding.textInputLayout5.isVisible = currentVisibility
        binding.textInputLayout6.isVisible = currentVisibility
        binding.loginbtn.isVisible = !currentVisibility
        binding.loginbtn2.isVisible = currentVisibility
        binding.loginbtn3.isVisible = currentVisibility
        binding.CloseIcon.isVisible = true
        binding.imageView2.isVisible = true
        binding.imageView.isVisible = true


        //Login Layout
        binding.AppIcon.isVisible=false
        binding.txtLoginEmailMobileId.isVisible=false
        binding.txtPassword.isVisible  = false
        binding.loginbtnmain.isVisible = false
        binding.loginwithgoogle.isVisible = false
        binding.registerbtn.isVisible = false
    }


    fun hideForLogin(){
        binding.textInputLayout1.isVisible = false
        binding.textInputLayout2.isVisible = false
        binding.textInputLayout3.isVisible = false
        binding.textInputLayout4.isVisible = false
        binding.textInputLayout5.isVisible = false
        binding.textInputLayout6.isVisible = false
        binding.loginbtn.isVisible = false
        binding.imageView2.isVisible = false
        binding.imageView.isVisible=false
        binding.loginbtn2.isVisible = false
        binding.loginbtn3.isVisible = false
        binding.CloseIcon.isVisible = true

    }

    fun SwitchToLogin(view: View) {
        // Initialize front card and flip button
        val scale = applicationContext.resources.displayMetrics.density
        val front = binding.cardFront
        val flip = binding.loginbtn

        // Set camera distance for card flip effect
        front.cameraDistance = 8000 * scale

        // Load the rotate_360 animation for the front card
        frontAnimation = AnimatorInflater.loadAnimator(applicationContext, R.animator.animation360) as AnimatorSet

        frontAnimation.setTarget(front)

        // Start the animation
        frontAnimation.start()

        // Toggle the card state
        isFront = !isFront

        hideForLogin()

        //Login Layout Visibility Modification

        binding.AppIcon.isVisible=true
        binding.txtLoginEmailMobileId.isVisible=true
        binding.txtPassword.isVisible  = true
        binding.loginbtnmain.isVisible = true
        binding.loginwithgoogle.isVisible = true
        binding.registerbtn.isVisible = true
        binding.CloseIcon.isVisible = false

    }

    fun SwitchToRegister(view: View) {
        // Initialize front card and flip button
        val scale = applicationContext.resources.displayMetrics.density
        val front = binding.cardFront
        val flip = binding.loginbtn

        // Set camera distance for card flip effect
        front.cameraDistance = 8000 * scale

        // Load the rotate_360 animation for the front card
        frontAnimation = AnimatorInflater.loadAnimator(applicationContext, R.animator.animation360) as AnimatorSet

        // Set the event listener for the flip button

            // Set the animation target to the front card
            frontAnimation.setTarget(front)

            // Start the animation
            frontAnimation.start()

            // Toggle the card state
            isFront = !isFront

            // Switch visibility of text input layouts and buttons
            switchControls()

    }

    fun LoginUser(view: View) {
        //Validating for empty fields!!
        if(binding.txtLoginEmailMobileId.editText!!.text.isEmpty() || binding.txtPassword.editText!!.text.isEmpty()) {
            Toast.makeText(this,"Empty Fields!!",Toast.LENGTH_SHORT).show()
            return // Exit the function to prevent further execution
        }
        val DbRef:DatabaseReference = FirebaseDatabase.getInstance().getReference("moviedb/usertb")

        DbRef.addListenerForSingleValueEvent(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                //checking if data exists
                if (snapshot.exists()) {
                    //traverse data
                    var loginSuccess = false
                    for (data in snapshot.children) {
                        val temp = data.getValue(UserTb::class.java)
                        if (temp != null) {
                            if (temp.uemail == binding.txtLoginEmailMobileId.editText!!.text.toString() ||
                                temp.umobile == binding.txtLoginEmailMobileId.editText!!.text.toString() ||
                                temp.uname == binding.txtLoginEmailMobileId.editText!!.text.toString()) {
                                if (temp.upassword == binding.txtPassword.editText!!.text.toString()) {
                                    Log.d("TAG", "onDataChange:${temp.uid} ")

                                    break // Exit the loop once login is successful
                                }
                            }
                        }
                    }
                }
            }

            override fun onCancelled(error: DatabaseError) {
                Log.d("Login Error", "Error in Firebase query: ${error.message}")
            }
        })

    }

    fun LoginWithGoogle(view: View) {
        signInGoogle()
    }

    private fun signInGoogle() {
        val signInIntent: Intent = mGoogleSignInClient.signInIntent
        startActivityForResult(signInIntent, Req_Code)
    }

    // onActivityResult() function : this is where
    // we provide the task and data for the Google Account
    @Deprecated("Deprecated in Java")
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

            val DbRef:DatabaseReference = FirebaseDatabase.getInstance().getReference("moviedb/usertb")

            DbRef.orderByChild("uemail").equalTo(email).addListenerForSingleValueEvent(object : ValueEventListener {
                override fun onDataChange(snapshot: DataSnapshot) {
                    if (snapshot.exists()) {

                        Toast.makeText(this@LoginAndRegister, "HO Gya MKC Finally ", Toast.LENGTH_SHORT).show()



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
                        Toast.makeText(this@LoginAndRegister, "HO Gya MKC Finally pr naya hai!!", Toast.LENGTH_SHORT).show()

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

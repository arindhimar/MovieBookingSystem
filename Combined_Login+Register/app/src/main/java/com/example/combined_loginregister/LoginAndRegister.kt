package com.example.combined_loginregister

import android.animation.AnimatorInflater
import android.animation.AnimatorSet
import android.annotation.SuppressLint
import android.app.Dialog
import android.content.Intent
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.os.Bundle
import android.os.Handler
import android.util.Log
import android.view.View
import android.view.Window
import android.widget.Button
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.TextView
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
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.google.firebase.FirebaseApp
import com.google.firebase.FirebaseException
import com.google.firebase.FirebaseTooManyRequestsException
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseAuthInvalidCredentialsException
import com.google.firebase.auth.FirebaseAuthMissingActivityForRecaptchaException
import com.google.firebase.auth.PhoneAuthCredential
import com.google.firebase.auth.PhoneAuthOptions
import com.google.firebase.auth.PhoneAuthProvider
import com.google.firebase.auth.ktx.auth
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import com.google.firebase.ktx.Firebase
import com.google.firebase.messaging.FirebaseMessaging
import java.util.concurrent.TimeUnit
import kotlin.properties.Delegates

class LoginAndRegister : AppCompatActivity() {
    private lateinit var binding: ActivityLoginAndRegisterBinding
    private lateinit var frontAnimation: AnimatorSet
    private var isFront = true

    lateinit var mGoogleSignInClient: GoogleSignInClient
    val Req_Code: Int = 123
    private lateinit var firebaseAuth: FirebaseAuth


    //For OTP
    var otpverification = 3

    var VerificationID: String? = null

    var validateotp by Delegates.notNull<Boolean>()

    // [START declare_auth]
    private lateinit var auth: FirebaseAuth
    // [END declare_auth]

    private var storedVerificationId: String? = ""
    private lateinit var resendToken: PhoneAuthProvider.ForceResendingToken
    private lateinit var callbacks: PhoneAuthProvider.OnVerificationStateChangedCallbacks


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

        binding.loginbtn.setOnClickListener {
            // Set the animation target to the front card

            if(validateRegisterPage1()) {

                frontAnimation.setTarget(front)

                // Start the animation
                frontAnimation.start()

                // Toggle the card state
                isFront = !isFront

                // Switch visibility of text input layouts and buttons
                switchControls()
            }
        }

        binding.textInputLayout4.setEndIconOnClickListener {
            //Validation for phone number
            if ( binding.textInputLayout4.editText!!.text.toString().trim().isEmpty()) {
                binding.textInputLayout4.error = "Phone number cannot be empty"
            } else if (! binding.textInputLayout4.editText!!.text.toString().trim().matches("[6-9]\\d{9}".toRegex())) {
                binding.textInputLayout4.error = "Invalid phone number"
            } else {
                //if phone number is valid generate OTP
                startPhoneNumberVerification(binding.textInputLayout4.editText!!.text.trim().toString())
            }
        }

        binding.textInputLayout5.setEndIconOnClickListener {
            verifyPhoneNumberWithCode(VerificationID,binding.textInputLayout5.editText!!.text.trim().toString())
        }


        binding.loginbtn2.setOnClickListener {
            // Set the animation target to the front card

                binding.textInputLayout5.isEnabled = false

                frontAnimation.setTarget(front)

                // Start the animation
                frontAnimation.start()

                // Toggle the card state
                isFront = !isFront

                // Switch visibility of text input layouts and buttons
                switchControls()

        }

        //Disable OTP field
        binding.textInputLayout5.isEnabled = false

        // Trigger the animation once to show the back of the card initially
        binding.CloseIcon.performClick()

        //For OTP
        validateotp = false
        auth = Firebase.auth
        onNewToken()

        // [START phone_auth_callbacks]
        callbacks = object : PhoneAuthProvider.OnVerificationStateChangedCallbacks() {

            override fun onVerificationCompleted(credential: PhoneAuthCredential) {
                // This callback will be invoked in two situations:
                // 1 - Instant verification. In some cases the phone number can be instantly
                //     verified without needing to send or enter a verification code.
                // 2 - Auto-retrieval. On some devices Google Play services can automatically
                //     detect the incoming verification SMS and perform verification without
                //     user action.
                Log.d("TAG", "onVerificationCompleted:$credential")
                signInWithPhoneAuthCredential(credential)
            }

            override fun onVerificationFailed(e: FirebaseException) {
                // This callback is invoked in an invalid request for verification is made,
                // for instance if the the phone number format is not valid.
                Log.w("TAG", "onVerificationFailed", e)

                if (e is FirebaseAuthInvalidCredentialsException) {
                    // Invalid request
                } else if (e is FirebaseTooManyRequestsException) {
                    // The SMS quota for the project has been exceeded
                } else if (e is FirebaseAuthMissingActivityForRecaptchaException) {
                    // reCAPTCHA verification attempted with null Activity
                }

                // Show a message and update the UI
            }

            @SuppressLint("SetTextI18n")
            override fun onCodeSent(
                verificationId: String,
                token: PhoneAuthProvider.ForceResendingToken,
            ) {
                // The SMS verification code has been sent to the provided phone number, we
                // now need to ask the user to enter the code and then construct a credential
                // by combining the code with a verification ID.
                Log.d("TAG", "onCodeSent:$verificationId")

                // Show the dialog that otp is sent
                val dialog = Dialog(this@LoginAndRegister)
                dialog.requestWindowFeature(Window.FEATURE_NO_TITLE)
                dialog.setCancelable(false)
                dialog.setContentView(R.layout.custom_dialog_layout)
                dialog.window?.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))
                dialog.window?.setWindowAnimations(R.style.AnimationsForDailog)

                val MessageIcon:ImageView = dialog.findViewById(R.id.imageView)
                val tvMessage: TextView = dialog.findViewById(R.id.textView2)
                val btnLayout: LinearLayout = dialog.findViewById(R.id.CustomDialogButtonLayout)
                btnLayout.isVisible=false

                MessageIcon.setImageResource(R.drawable.green_tick)
                tvMessage.text = "OTP sent successfully"
                dialog.show()

                // Enable the OTP field
                binding.textInputLayout5.isEnabled = true

                // Close the dialog after 2 seconds
                val handler = Handler()
                handler.postDelayed({
                    dialog.dismiss()
                }, 2000)




                VerificationID = verificationId

                // Save verification ID and resending token so we can use them later
                storedVerificationId = verificationId
                resendToken = token
            }
        }
        // [END phone_auth_callbacks]


    }

    fun onNewToken() {
        Log.d("TAG", "Refreshed token: ")

        FirebaseMessaging.getInstance().token.addOnCompleteListener {task->
            if(task.isSuccessful){
                Log.d("TAG", "Refreshed token: ${task.result}")
            }

        }


    }






    private fun verifyPhoneNumberWithCode(verificationId: String?, code: String) {
        // [START verify_with_code]
        val credential = PhoneAuthProvider.getCredential(verificationId!!, code)
        // [END verify_with_code]
        signInWithPhoneAuthCredential(credential)
    }



    private fun startPhoneNumberVerification(phoneNumber: String) {
        // [START start_phone_auth]
        val options = PhoneAuthOptions.newBuilder(auth)
            .setPhoneNumber("+91$phoneNumber") // Phone number to verify
            .setTimeout(60L, TimeUnit.SECONDS) // Timeout and unit
            .setActivity(this) // Activity (for callback binding)
            .setCallbacks(callbacks) // OnVerificationStateChangedCallbacks
            .build()
        PhoneAuthProvider.verifyPhoneNumber(options)
        // [END start_phone_auth]
    }


    // [START sign_in_with_phone]
    private fun signInWithPhoneAuthCredential(credential: PhoneAuthCredential) {
        auth.signInWithCredential(credential)
            .addOnCompleteListener(this) { task ->
                if (task.isSuccessful) {
                    // Sign in success, update UI with the signed-in user's information
                    Log.d("TAG", "signInWithCredential:success")

                    val user = task.result?.user


                    Toast.makeText(this,"Lol!!",Toast.LENGTH_SHORT).show()

                } else {
                    // Sign in failed, display a message and update the UI
                    Log.w("TAG", "signInWithCredential:failure", task.exception)
                    if (task.exception is FirebaseAuthInvalidCredentialsException) {
                        val dialog = Dialog(this@LoginAndRegister)
                        dialog.requestWindowFeature(Window.FEATURE_NO_TITLE)
                        dialog.setCancelable(false)
                        dialog.setContentView(R.layout.custom_dialog_layout)
                        dialog.window?.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))
                        dialog.window?.setWindowAnimations(R.style.AnimationsForDailog)

                        val MessageIcon:ImageView = dialog.findViewById(R.id.imageView)
                        val tvMessage: TextView = dialog.findViewById(R.id.textView2)
                        val btnLayout: LinearLayout = dialog.findViewById(R.id.CustomDialogButtonLayout)
                        btnLayout.isVisible=false

                        MessageIcon.setImageResource(R.drawable.red_wrong)
                        tvMessage.text = "Wrong OTP"
                        dialog.show()


                        // Close the dialog after 2 seconds
                        val handler = Handler()
                        handler.postDelayed({
                            dialog.dismiss()
                        }, 2000)                    }
                    // Update UI
                }
            }
    }
    // [END sign_in_with_phone]


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

    private fun validateRegisterPage1(): Boolean {
        // Validation for name
        if (binding.textInputLayout1.editText!!.text.isEmpty()) {
            binding.textInputLayout1.error = "Name cannot be empty"
        } else if (!binding.textInputLayout1.editText!!.text.matches("[a-zA-Z ]+".toRegex())) {
            binding.textInputLayout1.error = "Invalid characters in name"
        } else {
            binding.textInputLayout1.error = null
        }

        // Validation for Email
        if (binding.textInputLayout2.editText!!.text.toString().trim().isEmpty()) {
            binding.textInputLayout2.error = "Email cannot be empty"
        } else if (!android.util.Patterns.EMAIL_ADDRESS.matcher(binding.textInputLayout2.editText!!.text.toString().trim()).matches()) {
            binding.textInputLayout2.error = "Invalid email format"
        } else {
            binding.textInputLayout2.error = null
        }

        // Validation for Password
        if (binding.textInputLayout3.editText!!.text.toString().trim().isEmpty()) {
            binding.textInputLayout3.error = "Password cannot be empty"
        } else if (binding.textInputLayout3.editText!!.text.toString().trim().length < 8) {
            // Change 8 to your desired minimum password length
            binding.textInputLayout3.error = "Password must be at least 8 characters long"
        } else if (!binding.textInputLayout3.editText!!.text.toString().trim().matches("[a-zA-Z0-9@#$%^&+=]+".toRegex())) {
            binding.textInputLayout3.error = "Password must contain letters, numbers, and special characters"
        } else {
            binding.textInputLayout3.error = null
        }

        // Check if there are no errors in any field
//        return binding.textInputLayout1.error == null &&
//                binding.textInputLayout2.error == null &&
//                binding.textInputLayout3.error == null

        return true
    }

    private fun validateRegisterPage2(): Boolean {
        // Validation for Phone Number
        val phoneNumber = binding.textInputLayout4.editText!!.text.toString().trim()
        if (phoneNumber.isEmpty()) {
            binding.textInputLayout4.error = "Phone number cannot be empty"
        } else if (!phoneNumber.matches("[6-9]\\d{9}".toRegex())) {
            binding.textInputLayout4.error = "Invalid phone number format"
        } else {
            binding.textInputLayout4.error = null
        }


        // Check if there are no errors in any field
        return binding.textInputLayout4.error == null
    }



    override fun onStart() {
        super.onStart()
        if (GoogleSignIn.getLastSignedInAccount(this) != null) {
            Toast.makeText(this,"HO Gya bhai mkc 22",Toast.LENGTH_SHORT).show()

        }
    }
}

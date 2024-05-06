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
import androidx.appcompat.app.AppCompatDelegate
import androidx.core.view.isVisible
import com.example.combined_loginregister.databinding.ActivityLoginAndRegisterBinding
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.android.gms.auth.api.signin.GoogleSignInAccount
import com.google.android.gms.auth.api.signin.GoogleSignInClient
import com.google.android.gms.auth.api.signin.GoogleSignInOptions
import com.google.android.gms.common.api.ApiException
import com.google.android.gms.tasks.Task
import com.google.firebase.FirebaseApp
import com.google.firebase.FirebaseException
import com.google.firebase.FirebaseTooManyRequestsException
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseAuthInvalidCredentialsException
import com.google.firebase.auth.FirebaseAuthMissingActivityForRecaptchaException
import com.google.firebase.auth.GoogleAuthProvider
import com.google.firebase.auth.PhoneAuthCredential
import com.google.firebase.auth.PhoneAuthOptions
import com.google.firebase.auth.PhoneAuthProvider
import com.google.firebase.auth.ktx.auth
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.ktx.Firebase
import com.google.firebase.messaging.FirebaseMessaging
import org.imaginativeworld.oopsnointernet.callbacks.ConnectionCallback
import org.imaginativeworld.oopsnointernet.dialogs.signal.NoInternetDialogSignal
import java.util.concurrent.TimeUnit
import kotlin.properties.Delegates
import kotlin.system.exitProcess


class LoginAndRegister : AppCompatActivity() {
    private lateinit var binding: ActivityLoginAndRegisterBinding
    private lateinit var frontAnimation: AnimatorSet
    private var isFront = true
    private lateinit var loadingDialogBox:Dialog


    lateinit var mGoogleSignInClient: GoogleSignInClient
    val Req_Code: Int = 123
    private lateinit var firebaseAuth: FirebaseAuth

    lateinit var encryption: Encryption

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

        AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_NO);



        // No Internet Dialog: Signal
        NoInternetDialogSignal.Builder(
            this,
            lifecycle
        ).apply {
            dialogProperties.apply {
                connectionCallback = object : ConnectionCallback { // Optional
                    override fun hasActiveConnection(hasActiveConnection: Boolean) {
                        // ...
                    }
                }

                cancelable = false // Optional
                noInternetConnectionTitle = "No Internet" // Optional
                noInternetConnectionMessage =
                    "Check your Internet connection and try again." // Optional
                showInternetOnButtons = true // Optional
                pleaseTurnOnText = "Please turn on" // Optional
                wifiOnButtonText = "Wifi" // Optional
                mobileDataOnButtonText = "Mobile data" // Optional

                onAirplaneModeTitle = "No Internet" // Optional
                onAirplaneModeMessage = "You have turned on the airplane mode." // Optional
                pleaseTurnOffText = "Please turn off" // Optional
                airplaneModeOffButtonText = "Airplane mode" // Optional
                showAirplaneModeOffButtons = true // Optional
            }
        }.build()


        FirebaseApp.initializeApp(this)

        val gso = GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
            .requestIdToken("390229249723-kgf51fevhonod7sf18vnd5ga6tnna0ed.apps.googleusercontent.com")
            .requestEmail()
            .build()

        mGoogleSignInClient = GoogleSignIn.getClient(this, gso)
        firebaseAuth = FirebaseAuth.getInstance()


        //Checking previous login
        checkPrevLogin()


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
                showLoading("Sending OTP")
            }
        }

        binding.textInputLayout5.setEndIconOnClickListener {
            verifyPhoneNumberWithCode(VerificationID,binding.textInputLayout5.editText!!.text.trim().toString())
            
        }

        binding.sendEmailVerification.setOnClickListener {
            if(validateRegisterPage1()){

            }
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

        //Disable the Register Button
        binding.loginbtn3.isEnabled = false

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

                when (e) {
                    is FirebaseAuthInvalidCredentialsException -> {
                        // Invalid request
                    }

                    is FirebaseTooManyRequestsException -> {
                        // The SMS quota for the project has been exceeded
                    }

                    is FirebaseAuthMissingActivityForRecaptchaException -> {
                        // reCAPTCHA verification attempted with null Activity
                    }
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


                val successLoadingHelper = SuccessLoadingHelper()
                successLoadingHelper.showLoadingDialog(this@LoginAndRegister)
                successLoadingHelper.hideButtons()
                successLoadingHelper.updateText("Otp has been sent!!")


                // Enable the OTP field
                binding.textInputLayout5.isEnabled = true


                val handler = Handler()
                handler.postDelayed({
                    successLoadingHelper.dismissLoadingDialog()
                }, 2000)


                VerificationID = verificationId

                // Save verification ID and resending token so we can use them later
                storedVerificationId = verificationId
                resendToken = token
            }
        }
        // [END phone_auth_callbacks]

        //Initiating Loading Dialog Screen
        loadingDialogBox=Dialog(this)
        showLoading("Initiating")

        //Registering the user
        binding.loginbtn3.setOnClickListener {
            registerUser()
        }
    }

    fun signInGoogleAgain(view: View) {
        signInGoogle()
    }

    @Deprecated("Deprecated in Java")
    override fun onBackPressed() {
        super.onBackPressed()
        moveTaskToBack(true)
        android.os.Process.killProcess(android.os.Process.myPid())
        exitProcess(1)
    }

    private fun registerUser() {
        val loadingDialogHelper = LoadingDialogHelper()
        loadingDialogHelper.showLoadingDialog(this)

        val firebaseAuth = FirebaseAuth.getInstance()
        val firebaseDatabase = FirebaseDatabase.getInstance()

        // Validating user input
        val username = binding.textInputLayout1.editText!!.text.toString()
        val email = binding.textInputLayout2.editText!!.text.toString()
        val password = binding.textInputLayout3.editText!!.text.toString()
        val mobile = binding.textInputLayout4.editText!!.text.toString()

        if (username.isBlank() || email.isBlank() || password.isBlank() || mobile.isBlank()) {
            Toast.makeText(this, "Empty fields!", Toast.LENGTH_SHORT).show()
            loadingDialogHelper.dismissLoadingDialog()
            return
        }

        // Check if the email is already registered
        firebaseAuth.fetchSignInMethodsForEmail(email)
            .addOnCompleteListener { fetchTask ->
                if (fetchTask.isSuccessful) {
                    val signInMethods = fetchTask.result?.signInMethods
                    if (signInMethods != null && signInMethods.isNotEmpty()) {
                        // Email is already registered, handle accordingly
                        loadingDialogHelper.dismissLoadingDialog()
                        Log.e("TAG", "Email already registered")
                        Toast.makeText(this, "Email already registered", Toast.LENGTH_SHORT).show()
                    } else {
                        // Email is not registered, proceed with user registration
                        // Perform user registration with Firebase Authentication
                        firebaseAuth.createUserWithEmailAndPassword(email, password)
                            .addOnCompleteListener { authTask ->
                                if (authTask.isSuccessful) {
                                    val firebaseUser = firebaseAuth.currentUser
                                    if (firebaseUser != null) {
                                        // Use the user ID from Firebase Authentication as the key for database storage
                                        val userId = firebaseUser.uid

                                        // Create a UserTb object with the obtained user ID
                                        val tempUser = UserTb(userId, username,mobile, "user")
                                        val dbRef = FirebaseDatabase.getInstance().getReference("moviedb/usertb")
                                        val firebaseRestManager = FirebaseRestManager<UserTb>()

                                        firebaseRestManager.addItemWithCustomId(tempUser, userId, dbRef) { success, error ->
                                            if (success) {
                                                binding.CloseIcon.performClick()
                                                EnableAndCleanRegisterFields()
                                                loadingDialogHelper.dismissLoadingDialog()
                                                val successLoadingHelper = SuccessLoadingHelper()


                                                successLoadingHelper.showLoadingDialog(this)
                                                successLoadingHelper.hideButtons()
                                                successLoadingHelper.updateText("User Registered!!\nA link has been sent to the mail , verify the email to start using the application!!")
                                                val handler = Handler()
                                                handler.postDelayed({
                                                    successLoadingHelper.dismissLoadingDialog()
                                                    firebaseAuth.currentUser!!.sendEmailVerification().addOnCompleteListener {
                                                        Log.d("TAG", "registerUser:email sent!! ")
                                                    }
                                                }, 2000)
                                            } else {
                                                // Handle failure to add user data to the database
                                                loadingDialogHelper.dismissLoadingDialog()
                                                Log.e("TAG", "Error adding user data to the database: $error")
                                                // Display error message or take appropriate action
                                            }
                                        }
                                    } else {
                                        loadingDialogHelper.dismissLoadingDialog()
                                        // Registration failed, display error message
                                        Log.e("TAG", "User registration failed: ${authTask.exception}")
                                        Toast.makeText(this, "User registration failed", Toast.LENGTH_SHORT).show()
                                    }
                                } else {
                                    // Registration failed
                                    loadingDialogHelper.dismissLoadingDialog()
                                    Log.e("TAG", "User registration failed: ${authTask.exception}")
                                    Toast.makeText(this, "User registration failed", Toast.LENGTH_SHORT).show()
                                }
                            }
                    }
                } else {
                    // Error fetching sign-in methods
                    loadingDialogHelper.dismissLoadingDialog()
                    Log.e("TAG", "Error fetching sign-in methods: ${fetchTask.exception}")
                    Toast.makeText(this, "Error fetching sign-in methods", Toast.LENGTH_SHORT).show()
                }
            }
    }


    private fun showLoading(message: String?) {
        loadingDialogBox=Dialog(this)

        // Request window feature before setting content view
        loadingDialogBox.requestWindowFeature(Window.FEATURE_NO_TITLE)
        loadingDialogBox.setCancelable(false)

        // Set content view after requesting window feature
        loadingDialogBox.setContentView(R.layout.custom_dialog_layout)
        loadingDialogBox.window?.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))
        loadingDialogBox.window?.setWindowAnimations(R.style.AnimationsForDailog)

        val tvmessage: TextView = loadingDialogBox.findViewById(R.id.textView2)
        val btnyes: Button = loadingDialogBox.findViewById(R.id.btn_yes)
        val btnno: Button = loadingDialogBox.findViewById(R.id.btn_no)

        tvmessage.text = message

        btnyes.setOnClickListener {
            Toast.makeText(this, "click on yes", Toast.LENGTH_SHORT).show()
        }

        btnno.setOnClickListener {
            loadingDialogBox.dismiss()
        }
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

                    //OTP has been Verified along with validation enabling the register button
                    binding.loginbtn3.isEnabled = true


                    val successLoadingHelper = SuccessLoadingHelper()


                    successLoadingHelper.showLoadingDialog(this)
                    successLoadingHelper.hideButtons()
                    successLoadingHelper.updateText("Otp has been verified!!")
                    val handler = Handler()
                    handler.postDelayed({
                        successLoadingHelper.dismissLoadingDialog()
                    }, 2000)

                    //Disabling to prevent user from modifying the fields
                    LockUnlockRegisterationFields()

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

    private fun LockUnlockRegisterationFields(){
        binding.textInputLayout1.isEnabled = false
        binding.textInputLayout2.isEnabled = false
        binding.textInputLayout3.isEnabled = false
        binding.textInputLayout4.isEnabled = false
        binding.textInputLayout5.isEnabled = false
    }

    private fun EnableAndCleanRegisterFields(){
        binding.textInputLayout1.isEnabled = true
        binding.textInputLayout1.editText!!.text.clear()
        binding.textInputLayout2.isEnabled = true
        binding.textInputLayout2.editText!!.text.clear()
        binding.textInputLayout3.isEnabled = true
        binding.textInputLayout3.editText!!.text.clear()
        binding.textInputLayout4.isEnabled = true
        binding.textInputLayout4.editText!!.text.clear()
        binding.textInputLayout5.isEnabled = false
        binding.textInputLayout5.editText!!.text.clear()
    }


    private fun switchControls() {
        val currentVisibility = binding.textInputLayout1.isVisible

        binding.textInputLayout1.isVisible = !currentVisibility
        binding.textInputLayout2.isVisible = !currentVisibility
        binding.textInputLayout3.isVisible = !currentVisibility
        binding.textInputLayout4.isVisible = currentVisibility
        binding.textInputLayout5.isVisible = currentVisibility

        binding.loginbtn.isVisible = !currentVisibility
        binding.sendEmailVerification.isVisible = !currentVisibility

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

        binding.loginbtn.isVisible = false
        binding.sendEmailVerification.isVisible = false

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

    @SuppressLint("SuspiciousIndentation")
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
        // Validating for empty fields
        if (binding.txtLoginEmailMobileId.editText!!.text.isEmpty() || binding.txtPassword.editText!!.text.isEmpty()) {
            Toast.makeText(this, "Empty Fields!!", Toast.LENGTH_SHORT).show()
            return // Exit the function to prevent further execution
        }

        val loadingDialogHelper = LoadingDialogHelper()
        loadingDialogHelper.showLoadingDialog(this)

        val userLogin = binding.txtLoginEmailMobileId.editText!!.text.toString()
        val password = binding.txtPassword.editText!!.text.toString()

        val auth = FirebaseAuth.getInstance()
        auth.signInWithEmailAndPassword(userLogin, password)
            .addOnCompleteListener(this) { task ->
                if (task.isSuccessful) {
                    // Login successful, get the authenticated user
                    val user = auth.currentUser

                    // Now you have access to the authenticated user's details
                    if (user != null) {
                        val userId = user.uid
                        val userEmail = user.email
                        // access other details like displayName, photoUrl, etc. as needed
                        val firebaseRestManager = FirebaseRestManager<UserTb>()
                        firebaseRestManager.getSingleItem(UserTb::class.java, "moviedb/usertb", userId) { user ->

                            if(user!!.utype=="owner"){
                                val intent = Intent(this,OwnerActivity::class.java)
                                startActivity(intent)
                                finish()
                            }
                            else if(user.utype=="cinemaowner"){
                                intent = Intent(this@LoginAndRegister,CinemaOwnerActivity::class.java)
                                startActivity(intent)
                                finish()

                            }

                            loadingDialogHelper.dismissLoadingDialog()
                        }

                    } else {
                        // User is null, handle error
                        Log.e("TAG", "Current user is null")
                        Toast.makeText(baseContext, "User details not found.",
                            Toast.LENGTH_SHORT).show()
                        loadingDialogHelper.dismissLoadingDialog()

                    }
                } else {
                    // Login failed, display a message to the user ye wala wrong details hai bisi
                    Toast.makeText(baseContext, "New User ?? Register Please!!",
                        Toast.LENGTH_SHORT).show()
                    loadingDialogHelper.dismissLoadingDialog()

                }
            }
    }

    fun LoginWithGoogle(view: View) {
        signInGoogleAgain(view)
    }

    private fun signInGoogle() {
        // val auth = FirebaseAuth.getInstance()
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
                updateUI(account)
            }
        } catch (e: ApiException) {
            Toast.makeText(this, e.toString(), Toast.LENGTH_SHORT).show()
        }
    }
    private fun updateUI(account: GoogleSignInAccount) {
        val email = account.email
        if (email != null) {
            // Authenticate the user with Firebase using Google credentials
            val credential = GoogleAuthProvider.getCredential(account.idToken, null)
            FirebaseAuth.getInstance().signInWithCredential(credential)
                .addOnCompleteListener(this) { authTask ->
                    if (authTask.isSuccessful) {
                        // User authenticated with Firebase successfully
                        val firebaseUser = FirebaseAuth.getInstance().currentUser
                        firebaseUser?.let { user ->
                            val uid = user.uid
                            Log.d("TAG", "User UID: $uid")

                            val firebaseRestManager = FirebaseRestManager<UserTb>()
                            firebaseRestManager.getSingleItem(UserTb::class.java, "moviedb/usertb", user.uid) { user ->
                                if(user!=null) {
                                    if (user!!.utype == "owner") {
                                        val intent = Intent(this, OwnerActivity::class.java)
                                        startActivity(intent)
                                        finish()
                                    } else if (user.utype == "cinemaowner") {
                                        intent = Intent(
                                            this@LoginAndRegister,
                                            CinemaOwnerActivity::class.java
                                        )
                                        startActivity(intent)
                                        finish()

                                    }
                                }
                                else{
                                    firebaseAuth.signOut()
                                    mGoogleSignInClient.signOut()
                                }

                            }

                        }
                    } else {
                        // Authentication failed
                        Log.d("Login Error", "Firebase authentication failed: ${authTask.exception?.message}")
                    }
                }
        } else {
            // Email address is null
            Toast.makeText(this, "Email address is null somehow Database Breach ", Toast.LENGTH_SHORT).show()
        }

    }

    private fun checkPrevLogin(){
        val loadingDialogHelper = LoadingDialogHelper()
        loadingDialogHelper.showLoadingDialog(this)

        if(firebaseAuth.currentUser!=null) {
            val temp = firebaseAuth.currentUser
            if (temp != null) {
                Log.d("TAG", "checkPrevLogin:${temp.uid} ")

                val firebaseRestManager = FirebaseRestManager<UserTb>()
                firebaseRestManager.getSingleItem(
                    UserTb::class.java,
                    "moviedb/usertb",
                    temp.uid
                ) { user ->

                    if(user!=null) {

                        if (user.utype == "owner") {
                            intent = Intent(this, OwnerActivity::class.java)
                            startActivity(intent)
                            loadingDialogHelper.dismissLoadingDialog()

                            finish()

                        } else if (user.utype == "cinemaowner") {
                            intent = Intent(this, CinemaOwnerActivity::class.java)
                            startActivity(intent)
                            loadingDialogHelper.dismissLoadingDialog()

                            finish()
                        } else {
                            loadingDialogHelper.dismissLoadingDialog()

                        }

                    }
                    else{
                        loadingDialogHelper.dismissLoadingDialog()
                        firebaseAuth.signOut()
                        mGoogleSignInClient.signOut()
                    }
                }
            }
        }
        else {
            Log.d("TAG", "checkPrevLogin:bruhhhhhhhhhhhhhhhh ")
            loadingDialogHelper.dismissLoadingDialog()
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

//         Check if there are no errors in any field
        return binding.textInputLayout1.error == null &&
                binding.textInputLayout2.error == null &&
                binding.textInputLayout3.error == null

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
        if (GoogleSignIn.getLastSignedInAccount(this) == null) {
//            Toast.makeText(this,"Application SHA Code Verification Failure!!",Toast.LENGTH_SHORT).show()
        }

    }
}

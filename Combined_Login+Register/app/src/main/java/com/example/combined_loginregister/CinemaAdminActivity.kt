package com.example.combined_loginregister

import android.content.Intent
import android.os.Bundle
import android.os.Handler
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.app.AppCompatDelegate
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.core.view.isVisible
import com.example.combined_loginregister.databinding.ActivityCinemaAdminBinding
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.android.gms.auth.api.signin.GoogleSignInOptions
import com.google.firebase.auth.FirebaseAuth
import org.imaginativeworld.oopsnointernet.callbacks.ConnectionCallback
import org.imaginativeworld.oopsnointernet.dialogs.signal.NoInternetDialogSignal

class CinemaAdminActivity : AppCompatActivity() {
    lateinit var binding: ActivityCinemaAdminBinding
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        binding = ActivityCinemaAdminBinding.inflate(layoutInflater)
        setContentView(binding.root)
        AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_NO);
        validateUser()
        init()


    }

    private fun init(){
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






        setSupportActionBar(binding.ToolBaar)

        binding.ToolBaar.setOnClickListener {
            binding.drawerLayout.openDrawer(binding.navView)
        }

        binding.navView.setNavigationItemSelectedListener { menuItem ->
            // Close the drawer when item is tapped
            binding.drawerLayout.closeDrawers()

            // Set checked state for the selected item
            menuItem.isChecked = true

            // Handle navigation item clicks here
            when (menuItem.itemId) {
                R.id.nav_cinema_admin_dashboard -> {
                    binding.CinemaOwnerDashBoard.isVisible = true
                    // Add any additional logic specific to this item
                }
                R.id.nav_manage_shows->{
                }
                R.id.view_booking -> {
                    // Add any additional logic specific to this item
                }
                R.id.nav_rent_movies -> {
                    binding.dashboardLeaseMovies.performClick()
                    // Add any additional logic specific to this item
                }
                R.id.nav_menu_account -> {
                    binding.dashboardManageProfile.performClick()
                    // Add any additional logic specific to this item
                }
                R.id.logoutuser -> {
                    binding.logOut.performClick()
                    // Add any additional logic specific to this item
                }
                // Add more cases for other menu items if needed
            }

            true
        }

        binding.dashboardManageCinema.setOnClickListener {
        }


        binding.dashboardManageCinemaAdmin.setOnClickListener {
        }

        binding.dashboardLeaseMovies.setOnClickListener {
        }

        binding.dashboardCinemaOwnerManageBooking.setOnClickListener {
        }

        binding.dashboardManageProfile.setOnClickListener {
        }

        binding.logOut.setOnClickListener {
            val gso = GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
                .requestIdToken("390229249723-kgf51fevhonod7sf18vnd5ga6tnna0ed.apps.googleusercontent.com")
                .requestEmail()
                .build()

            val mGoogleSignInClient = GoogleSignIn.getClient(this, gso)

            mGoogleSignInClient.signOut()

            val auth = FirebaseAuth.getInstance()
            auth.signOut()

            val intent = Intent(this, LoginAndRegister::class.java)
            startActivity(intent)


        }

    }



    private fun validateUser(){
        val currentUser = FirebaseAuth.getInstance().currentUser
        if(currentUser==null){
            val warningLoadingHelper = WarningLoadingHelper()
            warningLoadingHelper.hideButtons()
            warningLoadingHelper.updateText("Invalid User Detection!!")

            val handler = Handler()
            handler.postDelayed({
                warningLoadingHelper.dismissLoadingDialog()
            }, 2000)
        }
    }
}
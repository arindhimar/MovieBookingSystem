package com.example.combined_loginregister

import android.content.Intent
import android.os.Bundle
import android.os.Handler
import android.util.Log
import android.view.MenuItem
import android.widget.TextView
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.app.AppCompatDelegate
import androidx.core.view.isVisible
import androidx.fragment.app.Fragment
import com.example.combined_loginregister.databinding.ActivityCinemaOwnerBinding
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.android.gms.auth.api.signin.GoogleSignInOptions
import com.google.firebase.auth.FirebaseAuth
import org.imaginativeworld.oopsnointernet.callbacks.ConnectionCallback
import org.imaginativeworld.oopsnointernet.dialogs.signal.NoInternetDialogSignal


class CinemaOwnerActivity : AppCompatActivity() {
    lateinit var binding: ActivityCinemaOwnerBinding
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        binding = ActivityCinemaOwnerBinding.inflate(layoutInflater)
        setContentView(binding.root)
        AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_NO);

        validateUser()

        init()



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
        else{
            val firebaseRestManager = FirebaseRestManager<UserTb>()

            val userName:String?=null
            firebaseRestManager.getSingleItem(UserTb::class.java,"moviedb/usertb",FirebaseAuth.getInstance().currentUser!!.uid){

            }
        }
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

        val headerView = binding.navView.getHeaderView(0)
        val usernameTextView = headerView.findViewById<TextView>(R.id.NavHeaderText)

        val user = FirebaseAuth.getInstance().currentUser
        if (user != null) {
            val email = user.email
            usernameTextView.text = email
            val firebaseRestManager = FirebaseRestManager<UserTb>()
            firebaseRestManager.getSingleItem(UserTb::class.java,"moviedb/usertb",user.uid){
                usernameTextView.text ="Welcome ${it!!.uname}"
            }
        }




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
                R.id.nav_cinema_owner_dashboard -> {
                    binding.CinemaOwnerDashBoard.isVisible = true
                    // Add any additional logic specific to this item
                }
                R.id.nav_manage_cinema->{
                    replaceFragment(CinemaOwnerManageCinema())

                }
                R.id.nav_manage_cinema_admin -> {
                    replaceFragment(CinemaOwnerManageCInemaAdmin())

                }
                R.id.nav_rent_movies -> {
                    replaceFragment(CinemaOwnerLeaseMovie())

                    // Add any additional logic specific to this item
                }
                R.id.manage_booking -> {
                    replaceFragment(CinemaOwnerManageBooking())
                    // Add any additional logic specific to this item
                }
                R.id.nav_menu_account -> {
                    replaceFragment(CommonProfileFragment())

                    // Add any additional logic specific to this item
                }
                R.id.logoutuser -> {
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
                    // Add any additional logic specific to this item
                }
                // Add more cases for other menu items if needed
            }

            true
        }

        binding.dashboardManageCinema.setOnClickListener {
            binding.navView.setCheckedItem(R.id.nav_manage_cinema)
            binding.navView.menu.performIdentifierAction(R.id.nav_manage_cinema, 0)
        }


        binding.dashboardManageCinemaAdmin.setOnClickListener {
            binding.navView.setCheckedItem(R.id.nav_manage_cinema_admin)
            binding.navView.menu.performIdentifierAction(R.id.nav_manage_cinema_admin, 0)
        }

        binding.dashboardLeaseMovies.setOnClickListener {
            binding.navView.setCheckedItem(R.id.nav_rent_movies)
            binding.navView.menu.performIdentifierAction(R.id.nav_rent_movies, 0)
        }

        binding.dashboardCinemaOwnerManageBooking.setOnClickListener {
            binding.navView.setCheckedItem(R.id.manage_booking)
            binding.navView.menu.performIdentifierAction(R.id.manage_booking, 0)
        }

        binding.dashboardManageProfile.setOnClickListener {
            binding.navView.setCheckedItem(R.id.nav_menu_account)
            binding.navView.menu.performIdentifierAction(R.id.nav_menu_account, 0)
        }

        binding.logOut.setOnClickListener {
            binding.navView.setCheckedItem(R.id.logoutuser)
            binding.navView.menu.performIdentifierAction(R.id.logoutuser, 0)
        }

        binding.navView.setCheckedItem(R.id.nav_cinema_owner_dashboard)


    }



    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        if (item.itemId == android.R.id.home) {
            if (binding.drawerLayout.isDrawerOpen(binding.navView)) {
                binding.drawerLayout.closeDrawer(binding.navView)
            } else {
                binding.drawerLayout.openDrawer(binding.navView)
            }
            return true
        }
        return super.onOptionsItemSelected(item)
    }

    private fun replaceFragment(fragment: Fragment) {
        binding.CinemaOwnerDashBoard.isVisible = false
        supportFragmentManager.beginTransaction()
            .replace(R.id.fragment_container, fragment)
            .commit()
    }
}
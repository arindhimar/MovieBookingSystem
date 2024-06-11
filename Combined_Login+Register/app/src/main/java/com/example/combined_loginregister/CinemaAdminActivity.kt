package com.example.combined_loginregister

import android.content.Intent
import android.os.Bundle
import android.os.Handler
import android.util.Log
import android.widget.TextView
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.app.AppCompatDelegate
import androidx.core.view.isVisible
import androidx.fragment.app.Fragment
import com.example.combined_loginregister.databinding.ActivityCinemaAdminBinding
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.android.gms.auth.api.signin.GoogleSignInOptions
import com.google.firebase.auth.FirebaseAuth
import org.imaginativeworld.oopsnointernet.callbacks.ConnectionCallback
import org.imaginativeworld.oopsnointernet.dialogs.signal.NoInternetDialogSignal

class CinemaAdminActivity : AppCompatActivity() {
    lateinit var binding: ActivityCinemaAdminBinding
    private lateinit var auth: FirebaseAuth

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        binding = ActivityCinemaAdminBinding.inflate(layoutInflater)
        setContentView(binding.root)
        AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_NO)

        auth = FirebaseAuth.getInstance()
        validateUser()
        init()
    }

    private fun init() {
        // No Internet Dialog: Signal
        NoInternetDialogSignal.Builder(
            this,
            lifecycle
        ).apply {
            dialogProperties.apply {
                connectionCallback = object : ConnectionCallback {
                    override fun hasActiveConnection(hasActiveConnection: Boolean) {
                        // ...
                    }
                }
                cancelable = false
                noInternetConnectionTitle = "No Internet"
                noInternetConnectionMessage = "Check your Internet connection and try again."
                showInternetOnButtons = true
                pleaseTurnOnText = "Please turn on"
                wifiOnButtonText = "Wifi"
                mobileDataOnButtonText = "Mobile data"
                onAirplaneModeTitle = "No Internet"
                onAirplaneModeMessage = "You have turned on the airplane mode."
                pleaseTurnOffText = "Please turn off"
                airplaneModeOffButtonText = "Airplane mode"
                showAirplaneModeOffButtons = true
            }
        }.build()

        val headerView = binding.navView.getHeaderView(0)
        val usernameTextView = headerView.findViewById<TextView>(R.id.NavHeaderText)

        val user = auth.currentUser
        if (user != null) {
            val email = user.email
            usernameTextView.text = email
            val firebaseRestManager = FirebaseRestManager<UserTb>()
            firebaseRestManager.getSingleItem(UserTb::class.java, "moviedb/usertb", user.uid) {
                usernameTextView.text = "Welcome ${it!!.uname}"
            }
        }

        setSupportActionBar(binding.ToolBaar)

        binding.ToolBaar.setOnClickListener {
            binding.drawerLayout.openDrawer(binding.navView)
        }

        binding.navView.setNavigationItemSelectedListener { menuItem ->
            binding.drawerLayout.closeDrawers()
            menuItem.isChecked = true

            when (menuItem.itemId) {
                R.id.nav_cinema_admin_dashboard -> {
                    binding.CinemaAdminDashBoard.isVisible = true
                }
                R.id.nav_manage_shows -> {
                    replaceFragment(CInemaAdminManageShows())
                }
                R.id.nav_view_cinema_rating -> {
                    replaceFragment(CinemaAdminViewCinemaRating())
                }
                R.id.view_booking -> {
                    replaceFragment(CinemaAdminViewBookings())
                }
                R.id.nav_menu_account -> {
                    replaceFragment(CommonProfileFragment())
                }
                R.id.logoutuser -> {
                    performLogout()
                }
            }
            true
        }

        binding.navView.setCheckedItem(R.id.nav_cinema_admin_dashboard)

        binding.dashboardManageShows.setOnClickListener {
            binding.navView.setCheckedItem(R.id.nav_manage_shows)
            binding.navView.menu.performIdentifierAction(R.id.nav_manage_shows, 0)
        }

        binding.dashboardViewCinemaRating.setOnClickListener {
            binding.navView.setCheckedItem(R.id.nav_view_cinema_rating)
            binding.navView.menu.performIdentifierAction(R.id.nav_view_cinema_rating, 0)
        }

        binding.dashboardViewBooking.setOnClickListener {
            binding.navView.setCheckedItem(R.id.view_booking)
            binding.navView.menu.performIdentifierAction(R.id.view_booking, 0)
        }

        binding.dashboardManageProfile.setOnClickListener {
            binding.navView.setCheckedItem(R.id.nav_menu_account)
            binding.navView.menu.performIdentifierAction(R.id.nav_menu_account, 0)
        }

        binding.logOut.setOnClickListener {
            binding.navView.setCheckedItem(R.id.logoutuser)
            binding.navView.menu.performIdentifierAction(R.id.logoutuser, 0)
        }
    }

    private fun validateUser() {
        val currentUser = auth.currentUser
        if (currentUser == null) {
            val warningLoadingHelper = WarningLoadingHelper()
            warningLoadingHelper.showLoadingDialog(this)
            warningLoadingHelper.hideButtons()
            warningLoadingHelper.updateText("Invalid User Detection!!")

            val handler = Handler()
            handler.postDelayed({
                warningLoadingHelper.dismissLoadingDialog()
            }, 2000)
        }
    }

    private fun performLogout() {
        auth.signOut()

        val gso = GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
            .requestIdToken("390229249723-kgf51fevhonod7sf18vnd5ga6tnna0ed.apps.googleusercontent.com")
            .requestEmail()
            .build()
        val mGoogleSignInClient = GoogleSignIn.getClient(this, gso)

        mGoogleSignInClient.signOut().addOnCompleteListener {
            val intent = Intent(this, LoginAndRegister::class.java)
            startActivity(intent)
            finish()
        }.addOnFailureListener {
            Log.e("CinemaAdminActivity", "Google Sign-Out failed", it)
        }
    }

    private fun replaceFragment(fragment: Fragment) {
        binding.CinemaAdminDashBoard.isVisible = false
        supportFragmentManager.beginTransaction()
            .replace(R.id.fragment_container, fragment)
            .commit()
    }
}

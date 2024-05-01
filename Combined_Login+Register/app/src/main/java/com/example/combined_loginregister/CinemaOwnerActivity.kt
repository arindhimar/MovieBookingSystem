package com.example.combined_loginregister

import android.content.Intent
import android.os.Bundle
import android.view.MenuItem
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.core.view.isVisible
import androidx.fragment.app.Fragment
import com.example.applicaitionowner.ManageCinemaOwner
import com.example.applicaitionowner.MyAccountFragment
import com.example.combined_loginregister.databinding.ActivityCinemaOwnerBinding
import org.imaginativeworld.oopsnointernet.callbacks.ConnectionCallback
import org.imaginativeworld.oopsnointernet.dialogs.signal.NoInternetDialogSignal

class CinemaOwnerActivity : AppCompatActivity() {
    lateinit var binding: ActivityCinemaOwnerBinding
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        binding = ActivityCinemaOwnerBinding.inflate(layoutInflater)
        setContentView(binding.root)

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
            // Set checked state for the selected item
            menuItem.isChecked = true
            // Close the drawer when item is tapped
            binding.drawerLayout.closeDrawers()
            // Handle navigation item clicks here
            when (menuItem.itemId) {
                R.id.nav_cinema_owner_dashboard -> {
                    binding.CinemaOwnerDashBoard.isVisible = true
                }
                R.id.nav_manage_cinema_admin -> {
                    binding.dashboardManageCinemaAdmin.performClick()
                }
                R.id.nav_rent_movies -> {
                    binding.dashboardLeaseMovies.performClick()
                }
                R.id.manage_booking -> {
                    binding.dashboardCinemaOwnerManageBooking.performClick()
                }
                R.id.nav_menu_account -> {
                    binding.dashboardManageProfile.performClick()
                }
                R.id.logoutuser-> {
                    binding.logOut.performClick()
                }
                // Add more cases for other menu items if needed
            }
            true
        }


        binding.dashboardManageCinemaAdmin.setOnClickListener {
            replaceFragment(CinemaOwnerManageCInemaAdmin())
        }
        binding.dashboardLeaseMovies.setOnClickListener {
            replaceFragment(CinemaOwnerLeaseMovie())
        }
        binding.dashboardCinemaOwnerManageBooking.setOnClickListener {
            replaceFragment(CinemaOwnerManageBooking())
        }
        binding.dashboardManageProfile.setOnClickListener {
            replaceFragment(CommonProfileFragment())
        }
        binding.logOut.setOnClickListener {
            val encryption = Encryption(this)

            if(encryption.decrypt("userId")!=""){
                encryption.removeData("userId")
                val intent = Intent(this, LoginAndRegister::class.java)
                startActivity(intent)
                finish()
            }
        }

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
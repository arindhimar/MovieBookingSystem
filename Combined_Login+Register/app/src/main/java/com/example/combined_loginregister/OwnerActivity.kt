package com.example.combined_loginregister
import android.os.Bundle
import android.util.Log
import android.view.MenuItem
import android.view.View
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.Fragment
import com.example.applicaitionowner.ManageCinemaOwner
import com.example.applicaitionowner.MyAccountFragment
import com.example.combined_loginregister.databinding.ActivityOwnerBinding
import com.google.android.material.navigation.NavigationView

class OwnerActivity : AppCompatActivity() {
    lateinit var binding: ActivityOwnerBinding
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        binding = ActivityOwnerBinding.inflate(layoutInflater)
        setContentView(binding.root)

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
                R.id.nav_ownerdashboard -> {
                    replaceFragment(OwnerDashBoardFragment())

                }
                R.id.nav_manage_cinemaowner -> {
                    replaceFragment(ManageCinemaOwner())
                }
                R.id.nav_movies -> {
                    replaceFragment(ManageMovies())
                }
                R.id.nav_menu_account -> {
                    replaceFragment(MyAccountFragment())
                }
                R.id.manage_feedback -> {

                }
                // Add more cases for other menu items if needed
            }
            true
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
        supportFragmentManager.beginTransaction()
            .replace(R.id.fragment_container, fragment)
            .commit()
    }
}

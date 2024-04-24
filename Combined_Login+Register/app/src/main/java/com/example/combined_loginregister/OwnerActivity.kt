package com.example.combined_loginregister

import android.os.Bundle
import android.view.MenuItem
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.fragment.app.Fragment
import com.example.applicaitionowner.ManageCinemaOwner
import com.example.applicaitionowner.ManageMovies
import com.example.applicaitionowner.MyAccountFragment
import com.example.combined_loginregister.databinding.ActivityOwnerBinding

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
                R.id.nav_manage_cinemaowner -> {
                    replaceFragment(ManageCinemaOwner())
                }
                R.id.nav_movies -> {
                    replaceFragment(ManageMovies())
                }
                R.id.nav_menu_account -> {
                    replaceFragment(MyAccountFragment())
                }
                // Add more cases for other menu items if needed
            }
            true
        }
        // Select the first item by default
        binding.navView.menu.getItem(0).isChecked = true
        replaceFragment(ManageCinemaOwner())
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
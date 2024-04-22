package com.example.navdrawer

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.view.MenuItem
import android.widget.Toast
import com.example.navdrawer.databinding.ActivityMainBinding

class MainActivity : AppCompatActivity() {
    lateinit var binding: ActivityMainBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
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
                R.id.nav_manage_screen -> {
                    // Handle Manage Screen click

                    Log.d("TAG", "Manage Screen clicked")
                }
                R.id.nav_manage_show -> {
                    // Handle Manage Shows click
                    Log.d("TAG", "Manage Shows clicked")
                }
                R.id.nav_view_booking -> {
                    // Handle View Bookings click
                    Log.d("TAG", "View Bookings clicked")
                }
                R.id.nav_menu_account -> {
                    // Handle My Account click
                    Log.d("TAG", "My Account clicked")
                }
                R.id.logoutuser -> {
                    // Handle Logout click
                    Log.d("TAG", "Logout clicked")
                }
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
}

package com.example.userbottomnavbar

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import androidx.core.content.ContextCompat
import com.google.android.material.bottomnavigation.BottomNavigationView

class MainActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        val bottomNavigationView = findViewById<BottomNavigationView>(R.id.bottom_navigation)
        val menu = bottomNavigationView.menu
        for (i in 0 until menu.size()) {
            val menuItem = menu.getItem(i)
            menuItem.background = ContextCompat.getDrawable(this, R.drawable.bottom_navigation_item_background)
        }

    }
}
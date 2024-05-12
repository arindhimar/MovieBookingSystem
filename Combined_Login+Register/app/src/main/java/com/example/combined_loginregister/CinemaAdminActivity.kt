package com.example.combined_loginregister

import android.os.Bundle
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.example.combined_loginregister.databinding.ActivityCinemaAdminBinding

class CinemaAdminActivity : AppCompatActivity() {
    lateinit var binding: ActivityCinemaAdminBinding
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        binding = ActivityCinemaAdminBinding.inflate(layoutInflater)
        setContentView(binding.root)

    }
}
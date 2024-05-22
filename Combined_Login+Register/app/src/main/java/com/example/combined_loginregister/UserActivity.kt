package com.example.combined_loginregister

import android.os.Bundle
import android.util.Log
import android.widget.ArrayAdapter
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.app.AppCompatDelegate
import com.example.combined_loginregister.databinding.ActivityUserBinding



class UserActivity : AppCompatActivity() {
    lateinit var binding: ActivityUserBinding
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        binding = ActivityUserBinding.inflate(layoutInflater)
        setContentView(binding.root)
        AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_NO);

        val cityAdapter = ArrayAdapter.createFromResource(
            this,
            R.array.city_name,
            R.layout.spinner_item
        )
        // Specify the layout to use when the list of choices appears
        cityAdapter.setDropDownViewResource(R.layout.spinner_item)

        // Apply the adapter to the spinner
        binding.LocationSelector.adapter = cityAdapter

        findShows("Surat")
    }


    private fun findShows(city: String) {
        val firebaseRestManager2 = FirebaseRestManager<CinemaTb>()
        firebaseRestManager2.getAllItems(CinemaTb::class.java, "moviedb/cinematb") { items ->
            val cinemaList = items.filter { it.city == city }
            val cinemaIds = cinemaList.map { it.cinemaID }

            val firebaseRestManager1 = FirebaseRestManager<ShowTb>()
            firebaseRestManager1.getAllItems(ShowTb::class.java, "moviedb/showtb") { shows ->
                if (shows.isNotEmpty()) {
                    for (show in shows) {
                        if (show.cinemaId in cinemaIds) {
                            Log.d("TAG", "findShows: $show")
                        }
                    }
                }
            }
        }
    }

}
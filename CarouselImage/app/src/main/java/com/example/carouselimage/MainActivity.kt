package com.example.carouselimage

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import androidx.recyclerview.widget.RecyclerView
import android.widget.ImageView
import java.util.ArrayList
import java.util.Timer
import java.util.TimerTask
import android.os.Handler

class MainActivity : AppCompatActivity() {

    private var currentIndex = 0
    private val imageUrls = ArrayList<String>()
    private lateinit var recyclerView: RecyclerView
    private lateinit var adapter: ImageAdapter
    private val handler = Handler()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        recyclerView = findViewById(R.id.recycler)

        // Add multiple images to the arrayList.
        imageUrls.add("https://imgkub.com/images/2024/04/01/image88c72a9d6bf66500.png")
        imageUrls.add("https://imgkub.com/images/2024/04/01/image597b74fe77157aae.png")
        imageUrls.add("https://imgkub.com/images/2024/04/01/image1cec867db9c206dc.png")

        adapter = ImageAdapter(this@MainActivity, imageUrls)
        recyclerView.adapter = adapter

        adapter.setOnItemClickListener(object : ImageAdapter.OnItemClickListener {
            override fun onClick(imageView: ImageView, path: String) {
                // Do something like opening the image in a new activity or showing it in full screen or something else.
            }
        })

        startAutoScroll()
    }

    private fun startAutoScroll() {
        val timer = Timer()
        val task = object : TimerTask() {
            override fun run() {
                handler.postDelayed({
                    currentIndex = (currentIndex + 1) % imageUrls.size
                    recyclerView.smoothScrollToPosition(currentIndex)
                }, 100) // Delay the initial smooth scroll operation by 100 milliseconds
            }
        }

        // Schedule the task to run every 3 seconds
        timer.schedule(task, 0, 3000)
    }


}

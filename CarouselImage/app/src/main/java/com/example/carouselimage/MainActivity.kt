package com.example.carouselimage

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import androidx.recyclerview.widget.RecyclerView
import android.widget.ImageView
import java.util.ArrayList

class MainActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        val recyclerView: RecyclerView = findViewById(R.id.recycler)
        val arrayList: ArrayList<String> = ArrayList()

        // Add multiple images to the arrayList.
        arrayList.add("https://imgkub.com/images/2024/04/01/image88c72a9d6bf66500.png")
        arrayList.add("https://imgkub.com/images/2024/04/01/image597b74fe77157aae.png")
        arrayList.add("https://imgkub.com/images/2024/04/01/image1cec867db9c206dc.png")


        val adapter = ImageAdapter(this@MainActivity, arrayList)
        recyclerView.adapter = adapter

        adapter.setOnItemClickListener(object : ImageAdapter.OnItemClickListener {
            override fun onClick(imageView: ImageView, path: String) {
                // Do something like opening the image in a new activity or showing it in full screen or something else.
            }
        })
    }
}

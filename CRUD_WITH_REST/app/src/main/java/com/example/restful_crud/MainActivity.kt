package com.example.restful_crud

import android.os.Bundle
import android.util.Log
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat

class MainActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_main)
        val firebaseRestManager = FirebaseRestManager()

        // Add item
//        val newItem = Item(name = "Item 1", description = "Description of item 1")
//        firebaseRestManager.addItem(newItem)

        //Display all items
//        firebaseRestManager.getAllItems {
//                items ->
//            Log.d("TAG", "onCreate: $items")
//        }

        //Delete item
//        firebaseRestManager.deleteItem("-NvvkmhOYRpwY0_2ZISv")


        // Update item
//        val updatedItem = Item("-Nvvr3K8qyirka88C5ZR", "Updated Item", "Updated description")
//        firebaseRestManager.updateItem("-Nvvr3K8qyirka88C5ZR", updatedItem)

        // Delete item
//        firebaseRestManager.deleteItem("Nvvr3K8qyirka88C5ZR")

    }
}
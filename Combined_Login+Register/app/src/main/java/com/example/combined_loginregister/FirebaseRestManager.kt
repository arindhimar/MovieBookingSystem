package com.example.combined_loginregister

import android.util.Log
import android.widget.Toast
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import com.google.gson.Gson
import okhttp3.*
import okhttp3.RequestBody.Companion.toRequestBody
import org.json.JSONObject
import java.io.IOException

class FirebaseRestManager<T> {
    private val client = OkHttpClient()
    private val firebaseUrl = "https://my-application-66768-default-rtdb.firebaseio.com/"

    fun addItem(item: T, dbRef: DatabaseReference, callback: (Boolean, Exception?) -> Unit) {
        val gson = Gson()
        val jsonItem = gson.toJson(item)

        dbRef.push().setValue(item)
            .addOnSuccessListener {
                println("Add Item Success")
                callback(true, null)
            }
            .addOnFailureListener { e ->
                println("Add Item Failed: $e")
                callback(false, e)
            }
    }

    fun <T> addItemWithCustomId(item: T, childId: String, dbRef: DatabaseReference, callback: (Boolean, Exception?) -> Unit) {
        val gson = Gson()
        val jsonItem = gson.toJson(item)

        // Get a reference to the child node with the specified ID
        val childRef = dbRef.child(childId)

        // Set the value of the child node with the specified ID
        childRef.setValue(item)
            .addOnSuccessListener {
                println("Add Item Success")
                callback(true, null)
            }
            .addOnFailureListener { e ->
                println("Add Item Failed: $e")
                callback(false, e)
            }
    }
    fun <T> getAllItems(itemClass: Class<T>, node: String, callback: (List<T>) -> Unit) {
        val dbRef = FirebaseDatabase.getInstance().reference.child(node)
        dbRef.addListenerForSingleValueEvent(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                if (snapshot.exists()) {
                    val items = mutableListOf<T>()
                    for (childSnapshot in snapshot.children) {
                        val item = childSnapshot.getValue(itemClass)
                        item?.let { items.add(it) }
                    }
                    callback(items)
                } else {
                    callback(emptyList())
                    Log.w("FirebaseRestManager", "Branch $node does not exist")
                }
            }

            override fun onCancelled(error: DatabaseError) {
                Log.e("FirebaseRestManager", "Error getting data: ${error.message}")
                callback(emptyList())
            }
        })
    }

    fun <T> getAllItems(itemClass: Class<T>, callback: (List<T>) -> Unit) {
        val dbRef = FirebaseDatabase.getInstance().reference.child("items")
        dbRef.addListenerForSingleValueEvent(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                if (snapshot.exists()) {
                    val items = mutableListOf<T>()
                    for (childSnapshot in snapshot.children) {
                        val item = childSnapshot.getValue(itemClass)
                        item?.let { items.add(it) }
                    }
                    callback(items)
                } else {
                    callback(emptyList())
                    Log.w("FirebaseRestManager", "Node 'items' does not exist")
                }
            }

            override fun onCancelled(error: DatabaseError) {
                Log.e("FirebaseRestManager", "Error getting data: ${error.message}")
                callback(emptyList())
            }
        })
    }
    private fun <T> String.parseItems(itemClass: Class<T>): List<T> {
        val items = mutableListOf<T>()
        if (isNullOrBlank()) {
            // Handle null or empty response
            return items
        }

        val json = JSONObject(this)
        val keys = json.keys()

        while (keys.hasNext()) {
            val key = keys.next()
            val itemJson = json.getJSONObject(key)
            val item = Gson().fromJson(itemJson.toString(), itemClass)
            items.add(item)
        }
        return items
    }






    fun <T> updateItem(dbRef: DatabaseReference, itemId: String, newItem: T, callback: (Boolean, Exception?) -> Unit) {
        val gson = Gson()
        val jsonItem = gson.toJson(newItem)

        // Get a reference to the child node with the specified ID
        val childRef = dbRef.child(itemId)

        // Set the value of the child node with the specified ID
        childRef.setValue(newItem)
            .addOnSuccessListener {
                println("Update Item Success")
                callback(true, null)
            }
            .addOnFailureListener { e ->
                println("Update Item Failed: $e")
                callback(false, e)
            }
    }


    fun deleteItem(dbRef: DatabaseReference, itemId: String, callback: (Boolean) -> Unit) {
        dbRef.child(itemId).removeValue()
            .addOnSuccessListener {
                callback(true) // Indicate deletion success
            }
            .addOnFailureListener { e ->
                e.printStackTrace()
                callback(false) // Indicate deletion failure
            }
    }

    private inline fun <reified R> String.parseItems(): List<R> {
        val items = mutableListOf<R>()
        val json = JSONObject(this)
        val keys = json.keys()

        while (keys.hasNext()) {
            val key = keys.next()
            val itemJson = json.getJSONObject(key)
            val item = Gson().fromJson(itemJson.toString(), R::class.java)
            items.add(item)
        }
        return items
    }

    fun <T> getSingleItem(itemClass: Class<T>, node: String, itemId: String, callback: (T?) -> Unit) {
        val dbRef = FirebaseDatabase.getInstance().reference.child(node).child(itemId)
        dbRef.addListenerForSingleValueEvent(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                if (snapshot.exists()) {
                    val item = snapshot.getValue(itemClass)
                    callback(item)
                } else {
                    callback(null)
                    Log.w("FirebaseRestManager", "Item $itemId does not exist in node $node")
                }
            }

            override fun onCancelled(error: DatabaseError) {
                Log.e("FirebaseRestManager", "Error getting data: ${error.message}")
                callback(null)
            }
        })
    }


    private fun <T> String.parseItem(itemClass: Class<T>): T? {
        if (isNullOrBlank()) {
            // Handle null or empty response
            return null
        }

        val json = JSONObject(this)
        val item = Gson().fromJson(json.toString(), itemClass)
        return item
    }
}

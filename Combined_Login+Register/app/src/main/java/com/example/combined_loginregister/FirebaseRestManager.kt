package com.example.combined_loginregister

import android.util.Log
import android.widget.Toast
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.DatabaseReference
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
        val request = Request.Builder()
            .url("$firebaseUrl/$node.json")
            .build()

        client.newCall(request).enqueue(object : Callback {
            override fun onFailure(call: Call, e: IOException) {
                e.printStackTrace()
            }

            override fun onResponse(call: Call, response: Response) {
                val items = response.body?.string()?.parseItems(itemClass)
                callback(items ?: emptyList())
            }
        })
    }

    fun <T> getAllItems(itemClass: Class<T>, callback: (List<T>) -> Unit) {
        val request = Request.Builder()
            .url("$firebaseUrl/items.json")
            .build()

        client.newCall(request).enqueue(object : Callback {
            override fun onFailure(call: Call, e: IOException) {
                e.printStackTrace()
            }

            override fun onResponse(call: Call, response: Response) {
                val items = response.body?.string()?.parseItems(itemClass)
                callback(items ?: emptyList())
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






    fun updateItem(itemId: String, newItem: T) {
        val gson = Gson()
        val jsonItem = gson.toJson(newItem)

        val requestBody = jsonItem.toString().toRequestBody()
        val request = Request.Builder()
            .url("$firebaseUrl/items/$itemId.json")
            .put(requestBody)
            .build()

        client.newCall(request).enqueue(object : Callback {
            override fun onFailure(call: Call, e: IOException) {
                e.printStackTrace()
            }

            override fun onResponse(call: Call, response: Response) {
                println("Update Item Response: ${response.body?.string()}")
            }
        })
    }

    fun deleteItem(itemId: String) {
        val request = Request.Builder()
            .url("$firebaseUrl/items/$itemId.json")
            .delete()
            .build()

        client.newCall(request).enqueue(object : Callback {
            override fun onFailure(call: Call, e: IOException) {
                e.printStackTrace()
            }

            override fun onResponse(call: Call, response: Response) {
                println("Delete Item Response: ${response.body?.string()}")
            }
        })
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
        val request = Request.Builder()
            .url("$firebaseUrl/$node/$itemId.json")
            .build()

        client.newCall(request).enqueue(object : Callback {
            override fun onFailure(call: Call, e: IOException) {
                e.printStackTrace()
            }

            override fun onResponse(call: Call, response: Response) {
                val item = response.body?.string()?.parseItem(itemClass)
                callback(item)
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

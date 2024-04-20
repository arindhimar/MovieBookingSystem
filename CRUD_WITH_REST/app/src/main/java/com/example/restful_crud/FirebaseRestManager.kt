package com.example.restful_crud

import okhttp3.Call
import okhttp3.Callback
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody.Companion.toRequestBody
import okhttp3.Response
import org.json.JSONObject
import java.io.IOException
import com.google.gson.Gson



class FirebaseRestManager {
    private val client = OkHttpClient()
    private val firebaseUrl = "https://my-application-66768-default-rtdb.firebaseio.com/"

    fun addItem(item: Item) {

        val gson = Gson()
        val jsonItem = gson.toJson(item)

        val requestBody = jsonItem.toRequestBody()
        val request = Request.Builder()
            .url("$firebaseUrl/items.json")
            .post(requestBody)
            .build()

        client.newCall(request).enqueue(object : Callback {
            override fun onFailure(call: Call, e: IOException) {
                e.printStackTrace()
            }

            override fun onResponse(call: Call, response: Response) {
                println("Add Item Response: ${response.body?.string()}")
            }
        })
    }

    fun getAllItems(callback: (List<Item>) -> Unit) {
        val request = Request.Builder()
            .url("$firebaseUrl/items.json")
            .build()

        client.newCall(request).enqueue(object : Callback {
            override fun onFailure(call: Call, e: IOException) {
                e.printStackTrace()
            }

            override fun onResponse(call: Call, response: Response) {
                val items = response.body?.string()?.parseItems()
                callback(items ?: emptyList())
            }
        })
    }

    fun updateItem(itemId: String, newItem: Item) {
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

    private fun String.parseItems(): List<Item> {
        val items = mutableListOf<Item>()
        val json = JSONObject(this)
        val keys = json.keys()

        while (keys.hasNext()) {
            val key = keys.next()
            val itemJson = json.getJSONObject(key)
            val item = Item(
                id = key,
                name = itemJson.getString("name"),
                description = itemJson.getString("description")
            )
            items.add(item)
        }
        return items
    }
}

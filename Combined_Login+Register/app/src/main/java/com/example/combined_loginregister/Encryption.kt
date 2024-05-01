package com.example.combined_loginregister

import android.content.Context
import android.content.SharedPreferences
import android.util.Base64
import javax.crypto.Cipher
import javax.crypto.spec.SecretKeySpec


class Encryption(private val context: Context) {
    private val key: String = "<A&]}yNO0<,0EB+!/?TPgP*VwT8B9Z8\$"
    private val secretKeySpec = SecretKeySpec(key.toByteArray(), "AES")

    fun encrypt(key:String,string: String): String {
        val cipher = Cipher.getInstance("AES/ECB/PKCS5Padding")
        cipher.init(Cipher.ENCRYPT_MODE, secretKeySpec)
        val encryptBytes = cipher.doFinal(string.toByteArray(Charsets.UTF_8))
        val encryptedData = Base64.encodeToString(encryptBytes, Base64.DEFAULT)
        val sharedPref: SharedPreferences = context.getSharedPreferences("encrypted_data", Context.MODE_PRIVATE)
        val editor = sharedPref.edit()
        editor.putString(key, encryptedData)
        editor.apply()
        return encryptedData
    }

    fun decrypt(key:String): String {
        val sharedPref: SharedPreferences = context.getSharedPreferences("encrypted_data", Context.MODE_PRIVATE)
        val encryptedData = sharedPref.getString(key, "")
        val cipher = Cipher.getInstance("AES/ECB/PKCS5Padding")
        cipher.init(Cipher.DECRYPT_MODE, secretKeySpec)
        val decryptedBytes = cipher.doFinal(Base64.decode(encryptedData, Base64.DEFAULT))
        return String(decryptedBytes, Charsets.UTF_8)
    }

    fun removeData(key:String){
        val sharedPref: SharedPreferences = context.getSharedPreferences("encrypted_data", Context.MODE_PRIVATE)
        val editor = sharedPref.edit()
        editor.remove(key)
        editor.apply()
    }

}
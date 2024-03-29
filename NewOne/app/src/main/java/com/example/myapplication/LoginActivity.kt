package com.example.myapplication

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.Toast
import com.example.myapplication.databinding.ActivityLoginBinding
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener

class LoginActivity : AppCompatActivity() {
    lateinit var binding: ActivityLoginBinding
    private lateinit var DbRef:DatabaseReference
    private lateinit var loadingDialog: LoadingDialog

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityLoginBinding.inflate(layoutInflater)
        setContentView(binding.root)

        DbRef = FirebaseDatabase.getInstance().getReference("moviedb/usertb")
        loadingDialog = LoadingDialog()
        loadingDialog.dialog(this)

        

    }

    fun OpenRegister(view: View) {
        val intent = Intent(this,RegisterActivity::class.java)
        startActivity(intent)
        finish()
    }

    fun loginUser(view: View) {
        loadingDialog.startLoadingdialog()

        //Validating for empty fields!!
        if(binding.txtemail.text.isEmpty() || binding.txtpass.text.isEmpty()) {
            loadingDialog.InvalidFieldIconShow()
            loadingDialog.startClosingWithDelay(2000)
            return // Exit the function to prevent further execution
        }

        DbRef.addListenerForSingleValueEvent(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                //checking if data exists
                if (snapshot.exists()) {
                    //traverse data
                    var loginSuccess = false
                    for (data in snapshot.children) {
                        val temp = data.getValue(UserTb::class.java)
                        if (temp != null) {
                            if (temp.uemail == binding.txtemail.text.toString() ||
                                temp.umobile == binding.txtemail.text.toString() ||
                                temp.uname == binding.txtemail.text.toString()) {
                                if (temp.upassword == binding.txtpass.text.toString()) {
                                    Log.d("TAG", "onDataChange:${temp.uid} ")
                                    loadingDialog.successIconShow()
                                    loginSuccess = true
                                    break // Exit the loop once login is successful
                                }
                            }
                        }
                    }

                    if (!loginSuccess) {
                        loadingDialog.failIconShow()
                    }
                    // Close the dialog after 2 seconds regardless of login success or failure
                    loadingDialog.startClosingWithDelay(2000)
                }
            }

            override fun onCancelled(error: DatabaseError) {
                Log.d("Login Error", "Error in Firebase query: ${error.message}")
            }
        })
    }

}
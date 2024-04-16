package com.example.fire_upload

import android.content.Intent
import android.net.Uri
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.webkit.MimeTypeMap
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import com.example.fire_upload.databinding.ActivityMainBinding
import com.google.firebase.Firebase
import com.google.firebase.database.database
import com.google.firebase.storage.storage
import com.squareup.picasso.Picasso

class MainActivity : AppCompatActivity() {
    private lateinit var binding: ActivityMainBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        binding.button.setOnClickListener {
            val intent = Intent()
            intent.action = Intent.ACTION_PICK
            intent.type = "image/*"
            imagelauncher.launch(intent)
        }
    }

    val imagelauncher =
        registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
            if (result.resultCode == RESULT_OK) {
                result.data?.data?.let { uri ->
                    val ref = Firebase.storage.reference.child(
                        "photo/${System.currentTimeMillis()}.${getFileType(uri)}"
                    )

                    ref.putFile(uri).addOnSuccessListener { taskSnapshot ->
                        taskSnapshot.storage.downloadUrl.addOnSuccessListener { downloadUri ->
                            // Save the download URL to Firebase Realtime Database
                            Firebase.database.reference.child("photo").push()
                                .setValue(downloadUri.toString())

                            // Display the uploaded image using Picasso
                            Picasso.get().load(downloadUri).into(binding.imageView)

                            Toast.makeText(this, "Upload photo", Toast.LENGTH_SHORT).show()
                        }.addOnFailureListener { exception ->
                            // Handle any errors that may occur while getting the download URL
                            Toast.makeText(this, "Failed to retrieve download URL: $exception", Toast.LENGTH_SHORT).show()
                        }
                    }.addOnFailureListener { exception ->
                        // Handle any errors that may occur while uploading the image
                        Toast.makeText(this, "Failed to upload image: $exception", Toast.LENGTH_SHORT).show()
                    }
                }
            }
        }

    private fun getFileType(data: Uri): String {
        val mimeTypeMap = MimeTypeMap.getSingleton()
        return mimeTypeMap.getExtensionFromMimeType(contentResolver.getType(data)) ?: ""
    }
}

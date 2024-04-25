package com.example.combined_loginregister

import android.app.Activity
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.Toast

private const val PICK_IMAGES_REQUEST = 1

class ManageMovies : Fragment() {

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.fragment_manage_movies, container, false)

        val pickImagesButton: Button = view.findViewById(R.id.pickImagesButton)
        pickImagesButton.setOnClickListener {
            openGallery()
        }

        return view
    }

    private fun openGallery() {
        val intent = Intent(Intent.ACTION_GET_CONTENT)
        intent.type = "image/*"
        intent.putExtra(Intent.EXTRA_ALLOW_MULTIPLE, true)
        startActivityForResult(intent, PICK_IMAGES_REQUEST)
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == PICK_IMAGES_REQUEST && resultCode == Activity.RESULT_OK) {
            if (data != null) {
                val clipData = data.clipData
                if (clipData != null) {
                    for (i in 0 until clipData.itemCount) {
                        val uri = clipData.getItemAt(i).uri
                        // Here you can handle each selected image URI, such as uploading it to the database
                        uploadImageToFirebaseStorage(uri)
                    }
                } else {
                    val uri = data.data
                    // Handle single selected image URI
                    uploadImageToFirebaseStorage(uri)
                }
            }
        }
    }

    private fun uploadImageToFirebaseStorage(imageUri: Uri?) {
        FirebaseStorageHelper.uploadImage(
            imageUri,
            onSuccess = { downloadUrl ->
                // Image uploaded successfully
                Toast.makeText(context, "Image uploaded successfully", Toast.LENGTH_SHORT).show()
                // You can do further processing with the downloadUrl here
            },
            onFailure = { errorMessage ->
                // Handle unsuccessful uploads
                Toast.makeText(context, "Image upload failed: $errorMessage", Toast.LENGTH_SHORT).show()
            }
        )
    }
}

package com.example.combined_loginregister
import android.net.Uri
import com.google.android.gms.tasks.OnFailureListener
import com.google.android.gms.tasks.OnSuccessListener
import com.google.firebase.storage.FirebaseStorage
import com.google.firebase.storage.StorageReference

object FirebaseStorageHelper {

    private const val STORAGE_PATH = "images/"

    fun uploadImage(
        imageUri: Uri?,
        onSuccess: (String) -> Unit,
        onFailure: (String) -> Unit
    ) {
        if (imageUri != null) {
            val storage = FirebaseStorage.getInstance()
            val storageReference = storage.reference.child(STORAGE_PATH + System.currentTimeMillis() + ".jpg")
            val uploadTask = storageReference.putFile(imageUri)

            uploadTask.addOnSuccessListener(OnSuccessListener {
                // File uploaded successfully
                storageReference.downloadUrl.addOnSuccessListener { uri ->
                    // Get the download URL of the uploaded file
                    val downloadUrl = uri.toString()
                    onSuccess(downloadUrl)
                }.addOnFailureListener {
                    // Failed to get download URL
                    onFailure("Failed to get download URL")
                }
            }).addOnFailureListener(OnFailureListener { exception ->
                // Handle unsuccessful uploads
                onFailure(exception.message ?: "Unknown error")
            })
        } else {
            // Invalid URI
            onFailure("Invalid image URI")
        }
    }

    fun getImageUri(
        imageName: String,
        onSuccess: (Uri) -> Unit,
        onFailure: () -> Unit
    ) {
        val storageReference = FirebaseStorage.getInstance().reference.child(STORAGE_PATH + imageName)
        storageReference.downloadUrl.addOnSuccessListener { uri ->
            onSuccess(uri)
        }.addOnFailureListener {
            onFailure()
        }
    }
}

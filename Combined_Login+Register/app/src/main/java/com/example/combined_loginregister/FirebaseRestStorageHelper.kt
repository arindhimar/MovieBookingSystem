package com.example.combined_loginregister
import android.net.Uri
import com.google.firebase.storage.FirebaseStorage
import com.google.firebase.storage.StorageReference

object FirebaseStorageHelper {

    private const val STORAGE_PATH = "images/"

    fun uploadImage(
        imageUri: Uri?,
        folderName: String,
        onSuccess: (String) -> Unit,
        onFailure: (String) -> Unit
    ) {
        if (imageUri != null) {
            val storage = FirebaseStorage.getInstance()
            val storageReference = storage.reference.child(STORAGE_PATH + folderName + "/" + System.currentTimeMillis() + ".jpg")
            val uploadTask = storageReference.putFile(imageUri)

            uploadTask.addOnSuccessListener { taskSnapshot ->
                // File uploaded successfully
                storageReference.downloadUrl.addOnSuccessListener { uri ->
                    // Get the download URL of the uploaded file
                    val downloadUrl = uri.toString()
                    onSuccess(downloadUrl)
                }.addOnFailureListener {
                    // Failed to get download URL
                    onFailure("Failed to get download URL")
                }
            }.addOnFailureListener { exception ->
                // Handle unsuccessful uploads
                onFailure(exception.message ?: "Unknown error")
            }
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

    fun getAllImagesInFolder(folderPath: String, onComplete: (List<String>) -> Unit) {
        val storageReference = FirebaseStorage.getInstance().reference.child(folderPath)

        storageReference.listAll().addOnSuccessListener { result ->
            val imageUrls = mutableListOf<String>()

            result.items.forEach { item ->
                item.downloadUrl.addOnSuccessListener { uri ->
                    imageUrls.add(uri.toString())
                }.addOnFailureListener { exception ->
                    // Handle any errors that may occur while retrieving the URL
                    // For example, log the error or show an error message
                    println("Failed to retrieve download URL: $exception")
                }
            }

            onComplete(imageUrls)
        }.addOnFailureListener { exception ->
            // Handle any errors that may occur while listing the items
            // For example, log the error or show an error message
            println("Failed to list items: $exception")
            onComplete(emptyList()) // Return an empty list if there's an error
        }
    }
}

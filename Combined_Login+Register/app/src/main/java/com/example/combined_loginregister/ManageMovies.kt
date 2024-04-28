package com.example.combined_loginregister

import android.annotation.SuppressLint
import android.app.Activity
import android.app.AlertDialog
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import com.bumptech.glide.Glide
import com.example.combined_loginregister.databinding.FragmentManageCinemaownerBinding
import com.example.combined_loginregister.databinding.FragmentManageMoviesBinding
import com.google.android.material.textfield.TextInputLayout
import com.google.firebase.database.FirebaseDatabase
import org.w3c.dom.Text

private const val PICK_IMAGES_REQUEST = 1

class ManageMovies : Fragment() {

    lateinit var binding: FragmentManageMoviesBinding
    lateinit var dialogView: View
    lateinit var alertDialog: AlertDialog
    private var selectedUris: MutableList<Uri>? = null

    @SuppressLint("InflateParams")
    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = FragmentManageMoviesBinding.inflate(layoutInflater, container, false)





        binding.AddMovieBtn.setOnClickListener {
            // Inflate the custom layout
            dialogView = layoutInflater.inflate(R.layout.add_update_movie_dialog, null)

            // Create the dialog
            val dialogBuilder = AlertDialog.Builder(requireContext())
            dialogBuilder.setView(dialogView)

            // Set custom window animations
            alertDialog = dialogBuilder.create()
            alertDialog.window?.attributes?.windowAnimations = R.style.DialogAnimation

            val uploadImagesBtn = dialogView.findViewById<Button>(R.id.uploadImagesBtn)
            val AddMovieBtnFinal = dialogView.findViewById<Button>(R.id.AddMovieBtnFinal)
            val textInputLayout1: TextInputLayout = dialogView.findViewById(R.id.textInputLayout1)
            val textInputLayout2: TextInputLayout = dialogView.findViewById(R.id.textInputLayout2)

            uploadImagesBtn.setOnClickListener {
                val movieName = textInputLayout1.editText?.text.toString()
                val movieDuration = textInputLayout2.editText?.text.toString()

                if (movieName.isEmpty()) {
                    // Display error for empty movie name
                    textInputLayout1.error = "Please enter a movie name"

                } else {
                    // Clear any previous error
                    textInputLayout1.error = null
                }

                if (movieDuration.isEmpty()) {
                    // Display error for empty movie duration
                    textInputLayout2.error = "Please enter movie duration in minutes"
                } else {
                    // Clear any previous error
                    textInputLayout2.error = null
                }

                // Open gallery only if both fields are not empty
                openGallery()
            }

            AddMovieBtnFinal.setOnClickListener {
                // Perform action when Add Movie button is clicked
                val movieName = textInputLayout1.editText?.text.toString()
                val movieDuration = textInputLayout2.editText?.text.toString()

                if (movieName.isEmpty() || movieDuration.isEmpty() || selectedUris == null) {
                    Toast.makeText(
                        requireContext(),
                        "Make sure to add movie name, movie duration, and at least 1 movie poster!",
                        Toast.LENGTH_SHORT
                    ).show()
                } else {
                    uploadImageToFirebaseStorage(selectedUris)
                    displayMovies()

                }
            }

            // Show the dialog
            alertDialog.show()
        }

        displayMovies()

        return binding.root
    }

    private fun openGallery() {
        val intent = Intent(Intent.ACTION_GET_CONTENT)
        intent.type = "image/*"
        intent.putExtra(Intent.EXTRA_ALLOW_MULTIPLE, true)
        startActivityForResult(intent, PICK_IMAGES_REQUEST)
    }

    private fun displayMovies() {
        val loadingScreen = LoadingDialogHelper()
        loadingScreen.showLoadingDialog(requireContext())

        //get data of all the move in movietb
        val movieClass = MovieTB::class.java
        val node = "moviedb/movietb"

        val firebaseRestManager = FirebaseRestManager<MoviePosterTb>()
        firebaseRestManager.getAllItems(movieClass, node) { items ->
            // Run UI-related code on the main thread
            requireActivity().runOnUiThread {
                if (items.isNotEmpty()) {
                    // Create a counter to track the number of items processed
                    var itemsProcessed = 0

                    // Iterate over the list of MoviePosterTb objects
                    for (item in items) {
                        // Access the properties of each MoviePosterTb object
                        val movieid = item.mid

                        val movielistcard = layoutInflater.inflate(R.layout.listmoviecard, null, false)

                        val MainPoster: ImageView = movielistcard.findViewById(R.id.MainPoster)
                        val textView: TextView = movielistcard.findViewById(R.id.textView)

                        textView.text = "Name : ${item.mname}\nDuration : ${item.duration}"

                        //getting all the poster of all the movies
                        val moviePosterClass = MoviePosterTb::class.java
                        val node = "moviedb/moviepostertb"

                        val firebaseRestManager2 = FirebaseRestManager<MoviePosterTb>()
                        firebaseRestManager2.getAllItems(moviePosterClass, node) { posterItems ->
                            if (posterItems.isNotEmpty()) {
                                // Iterate over the list of MoviePosterTb objects
                                for (item2 in posterItems) {
                                    // Access the properties of each MoviePosterTb object
                                    if (item2.mid == item.mid) {
                                        // Update ImageView on the main thread
                                        requireActivity().runOnUiThread {
                                            Glide.with(requireContext())
                                                .load(item2.mlink)
                                                .into(MainPoster)
                                            Log.d("TAG", "onCreateView: ${item2.mlink}")

                                            // Increment the counter
                                            itemsProcessed++

                                            // If all items are processed, dismiss the loading dialog
                                            if (itemsProcessed == items.size) {
                                                loadingScreen.dismissLoadingDialog()
                                            }
                                        }
                                        break
                                    }
                                }
                            } else {
                                Log.d("Firebase", "No movie posters found")
                            }
                        }
                        binding.CardsHere.addView(movielistcard)
                    }
                } else {
                    Log.d("Firebase", "No movie posters found")
                    // Dismiss the loading dialog if no items are found
                    loadingScreen.dismissLoadingDialog()
                }
            }
        }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == PICK_IMAGES_REQUEST && resultCode == Activity.RESULT_OK) {
            if (data != null) {
                if (selectedUris == null) {
                    selectedUris = mutableListOf()
                }
                val clipData = data.clipData
                val textView: TextView = dialogView.findViewById(R.id.textView)
                textView.text =
                    "Poster uploaded : ${clipData!!.itemCount}\nThe first image uploaded will be\nshown to users as the main poster**"
                if (clipData != null && clipData.itemCount > 0) {
                    for (i in 0 until clipData.itemCount) {
                        val uri = clipData.getItemAt(i).uri
                        selectedUris!!.add(uri)
                    }
                } else if (data.data != null) {
                    val uri = data.data
                    if (uri != null) {
                        selectedUris!!.add(uri)
                    }
                } else {
                    Toast.makeText(requireContext(), "No images selected", Toast.LENGTH_SHORT)
                        .show()
                }
            }
        }
    }


    private fun uploadImageToFirebaseStorage(imageUri: MutableList<Uri>?) {
        val textInputLayout1: TextInputLayout = dialogView.findViewById(R.id.textInputLayout1)
        val textInputLayout2: TextInputLayout = dialogView.findViewById(R.id.textInputLayout2)

        val firebaseDatabase = FirebaseDatabase.getInstance()
        val firebaseRestManager = FirebaseRestManager<MovieTB>()
        val dbRef = FirebaseDatabase.getInstance().getReference("moviedb/movietb")
        val movieId = dbRef.push().key
        // Create a UserTb object with the obtained user ID
        val tempMovie = MovieTB(movieId, textInputLayout1.editText!!.text.toString(), textInputLayout2.editText!!.text.toString())
        firebaseRestManager.addItem(tempMovie, dbRef) { success, error ->
            if (success) {
                // Movie added successfully, now upload the image
                for (i in 0 until selectedUris!!.size) {
                    FirebaseStorageHelper.uploadImage(
                        selectedUris!![i],
                        movieId!!, // pass the movie ID instead of the movie title
                        onSuccess = { downloadUrl ->
                            // Image uploaded successfully
                            Toast.makeText(
                                context,
                                "Image uploaded successfully",
                                Toast.LENGTH_SHORT
                            ).show()

                            Log.d("TAG", "uploadImageToFirebaseStorage: $downloadUrl")

                            // add the movie poster to the database
                            val firebaseRestManager2 = FirebaseRestManager<MoviePosterTb>()
                            val dbRef2 =
                                FirebaseDatabase.getInstance().getReference("moviedb/moviepostertb")
                            val moviePosterId = dbRef2.push().key
                            val tempMoviePoster = MoviePosterTb(moviePosterId, downloadUrl, movieId)
                            firebaseRestManager2.addItem(
                                tempMoviePoster,
                                dbRef2
                            ) { success2, error2 ->
                                if (success2) {
                                    // Movie poster added successfully
                                    Toast.makeText(
                                        context,
                                        "Movie poster added successfully",
                                        Toast.LENGTH_SHORT
                                    ).show()
                                } else {
                                    // Handle error adding movie poster to database
                                    Toast.makeText(
                                        context,
                                        "Error adding movie poster to database: $error2",
                                        Toast.LENGTH_SHORT
                                    ).show()
                                }
                            }

                        },
                        onFailure = { errorMessage ->
                            // Handle unsuccessful uploads
                            Toast.makeText(
                                context,
                                "Image upload failed: $errorMessage",
                                Toast.LENGTH_SHORT
                            ).show()
                        }
                    )
                }
            } else {
                // Handle error adding movie to database
                Toast.makeText(
                    context,
                    "Error adding movie to database: $error",
                    Toast.LENGTH_SHORT
                ).show()
            }
        }
    }
}
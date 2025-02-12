package com.example.modulesubmission.ui.gallery

import android.app.Activity
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.LinearLayoutManager
import com.bumptech.glide.Glide
import com.example.modulesubmission.FirebaseRestManager
import com.example.modulesubmission.FirebaseStorageHelper
import com.example.modulesubmission.R
import com.example.modulesubmission.databinding.FragmentGalleryBinding
import com.google.android.material.textfield.TextInputLayout
import com.google.firebase.database.FirebaseDatabase

private const val PICK_IMAGES_REQUEST = 1

class GalleryFragment : Fragment() {

    private var _binding: FragmentGalleryBinding? = null

    // This property is only valid between onCreateView and
    // onDestroyView.
    private val binding get() = _binding!!
    private var selectedUris: MutableList<Uri>? = null

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        val galleryViewModel =
            ViewModelProvider(this)[GalleryViewModel::class.java]

        _binding = FragmentGalleryBinding.inflate(inflater, container, false)
        val root: View = binding.root
        displayMovies()


        binding.btnAddMovie.setOnClickListener{
            openGallery()
        }

        return root
    }

    private fun openGallery() {
        val intent = Intent(Intent.ACTION_GET_CONTENT)
        intent.type = "image/*"
        intent.putExtra(Intent.EXTRA_ALLOW_MULTIPLE, true)
        startActivityForResult(intent, PICK_IMAGES_REQUEST)
    }

    private fun displayMovies() {

        requireActivity().runOnUiThread {

        binding.MovieCardsHere.removeAllViews()
    }
        //get data of all the move in movietb
        val movieClass = MovieTb::class.java
        val node = "arindb/movietb"

        val firebaseRestManager = FirebaseRestManager<MoviePosterTb>()
        firebaseRestManager.getAllItems(movieClass, node) { items ->
            requireActivity().runOnUiThread {
                if (items.isNotEmpty()) {
                    for(item in items){
                        val card = layoutInflater.inflate(R.layout.display_card,binding.MovieCardsHere,false)

                        val img = card.findViewById<ImageView>(R.id.image)
                        val txt = card.findViewById<TextView>(R.id.text)

                        val firebaseRestManager2 = FirebaseRestManager<MoviePosterTb>()
                        firebaseRestManager2.getAllItems(MoviePosterTb::class.java, "arindb/moviepostertb") { posterItems ->
                            if (posterItems.isNotEmpty()) {
                                for (item2 in posterItems) {
                                    Log.d("TAG", "displayMovies: $item2")
                                    if (item.movieId == item2.movieId) {
                                        Glide.with(requireContext())
                                            .load(item2.link)
                                            .into(img)
                                        txt.text = "Name : ${item.movieName}"
                                        break
                                    }
                                }
                            } else {
                                Log.d("Firebase", "No movie posters found")
                            }
                        }

                        binding.MovieCardsHere.addView(card)
                    }
                } else {
                    Toast.makeText(requireContext(), "No movies found", Toast.LENGTH_SHORT).show()
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

                if (clipData != null && clipData.itemCount > 0) {
                    for (i in 0 until clipData.itemCount) {
                        val uri = clipData.getItemAt(i).uri
                        selectedUris!!.add(uri)
                    }
                    uploadImageToFirebaseStorage(selectedUris)
                } else if (data.data != null) {
                    val uri = data.data
                    if (uri != null) {
                        selectedUris!!.add(uri)
                    }
                    uploadImageToFirebaseStorage(selectedUris)

                } else {
                    Toast.makeText(requireContext(), "No images selected", Toast.LENGTH_SHORT)
                        .show()
                }
            }
        }
    }



    private fun uploadImageToFirebaseStorage(imageUris: List<Uri>?) {
        val dbRef = FirebaseDatabase.getInstance().getReference("arindb/movietb")
        val movieId = dbRef.push().key
        if (movieId == null) {
            // Handle the case where movieId is null
            Toast.makeText(requireContext(), "Error creating movie", Toast.LENGTH_SHORT).show()
            return
        }
        val firebaseRestManager = FirebaseRestManager<MovieTb>()
        val tempMovie = MovieTb(movieId, "Moviename")

        firebaseRestManager.addItem(tempMovie, dbRef) { success, error ->
            if (success) {
                if (imageUris != null) {
                    for (uri in imageUris) {
                        FirebaseStorageHelper.uploadImage(
                            uri,
                            movieId,
                            onSuccess = { downloadUrl ->
                                val dbRef2 = FirebaseDatabase.getInstance().getReference("arindb/moviepostertb")
                                val moviePosterId = dbRef2.push().key
                                if (moviePosterId != null) {
                                    val tempMoviePoster = MoviePosterTb(moviePosterId, movieId, downloadUrl)
                                    val firebaseRestManager2 = FirebaseRestManager<MoviePosterTb>()
                                    firebaseRestManager2.addItem(tempMoviePoster, dbRef2) { success2, error2 ->
                                        if (success2) {
                                            // Movie poster added successfully
                                            Toast.makeText(context, "Movie poster added successfully", Toast.LENGTH_SHORT).show()
                                            displayMovies()

                                        } else {
                                            // Handle error adding movie poster to database
                                            Toast.makeText(context, "Error adding movie poster to database: $error2", Toast.LENGTH_SHORT).show()
                                        }
                                    }
                                } else {
                                    // Handle the case where moviePosterId is null
                                    Toast.makeText(context, "Error creating movie poster", Toast.LENGTH_SHORT).show()
                                }
                            },
                            onFailure = { errorMessage ->
                                // Handle unsuccessful uploads
                                Toast.makeText(context, "Image upload failed: $errorMessage", Toast.LENGTH_SHORT).show()
                            }
                        )
                    }
                } else {
                    // Handle the case where imageUris is null
                    Toast.makeText(context, "No images selected", Toast.LENGTH_SHORT).show()
                }
            } else {
                // Handle error adding movie to database
                Toast.makeText(context, "Error adding movie to database: $error", Toast.LENGTH_SHORT).show()
            }
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

}
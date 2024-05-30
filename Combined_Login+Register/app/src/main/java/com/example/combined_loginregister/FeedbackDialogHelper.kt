package com.example.combined_loginregister

import android.app.AlertDialog
import android.content.Context
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.widget.Button
import android.widget.ImageView
import android.widget.RatingBar
import android.widget.TextView
import com.bumptech.glide.Glide

class FeedbackDialogHelper(private val showId: String) {

    private lateinit var dialog: AlertDialog
    private lateinit var view: View

    // Declare views
    private lateinit var ratingImage: ImageView
    private lateinit var movieImage: ImageView
    private lateinit var cinemaImage: ImageView
    private lateinit var cinemaRatingBar: RatingBar
    private lateinit var movieRatingBar: RatingBar
    private lateinit var ratingNowBtn: Button

    fun showFeedbackDialog(context: Context) {
        val inflater = LayoutInflater.from(context)
        view = inflater.inflate(R.layout.feedback_layout, null)

        val builder = AlertDialog.Builder(context)
        builder.setView(view)
        builder.setCancelable(true)

        dialog = builder.create()
        dialog.window?.attributes?.windowAnimations = R.style.DialogAnimation
        dialog.show()

        // Initialize views
        ratingImage = view.findViewById(R.id.ratingImage)
        movieImage = view.findViewById(R.id.movieImage)
        cinemaImage = view.findViewById(R.id.cinemaImage)
        cinemaRatingBar = view.findViewById(R.id.cinemaRatingBar)
        movieRatingBar = view.findViewById(R.id.movieRatingBar)
        ratingNowBtn = view.findViewById(R.id.ratingNowBtn)

        setupCard()
        setupRatingListeners()
    }

    private fun setupCard() {
        val firebaseRestManager = FirebaseRestManager<ShowTb>()
        firebaseRestManager.getSingleItem(
            ShowTb::class.java,
            "moviedb/showtb",
            showId
        ) { showData ->
            if (showData != null) {
                val firebaseRestManager1 = FirebaseRestManager<CinemaTb>()
                // Getting all the posters of all the movies
                val moviePosterClass = MoviePosterTb::class.java
                val node = "moviedb/moviepostertb"

                val firebaseRestManager2 = FirebaseRestManager<MoviePosterTb>()
                firebaseRestManager2.getAllItems(moviePosterClass, node) { posterItems ->
                    if (posterItems.isNotEmpty()) {
                        for (item2 in posterItems) {
                            if (item2.mid == showData.movieId) {
                                Glide.with(view.context)
                                    .load(item2.mlink)
                                    .into(movieImage)
                                break
                            }
                        }
                    } else {
                        Log.d("Firebase", "No movie posters found")
                    }
                }

                val firebaseRestManager3 = FirebaseRestManager<CinemaTb>()
                firebaseRestManager3.getSingleItem(
                    CinemaTb::class.java,
                    "moviedb/cinematb",
                    showData.cinemaId
                ) { cinemaData ->
                    if (cinemaData != null) {
                        Glide.with(view.context)
                            .load(cinemaData.cinemaPicture)
                            .into(cinemaImage)
                    }
                }
            }
        }
    }

    private fun setupRatingListeners() {
        cinemaRatingBar.setOnRatingBarChangeListener { _, _, _ -> updateRatingImage() }
        movieRatingBar.setOnRatingBarChangeListener { _, _, _ -> updateRatingImage() }
    }

    private fun updateRatingImage() {
        val cinemaRating = cinemaRatingBar.rating
        val movieRating = movieRatingBar.rating

        val totalRating = (cinemaRating + movieRating) / 2

        val ratingImageResource = when {
            totalRating >= 4.5 -> R.drawable.five_star
            totalRating >= 3.5 -> R.drawable.four_star
            totalRating >= 2.5 -> R.drawable.three_star
            totalRating >= 1.5 -> R.drawable.two_star
            else -> R.drawable.one_star
        }

        ratingImage.setImageResource(ratingImageResource)
    }


    fun returnView(): View {
        return view
    }

    fun dismissFeedbackDialog() {
        if (dialog.isShowing) {
            dialog.dismiss()
        }
    }
}
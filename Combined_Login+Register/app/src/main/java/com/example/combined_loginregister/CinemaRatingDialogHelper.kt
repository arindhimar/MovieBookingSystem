package com.example.combined_loginregister

import android.app.AlertDialog
import android.content.Context
import android.view.LayoutInflater
import android.widget.ImageView
import android.widget.TextView
import androidx.appcompat.widget.AppCompatRatingBar
import com.bumptech.glide.Glide

class CinemaRatingDialogHelper(val context: Context, private val cinemaData: CinemaTb) {
    private var dialog: AlertDialog? = null
    private val loadingDialogHelper = LoadingDialogHelper()

    fun showCinemaRatingDialog() {
        loadingDialogHelper.showLoadingDialog(context)
        fetchFeedbackData()
    }

    private fun fetchFeedbackData() {
        val feedbackFirebaseRestManager = FirebaseRestManager<FeedbackTb>()
        val showFirebaseRestManager = FirebaseRestManager<ShowTb>()

        showFirebaseRestManager.getAllItems(ShowTb::class.java, "moviedb/showtb") { showData ->
            val cinemaShows = showData.filter { it.cinemaId == cinemaData.cinemaID }

            if (cinemaShows.isEmpty()) {
                // No shows found for the cinema, display the dialog with no rating
                displayRatingDialog(0f)
                return@getAllItems
            }

            val feedbackList = mutableListOf<FeedbackTb>()
            var remainingRequests = cinemaShows.size

            cinemaShows.forEach { show ->
                feedbackFirebaseRestManager.getAllItems(FeedbackTb::class.java, "moviedb/feedbacktb") { feedbackData ->
                    feedbackList.addAll(feedbackData.filter { it.showId == show.showId })
                    remainingRequests--

                    if (remainingRequests == 0) {
                        // All feedback data has been fetched
                        val overallRating = feedbackList.mapNotNull { it.cinemaRating?.toFloat() }
                        val averageRating = if (overallRating.isNotEmpty()) overallRating.average().toFloat() else 0f
                        displayRatingDialog(averageRating)
                    }
                }
            }
        }
    }

    private fun displayRatingDialog(averageRating: Float) {
        loadingDialogHelper.dismissLoadingDialog()

        val inflater = LayoutInflater.from(context)
        val view = inflater.inflate(R.layout.cinema_rating, null)

        // Find and update the views in the dialog layout
        val ratingBar: AppCompatRatingBar = view.findViewById(R.id.cinemaRatingBar)
        val ratingText: TextView = view.findViewById(R.id.cinemaRatingText)
        val ratingImage: ImageView = view.findViewById(R.id.ratingImage)
        val cinemaImage: ImageView = view.findViewById(R.id.cinemaImage)

        // Update the rating bar
        ratingBar.rating = averageRating

        // Change the rating image based on the rating value
        val ratingImageResource = when {
            averageRating >= 4.5 -> R.drawable.five_star
            averageRating >= 3.5 -> R.drawable.four_star
            averageRating >= 2.5 -> R.drawable.three_star
            averageRating >= 1.5 -> R.drawable.two_star
            else -> R.drawable.one_star
        }
        ratingImage.setImageResource(ratingImageResource)

        // Update the text based on the average rating
        val ratingMessage = when {
            averageRating >= 4.5 -> "Excellent"
            averageRating >= 3.5 -> "Good"
            averageRating >= 2.5 -> "Average"
            else -> "Poor"
        }
        ratingText.text = "Overall Rating: $averageRating ($ratingMessage)"

        // Load cinema image using Glide
        Glide.with(context)
            .load(cinemaData.cinemaPicture)
            .into(cinemaImage)

        val builder = AlertDialog.Builder(context)
        builder.setView(view)
        builder.setCancelable(true)

        dialog = builder.create()
        dialog?.show()
    }

    fun dismissRatingDialog() {
        dialog?.let {
            if (it.isShowing) {
                it.dismiss()
            }
        }
    }
}

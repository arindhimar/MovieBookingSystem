package com.example.combined_loginregister

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.appcompat.widget.AppCompatRatingBar
import com.bumptech.glide.Glide
import com.example.combined_loginregister.databinding.FragmentCinemaAdminViewCinemaRatingBinding
import com.google.firebase.auth.FirebaseAuth

private const val ARG_PARAM1 = "param1"
private const val ARG_PARAM2 = "param2"

class CinemaAdminViewCinemaRating : Fragment() {
    private var param1: String? = null
    private var param2: String? = null
    lateinit var binding: FragmentCinemaAdminViewCinemaRatingBinding
    private lateinit var cinemaRatingCard: View
    private val loadingDialogHelper = LoadingDialogHelper()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        arguments?.let {
            param1 = it.getString(ARG_PARAM1)
            param2 = it.getString(ARG_PARAM2)
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = FragmentCinemaAdminViewCinemaRatingBinding.inflate(inflater, container, false)

        // Inflate the cinema rating card layout
        cinemaRatingCard = inflater.inflate(R.layout.cinema_rating, binding.ratingCardContainer, false)

        // Add the inflated layout to the rating card container
        binding.ratingCardContainer.addView(cinemaRatingCard)

        loadCinemaData()
        return binding.root
    }

    private fun loadCinemaData() {
        loadingDialogHelper.showLoadingDialog(requireContext())

        val firebaseRestManagerCinemaAdmin = FirebaseRestManager<CinemaAdminTb>()
        val firebaseRestManagerShow = FirebaseRestManager<ShowTb>()
        val firebaseRestManagerFeedback = FirebaseRestManager<FeedbackTb>()

        val currentUserId = FirebaseAuth.getInstance().currentUser?.uid

        firebaseRestManagerCinemaAdmin.getAllItems(CinemaAdminTb::class.java, "moviedb/cinemaadmintb") { cinemaAdminList ->
            val currentAdmin = cinemaAdminList.find { it.userId == currentUserId }

            currentAdmin?.let { admin ->
                firebaseRestManagerShow.getAllItems(ShowTb::class.java, "moviedb/showtb") { showList ->
                    val showsForCurrentCinema = showList.filter { it.cinemaAdminId == admin.cinemaadminid }

                    // Get the cinema ID
                    val ownerId = admin.cinemaOwnerId

                    val firebaseRestManagerCo = FirebaseRestManager<CinemaOwnerTb>()
                    firebaseRestManagerCo.getSingleItem(CinemaOwnerTb::class.java, "moviedb/CinemaOwnerTb", ownerId!!) { cinemaOwner ->
                        val currentCinemaId = cinemaOwner!!.cinemaId
                        val showIds = showsForCurrentCinema.map { it.showId }
                        firebaseRestManagerFeedback.getAllItems(
                            FeedbackTb::class.java,
                            "moviedb/feedbacktb"
                        ) { feedbackList ->
                            val feedbackForCurrentCinema = feedbackList.filter { feedback ->
                                feedback.showId in showIds && !feedback.cinemaRating.isNullOrEmpty()
                            }

                            // Calculate the overall rating
                            val overallRating =
                                feedbackForCurrentCinema.mapNotNull { it.cinemaRating?.toFloat() }
                            val averageRating =
                                if (overallRating.isNotEmpty()) overallRating.average()
                                    .toFloat() else 0f

                            // Update the UI with the overall rating and message, along with cinema ID
                            updateRatingUI(averageRating, currentCinemaId)
                            loadingDialogHelper.dismissLoadingDialog()

                        }
                    }


                }
            }
        }
    }

    private fun updateRatingUI(averageRating: Float, currentCinemaId: String?) {
        // Find the views in the cinema rating card layout
        val ratingBar: AppCompatRatingBar = cinemaRatingCard.findViewById(R.id.cinemaRatingBar)
        val ratingText: TextView = cinemaRatingCard.findViewById(R.id.cinemaRatingText)
        val ratingImage: ImageView = cinemaRatingCard.findViewById(R.id.ratingImage)
        val cinemaImage: ImageView = cinemaRatingCard.findViewById(R.id.cinemaImage)

        // Update the rating bar
        ratingBar.rating = averageRating

        // Change the rating image based on the rating value (example logic)
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
        val firebaseRestManagerCinema = FirebaseRestManager<CinemaTb>()
        firebaseRestManagerCinema.getSingleItem(CinemaTb::class.java, "moviedb/cinematb", currentCinemaId!!) { cinema ->
            if(cinema!=null) {

                Glide.with(requireContext())
                    .load(cinema.cinemaPicture)
                    .into(cinemaImage)
            }

        }
    }

    companion object {
        @JvmStatic
        fun newInstance(param1: String, param2: String) =
            CinemaAdminViewCinemaRating().apply {
                arguments = Bundle().apply {
                    putString(ARG_PARAM1, param1)
                    putString(ARG_PARAM2, param2)
                }
            }
    }
}

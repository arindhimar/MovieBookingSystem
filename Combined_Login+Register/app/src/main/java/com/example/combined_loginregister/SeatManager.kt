package com.example.combined_loginregister

import android.app.AlertDialog
import android.app.Dialog
import android.content.Context
import android.content.Intent
import android.graphics.Typeface
import android.os.Build
import android.os.Handler
import android.text.Spannable
import android.text.SpannableString
import android.text.style.StyleSpan
import android.util.Log
import android.view.Gravity
import android.view.LayoutInflater
import android.view.View
import android.widget.GridLayout
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.TextView
import androidx.annotation.RequiresApi
import androidx.core.view.isVisible
import com.bumptech.glide.Glide
import com.google.android.material.bottomsheet.BottomSheetDialog
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import java.util.TimeZone
import java.util.UUID

class SeatManager(
    private val show: ShowTb,
    private val requireContext: Context
) {
    private val selectedSeats = mutableSetOf<String>()
    private lateinit var dialog: Dialog
    private lateinit var view: View
    private lateinit var bottomSheetDialog: BottomSheetDialog

    @RequiresApi(Build.VERSION_CODES.O)
    fun showLoadingDialog(context: Context) {
        // Inflate the cinema seat layout
        view = LayoutInflater.from(context).inflate(R.layout.cinema_seat_layout, null)

        // Create the dialog with full screen theme
        dialog = Dialog(context, android.R.style.Theme_Black_NoTitleBar_Fullscreen)
        dialog.setContentView(view)
        dialog.setCancelable(true)
        dialog.show()

        setUpCard()
        realTimeSetUp()
    }

    fun returnView(): View {
        return view
    }

    fun dismissLoadingDialog() {
        if (::dialog.isInitialized && dialog.isShowing) {
            dialog.dismiss()
        }
    }

    private fun realTimeSetUp() {
        val bookingRef = FirebaseDatabase.getInstance().getReference("moviedb/bookingtb")
        val bookingListener = object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                setUpCard()
            }

            override fun onCancelled(error: DatabaseError) {
                Log.e("SeatManager", "Error: ${error.message}")
            }
        }
        bookingRef.addValueEventListener(bookingListener)
    }

    private fun setUpCard() {
        val firebaseRestManager = FirebaseRestManager<CinemaTb>()
        firebaseRestManager.getSingleItem(
            CinemaTb::class.java,
            "moviedb/cinematb",
            show.cinemaId
        ) { cinema ->
            val cinema = cinema ?: return@getSingleItem

            val capacity = cinema.cinemaCapacity?.toInt() ?: 0
            val seatContainer = view.findViewById<GridLayout>(R.id.seatContainer)
            seatContainer?.removeAllViews()

            // Fixed number of columns
            val columns = 5
            // Calculate seat size based on fixed number of columns and container width
            seatContainer?.post {
                val containerWidth = seatContainer.width
                val seatSize = (containerWidth / columns).coerceAtMost(150).coerceAtLeast(100)

                seatContainer.columnCount = columns

                val bookedSeatsSet = mutableSetOf<String>()
                val firebaseRestManagerBooking = FirebaseRestManager<BookingTb>()

                firebaseRestManagerBooking.getAllItems(BookingTb::class.java, "moviedb/bookingtb") { items ->
                    for (item in items) {
                        if (item.showId == show.showId) {
                            bookedSeatsSet.addAll(item.bookedSeats.split(","))
                        }
                    }

                    for (i in 0 until capacity) {
                        val seatView = ImageView(requireContext)
                        seatView.id = View.generateViewId()
                        seatView.setImageResource(R.drawable.chair_default)

                        val params = GridLayout.LayoutParams().apply {
                            width = seatSize
                            height = seatSize
                            columnSpec = GridLayout.spec(i % columns)
                            rowSpec = GridLayout.spec(i / columns)
                            setMargins(seatSize / 8, seatSize / 8, seatSize / 8, seatSize / 8)
                        }

                        val seatId = "${i / columns}-${i % columns}"

                        if (bookedSeatsSet.contains(seatId)) {
                            seatView.setImageResource(R.drawable.chair_booked)
                            seatView.isEnabled = requireContext is CinemaOwnerActivity
                        } else {
                            seatView.isEnabled = true
                        }

                        seatContainer.addView(seatView, params)
                        seatView.setOnClickListener { onSeatClicked(seatId, seatView, bookedSeatsSet) }
                    }

                    val parent = seatContainer.parent as LinearLayout
                    parent.gravity = Gravity.CENTER
                }
            }
        }

        val confirmSeatButton = view.findViewById<TextView>(R.id.confirm_seat_button)
        if (requireContext is UserActivity || requireContext is CinemaAdminActivity) {
            confirmSeatButton?.setOnClickListener {
                if (selectedSeats.isNotEmpty()) {
                    showBottomSheet()
                } else {
                    val warningLoadingHelper = WarningLoadingHelper()
                    warningLoadingHelper.showLoadingDialog(requireContext)
                    warningLoadingHelper.hideButtons()
                    warningLoadingHelper.updateText("Select at least one seat!!")

                    val handler = Handler()
                    handler.postDelayed({ warningLoadingHelper.dismissLoadingDialog() }, 2000)
                }
            }
        } else {
            confirmSeatButton.isVisible = false
        }
    }

    private fun onSeatClicked(seatId: String, seatView: ImageView, bookedSeatsSet: Set<String>) {
        if (requireContext is UserActivity) {
            if (selectedSeats.contains(seatId)) {
                selectedSeats.remove(seatId)
                seatView.setImageResource(R.drawable.chair_default)
            } else {
                selectedSeats.add(seatId)
                seatView.setImageResource(R.drawable.chair_selected)
            }
        } else if (requireContext is CinemaAdminActivity || requireContext is CinemaOwnerActivity) {
            if (bookedSeatsSet.contains(seatId)) {
                val firebaseRestManager = FirebaseRestManager<BookingTb>()
                firebaseRestManager.getAllItems(BookingTb::class.java, "moviedb/bookingtb") { items ->
                    val booking = items.find { it.bookedSeats.split(",").contains(seatId) && it.showId == show.showId }
                    if (booking != null) {
                        try {
                            val dialogView = LayoutInflater.from(requireContext).inflate(R.layout.ticket_layout, null)
                            val dialogBuilder = AlertDialog.Builder(requireContext).setView(dialogView)
                            val alertDialog: AlertDialog = dialogBuilder.create()
                            alertDialog.show()

                            val moviePoster: ImageView = dialogView.findViewById(R.id.moviePoster)
                            val showDate: TextView = dialogView.findViewById(R.id.showDate)
                            val showTime: TextView = dialogView.findViewById(R.id.showTime)
                            val bookingId: TextView = dialogView.findViewById(R.id.bookingId)
                            val showRowandSeat: TextView = dialogView.findViewById(R.id.showRowandSeat)

                            loadMoviePoster(show.movieId, moviePoster)
                            showDate.text = booking.bookingDate
                            showTime.text = booking.bookingTime
                            bookingId.text = booking.bookingId
                            showRowandSeat.text = seatId

                            alertDialog.window?.attributes?.windowAnimations = R.style.DialogAnimation
                        } catch (e: Exception) {
                            Log.e("SeatManager", "Error displaying dialog", e)
                        }
                    } else {
                        Log.d("TAG", "Seat not booked: $seatId")
                    }
                }
            } else {
                handleUnbookedSeatClick(seatId, seatView)
            }
        }
    }

    private fun loadMoviePoster(movieId: String, moviePoster: ImageView) {
        val firebaseRestManager = FirebaseRestManager<MoviePosterTb>()
        firebaseRestManager.getAllItems(MoviePosterTb::class.java, "moviedb/moviepostertb") { posterItems ->
            val poster = posterItems.find { it.mid == movieId }
            poster?.let {
                Glide.with(requireContext).load(it.mlink).into(moviePoster)
            } ?: Log.d("Firebase", "No movie posters found")
        }
    }

    private fun handleUnbookedSeatClick(seatId: String, seatView: ImageView) {
        if (requireContext is CinemaOwnerActivity) {
            val warningLoadingHelper = WarningLoadingHelper()
            warningLoadingHelper.showLoadingDialog(requireContext)
            warningLoadingHelper.hideButtons()
            warningLoadingHelper.updateText("Seat is not booked yet!!")
            val handler = Handler()
            handler.postDelayed({ warningLoadingHelper.dismissLoadingDialog() }, 2000)
        } else {
            if (selectedSeats.contains(seatId)) {
                selectedSeats.remove(seatId)
                seatView.setImageResource(R.drawable.chair_default)
            } else {
                selectedSeats.add(seatId)
                seatView.setImageResource(R.drawable.chair_selected)
            }
        }
    }

    private fun showBottomSheet() {
        val inflater = LayoutInflater.from(requireContext)
        val bottomSheetView = inflater.inflate(R.layout.user_seat_confirmation, null)

        val textView = bottomSheetView.findViewById<TextView>(R.id.seat_text)
        textView.text = "Selected Seats: ${selectedSeats.joinToString(", ")}"

        val pricingBreakdown = bottomSheetView.findViewById<TextView>(R.id.pricing_breakdown)
        val breakdownText = "Total Cost Breakdown\nTotal Selected Seats : ${selectedSeats.size}\nPrice : ${show.price}\nTotal Cost : ${selectedSeats.size} X ${show.price} =  ${selectedSeats.size * show.price.toInt()}"
        val spannableString = SpannableString(breakdownText)
        spannableString.setSpan(StyleSpan(Typeface.BOLD), 0, 20, Spannable.SPAN_EXCLUSIVE_EXCLUSIVE)
        pricingBreakdown.text = spannableString

        val totalCost = selectedSeats.size * show.price.toInt()
        val paymentButton = bottomSheetView.findViewById<TextView>(R.id.payment_button)
        paymentButton.text = "Pay : $totalCost"

        paymentButton.setOnClickListener {
            if (requireContext is UserActivity) {
                val intent = Intent(requireContext, PaymentActivity::class.java).apply {
                    putExtra("key_amount", totalCost)
                    putExtra("key_showId", show.showId)
                    putExtra("key_seats", ArrayList(selectedSeats))
                }

                requireContext.startActivity(intent)
                bottomSheetDialog.dismiss()
                dialog.dismiss()
            } else if (requireContext is CinemaAdminActivity) {
                handleCinemaAdminPayment(totalCost)
            }
        }

        bottomSheetDialog = BottomSheetDialog(requireContext)
        bottomSheetDialog.setContentView(bottomSheetView)
        bottomSheetDialog.show()
    }

    private fun handleCinemaAdminPayment(totalCost: Int) {
        val loadingDialogHelper = LoadingDialogHelper()
        val bookingId = FirebaseDatabase.getInstance().reference.push().key ?: ""
        val timeZone = TimeZone.getTimeZone("Asia/Kolkata")
        val dateFormat = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).apply { this.timeZone = timeZone }
        val timeFormat = SimpleDateFormat("HH:mm:ss", Locale.getDefault()).apply { this.timeZone = timeZone }
        val bookingDate = dateFormat.format(Date())
        val bookingTime = timeFormat.format(Date())

        val booking = BookingTb(
            bookingId = bookingId,
            paymentId = UUID.randomUUID().toString().replace("-", "").substring(0, 16),
            bookingDate = bookingDate,
            bookingTime = bookingTime,
            showId = show.showId,
            userId = FirebaseAuth.getInstance().currentUser?.uid ?: "",
            bookedSeats = selectedSeats.joinToString(",")
        )

        val firebaseRestManager = FirebaseRestManager<BookingTb>()
        firebaseRestManager.addItemWithCustomId(booking, bookingId, FirebaseDatabase.getInstance().getReference("moviedb/bookingtb")) { success, error ->
            loadingDialogHelper.dismissLoadingDialog()
            if (success) {
                val successLoadingHelper = SuccessLoadingHelper()
                successLoadingHelper.showLoadingDialog(requireContext)
                successLoadingHelper.hideButtons()
                successLoadingHelper.updateText("Ticket has been booked!")
                bottomSheetDialog.dismiss()
                val handler = Handler()
                handler.postDelayed({ successLoadingHelper.dismissLoadingDialog() }, 2000)
            } else {
                val warningLoadingHelper = WarningLoadingHelper()
                warningLoadingHelper.showLoadingDialog(requireContext)
                bottomSheetDialog.dismiss()

                val handler = Handler()
                handler.postDelayed({ warningLoadingHelper.dismissLoadingDialog() }, 2000)
            }
        }
    }

    fun getSelectedSeats(): List<String> {
        return selectedSeats.toList()
    }
}

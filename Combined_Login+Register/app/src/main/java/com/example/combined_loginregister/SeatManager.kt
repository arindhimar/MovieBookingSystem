package com.example.combined_loginregister

import android.app.Activity
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
        val dialog = Dialog(context, android.R.style.Theme_Black_NoTitleBar_Fullscreen)
        dialog.setContentView(view)
        dialog.setCancelable(true)

        // Show the dialog
        dialog.show()

        // Set the dialog to class variable for dismissing later if needed
        this.dialog = dialog

        // Call setup method after the dialog is shown
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
        val firebaseDatabase = FirebaseDatabase.getInstance()
        val bookingRef = firebaseDatabase.getReference("moviedb/bookingtb")
        val bookingListener = object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                setUpCard()
            }

            override fun onCancelled(error: DatabaseError) {
                // Handle the error
                Log.e("SeatManager", "Error: ${error.message}")
            }
        }
        bookingRef.addValueEventListener(bookingListener)
    }

    private fun setUpCard() {
        val loadingDialogHelper = LoadingDialogHelper()
        loadingDialogHelper.showLoadingDialog(requireContext)

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

            // Get the container dimensions
            seatContainer?.post {
                val containerWidth = seatContainer.width

                val maxSeatSize = 150 // Set a max seat size (adjust as needed)
                val minSeatSize = 100 // Set a minimum seat size (adjust as needed)
                val minColumns = 5 // Set a minimum number of columns (adjust as needed)
                val columns = minColumns.coerceAtLeast(containerWidth / maxSeatSize)
                // Calculate the seat size, ensuring it's not below the minimum size and not above the maximum size
                val seatSize = minSeatSize.coerceAtLeast(containerWidth / columns).coerceAtMost(maxSeatSize)

                // Set the column count
                seatContainer.columnCount = columns

                val bookedSeatsSet = mutableSetOf<String>()

                val firebaseRestManager = FirebaseRestManager<BookingTb>()
                firebaseRestManager.getAllItems(BookingTb::class.java, "moviedb/bookingtb") { items ->
                    for (item in items) {
                        if (item.showId == show.showId) {
                            val bookedSeats = item.bookedSeats
                            bookedSeatsSet.addAll(bookedSeats.split(","))
                            Log.d("TAG", "bookedSeatsSet: $bookedSeatsSet")
                        }
                    }

                    for (i in 0 until capacity) {
                        val seatView = ImageView(requireContext)
                        seatView.id = View.generateViewId()

                        seatView.setImageResource(R.drawable.chair_default)

                        // Set seat layout params
                        val params = GridLayout.LayoutParams().apply {
                            width = seatSize
                            height = seatSize
                            columnSpec = GridLayout.spec(i % columns)
                            rowSpec = GridLayout.spec(i / columns)
                            setMargins(
                                seatSize / 8,
                                seatSize / 8,
                                seatSize / 8,
                                seatSize / 8
                            ) // uniform margin
                        }

                        val seatId = "${i / columns}-${i % columns}" // Unique identifier for the seat

                        if (bookedSeatsSet.contains(seatId)) {
                            seatView.setImageResource(R.drawable.chair_booked)

                            if (requireContext is UserActivity) {
                                seatView.isEnabled = true
                            } else if (requireContext is CinemaAdminActivity) {
                                seatView.isEnabled = false
                            }
                        } else {
                            seatView.isEnabled = true
                        }

                        seatContainer.addView(seatView, params)

                        seatView.setOnClickListener {
                            onSeatClicked(seatId, seatView, bookedSeatsSet)
                        }
                    }

                    // Center the GridLayout in its parent
                    val parent = seatContainer.parent as LinearLayout
                    parent.gravity = Gravity.CENTER

                    loadingDialogHelper.dismissLoadingDialog()
                }
            }
        }

        val confirmSeatButton = view.findViewById<TextView>(R.id.confirm_seat_button)
        confirmSeatButton?.setOnClickListener {
            if (selectedSeats.isNotEmpty()) {
                showBottomSheet()
            } else {
                val warningLoadingHelper = WarningLoadingHelper()
                warningLoadingHelper.showLoadingDialog(requireContext)
                warningLoadingHelper.hideButtons()
                warningLoadingHelper.updateText("Select at least one seat!!")

                val handler = Handler()
                handler.postDelayed({
                    warningLoadingHelper.dismissLoadingDialog()
                }, 2000)
            }
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
        } else if (requireContext is CinemaAdminActivity) {
            if (bookedSeatsSet.contains(seatId)) {
                try {
                    val layoutInflater = LayoutInflater.from(requireContext)
                    val dialogView: View = layoutInflater.inflate(R.layout.ticket_layout, null)

                    val dialogBuilder = AlertDialog.Builder(requireContext)
                    dialogBuilder.setView(dialogView)

                    val alertDialog: AlertDialog = dialogBuilder.create()
                    alertDialog.show()

                    val moviePoster: ImageView = dialogView.findViewById(R.id.moviePoster)
                    val showDate: TextView = dialogView.findViewById(R.id.showDate)
                    val showTime: TextView = dialogView.findViewById(R.id.showTime)
                    val bookingId: TextView = dialogView.findViewById(R.id.bookingId)
                    val showRowandSeat: TextView = dialogView.findViewById(R.id.showRowandSeat)

                    alertDialog.window?.attributes?.windowAnimations = R.style.DialogAnimation
                } catch (e: Exception) {
                    Log.e("SeatManager", "Error displaying dialog", e)
                }
            } else {
                Log.d("TAG", "Seat not booked: $seatId")
                if (selectedSeats.contains(seatId)) {
                    selectedSeats.remove(seatId)
                    seatView.setImageResource(R.drawable.chair_default)
                } else {
                    selectedSeats.add(seatId)
                    seatView.setImageResource(R.drawable.chair_selected)
                }
            }
        }
    }

    private fun showBottomSheet() {
        val inflater = LayoutInflater.from(requireContext)
        val bottomSheetView = inflater.inflate(R.layout.user_seat_confirmation, null)

        val textView = bottomSheetView.findViewById<TextView>(R.id.seat_text)
        textView.text = "Selected Seats: ${selectedSeats.joinToString(", ")}"

        val pricing_breakdown = bottomSheetView.findViewById<TextView>(R.id.pricing_breakdown)
        val breakdownText = "Total Cost Breakdown\nTotal Selected Seats : ${selectedSeats.size}\nPrice : ${show.price}\nTotal Cost : ${selectedSeats.size} X ${show.price} =  ${selectedSeats.size * show.price.toInt()}"

        val spannableString = SpannableString(breakdownText)
        spannableString.setSpan(
            StyleSpan(Typeface.BOLD),
            0, // Start index of "Total Cost Breakdown"
            20, // End index of "Total Cost Breakdown" (assuming it has 20 characters)
            Spannable.SPAN_EXCLUSIVE_EXCLUSIVE
        )

        pricing_breakdown.text = spannableString

        val totalCost = selectedSeats.size * show.price.toInt()
        val paymentButton = bottomSheetView.findViewById<TextView>(R.id.payment_button)
        paymentButton.text = "Pay : $totalCost"

        paymentButton.setOnClickListener {
            if (requireContext is UserActivity) {
                val intent = Intent(requireContext, PaymentActivity::class.java)
                intent.putExtra("key_amount", totalCost)
                intent.putExtra("key_showId", show.showId)
                intent.putExtra("key_seats", ArrayList(selectedSeats))
                requireContext.startActivity(intent)

                bottomSheetDialog.dismiss()
                dialog.dismiss()
            } else if (requireContext is CinemaAdminActivity) {
                // Generate bookingId
                val loadingDialogHelper = LoadingDialogHelper()

                val bookingId = FirebaseDatabase.getInstance().reference.push().key ?: ""

                // Set time zone to IST
                val timeZone = TimeZone.getTimeZone("Asia/Kolkata")

                // Get current date and time in IST
                val dateFormat = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).apply {
                    this.timeZone = timeZone
                }
                val timeFormat = SimpleDateFormat("HH:mm:ss", Locale.getDefault()).apply {
                    this.timeZone = timeZone
                }

                val bookingDate = dateFormat.format(Date())
                val bookingTime = timeFormat.format(Date())

                // Create BookingTb object
                val booking = BookingTb(
                    bookingId = bookingId,
                    paymentId = UUID.randomUUID().toString().replace("-", "").substring(0, 16),
                    bookingDate = bookingDate,
                    bookingTime = bookingTime,
                    showId = show.showId,
                    userId = FirebaseAuth.getInstance().currentUser?.uid ?: "",
                    bookedSeats = selectedSeats.joinToString(",")
                )

                // Push to Firebase
                val firebaseRestManager = FirebaseRestManager<BookingTb>()
                firebaseRestManager.addItemWithCustomId(booking, bookingId, FirebaseDatabase.getInstance().getReference("moviedb/bookingtb")) { success, error ->
                    loadingDialogHelper.dismissLoadingDialog()
                    if (success) {
                        val successLoadingHelper = SuccessLoadingHelper()
                        successLoadingHelper.showLoadingDialog(requireContext)
                        successLoadingHelper.hideButtons()
                        successLoadingHelper.updateText("Ticket has been booked!")


                        val handler = Handler()
                        handler.postDelayed({
                            successLoadingHelper.dismissLoadingDialog()
                        }, 2000)

                    } else {
                        val warningLoadingHelper = WarningLoadingHelper()
                        warningLoadingHelper.showLoadingDialog(requireContext)

                        val handler = Handler()
                        handler.postDelayed({
                            warningLoadingHelper.dismissLoadingDialog()
                        }, 2000)
                    }
                }
            }
        }

        bottomSheetDialog = BottomSheetDialog(requireContext)
        bottomSheetDialog.setContentView(bottomSheetView)
        bottomSheetDialog.show()
    }

    fun getSelectedSeats(): List<String> {
        return selectedSeats.toList()
    }
}

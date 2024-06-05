package com.example.combined_loginregister

import android.app.Activity
import android.app.Dialog
import android.content.Context
import android.content.Intent
import android.graphics.Typeface
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
import androidx.core.content.ContextCompat.startActivity
import com.google.android.material.bottomsheet.BottomSheetDialog

class SeatManager(
    private val show: ShowTb,
    private val requireContext: Context
) {
    private val selectedSeats = mutableSetOf<String>()
    private lateinit var dialog: Dialog
    private lateinit var view: View
    private lateinit var bottomSheetDialog: BottomSheetDialog

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
    }

    fun returnView(): View {
        return view
    }

    fun dismissLoadingDialog() {
        if (dialog.isShowing) {
            dialog.dismiss()
        }
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

            val capacity = cinema.cinemaCapacity!!.toInt()
            val seatContainer = view?.findViewById<GridLayout>(R.id.seatContainer)
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

                var bookedSeatsSet = mutableSetOf<String>()

                val firebaseRestManager = FirebaseRestManager<BookingTb>()
                firebaseRestManager.getAllItems(BookingTb::class.java, "moviedb/bookingtb") { items ->
                    for (item in items) {
                        if (item.showId == show.showId) {
                            val bookedSeats = item.bookedSeats
                            bookedSeatsSet = bookedSeats.split(",").toMutableSet()
                            Log.d("TAG", "bookedSeatsSet: $bookedSeatsSet ")
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
                            seatView.isEnabled = false
                        }

                        seatContainer.addView(seatView, params)

                        seatView.setOnClickListener {
                            onSeatClicked(seatId, seatView)
                        }
                    }

                    // Center the GridLayout in its parent
                    val parent = seatContainer.parent as LinearLayout
                    parent.gravity = Gravity.CENTER

                    loadingDialogHelper.dismissLoadingDialog()
                }
            }
        }

        val confirmSeatButton = view?.findViewById<TextView>(R.id.confirm_seat_button)
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
    private fun onSeatClicked(seatId: String, seatView: ImageView) {
        if (selectedSeats.contains(seatId)) {
            selectedSeats.remove(seatId)
            seatView.setImageResource(R.drawable.chair_default)
        } else {
            selectedSeats.add(seatId)
            seatView.setImageResource(R.drawable.chair_selected)
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
            val intent = Intent(requireContext, PaymentActivity::class.java)

            intent.putExtra("key_amount", totalCost)
            intent.putExtra("key_showId", show.showId)
            intent.putExtra("key_seats", ArrayList(selectedSeats))
            requireContext.startActivity(intent)


        }


        bottomSheetDialog = BottomSheetDialog(requireContext)
        bottomSheetDialog.setContentView(bottomSheetView)
        bottomSheetDialog.show()
    }

    fun getSelectedSeats(): List<String> {
        return selectedSeats.toList()
    }
}

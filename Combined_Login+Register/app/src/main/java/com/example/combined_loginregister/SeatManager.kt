package com.example.combined_loginregister

import android.app.AlertDialog
import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.widget.ImageView
import android.widget.RelativeLayout
import android.widget.Toast
import com.example.combined_loginregister.R

class SeatManager(private val show: ShowTb, private val requireContext: Context) {
    private val selectedSeats = mutableSetOf<Int>()
    private lateinit var dialog: AlertDialog
    private lateinit var view: View

    fun showLoadingDialog(context: Context) {
        val inflater = LayoutInflater.from(context)
        view = inflater.inflate(R.layout.cinema_seat_layout, null)

        val builder = AlertDialog.Builder(context)
        builder.setView(view)
        builder.setCancelable(true)

        dialog = builder.create()
        dialog.window?.attributes?.windowAnimations = R.style.DialogAnimation
        dialog.show()
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
        firebaseRestManager.getSingleItem(CinemaTb::class.java, "moviedb/cinematb", show.cinemaId) {
            val cinema = it ?: return@getSingleItem

            val capacity = cinema.cinemaCapacity!!.toInt()
            val seatContainer = view.findViewById<RelativeLayout>(R.id.seatContainer)
            seatContainer.removeAllViews()

            // Calculate the number of rows and columns
            val columns = 10 // assuming fixed number of columns
            val rows = (capacity + columns - 1) / columns // calculate the number of rows needed

            // Get the container dimensions
            seatContainer.post {
                val containerWidth = seatContainer.width
                val containerHeight = seatContainer.height

                // Calculate seat size and margin
                val seatSize = containerWidth / (columns + 1) // dynamic seat size
                val horizontalSeatMargin = seatSize / 4 // increase horizontal margin
                val verticalSeatMargin = seatSize / 2 // increase vertical margin

                for (i in 0 until capacity) {
                    val seatView = ImageView(requireContext)
                    seatView.id = View.generateViewId()
                    seatView.setImageResource(R.drawable.chair_default) // Replace with your seat drawable

                    // Set seat layout params
                    val params = RelativeLayout.LayoutParams(seatSize, seatSize)
                    if (i % columns != 0) { // for all but the first seat in a row
                        params.addRule(RelativeLayout.RIGHT_OF, seatContainer.getChildAt(i - 1).id)
                        params.setMargins(horizontalSeatMargin, 0, 0, verticalSeatMargin)
                    }
                    if (i >= columns) { // for all seats not in the first row
                        params.addRule(RelativeLayout.BELOW, seatContainer.getChildAt(i - columns).id)
                        if (i % columns == 0) { // first seat in a row
                            params.setMargins(0, verticalSeatMargin, 0, verticalSeatMargin)
                        }
                    }
                    seatContainer.addView(seatView, params)

                    loadingDialogHelper.dismissLoadingDialog()

                    seatView.setOnClickListener {
                        onSeatClicked(i, seatView)
                    }
                }
            }
        }
    }

    private fun onSeatClicked(seatIndex: Int, seatView: ImageView) {
        if (selectedSeats.contains(seatIndex)) {
            selectedSeats.remove(seatIndex)
            seatView.setImageResource(R.drawable.chair_default)
        } else {
            selectedSeats.add(seatIndex)
            seatView.setImageResource(R.drawable.chair_selected)
        }
    }

    fun getSelectedSeats(): List<Int> {
        return selectedSeats.toList()
    }
}

package com.example.combined_loginregister

import android.app.AlertDialog
import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.widget.GridLayout
import android.widget.ImageView
import com.example.combined_loginregister.R

class SeatManager(private val show: ShowTb, private val requireContext: Context) {
    private val selectedSeats = mutableSetOf<String>()
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
        firebaseRestManager.getSingleItem(
            CinemaTb::class.java,
            "moviedb/cinematb",
            show.cinemaId
        ) { cinema ->
            val cinema = cinema ?: return@getSingleItem

            val capacity = cinema.cinemaCapacity!!.toInt()
            val seatContainer = view.findViewById<GridLayout>(R.id.seatContainer)
            seatContainer.removeAllViews()

            // Get the container dimensions
            seatContainer.post {
                val containerWidth = seatContainer.width

                // Dynamically determine the number of columns based on the capacity and container width
                val maxSeatSize = 100 // Set a max seat size (adjust as needed)
                val minSeatSize = 60 // Set a minimum seat size (adjust as needed)
                val minColumns = 5 // Set a minimum number of columns (adjust as needed)
                val columns = Math.max(minColumns, containerWidth / maxSeatSize)

                // Calculate the seat size, ensuring it's not below the minimum size
                val seatSize = Math.max(minSeatSize, containerWidth / columns)

                // Set the column count
                seatContainer.columnCount = columns

                for (i in 0 until capacity) {
                    val seatView = ImageView(requireContext)
                    seatView.id = View.generateViewId()
                    seatView.setImageResource(R.drawable.chair_default) // Replace with your seat drawable

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

                    seatContainer.addView(seatView, params)

                    loadingDialogHelper.dismissLoadingDialog()

                    seatView.setOnClickListener {
                        onSeatClicked(seatId, seatView)
                    }
                }
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

    fun getSelectedSeats(): List<String> {
        return selectedSeats.toList()
    }
}

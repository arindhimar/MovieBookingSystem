package com.example.combined_loginregister


import android.app.AlertDialog
import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.widget.ImageView
import android.widget.RelativeLayout
import android.widget.Toast

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
//        val seatLayout: RelativeLayout = view.findViewById(R.id.seatLayout)

        val firebaseRestManager = FirebaseRestManager<CinemaTb>()
        firebaseRestManager.getSingleItem(CinemaTb::class.java,"moviedb/cinematb",show.cinemaId){
            val cinema = it
//
//
//            val capacity = cinema.
//
//            // Generate seats dynamically
//            for (seatNumber in 1..capacity) {
//                val seatView = ImageView(requireContext)
//                val layoutParams = RelativeLayout.LayoutParams(
//                    RelativeLayout.LayoutParams.WRAP_CONTENT,
//                    RelativeLayout.LayoutParams.WRAP_CONTENT
//                )
//
//                // Set seat image or background
//                seatView.setImageResource(R.drawable.seat_icon)
//                seatView.id = View.generateViewId() // Set a unique ID for each seat view
//                seatView.tag = seatNumber // Set seat number as tag
//                seatView.setOnClickListener {
//                    toggleSeatSelection(seatNumber, seatView)
//                }
//
//                // Set seat position in the layout
//                // Here you can define your logic to position seats according to your layout requirements
//                layoutParams.leftMargin = 100 // Example left margin
//                layoutParams.topMargin = 100 * seatNumber // Example top margin, adjust as needed
//
//                seatLayout.addView(seatView, layoutParams)
//            }
        }


    }

//    private fun toggleSeatSelection(seatNumber: Int, seatView: ImageView) {
//        if (selectedSeats.contains(seatNumber)) {
//            selectedSeats.remove(seatNumber)
//            // Deselect seat
//            seatView.setImageResource(R.drawable.seat_icon)
//        } else {
//            selectedSeats.add(seatNumber)
//            // Select seat
//            seatView.setImageResource(R.drawable.selected_seat_icon)
//        }
//    }

    fun getSelectedSeats(): List<Int> {
        return selectedSeats.toList()
    }
}

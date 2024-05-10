package com.example.seats_layout

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.TextView
import android.widget.Toast
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.RecyclerView

class MainActivity2 : AppCompatActivity() {
    private lateinit var seatAdapter: SeatAdapter
    private lateinit var totalTicketsTextView: TextView
    private lateinit var totalAmountTextView: TextView
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main2)
        val buttonBook: Button = findViewById(R.id.buttonBook)
        val recyclerView: RecyclerView = findViewById(R.id.recyclerViewSeats)
        totalTicketsTextView = findViewById(R.id.totalTicketsTextView)
        totalAmountTextView = findViewById(R.id.totalAmountTextView)
        val seats = mutableListOf<Seat>()


        //  val seats = mutableListOf<Seat>()
        for (i in 1..50) {
            seats.add(Seat("$i"))
        }


        seatAdapter = SeatAdapter(seats) { position ->
            val seat = seats[position]
            seat.isSelected = !seat.isSelected
            seatAdapter.notifyItemChanged(position)
            updateTotal()
        }

        recyclerView.apply {
            layoutManager = GridLayoutManager(this@MainActivity2, 10)
            adapter = seatAdapter
        }
        updateTotal()
        buttonBook.setOnClickListener {
            val selectedSeats = seatAdapter.getSelectedSeats()
            val selectedSeatsMessage = if (selectedSeats.isNotEmpty()) {
                "Selected Seats: ${selectedSeats.joinToString(", ")}"
            } else {
                "No seats selected"
            }
            Toast.makeText(this, selectedSeatsMessage, Toast.LENGTH_SHORT).show()
        }

    }

    private fun updateTotal() {
        val totalTickets = seatAdapter.getTotalTickets()
        val totalAmount = seatAdapter.getTotalAmount()

        totalTicketsTextView.text = "Total Tickets: $totalTickets"
        totalAmountTextView.text = "Total Amount: $totalAmount"
    }

    class SeatAdapter(private val seats: List<Seat>, private val onItemClick: (Int) -> Unit) :
        RecyclerView.Adapter<SeatAdapter.SeatViewHolder>() {

        private var totalTickets = 0
        private var totalAmount = 0

        // private val selectedSeats = mutableListOf<Seat>()
        override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): SeatViewHolder {
            val view = LayoutInflater.from(parent.context)
                .inflate(R.layout.item_seats, parent, false)
            return SeatViewHolder(view)
        }

        override fun onBindViewHolder(holder: SeatViewHolder, position: Int) {
            val seat = seats[position]
            holder.bind(seat)
            holder.itemView.setOnClickListener {
                onItemClick(position)
            }
        }


        override fun getItemCount(): Int = seats.size

        inner class SeatViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
            private val textViewSeat: TextView = itemView.findViewById(R.id.textViewSeat)

            fun bind(seat: Seat) {
                textViewSeat.text = seat.seatNumber
                when (seat.status) {
                    "Booked" -> {
                        textViewSeat.setBackgroundResource(R.drawable.ic_launcher_background)
                        itemView.setOnClickListener(null)
                    }

                    "Available" -> {
                        if (seat.isSelected) {
                            textViewSeat.setBackgroundResource(R.drawable.chair)
                            totalTickets++
                            totalAmount = totalAmount + seat.ticketPrice
                        } else {
                            // Reset background color or image for unselected seats
                            textViewSeat.setBackgroundResource(R.drawable.seat)
                            totalTickets--
                            totalAmount = seat.ticketPrice
                        }
                        itemView.setOnClickListener {
                            onItemClick(adapterPosition)
                        }
                    }
                }
            }
        }

        /* fun bind(seat: Seat) {
                textViewSeat.text = seat.seatNumber

                if (seat.isSelected) {
                    // Change background color or image for selected seats
                    textViewSeat.setBackgroundResource(R.drawable.chair)
                    totalTickets++
                    totalAmount =totalAmount+ seat.ticketPrice
                } else {
                    // Reset background color or image for unselected seats
                    textViewSeat.setBackgroundResource(R.drawable.seat)
                    totalTickets--
                    totalAmount =seat.ticketPrice
                }
                itemView.setOnClickListener {
                    onItemClick(adapterPosition)
                }
            }
*/


        fun getSelectedSeats(): List<String> {
            val selectedSeatNumbers = mutableListOf<String>()
            for (seat in seats) {
                if (seat.isSelected) {
                    selectedSeatNumbers.add(seat.seatNumber)
                }
            }
            return selectedSeatNumbers
        }

        fun getTotalTickets(): Int {
            return seats.count { it.isSelected }
        }

        fun getTotalAmount(): Int {
            return seats.filter { it.isSelected }.sumOf { it.ticketPrice }
        }
    }
}
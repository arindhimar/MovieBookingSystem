package com.example.seats_layout

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.TextView
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import androidx.recyclerview.widget.RecyclerView.ViewHolder

class MainActivity : AppCompatActivity() {
    private lateinit var recyclerView: RecyclerView
    private lateinit var seatAdapter: SeatAdapter
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        recyclerView = findViewById(R.id.recyclerViewSeats)
        recyclerView.layoutManager = LinearLayoutManager(this)

        val seats = mutableListOf<String>()
        for (i in 1..50) {
            seats.add(" $i")
        }

        seatAdapter = SeatAdapter(seats)
        recyclerView.adapter = seatAdapter


        val bookButton: Button = findViewById(R.id.buttonBook)
        bookButton.setOnClickListener {
            val selectedSeats = seatAdapter.getSelectedSeats()
            // Do something with selected seats
        }

    }
    class SeatAdapter(private val seats: List<String>) :
        RecyclerView.Adapter<SeatAdapter.SeatViewHolder>() {

        private val selectedSeats = HashSet<String>()

        override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): SeatViewHolder {
            val view = LayoutInflater.from(parent.context)
                .inflate(R.layout.item_seats, parent, false)
            return SeatViewHolder(view)
        }

        override fun onBindViewHolder(holder: SeatViewHolder, position: Int) {
            val seat = seats[position]
            holder.bind(seat)
            holder.itemView.setOnClickListener {
                if (selectedSeats.contains(seat)) {
                    selectedSeats.remove(seat)
                    holder.itemView.setBackgroundResource(R.drawable.seat)
                } else {
                    selectedSeats.add(seat)
                    holder.itemView.setBackgroundResource(R.drawable.chair)
                }
            }
        }

        override fun getItemCount(): Int = seats.size

        fun getSelectedSeats(): Set<String> {
            return selectedSeats
        }

        class SeatViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
            private val textViewSeatNumber: TextView =
                itemView.findViewById(R.id.textViewSeat)

            fun bind(seat: String) {
                textViewSeatNumber.text = seat
            }
        }
    }
}
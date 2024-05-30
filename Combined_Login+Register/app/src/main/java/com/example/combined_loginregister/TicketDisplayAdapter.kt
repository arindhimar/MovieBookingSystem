package com.example.combined_loginregister

import android.annotation.SuppressLint
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide

class TicketDisplayAdapter(private val bookingList: List<BookingTb>) : RecyclerView.Adapter<TicketDisplayAdapter.TicketViewHolder>() {

    private lateinit var onItemClickListener: OnItemClickListener

    interface OnItemClickListener {
        fun onItemClick(booking: BookingTb)
    }

    fun setOnItemClickListener(listener: OnItemClickListener) {
        onItemClickListener = listener
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): TicketViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.ticket_layout, parent, false)
        return TicketViewHolder(view)
    }

    @SuppressLint("SetTextI18n")
    override fun onBindViewHolder(holder: TicketViewHolder, position: Int) {
        val booking = bookingList[position]



        val firebaseRestManager1 = FirebaseRestManager<ShowTb>()
         firebaseRestManager1.getSingleItem(ShowTb::class.java, "moviedb/showtb",booking.showId){
             if(it!=null) {
                 val showData = it
                 holder.showDate.text = showData.showDate
                 holder.showTime.text = showData.showStartTime+"to"+showData.showEndTime

                 val seats = booking.bookedSeats.split(",")
                 holder.showRowandSeat.text = booking.bookedSeats


                 // Getting all the posters of the movies
                 val moviePosterClass = MoviePosterTb::class.java
                 val node = "moviedb/moviepostertb"

                 val firebaseRestManager2 = FirebaseRestManager<MoviePosterTb>()
                 firebaseRestManager2.getAllItems(moviePosterClass, node) { posterItems ->
                     if (posterItems.isNotEmpty()) {
                         for (item2 in posterItems) {
                             if (item2.mid == showData.movieId) {
                                 Glide.with(holder.itemView.context)
                                     .load(item2.mlink)
                                     .into(holder.moviePoster)
                                 break
                             }
                         }
                     } else {
                         Log.d("Firebase", "No movie posters found")
                     }

             }
        }



        }

        holder.itemView.setOnClickListener {
            onItemClickListener.onItemClick(booking)
        }
    }

    override fun getItemCount(): Int {
        Log.d("TAG", "getItemCount: ${bookingList.size}")
        return bookingList.size
    }

    class TicketViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val moviePoster: ImageView = itemView.findViewById(R.id.moviePoster)
        val showDate: TextView = itemView.findViewById(R.id.showDate)
        val showTime: TextView = itemView.findViewById(R.id.showTime)
        val showRowandSeat: TextView = itemView.findViewById(R.id.showRowandSeat)

    }
}

package com.example.combined_loginregister

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.chip.Chip
import com.google.android.material.chip.ChipGroup

class CinemaShowsAdapter(
    private val groupedShows: Map<String, List<ShowTb>>,
    private val context: Context
) : RecyclerView.Adapter<CinemaShowsAdapter.CinemaViewHolder>() {

    class CinemaViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val cinemaName: TextView = view.findViewById(R.id.cinemaName)
        val showTimings: ChipGroup = view.findViewById(R.id.ShowTimingsHere)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): CinemaViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.cinema_show_card, parent, false)
        return CinemaViewHolder(view)
    }

    override fun onBindViewHolder(holder: CinemaViewHolder, position: Int) {
        val cinemaId = groupedShows.keys.elementAt(position)
        val shows = groupedShows[cinemaId] ?: emptyList()

        val firebaseRestManager = FirebaseRestManager<CinemaTb>()
        firebaseRestManager.getSingleItem(CinemaTb::class.java,"moviedb/cinematb",cinemaId){
            holder.cinemaName.text = "Cinema Name : ${it!!.cinemaName}" // Replace with cinema name if available
        }


        holder.showTimings.removeAllViews()

        for (show in shows) {
            val chip = Chip(context)
            chip.text = show.showStartTime
            holder.showTimings.addView(chip)
        }
    }

    override fun getItemCount(): Int {
        return groupedShows.size
    }
}

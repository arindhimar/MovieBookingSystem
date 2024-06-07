package com.example.combined_loginregister

import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide

class CinemaOwnerCinemaListAdapter(private var cinemaList: List<CinemaTb>) : RecyclerView.Adapter<CinemaOwnerCinemaListAdapter.OwnerCinemaViewHolder>() {

    private lateinit var onItemClickListener: OnItemClickListener

    interface OnItemClickListener {
        fun onItemClick(cinema: CinemaTb)
    }
    fun updateList(newList: List<CinemaTb>) {
        cinemaList = newList
        notifyDataSetChanged()
    }
    fun setOnItemClickListener(listener: OnItemClickListener) {
        onItemClickListener = listener
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): OwnerCinemaViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.listmoviecard, parent, false)
        return OwnerCinemaViewHolder(view)
    }

    override fun onBindViewHolder(holder: OwnerCinemaViewHolder, position: Int) {
        val cinema = cinemaList[position]

        holder.Heading.text = "Name : ${cinema.cinemaName}"
        holder.SubHeading1.text = "City : ${cinema.city}"
        Glide.with(holder.itemView.context)
            .load(cinema.cinemaPicture)
            .into(holder.MainPoster)




        holder.itemView.setOnClickListener {
            onItemClickListener.onItemClick(cinema)
        }
    }

    override fun getItemCount(): Int {
        return cinemaList.size
    }

    class OwnerCinemaViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val MainPoster: ImageView = itemView.findViewById(R.id.MainPoster)
        val Heading: TextView = itemView.findViewById(R.id.Heading)
        val SubHeading1: TextView = itemView.findViewById(R.id.subheading1)
    }
}

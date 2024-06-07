package com.example.combined_loginregister

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.core.view.isVisible
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide

class CinemaAdminDisplayAdpater(private var cinemaAdminsList: List<CinemaAdminTb>) : RecyclerView.Adapter<CinemaAdminDisplayAdpater.AdminCinemaViewHolder>() {

    private var onItemClickListener: OnItemClickListener? = null

    interface OnItemClickListener {
        fun onItemClick(cinema: CinemaAdminTb)
    }
    fun getAllItems(): List<CinemaAdminTb> {
        return cinemaAdminsList
    }

    fun updateList(newList: List<CinemaAdminTb>) {
        cinemaAdminsList = newList
        notifyDataSetChanged()
    }
    fun setOnItemClickListener(listener: OnItemClickListener) {
        onItemClickListener = listener
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): AdminCinemaViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.listmoviecard, parent, false)
        return AdminCinemaViewHolder(view)
    }

    override fun onBindViewHolder(holder: AdminCinemaViewHolder, position: Int) {
        val cinemaAdmin = cinemaAdminsList[position]
        holder.isDataLoaded = false
        val firebaseRestManager = FirebaseRestManager<UserTb>()
        firebaseRestManager.getSingleItem(UserTb::class.java, "moviedb/usertb", cinemaAdmin.userId!!) { userData ->
            if (userData != null) {
                val firebaseRestManager2 = FirebaseRestManager<CinemaOwnerTb>()
                firebaseRestManager2.getSingleItem(CinemaOwnerTb::class.java, "moviedb/CinemaOwnerTb", cinemaAdmin.cinemaOwnerId!!) { item, ->
                    if (item != null) {
                        val firebaseRestManager3 = FirebaseRestManager<CinemaTb>()
                        firebaseRestManager3.getSingleItem(CinemaTb::class.java, "moviedb/cinematb", item.cinemaId!!) { cinemaData ->
                            if (cinemaData != null) {
                                holder.Heading.isVisible = false
                                val userName = userData.uname ?: "Unknown User"
                                val cinemaName = cinemaData.cinemaName ?: "Unknown Cinema"
                                val displayText = "UserName b : $userName\nCinema : $cinemaName"
                                holder.SubHeading1.text = displayText
                                Glide.with(holder.itemView.context)
                                    .load(R.drawable.account_fill)
                                    .into(holder.MainPoster)
                                holder.itemView.isVisible = true
                                holder.isDataLoaded = true
                            }
                        }
                    }
                }
            }
        }

        holder.itemView.setOnClickListener {
            if (holder.isDataLoaded) {
                onItemClickListener?.onItemClick(cinemaAdmin)
            }
        }
    }

    override fun getItemCount(): Int {
        return cinemaAdminsList.size
    }

    class AdminCinemaViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val MainPoster: ImageView = itemView.findViewById(R.id.MainPoster)
        val Heading: TextView = itemView.findViewById(R.id.Heading)
        val SubHeading1: TextView = itemView.findViewById(R.id.subheading1)
        var isDataLoaded = false

        init {
            itemView.isVisible = false
        }
    }
}
package com.example.combined_loginregister

import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.bumptech.glide.load.engine.DiskCacheStrategy
import com.bumptech.glide.request.RequestOptions

class ShowAdapter(private val showList: List<ShowTb>) : RecyclerView.Adapter<ShowAdapter.ShowViewHolder>() {

    private lateinit var mListener: OnItemClickListener

    interface OnItemClickListener {
        fun onItemClick(show: ShowTb)
    }

    fun setOnItemClickListener(listener: OnItemClickListener) {
        mListener = listener
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ShowViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.listshowcard, parent, false)
        return ShowViewHolder(view, showList, mListener)
    }

    override fun onBindViewHolder(holder: ShowViewHolder, position: Int) {
        val show = showList[position]
        Log.d("ShowAdapter", "Processing show: ${show.showId}")

        val node = "moviedb/moviepostertb"

        val firebaseRestManager2 = FirebaseRestManager<MoviePosterTb>()
        firebaseRestManager2.getAllItems(MoviePosterTb::class.java, node) { posterItems ->
            if (posterItems.isNotEmpty()) {
                for (item2 in posterItems) {
                    if (item2.mid == show.movieId) {
                        Glide.with(holder.itemView.context)
                            .load(item2.mlink)
                            .into(holder.MainPoster)
                        break
                    }
                }
            } else {
                Log.d("Firebase", "No movie posters found")
            }
        }

        holder.showDate.text = "Date: ${show.showDate}"
        holder.showStartTime.text = "Start Time: ${show.showStartTime}"
        holder.showEndTime.text = "End Time: ${show.showEndTime}"
    }

    override fun getItemCount(): Int {
        Log.d("ShowAdapter", "getItemCount: ${showList.size}")
        return showList.size
    }

    class ShowViewHolder(itemView: View, private val showList: List<ShowTb>, private val mListener: OnItemClickListener) : RecyclerView.ViewHolder(itemView) {
        val MainPoster: ImageView = itemView.findViewById(R.id.MainPoster)
        val showDate: TextView = itemView.findViewById(R.id.showDate)
        val showStartTime: TextView = itemView.findViewById(R.id.showStartTime)
        val showEndTime: TextView = itemView.findViewById(R.id.showEndTime)

        init {
            itemView.setOnClickListener {
                mListener.onItemClick(showList[adapterPosition])
            }
        }
    }

}
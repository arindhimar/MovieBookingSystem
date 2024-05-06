package com.example.combined_loginregister

import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide

class OwnerMovieListAdapter(private val movieList: ArrayList<MovieTB>) : RecyclerView.Adapter<OwnerMovieListAdapter.OwnerMovieViewHolder>() {

    private lateinit var onItemClickListener: OnItemClickListener

    interface OnItemClickListener {
        fun onItemClick(movie: MovieTB)
    }

    fun setOnItemClickListener(listener: OnItemClickListener) {
        onItemClickListener = listener
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): OwnerMovieViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.listmoviecard, parent, false)
        return OwnerMovieViewHolder(view)
    }



    override fun onBindViewHolder(holder: OwnerMovieViewHolder, position: Int) {
        val movie = movieList[position]

        holder.Heading.text = "Name : ${movie.mname}"
        holder.SubHeading1.text = "Duration : ${movie.duration}"



        //getting all the poster of all the movies
        val moviePosterClass = MoviePosterTb::class.java
        val node = "moviedb/moviepostertb"

        val firebaseRestManager2 = FirebaseRestManager<MoviePosterTb>()
        firebaseRestManager2.getAllItems(moviePosterClass, node) { posterItems ->
            if (posterItems.isNotEmpty()) {
                for (item2 in posterItems) {
                    if (item2.mid == movie.mid) {
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

        holder.itemView.setOnClickListener {
            onItemClickListener.onItemClick(movie)
        }
    }

    override fun getItemCount(): Int {
        return movieList.size
    }

    class OwnerMovieViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val MainPoster: ImageView = itemView.findViewById(R.id.MainPoster)
        val Heading: TextView = itemView.findViewById(R.id.Heading)
        val SubHeading1: TextView = itemView.findViewById(R.id.subheading1)

    }
}
package com.example.combined_loginregister

import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide

class MovieListAdapter(private val itemList: List<MovieTB>) : RecyclerView.Adapter<MovieListAdapter.MyViewHolder>() {
    private lateinit var onItemClickListener: MovieListAdapter.OnItemClickListener


    interface OnItemClickListener {
        fun onItemClick(movie: MovieTB)
    }

    fun setOnItemClickListener(listener: OnItemClickListener) {
        onItemClickListener = listener
    }

    inner class MyViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val MainPoster: ImageView = itemView.findViewById(R.id.MainPoster)
        val Heading = itemView.findViewById<TextView>(R.id.Heading)
        val SubHeading1 = itemView.findViewById<TextView>(R.id.SubHeading1)
        val SubHeading2 = itemView.findViewById<TextView>(R.id.SubHeading2)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): MyViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.moviecard, parent, false)
        return MyViewHolder(view)
    }

    override fun onBindViewHolder(holder: MyViewHolder, position: Int) {
        val item = itemList[position]


        //getting all the poster of all the movies
        val moviePosterClass = MoviePosterTb::class.java
        val node = "moviedb/moviepostertb"

        val firebaseRestManager2 = FirebaseRestManager<MoviePosterTb>()
        firebaseRestManager2.getAllItems(moviePosterClass, node) { posterItems ->
            if (posterItems.isNotEmpty()) {
                for (item2 in posterItems) {
                    if (item2.mid == item.mid) {
                        Glide.with(holder.itemView.context)
                            .load(item2.mlink)
                            .into(holder.MainPoster)


                        holder.Heading.text = item.mname
                        holder.SubHeading1.text = item.duration + " Minutes"
                        holder.SubHeading2.visibility = View.GONE


                        break
                    }
                }
            } else {
                Log.d("Firebase", "No movie posters found")
            }
        }

        holder.itemView.setOnClickListener {
            onItemClickListener.onItemClick(item)
        }



//        holder.imageView.setImageResource(item)
    }

    override fun getItemCount() = itemList.size
}

package com.example.combined_loginregister

import android.app.AlertDialog
import android.content.Context
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide

class MovieShowsHelperClass(private var movieData: MovieTB,val  requireContext: Context) : HorizontalCalendarAdapter.OnItemClickListener {
    private lateinit var dialog: AlertDialog
    private lateinit var view: View

    private lateinit var recyclerView: RecyclerView
    private lateinit var tvDateMonth: TextView
    private lateinit var ivCalendarNext: ImageView
    private lateinit var ivCalendarPrevious: ImageView

    fun showLoadingDialog(context: Context) {
        val inflater = LayoutInflater.from(context)
        view = inflater.inflate(R.layout.movie_shows_card, null)

        val builder = AlertDialog.Builder(context)
        builder.setView(view)
        builder.setCancelable(true)

        dialog = builder.create()
        dialog.window?.attributes?.windowAnimations = R.style.DialogAnimation
        dialog.show()
        setUpCard()
    }

    fun returnView():View{
        return view
    }

    private fun setUpCard(){
        val MainPoster: ImageView = view.findViewById(R.id.MainPoster)
        val Heading = view.findViewById<TextView>(R.id.Heading)
        val SubHeading1 = view.findViewById<TextView>(R.id.SubHeading1)
        val SubHeading2 = view.findViewById<TextView>(R.id.SubHeading2)
        tvDateMonth =view.findViewById(R.id.text_date_month)
        ivCalendarNext = view.findViewById(R.id.iv_calendar_next)
        ivCalendarPrevious = view.findViewById(R.id.iv_calendar_previous)
        recyclerView = view.findViewById(R.id.recyclerView)

        recyclerView.layoutManager = LinearLayoutManager(requireContext, LinearLayoutManager.HORIZONTAL, false)

        val calendarSetUp = HorizontalCalendarSetUp()
        val tvMonth = calendarSetUp.setUpCalendarAdapter(recyclerView, this@MovieShowsHelperClass)
        tvDateMonth.text = tvMonth

        calendarSetUp.setUpCalendarPrevNextClickListener(ivCalendarNext, ivCalendarPrevious, this@MovieShowsHelperClass) {
            tvDateMonth.text = it
        }





        //getting all the poster of all the movies
        val moviePosterClass = MoviePosterTb::class.java
        val node = "moviedb/moviepostertb"

        val firebaseRestManager2 = FirebaseRestManager<MoviePosterTb>()
        firebaseRestManager2.getAllItems(moviePosterClass, node) { posterItems ->
            if (posterItems.isNotEmpty()) {
                for (item2 in posterItems) {
                    if (item2.mid == movieData.mid) {
                        Glide.with(view.context)
                            .load(item2.mlink)
                            .into(MainPoster)


                        Heading.text = movieData.mname
                        SubHeading1.text = movieData.duration + " Minutes"
                        break
                    }
                }
            } else {
                Log.d("Firebase", "No movie posters found")
            }
        }

    }

    fun dismissLoadingDialog() {
        if (dialog.isShowing) {
            dialog.dismiss()
        }
    }

    override fun onItemClick(ddMmYy: String, dd: String, day: String) {
        TODO("Not yet implemented")
    }
}
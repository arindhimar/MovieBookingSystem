package com.example.combined_loginregister

import android.app.AlertDialog
import android.content.Context
import android.os.Build
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.widget.ImageView
import android.widget.TextView
import androidx.annotation.RequiresApi
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import java.text.SimpleDateFormat
import java.util.Locale
import java.util.TimeZone
import java.util.Calendar

class MovieShowsHelperClass(private var movieData: MovieTB, val requireContext: Context) : HorizontalCalendarAdapter.OnItemClickListener, CinemaShowsAdapter.OnChipClickListener {
    private lateinit var dialog: AlertDialog
    private lateinit var view: View

    private lateinit var recyclerView: RecyclerView
    private lateinit var tvDateMonth: TextView
    private lateinit var ivCalendarNext: ImageView
    private lateinit var ivCalendarPrevious: ImageView
    private lateinit var showsHere: RecyclerView

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

    fun returnView(): View {
        return view
    }

    private fun setUpCard() {
        val mainPoster: ImageView = view.findViewById(R.id.MainPoster)
        val heading = view.findViewById<TextView>(R.id.Heading)
        val subHeading1 = view.findViewById<TextView>(R.id.SubHeading1)
        val subHeading2 = view.findViewById<TextView>(R.id.SubHeading2)
        tvDateMonth = view.findViewById(R.id.text_date_month)
        ivCalendarNext = view.findViewById(R.id.iv_calendar_next)
        ivCalendarPrevious = view.findViewById(R.id.iv_calendar_previous)
        recyclerView = view.findViewById(R.id.recyclerView)
        showsHere = view.findViewById(R.id.ShowsHere)

        subHeading2.visibility = View.GONE

        recyclerView.layoutManager = LinearLayoutManager(requireContext, LinearLayoutManager.HORIZONTAL, false)
        showsHere.layoutManager = LinearLayoutManager(requireContext)

        val calendarSetUp = HorizontalCalendarSetUp()
        val tvMonth = calendarSetUp.setUpCalendarAdapter(recyclerView, this@MovieShowsHelperClass)
        tvDateMonth.text = tvMonth

        calendarSetUp.setUpCalendarPrevNextClickListener(ivCalendarNext, ivCalendarPrevious, this@MovieShowsHelperClass) {
            tvDateMonth.text = it
        }

        // getting all the posters of all the movies
        val moviePosterClass = MoviePosterTb::class.java
        val node = "moviedb/moviepostertb"

        val firebaseRestManager2 = FirebaseRestManager<MoviePosterTb>()
        firebaseRestManager2.getAllItems(moviePosterClass, node) { posterItems ->
            if (posterItems.isNotEmpty()) {
                for (item2 in posterItems) {
                    if (item2.mid == movieData.mid) {
                        Glide.with(view.context)
                            .load(item2.mlink)
                            .into(mainPoster)

                        heading.text = movieData.mname
                        subHeading1.text = "${movieData.duration} Minutes"
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
        val selectedDate = SimpleDateFormat("dd MMM yyyy", Locale.getDefault()).parse(ddMmYy)
        val currentDate = Calendar.getInstance(TimeZone.getTimeZone("Asia/Kolkata"))

        // Check if the selected date is in the past
        if (selectedDate.before(currentDate.time)) {
            // If the selected date is in the past, force select the current date
            val today = currentDate.time
            val formattedToday = SimpleDateFormat("dd MMM yyyy", Locale.getDefault()).format(today)
            val currentDay = currentDate.get(Calendar.DAY_OF_MONTH).toString()
            val currentMonthYear = SimpleDateFormat("MMMM yyyy", Locale.getDefault()).format(today)

            // Update the calendar UI to show today's date
            tvDateMonth.text = currentMonthYear
            recyclerView.smoothScrollToPosition(currentDate.get(Calendar.DAY_OF_MONTH) - 1)

            // Perform the same logic as if today's date was selected
            val dateFormat = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
            val formattedSelectedDate = dateFormat.format(today)

            val firebaseRestManager = FirebaseRestManager<ShowTb>()
            firebaseRestManager.getAllItems(ShowTb::class.java, "moviedb/showtb") { showTbs ->
                // Get the current time in India
                val indiaTimeZone = TimeZone.getTimeZone("Asia/Kolkata")
                val indiaCalendar = Calendar.getInstance(indiaTimeZone)
                val currentTime = indiaCalendar.time

                // Filter shows by today's date and ensure they are at least 15 minutes from now
                val filteredShows = showTbs.filter { it.showDate == formattedSelectedDate && it.movieId == movieData.mid }
                    .filter { show ->
                        true
                    }

                //                        val showStartTimeFormat = SimpleDateFormat("yyyy-MM-dd HH:mm", Locale.getDefault())
//                        showStartTimeFormat.timeZone = indiaTimeZone
//                        val showStartTime = showStartTimeFormat.parse("${show.showDate} ${show.showStartTime}")
//                        showStartTime?.let { it.time - currentTime.time >= 15 * 60 * 1000 } ?: false

                // Group shows by cinema ID
                val groupedShows = filteredShows.groupBy { it.cinemaId }

                Log.d("TAG", "onItemClick: $groupedShows")
                // Use the groupedShows to set up your adapter or process further

                val adapter = CinemaShowsAdapter(groupedShows, requireContext, this)
                showsHere.adapter = adapter
            }
        } else {
            // If the selected date is today or in the future, proceed with the normal logic
            val dateFormat = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
            val formattedSelectedDate = dateFormat.format(selectedDate)

            val firebaseRestManager = FirebaseRestManager<ShowTb>()
            firebaseRestManager.getAllItems(ShowTb::class.java, "moviedb/showtb") { showTbs ->
                // Get the current time in India
                val indiaTimeZone = TimeZone.getTimeZone("Asia/Kolkata")
                val indiaCalendar = Calendar.getInstance(indiaTimeZone)
                val currentTime = indiaCalendar.time

                // Filter shows by the selected date and ensure they are at least 15 minutes from now
                val filteredShows = showTbs.filter { it.showDate == formattedSelectedDate && it.movieId == movieData.mid }
                    .filter { show ->
                        val showStartTimeFormat = SimpleDateFormat("yyyy-MM-dd HH:mm", Locale.getDefault())
                        showStartTimeFormat.timeZone = indiaTimeZone
                        val showStartTime = showStartTimeFormat.parse("${show.showDate} ${show.showStartTime}")
                        showStartTime?.let { it.time - currentTime.time >= 15 * 60 * 1000 } ?: false
                    }

                // Group shows by cinema ID
                val groupedShows = filteredShows.groupBy { it.cinemaId }

                Log.d("TAG", "onItemClick: $groupedShows")
                // Use the groupedShows to set up your adapter or process further

                val adapter = CinemaShowsAdapter(groupedShows, requireContext, this)
                showsHere.adapter = adapter
            }
        }
    }

    @RequiresApi(Build.VERSION_CODES.O)
    override fun onChipClick(show: ShowTb) {
        val seatManager = SeatManager(show,requireContext)
        seatManager.showLoadingDialog(requireContext)
    }
}

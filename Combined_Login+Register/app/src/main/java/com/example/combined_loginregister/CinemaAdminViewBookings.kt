package com.example.combined_loginregister

import android.os.Build
import android.os.Bundle
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.annotation.RequiresApi
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.arjungupta08.horizontal_calendar_date.HorizontalCalendarAdapter
import com.arjungupta08.horizontal_calendar_date.HorizontalCalendarSetUp
import com.example.combined_loginregister.databinding.FragmentCinemaAdminViewBookingsBinding
import com.google.firebase.auth.FirebaseAuth
import java.text.SimpleDateFormat
import java.util.Locale
import java.util.Calendar
import java.util.Date

private const val ARG_PARAM1 = "param1"
private const val ARG_PARAM2 = "param2"

class CinemaAdminViewBookings : Fragment(), HorizontalCalendarAdapter.OnItemClickListener {
    private var param1: String? = null
    private var param2: String? = null
    lateinit var binding: FragmentCinemaAdminViewBookingsBinding
    private lateinit var recyclerView: RecyclerView
    private lateinit var tvDateMonth: TextView
    private lateinit var ivCalendarNext: ImageView
    private lateinit var ivCalendarPrevious: ImageView
    private lateinit var cinemaOwnerId: String
    private lateinit var cinemaId: String

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        arguments?.let {
            param1 = it.getString(ARG_PARAM1)
            param2 = it.getString(ARG_PARAM2)
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = FragmentCinemaAdminViewBookingsBinding.inflate(layoutInflater)

        tvDateMonth = binding.textDateMonth
        ivCalendarNext = binding.ivCalendarNext
        ivCalendarPrevious = binding.ivCalendarPrevious

        binding.recyclerView.layoutManager = LinearLayoutManager(requireContext(), LinearLayoutManager.HORIZONTAL, false)

        val calendarSetUp = HorizontalCalendarSetUp()
        val tvMonth = calendarSetUp.setUpCalendarAdapter(binding.recyclerView, this@CinemaAdminViewBookings)
        tvDateMonth.text = tvMonth

        calendarSetUp.setUpCalendarPrevNextClickListener(ivCalendarNext, ivCalendarPrevious, this@CinemaAdminViewBookings) {
            tvDateMonth.text = it
        }
        cinemaOwnerId = null.toString()
        cinemaId = null.toString()
        loadInitialData()

        return binding.root
    }

    private fun loadInitialData() {
        val firebaseRestManager1 = FirebaseRestManager<CinemaAdminTb>()
        firebaseRestManager1.getAllItems(CinemaAdminTb::class.java, "moviedb/cinemaadmintb") { cinemaAdminTbs ->
            val cinemaAdmin = cinemaAdminTbs.find { it.userId == FirebaseAuth.getInstance().currentUser!!.uid }
            cinemaAdmin?.let {
                cinemaOwnerId = it.cinemaOwnerId.toString()
                val firebaseRestManager2 = FirebaseRestManager<CinemaOwnerTb>()
                firebaseRestManager2.getAllItems(CinemaOwnerTb::class.java, "moviedb/CinemaOwnerTb") { cinemaOwnerTbs ->
                    val cinemaOwner = cinemaOwnerTbs.find { it.cinemaOwnerId == cinemaOwnerId }
                    cinemaId = cinemaOwner?.cinemaId.toString()
                }
            }
        }
    }

    companion object {
        @JvmStatic
        fun newInstance(param1: String, param2: String) =
            CinemaAdminViewBookings().apply {
                arguments = Bundle().apply {
                    putString(ARG_PARAM1, param1)
                    putString(ARG_PARAM2, param2)
                }
            }
    }

    override fun onItemClick(ddMmYy: String, dd: String, day: String) {
        val selectedDate = SimpleDateFormat("dd MMM yyyy", Locale.getDefault()).parse(ddMmYy)
        val dateFormat = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
        val formattedSelectedDate = dateFormat.format(selectedDate)
        val currentDate = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(Date())
        val currentDateTime = Calendar.getInstance().time

        val firebaseRestManager = FirebaseRestManager<ShowTb>()
        firebaseRestManager.getAllItems(ShowTb::class.java, "moviedb/showtb") { showTbs ->
            val filteredShows = showTbs.filter { show ->
                when {
                    formattedSelectedDate < currentDate -> false
                    formattedSelectedDate == currentDate -> isShowTimeValid(show, currentDateTime)
                    else -> show.showDate == formattedSelectedDate
                }
            }

            Log.d("TAG", "onItemClick: $filteredShows")

            val showAdapter = ShowAdapter(filteredShows)
            showAdapter.setOnItemClickListener(object : ShowAdapter.OnItemClickListener {
                @RequiresApi(Build.VERSION_CODES.O)
                override fun onItemClick(show: ShowTb) {
                    Log.d("TAG", "onItemClick: $show")
                    val seatManager = SeatManager(show, requireContext())
                    seatManager.showLoadingDialog(requireContext())
                }
            })
            binding.ShowsHere.adapter = showAdapter
        }
    }

    private fun isShowTimeValid(show: ShowTb, currentDateTime: Date): Boolean {
        val showTimeFormat = SimpleDateFormat("HH:mm:ss", Locale.getDefault())
        val showDateTimeFormat = SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault())

        return try {
            val showDateTime = showDateTimeFormat.parse("${show.showDate} ${show.showStartTime}")
            showDateTime?.let {
                val timeDifference = it.time - currentDateTime.time
                timeDifference >= 5 * 60 * 1000 // 5 minutes in milliseconds
            } ?: false
        } catch (e: Exception) {
            false
        }
    }
}

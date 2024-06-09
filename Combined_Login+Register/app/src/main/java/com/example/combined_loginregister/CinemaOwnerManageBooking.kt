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
import com.example.combined_loginregister.databinding.FragmentCInemaAdminManageShowsBinding
import com.google.firebase.auth.FirebaseAuth
import java.text.SimpleDateFormat
import java.util.Locale

private const val ARG_PARAM1 = "param1"
private const val ARG_PARAM2 = "param2"

class CinemaOwnerManageBooking : Fragment(), HorizontalCalendarAdapter.OnItemClickListener {
    private var param1: String? = null
    private var param2: String? = null
    private lateinit var binding: FragmentCInemaAdminManageShowsBinding
    private lateinit var tvDateMonth: TextView
    private lateinit var ivCalendarNext: ImageView
    private lateinit var ivCalendarPrevious: ImageView
    private var cinemaOwnerIds: MutableList<String> = mutableListOf()
    private var cinemaIds: MutableList<String> = mutableListOf()

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
        binding = FragmentCInemaAdminManageShowsBinding.inflate(layoutInflater, container, false)
        tvDateMonth = binding.textDateMonth
        ivCalendarNext = binding.ivCalendarNext
        ivCalendarPrevious = binding.ivCalendarPrevious
        binding.recyclerView.layoutManager = LinearLayoutManager(requireContext(), LinearLayoutManager.HORIZONTAL, false)

        val calendarSetUp = HorizontalCalendarSetUp()
        val tvMonth = calendarSetUp.setUpCalendarAdapter(binding.recyclerView, this@CinemaOwnerManageBooking)
        tvDateMonth.text = tvMonth

        calendarSetUp.setUpCalendarPrevNextClickListener(ivCalendarNext, ivCalendarPrevious, this@CinemaOwnerManageBooking) {
            tvDateMonth.text = it
        }

        loadInitialData()

        return binding.root
    }

    private fun loadInitialData() {
        val firebaseRestManager = FirebaseRestManager<CinemaOwnerTb>()
        val currentUser = FirebaseAuth.getInstance().currentUser

        firebaseRestManager.getAllItems(CinemaOwnerTb::class.java, "moviedb/CinemaOwnerTb") { cinemaOwnerTbs ->
            val cinemaOwners = cinemaOwnerTbs.filter { it.uid == currentUser?.uid }
            cinemaOwnerIds = cinemaOwners.map { it.cinemaOwnerId ?: "" }.toMutableList()
            cinemaIds = cinemaOwners.map { it.cinemaId ?: "" }.toMutableList()
        }
    }

    override fun onItemClick(ddMmYy: String, dd: String, day: String) {
        val selectedDate = SimpleDateFormat("dd MMM yyyy", Locale.getDefault()).parse(ddMmYy)
        val dateFormat = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
        val formattedSelectedDate = dateFormat.format(selectedDate!!)

        val firebaseRestManager = FirebaseRestManager<ShowTb>()
        firebaseRestManager.getAllItems(ShowTb::class.java, "moviedb/showtb") { showTbs ->
            val filteredShows = showTbs.filter { cinemaIds.contains(it.cinemaId) && it.showDate == formattedSelectedDate }

            Log.d("TAG", "onItemClick: $filteredShows")
            val showAdapter = ShowAdapter(filteredShows)
            showAdapter.setOnItemClickListener(object : ShowAdapter.OnItemClickListener {
                @RequiresApi(Build.VERSION_CODES.O)
                override fun onItemClick(show: ShowTb) {
                    val seatManager = SeatManager(show, requireContext())
                    seatManager.showLoadingDialog(requireContext())
                }
            })
            binding.ShowsHere.adapter = showAdapter
        }
    }

    companion object {
        @JvmStatic
        fun newInstance(param1: String, param2: String) =
            CinemaOwnerManageBooking().apply {
                arguments = Bundle().apply {
                    putString(ARG_PARAM1, param1)
                    putString(ARG_PARAM2, param2)
                }
            }
    }
}

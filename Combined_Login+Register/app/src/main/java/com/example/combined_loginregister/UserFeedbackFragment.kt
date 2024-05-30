package com.example.combined_loginregister

import android.os.Bundle
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.combined_loginregister.BookingTb
import com.example.combined_loginregister.FirebaseRestManager
import com.example.combined_loginregister.ShowTb
import com.example.combined_loginregister.databinding.FragmentUserFeedbackBinding
import com.google.firebase.auth.FirebaseAuth
import java.text.SimpleDateFormat
import java.util.*

private const val ARG_PARAM1 = "param1"
private const val ARG_PARAM2 = "param2"

class UserFeedbackFragment : Fragment() {
    private var param1: String? = null
    private var param2: String? = null
    lateinit var binding: FragmentUserFeedbackBinding
    private val endedBookings = mutableListOf<BookingTb>()
    private lateinit var adapter: TicketDisplayAdapter

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
        binding = FragmentUserFeedbackBinding.inflate(layoutInflater)
        setupRecyclerView()
        fetchTicketDetails()
        return binding.root
    }

    private fun setupRecyclerView() {
        adapter = TicketDisplayAdapter(endedBookings)
        binding.TicketCardsHere.adapter = adapter
        binding.TicketCardsHere.layoutManager = LinearLayoutManager(requireContext())

        adapter.setOnItemClickListener(object : TicketDisplayAdapter.OnItemClickListener {
            override fun onItemClick(booking: BookingTb) {
                val feedbackDialogHelper = FeedbackDialogHelper(booking.showId)
                feedbackDialogHelper.showFeedbackDialog(requireContext())
            }
        })
    }

    private fun fetchTicketDetails() {
        val firebaseRestManager = FirebaseRestManager<BookingTb>()
        firebaseRestManager.getAllItems(BookingTb::class.java, "moviedb/bookingtb") { bookingList ->
            if (bookingList.isNotEmpty()) {
                val userBookings = bookingList.filter { it.userId == FirebaseAuth.getInstance().currentUser?.uid }
                userBookings.forEach { booking ->
                    fetchShowDetails(booking)
                }
            }
        }
    }

    private fun fetchShowDetails(booking: BookingTb) {
        val firebaseRestManager = FirebaseRestManager<ShowTb>()
        firebaseRestManager.getSingleItem(ShowTb::class.java, "moviedb/showtb", booking.showId) { show ->
            show?.let {
                val currentTime = Calendar.getInstance().time
                val showEndTime = parseTime(it.showEndTime)
                if (showEndTime.before(currentTime)) {
                    // Show has ended, add booking to the endedBookings list
                    endedBookings.add(booking)
                    updateUI()
                }
            }
        }
    }

    private fun parseTime(time: String): Date {
        val dateFormat = SimpleDateFormat("HH:mm", Locale.getDefault())
        return dateFormat.parse(time) ?: Date()
    }

    private fun updateUI() {
        // Notify the adapter about data changes
        adapter.notifyDataSetChanged()
        Log.d("TAG", "Ended bookings: ${endedBookings.joinToString("\n") { it.bookingId }}")
    }

    companion object {
        @JvmStatic
        fun newInstance(param1: String, param2: String) =
            UserFeedbackFragment().apply {
                arguments = Bundle().apply {
                    putString(ARG_PARAM1, param1)
                    putString(ARG_PARAM2, param2)
                }
            }
    }
}

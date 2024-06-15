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
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import com.google.firebase.database.ktx.database
import com.google.firebase.ktx.Firebase
import java.text.SimpleDateFormat
import java.util.*

private const val ARG_PARAM1 = "param1"
private const val ARG_PARAM2 = "param2"

class UserFeedbackFragment : Fragment() {
    private var param1: String? = null
    private var param2: String? = null
    lateinit var binding: FragmentUserFeedbackBinding
    private val endedBookings = mutableListOf<BookingTb>()
    private val uniqueShowIds = mutableSetOf<String>()
    private lateinit var adapter: TicketDisplayAdapter
    private lateinit var noDataDialogHelper: NoDataDialogHelper
    private var noDataDialogShown = false // Flag to track if the no data dialog has been shown

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
        noDataDialogHelper = NoDataDialogHelper() // Initialize here
        setupRecyclerView()
        fetchTicketDetails()
        listenForFeedbackChanges()
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
        val loadingDialogHelper = LoadingDialogHelper()
        loadingDialogHelper.showLoadingDialog(requireContext())
        val firebaseRestManager = FirebaseRestManager<BookingTb>()
        firebaseRestManager.getAllItems(BookingTb::class.java, "moviedb/bookingtb") { bookingList ->
            if (bookingList.isNotEmpty()) {
                val userBookings = bookingList.filter { it.userId == FirebaseAuth.getInstance().currentUser?.uid }
                if (userBookings.isNotEmpty()) {
                    userBookings.forEach { booking ->
                        if (!uniqueShowIds.contains(booking.showId)) {
                            uniqueShowIds.add(booking.showId)
                            fetchShowDetails(booking)
                        }
                    }
                } else {
                    showNoDataDialog()
                }
            } else {
                showNoDataDialog()
            }
            // Dismiss the loading dialog after data retrieval is complete
            loadingDialogHelper.dismissLoadingDialog()
        }
    }

    private fun fetchShowDetails(booking: BookingTb) {
        val firebaseRestManager = FirebaseRestManager<ShowTb>()
        firebaseRestManager.getSingleItem(ShowTb::class.java, "moviedb/showtb", booking.showId) { show ->
            show?.let { showdata ->
                val currentDate = Calendar.getInstance().time
                val showDate = parseDate(showdata.showDate, "yyyy-MM-dd")

                if (showDate != null) {
                    if (!showDate.after(currentDate)) {
                        if (showDate == currentDate) {
                            val currentTime = Calendar.getInstance().time
                            val showEndTime = parseDate(showdata.showEndTime, "HH:mm")
                            if (showEndTime != null && showEndTime.before(currentTime)) {
                                addBookingIfNoFeedback(booking)
                            }
                        } else {
                            addBookingIfNoFeedback(booking)
                        }
                    }
                }
            }
        }
    }

    private fun addBookingIfNoFeedback(booking: BookingTb) {
        val feedbackFirebaseRestManager = FirebaseRestManager<FeedbackTb>()
        feedbackFirebaseRestManager.getAllItems(FeedbackTb::class.java, "moviedb/feedbacktb") { feedbackList ->
            if (feedbackList.isNotEmpty()) {
                val existingFeedback = feedbackList.find { it.showId == booking.showId && it.userId == booking.userId }
                if (existingFeedback == null && !endedBookings.contains(booking)) {
                    endedBookings.add(booking)
                    updateUI()
                    if (noDataDialogShown) {
                        noDataDialogHelper.dismissNoDataDialog()
                        noDataDialogShown = false
                    }
                }
            } else {
                if (!endedBookings.contains(booking)) {
                    endedBookings.add(booking)
                    updateUI()
                    if (noDataDialogShown) {
                        noDataDialogHelper.dismissNoDataDialog()
                        noDataDialogShown = false
                    }
                }
            }

            if (endedBookings.isEmpty() && !noDataDialogShown) {
                showNoDataDialog()
            }
        }
    }

    private fun parseDate(dateStr: String, format: String): Date? {
        return try {
            val dateFormat = SimpleDateFormat(format, Locale.getDefault())
            dateFormat.parse(dateStr)
        } catch (e: Exception) {
            null
        }
    }

    private fun updateUI() {
        adapter.notifyDataSetChanged()
        Log.d("TAG", "Ended bookings: ${endedBookings.joinToString("\n") { it.bookingId }}")
    }

    private fun listenForFeedbackChanges() {
        val firebaseDatabase = FirebaseDatabase.getInstance().reference
        val rootRef = firebaseDatabase.child("moviedb/feedbacktb")
        rootRef.addValueEventListener(object : ValueEventListener {
            override fun onDataChange(dataSnapshot: DataSnapshot) {
                // This method will be called whenever there is a change in the data
                Log.d("TAG", "onDataChange: Feedback data changed")
                endedBookings.clear()
                uniqueShowIds.clear()
                adapter.notifyDataSetChanged()
                fetchTicketDetails()
            }

            override fun onCancelled(error: DatabaseError) {
                // This method will be called if there is an error
                Log.e("TAG", "Error: ${error.message}")
            }
        })
    }

    private fun showNoDataDialog() {
        noDataDialogHelper.showNoDataDialog(requireContext())
        noDataDialogHelper.hideButtons()
        noDataDialogHelper.updateText("No pending feedback found!! Start booking tickets to give feedback and rate your experience.")
        noDataDialogShown = true // Set the flag to true
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

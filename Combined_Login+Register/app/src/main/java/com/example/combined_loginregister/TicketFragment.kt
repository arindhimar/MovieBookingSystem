package com.example.combined_loginregister

import android.os.Bundle
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.combined_loginregister.databinding.FragmentTicketBinding
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener

private const val ARG_PARAM1 = "param1"
private const val ARG_PARAM2 = "param2"

class TicketFragment : Fragment() {
    private var param1: String? = null
    private var param2: String? = null
    lateinit var binding: FragmentTicketBinding
    private lateinit var noDataDialogHelper: NoDataDialogHelper
    private var isNoDataDialogShown: Boolean = false

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
    ): View? {
        binding = FragmentTicketBinding.inflate(inflater, container, false)

        noDataDialogHelper = NoDataDialogHelper() // Initialize here

        loadUserTickets()

        setUpRealTimeDatabase()

        return binding.root
    }

    private fun setUpRealTimeDatabase() {
        val firebaseDatabase = FirebaseDatabase.getInstance().reference
        val rootRef = firebaseDatabase.child("moviedb/bookingtb")
        rootRef.addValueEventListener(object : ValueEventListener {
            override fun onDataChange(dataSnapshot: DataSnapshot) {
                Log.d("TAG", "onDataChange: data changed ")
                loadUserTickets()
            }

            override fun onCancelled(error: DatabaseError) {
                Log.e("TAG", "Error: ${error.message}")
            }
        })
    }

    private fun loadUserTickets() {
        val loadingDialogHelper = LoadingDialogHelper()
        loadingDialogHelper.showLoadingDialog(requireContext())

        val firebaseRestManager = FirebaseRestManager<BookingTb>()
        firebaseRestManager.getAllItems(BookingTb::class.java, "moviedb/bookingtb") { bookingTb ->

            val userTickets = bookingTb.filter { it.userId == FirebaseAuth.getInstance().currentUser!!.uid }

            if (userTickets.isNotEmpty()) {
                isNoDataDialogShown = false // Reset flag if tickets are found

                // Sort the tickets by their combined date and time in descending order
                val sortedTickets = userTickets.sortedByDescending { it.getCombinedDateTime() }

                val adapter = TicketDisplayAdapter(sortedTickets)
                Log.d("TAG", "displayCinemaOwner: $sortedTickets")
                adapter.setOnItemClickListener(object : TicketDisplayAdapter.OnItemClickListener {
                    override fun onItemClick(booking: BookingTb) {
                        // Handle item click
                    }
                })

                binding.TicketCardsHere.adapter = adapter
                binding.TicketCardsHere.layoutManager = LinearLayoutManager(requireContext())

            } else {
                showNoDataDialog()
            }

            loadingDialogHelper.dismissLoadingDialog()
        }
    }

    private fun showNoDataDialog() {
        if (!isNoDataDialogShown) {
            noDataDialogHelper.showNoDataDialog(requireContext())
            noDataDialogHelper.hideButtons()
            noDataDialogHelper.updateText("No pending tickets found!! Start booking tickets to give feedback and rate your experience.")
            isNoDataDialogShown = true // Set flag to true after showing the dialog
        }
    }

    companion object {
        @JvmStatic
        fun newInstance(param1: String, param2: String) =
            TicketFragment().apply {
                arguments = Bundle().apply {
                    putString(ARG_PARAM1, param1)
                    putString(ARG_PARAM2, param2)
                }
            }
    }
}

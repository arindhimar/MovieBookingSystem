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

// TODO: Rename parameter arguments, choose names that match
// the fragment initialization parameters, e.g. ARG_ITEM_NUMBER
private const val ARG_PARAM1 = "param1"
private const val ARG_PARAM2 = "param2"

/**
 * A simple [Fragment] subclass.
 * Use the [TicketFragment.newInstance] factory method to
 * create an instance of this fragment.
 */
class TicketFragment : Fragment() {
    // TODO: Rename and change types of parameters
    private var param1: String? = null
    private var param2: String? = null
    lateinit var binding: FragmentTicketBinding

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
        // Inflate the layout for this fragment
        binding = FragmentTicketBinding.inflate(inflater, container, false)


        loadUserTickets()



        return binding.root
    }

    private fun loadUserTickets() {
        val firebaseRestManager = FirebaseRestManager<BookingTb>()
        firebaseRestManager.getAllItems(BookingTb::class.java, "moviedb/bookingtb") { bookingTb ->
            val userTickets = bookingTb.filter { it.userId == FirebaseAuth.getInstance().currentUser!!.uid }

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
        }
    }

    companion object {
        /**
         * Use this factory method to create a new instance of
         * this fragment using the provided parameters.
         *
         * @param param1 Parameter 1.
         * @param param2 Parameter 2.
         * @return A new instance of fragment TicketFragment.
         */
        // TODO: Rename and change types and number of parameters
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
package com.example.combined_loginregister

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.os.Build
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.AdapterView
import android.widget.ArrayAdapter
import android.widget.Toast
import androidx.annotation.RequiresApi
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.ItemTouchHelper
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.arjungupta08.horizontal_calendar_date.HorizontalCalendarAdapter
import com.example.combined_loginregister.databinding.FragmentUserMainBinding
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Locale
import java.util.TimeZone

private const val ARG_PARAM1 = "param1"
private const val ARG_PARAM2 = "param2"

class UserMainFragment : Fragment(), HorizontalCalendarAdapter.OnItemClickListener {
    private var param1: String? = null
    private var param2: String? = null
    private lateinit var binding: FragmentUserMainBinding

    private val bookingReceiver = BookingReceiver()
    private var currentCity: String? = null
    private var movieList: MutableList<MovieTB> = mutableListOf()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        arguments?.let {
            param1 = it.getString(ARG_PARAM1)
            param2 = it.getString(ARG_PARAM2)
        }
    }

    @RequiresApi(Build.VERSION_CODES.O)
    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = FragmentUserMainBinding.inflate(inflater, container, false)



        setupLocationSpinner()
        setupSearchBar()

        return binding.root
    }

    override fun onDestroyView() {
        super.onDestroyView()
    }

    private fun setupLocationSpinner() {
        val cityAdapter = ArrayAdapter.createFromResource(
            requireContext(),
            R.array.city_name,
            R.layout.spinner_item
        )
        cityAdapter.setDropDownViewResource(R.layout.spinner_item)
        binding.LocationSelector.adapter = cityAdapter

        binding.LocationSelector.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
            override fun onItemSelected(parent: AdapterView<*>?, view: View?, position: Int, id: Long) {
                currentCity = parent?.getItemAtPosition(position).toString()
                currentCity?.let { findShows(it) }
            }

            override fun onNothingSelected(parent: AdapterView<*>?) {
                // Do nothing
            }
        }
    }

    private fun setupSearchBar() {
        binding.searchBar.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {
                // Do something before the text is changed
            }

            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
                // Do something when the text is being changed
            }

            override fun afterTextChanged(s: Editable?) {
                // Do something after the text has changed
                val searchText = s.toString()
                performSearch(searchText)
            }
        })
    }

    private fun performSearch(query: String) {
        if (query.isNotEmpty()) {
            val filteredMovies = movieList.filter { it.mname!!.contains(query, true) }
            setupRecyclerView(filteredMovies.toMutableList())
        } else {
            setupRecyclerView(movieList)
        }
    }

    private fun findShows(city: String) {
        val loadingDialogHelper = LoadingDialogHelper()
        loadingDialogHelper.showLoadingDialog(requireContext())

        val firebaseRestManager2 = FirebaseRestManager<CinemaTb>()
        firebaseRestManager2.getAllItems(CinemaTb::class.java, "moviedb/cinematb") { cinemaItems ->
            val cinemaList = cinemaItems.filter { it.city == city }
            val cinemaIds = cinemaList.map { it.cinemaID }

            val firebaseRestManager1 = FirebaseRestManager<ShowTb>()
            firebaseRestManager1.getAllItems(ShowTb::class.java, "moviedb/showtb") { showItems ->
                if (showItems.isNotEmpty()) {
                    val relevantShows = showItems.filter { it.cinemaId in cinemaIds }

                    // Get current date and time in IST
                    val istTimeZone = TimeZone.getTimeZone("Asia/Kolkata")
                    val currentISTTime = Calendar.getInstance(istTimeZone)

                    // Filter shows that are from today or future in IST
                    val validShows = relevantShows.filter { show ->
                        val showDateCalendar = Calendar.getInstance()
                        showDateCalendar.time = SimpleDateFormat(
                            "yyyy-MM-dd",
                            Locale.getDefault()
                        ).parse(show.showDate)!!

                        // Set the time of the show date to 00:00:00
                        showDateCalendar.set(Calendar.HOUR_OF_DAY, 0)
                        showDateCalendar.set(Calendar.MINUTE, 0)
                        showDateCalendar.set(Calendar.SECOND, 0)
                        showDateCalendar.set(Calendar.MILLISECOND, 0)

                        // Get the current date without time
                        val currentISTDate = Calendar.getInstance(TimeZone.getTimeZone("Asia/Kolkata"))
                        currentISTDate.set(Calendar.HOUR_OF_DAY, 0)
                        currentISTDate.set(Calendar.MINUTE, 0)
                        currentISTDate.set(Calendar.SECOND, 0)
                        currentISTDate.set(Calendar.MILLISECOND, 0)

                        // Compare the show date with the current date
                        !showDateCalendar.before(currentISTDate)
                    }

                    val movieIds = validShows.map { it.movieId }.distinct()
                    Log.d("movieList", movieIds.toString())

                    val firebaseRestManager3 = FirebaseRestManager<MovieTB>()
                    firebaseRestManager3.getAllItems(MovieTB::class.java, "moviedb/movietb") { movieItems ->
                        if (movieItems.isNotEmpty()) {
                            movieList = movieItems.filter { it.mid in movieIds }.toMutableList()
                            Log.d("movieList", movieList.toString())
                            setupRecyclerView(movieList)
                            loadingDialogHelper.dismissLoadingDialog()
                        }
                    }
                }
            }
        }
    }

    private fun setupRecyclerView(movieList: MutableList<MovieTB>) {
        val adapter = MovieListAdapter(movieList)
        adapter.setOnItemClickListener(object : MovieListAdapter.OnItemClickListener {
            override fun onItemClick(movie: MovieTB) {
                val movieShowsHelperClass = MovieShowsHelperClass(movie, requireContext())
                movieShowsHelperClass.showLoadingDialog(requireContext())
            }
        })

        binding.horizontalRecyclerView.layoutManager = LinearLayoutManager(context, LinearLayoutManager.HORIZONTAL, false)
        binding.horizontalRecyclerView.adapter = adapter

        val itemTouchHelper = ItemTouchHelper(object : ItemTouchHelper.SimpleCallback(0, ItemTouchHelper.LEFT or ItemTouchHelper.RIGHT) {
            override fun onMove(
                recyclerView: RecyclerView,
                viewHolder: RecyclerView.ViewHolder,
                target: RecyclerView.ViewHolder
            ): Boolean = false

            override fun onSwiped(viewHolder: RecyclerView.ViewHolder, direction: Int) {
                val position = viewHolder.adapterPosition
                movieList.removeAt(position)
                adapter.notifyItemRemoved(position)
            }
        })

        itemTouchHelper.attachToRecyclerView(binding.horizontalRecyclerView)
    }



    companion object {
        @JvmStatic
        fun newInstance(param1: String, param2: String) =
            UserMainFragment().apply {
                arguments = Bundle().apply {
                    putString(ARG_PARAM1, param1)
                    putString(ARG_PARAM2, param2)
                }
            }
    }

    override fun onItemClick(ddMmYy: String, dd: String, day: String) {
        // TODO: Implement calendar item click handling
    }
}

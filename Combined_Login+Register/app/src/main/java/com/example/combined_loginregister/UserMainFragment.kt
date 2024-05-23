package com.example.combined_loginregister

import android.graphics.Canvas
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.os.Bundle
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
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

class UserMainFragment : Fragment() , HorizontalCalendarAdapter.OnItemClickListener{
    private var param1: String? = null
    private var param2: String? = null
    private lateinit var binding: FragmentUserMainBinding

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
        binding = FragmentUserMainBinding.inflate(inflater, container, false)
        findShows("Surat")
        return binding.root
    }


    private fun findShows(city: String) {
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

                        showDateCalendar.set(Calendar.HOUR_OF_DAY, 0)
                        showDateCalendar.set(Calendar.MINUTE, 0)
                        showDateCalendar.set(Calendar.SECOND, 0)
                        showDateCalendar.set(Calendar.MILLISECOND, 0)

                        !showDateCalendar.before(currentISTTime)
                    }

                    val movieIds = validShows.map { it.movieId }.distinct()
                    Log.d("movieList", movieIds.toString())

                    val firebaseRestManager3 = FirebaseRestManager<MovieTB>()
                    firebaseRestManager3.getAllItems(MovieTB::class.java, "moviedb/movietb") { movieItems ->
                        if (movieItems.isNotEmpty()) {
                            val movieList = movieItems.filter { it.mid in movieIds }.toMutableList()
                            Log.d("movieList", movieList.toString())
                            setupRecyclerView(movieList)
                        }
                    }
                }
            }
        }
    }



    private fun setupRecyclerView(movieList: MutableList<MovieTB>) {
        val adapter = MovieListAdapter(movieList,)
        adapter.setOnItemClickListener(object : MovieListAdapter.OnItemClickListener {
            override fun onItemClick(movie: MovieTB) {
                val movieShowsHelperClass = MovieShowsHelperClass(movie,requireContext())
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

            override fun onChildDraw(
                c: Canvas,
                recyclerView: RecyclerView,
                viewHolder: RecyclerView.ViewHolder,
                dX: Float,
                dY: Float,
                actionState: Int,
                isCurrentlyActive: Boolean
            ) {
                super.onChildDraw(c, recyclerView, viewHolder, dX, dY, actionState, isCurrentlyActive)
                val itemView = viewHolder.itemView
                val background = ColorDrawable(Color.RED)
                background.setBounds(
                    itemView.left + dX.toInt(),
                    itemView.top,
                    itemView.right,
                    itemView.bottom
                )
                background.draw(c)
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
        TODO("Not yet implemented")
    }
}

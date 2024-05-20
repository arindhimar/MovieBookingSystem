package com.example.combined_loginregister

import android.app.AlertDialog
import android.graphics.Movie
import android.os.Bundle
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.arjungupta08.horizontal_calendar_date.HorizontalCalendarAdapter
import com.arjungupta08.horizontal_calendar_date.HorizontalCalendarSetUp
import com.example.combined_loginregister.databinding.FragmentCInemaAdminManageShowsBinding
import com.google.android.material.datepicker.MaterialDatePicker
import com.google.android.material.timepicker.MaterialTimePicker
import com.google.android.material.timepicker.MaterialTimePicker.INPUT_MODE_CLOCK
import com.google.android.material.timepicker.TimeFormat
import com.google.firebase.auth.FirebaseAuth
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Locale
import java.util.TimeZone

// TODO: Rename parameter arguments, choose names that match
// the fragment initialization parameters, e.g. ARG_ITEM_NUMBER
private const val ARG_PARAM1 = "param1"
private const val ARG_PARAM2 = "param2"

/**
 * A simple [Fragment] subclass.
 * Use the [CInemaAdminManageShows.newInstance] factory method to
 * create an instance of this fragment.
 */
class CInemaAdminManageShows : Fragment() , HorizontalCalendarAdapter.OnItemClickListener {
    private lateinit var recyclerView: RecyclerView
    private lateinit var tvDateMonth: TextView
    private lateinit var ivCalendarNext: ImageView
    private lateinit var ivCalendarPrevious: ImageView

    private var param1: String? = null
    private var param2: String? = null
    lateinit var binding:FragmentCInemaAdminManageShowsBinding
    lateinit var dialogView: View
    lateinit var alertDialog: AlertDialog
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
        // Inflate the layout for this fragment
        binding = FragmentCInemaAdminManageShowsBinding.inflate(layoutInflater, container, false)



        binding.btnOpenAddShowDialog?.setOnClickListener {
            if (FirebaseAuth.getInstance().currentUser == null) {
                // Handle user not logged in
                return@setOnClickListener
            }

            dialogView = layoutInflater.inflate(R.layout.addleasemoviesdialog, null)

            // Create the dialog
            val dialogBuilder = AlertDialog.Builder(requireContext())
            dialogBuilder.setView(dialogView)

            // Show the dialog
            alertDialog = dialogBuilder.create()
            alertDialog.show()

            // Initialize the recyclerView object
            val recyclerView = dialogView.findViewById<RecyclerView>(R.id.UnleasedMoviesHereForCO)

            // Set custom window animations
            alertDialog.window?.attributes?.windowAnimations = R.style.DialogAnimation

            loadLeasedMovies(recyclerView)
        }








        tvDateMonth = binding.textDateMonth
        ivCalendarNext = binding.ivCalendarNext
        ivCalendarPrevious = binding.ivCalendarPrevious

        binding.recyclerView.layoutManager = LinearLayoutManager(requireContext(), LinearLayoutManager.HORIZONTAL, false)

        val calendarSetUp = HorizontalCalendarSetUp()
        val tvMonth = calendarSetUp.setUpCalendarAdapter(binding.recyclerView, this@CInemaAdminManageShows)
        tvDateMonth.text = tvMonth

        calendarSetUp.setUpCalendarPrevNextClickListener(ivCalendarNext, ivCalendarPrevious, this@CInemaAdminManageShows) {
            tvDateMonth.text = it
        }




















        return binding.root
    }

    private fun fetchCinemaOwnerId() {
        val userId = FirebaseAuth.getInstance().currentUser!!.uid

        val firebaseRestManager1 = FirebaseRestManager<CinemaAdminTb>()
        firebaseRestManager1.getAllItems(CinemaAdminTb::class.java, "moviedb/cinemaadmintb") { cinemaAdminTbs ->
            val cinemaAdmin = cinemaAdminTbs.find { it.userId == userId }
            val firebaseRestManager2 = FirebaseRestManager<CinemaOwnerTb>()
            firebaseRestManager2.getAllItems(CinemaOwnerTb::class.java, "moviedb/CinemaOwnerTb") { cinemaOwnerTbs ->
                val cinemaOwner =
                    cinemaOwnerTbs.find { it.cinemaOwnerId == cinemaAdmin?.cinemaOwnerId.toString() }
            }
        }

    }

    private fun loadLeasedMovies(recyclerView: RecyclerView) {
        val currentUser = FirebaseAuth.getInstance().currentUser
        currentUser?.let {
            val firebaseRestManager = FirebaseRestManager<CinemaAdminTb>()
            firebaseRestManager.getAllItems(CinemaAdminTb::class.java, "moviedb/cinemaadmintb") { cinemaAdminTbs ->
                val cinemaAdmin = cinemaAdminTbs.find { it.userId == currentUser.uid }
                cinemaAdmin?.let {
                    val cinemaOwnerId = it.cinemaOwnerId.toString()
                    loadCinemaOwner(cinemaOwnerId) { userId ->
                        loadLeasedMoviesForUser(userId) { movies ->
                            // Update RecyclerView with movies
                            // recyclerView.adapter = MoviesAdapter(movies)
                            Log.d("TAG", "loadLeasedMovies: waah re chl rha hai!! ${movies.size}")
                            val adapter = OwnerMovieListAdapter(movies)// movies should be an arraylist
                            adapter.setOnItemClickListener(object : OwnerMovieListAdapter.OnItemClickListener {
                                override fun onItemClick(movie: MovieTB) {
                                    val yesOrNoLoadingHelper = YesOrNoLoadingHelper()
                                    yesOrNoLoadingHelper.showLoadingDialog(requireContext())
                                    yesOrNoLoadingHelper.hideCheckBox()
                                    yesOrNoLoadingHelper.updateText("Are you sure you want to add this movie , You won't be able to make any changes to it")
                                    val view = yesOrNoLoadingHelper.getView()
                                    val btnYes = view.findViewById<Button>(R.id.btn_yes)
                                    val btnNo = view.findViewById<Button>(R.id.btn_no)

                                    btnNo.setOnClickListener {
                                        yesOrNoLoadingHelper.dismissLoadingDialog()
                                        alertDialog.dismiss()
                                    }

                                    btnYes.setOnClickListener {
                                        alertDialog.dismiss()

                                        showAddShowDialog()

                                    }

                                }
                            })
                            recyclerView.adapter = adapter
                            recyclerView.layoutManager = LinearLayoutManager(requireContext())
                        }
                    }
                }
            }
        }
    }

    private fun showAddShowDialog() {
        val dialogView2: View = layoutInflater.inflate(R.layout.custom_show_add_dialog, null)
        val alertDialog2: AlertDialog

        val dialogBuilder2 = AlertDialog.Builder(requireContext())
        dialogBuilder2.setView(dialogView2)

        alertDialog2 = dialogBuilder2.create()
        alertDialog2.show()

        val btnDate = dialogView2.findViewById<Button>(R.id.btnDate)
        val btnTime = dialogView2.findViewById<Button>(R.id.btnTime)
        val textView = dialogView2.findViewById<TextView>(R.id.textView)
        val AddFinalShowBtn = dialogView2.findViewById<Button>(R.id.AddFinalShowBtn)

        val currentDateTime = Calendar.getInstance(TimeZone.getTimeZone("Asia/Kolkata"))
        val dateFormat = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
        dateFormat.timeZone = TimeZone.getTimeZone("Asia/Kolkata")
        val timeFormat = SimpleDateFormat("HH:mm", Locale.getDefault())
        timeFormat.timeZone = TimeZone.getTimeZone("Asia/Kolkata")
        val currentIndianDate = dateFormat.format(currentDateTime.time)
        val currentIndianTime = timeFormat.format(currentDateTime.time)

        textView.text = "Date: $currentIndianDate\nTime: $currentIndianTime"

        var selectedDateText = currentIndianDate
        var selectedTimeText = currentIndianTime

        btnDate.setOnClickListener {
            val datePicker = MaterialDatePicker.Builder.datePicker()
                .setTitleText("Select Show Date")
                .setSelection(currentDateTime.timeInMillis)
                .setInputMode(MaterialDatePicker.INPUT_MODE_CALENDAR)

                .build()

            datePicker.addOnPositiveButtonClickListener {
                val selectedDateInMillis = datePicker.selection
                if (selectedDateInMillis != null) {
                    val selectedDate = Calendar.getInstance().apply {
                        timeInMillis = selectedDateInMillis
                        timeZone = TimeZone.getTimeZone("Asia/Kolkata")
                    }

                    val formattedSelectedDate = dateFormat.format(selectedDate.time)
                    selectedDateText = formattedSelectedDate

                    textView.text = "Date: $selectedDateText\nTime: $selectedTimeText"
                }
            }
            datePicker.show(parentFragmentManager, "date")
        }

        btnTime.setOnClickListener {
            val timePicker =
                MaterialTimePicker.Builder()
                    .setTimeFormat(TimeFormat.CLOCK_12H)
                    .setHour(currentDateTime.get(Calendar.HOUR_OF_DAY))
                    .setMinute(currentDateTime.get(Calendar.MINUTE))
                    .setTitleText("Select Movie time")
                    .setInputMode(MaterialTimePicker.INPUT_MODE_CLOCK)
                    .build()

            timePicker.addOnPositiveButtonClickListener {
                val selectedHour = timePicker.hour
                val selectedMinute = timePicker.minute
                val formattedSelectedTime = String.format(Locale.getDefault(), "%02d:%02d", selectedHour, selectedMinute)
                selectedTimeText = formattedSelectedTime

                textView.text = "Date: $selectedDateText\nTime: $selectedTimeText"
            }
            timePicker.show(parentFragmentManager, "Time")
        }

        AddFinalShowBtn.setOnClickListener {
            val selectedDate = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).parse(selectedDateText)
            val startingDate = Calendar.getInstance().apply {
                add(Calendar.DAY_OF_YEAR, 1)
            }.time

            val finalDate = Calendar.getInstance().apply {
                add(Calendar.DAY_OF_YEAR, 7)
            }.time

            if (selectedDate != null && selectedDate in startingDate..finalDate) {
                // The selected date is within the specified range
                // Your logic here...
                // For example:
                Toast.makeText(requireContext(), "Selected date is within the range", Toast.LENGTH_SHORT).show()
            } else {
                // The selected date is not within the specified range
                // Your logic here...
                // For example:
                Toast.makeText(requireContext(), "Selected date is not within the range", Toast.LENGTH_SHORT).show()
            }
        }


        alertDialog2.window?.attributes?.windowAnimations = R.style.DialogAnimation
    }

    private fun loadCinemaOwner(cinemaOwnerId: String, callback: (String?) -> Unit) {
        val firebaseRestManager = FirebaseRestManager<CinemaOwnerTb>()
        firebaseRestManager.getAllItems(CinemaOwnerTb::class.java, "moviedb/CinemaOwnerTb") { cinemaOwnerTbs ->
            val cinemaOwner = cinemaOwnerTbs.find { it.cinemaOwnerId == cinemaOwnerId }
            callback(cinemaOwner?.uid?.toString())
        }
    }

    private fun loadLeasedMoviesForUser(userId: String?, callback: (List<MovieTB>) -> Unit) {
        userId?.let {
            val firebaseRestManager = FirebaseRestManager<LeaseMovieTb>()
            firebaseRestManager.getAllItems(LeaseMovieTb::class.java, "moviedb/leasemovietb") { leaseMovieTbs ->
                val movieIds = leaseMovieTbs.filter { it.userId == userId }.map { it.movieId }
                loadMovies(movieIds, callback)
            }
        }
    }

    private fun loadMovies(movieIds: List<String?>, callback: (List<MovieTB>) -> Unit) {
        val firebaseRestManager = FirebaseRestManager<MovieTB>()
        firebaseRestManager.getAllItems(MovieTB::class.java, "moviedb/movietb") { movies ->
            val filteredMovies = movies.filter { movieIds.contains(it.mid) }
            callback(filteredMovies)
        }
    }


//    private fun loadLeasedMovies(recyclerView:RecyclerView) {
//
//        val firebaseRestManager1 = FirebaseRestManager<CinemaAdminTb>()
//        var cinemaOwnerId:String = null.toString()
//        firebaseRestManager1.getAllItems(CinemaAdminTb::class.java,"moviedb/cinemaadmintb"){cinemaAdminTbs ->
//            if(cinemaAdminTbs.isNotEmpty()){
//                for(tempcinemaAdminTbs in cinemaAdminTbs){
//                    if(tempcinemaAdminTbs.userId==FirebaseAuth.getInstance().currentUser!!.uid){
//                        cinemaOwnerId = tempcinemaAdminTbs.cinemaOwnerId.toString()
//
//                        val firebaseRestManager2  = FirebaseRestManager<CinemaOwnerTb>()
//
//                        var userId:String? = null
//
//                        firebaseRestManager2.getAllItems(CinemaOwnerTb::class.java,"moviedb/CinemaOwnerTb"){cinemaOwnerTbs ->
//                            if(cinemaOwnerTbs.isNotEmpty()){
//                                for(tempcinemaOwnerTbs in cinemaOwnerTbs){
//                                    if(tempcinemaOwnerTbs.cinemaOwnerId==cinemaOwnerId){
//                                        userId = tempcinemaOwnerTbs.uid!!.toString()
//
//                                        val moviesList = ArrayList<MovieTB>()
//                                        var movieId:String?=null
//
//                                        val firebaseRestManager3 = FirebaseRestManager<LeaseMovieTb>()
//                                        firebaseRestManager3.getAllItems(LeaseMovieTb::class.java,"moviedb/leasemoviestb"){leaseMovieTbs ->
//                                            if(leaseMovieTbs.isNotEmpty()){
//                                                for(templeaseMovieTbs in leaseMovieTbs){
//                                                    if(templeaseMovieTbs.userId==userId){
//                                                        movieId = templeaseMovieTbs.movieId!!.toString()
//                                                        val firebaseRestManager4 = FirebaseRestManager<MovieTB>()
//                                                        firebaseRestManager4.getAllItems(MovieTB::class.java,"movied/movietb"){items->
//                                                            for(item in items){
//                                                                if(item.mid==movieId){
//                                                                    moviesList.add(item)
//                                                                }
//                                                            }
//                                                        }
//                                                    }
//                                                }
//                                            }
//                                        }
//
//                                    }
//                                }
//                            }
//                        }
//
//
//
//
//                    }
//                }
//            }
//        }
//
//
//
//
//
//
//    }

    companion object {
        /**
         * Use this factory method to create a new instance of
         * this fragment using the provided parameters.
         *
         * @param param1 Parameter 1.
         * @param param2 Parameter 2.
         * @return A new instance of fragment CInemaAdminManageShows.
         */
        // TODO: Rename and change types and number of parameters
        @JvmStatic
        fun newInstance(param1: String, param2: String) =
            CInemaAdminManageShows().apply {
                arguments = Bundle().apply {
                    putString(ARG_PARAM1, param1)
                    putString(ARG_PARAM2, param2)
                }
            }
    }

    override fun onItemClick(ddMmYy: String, dd: String, day: String) {
        binding.selectedDate.text = "Selected date: $ddMmYy"
        binding.selectedDD.text = "Selected DD: $dd"
        binding.selectedDay.text = "Selected day: $day"
    }
}
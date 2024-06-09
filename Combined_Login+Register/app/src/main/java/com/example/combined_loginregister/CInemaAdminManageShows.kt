package com.example.combined_loginregister

import android.app.AlertDialog
import android.os.Build
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.ImageView
import android.widget.TextView
import androidx.annotation.RequiresApi
import androidx.core.i18n.DateTimeFormatter
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.arjungupta08.horizontal_calendar_date.HorizontalCalendarAdapter
import com.arjungupta08.horizontal_calendar_date.HorizontalCalendarSetUp
import com.example.combined_loginregister.databinding.FragmentCInemaAdminManageShowsBinding
import com.google.android.material.datepicker.MaterialDatePicker
import com.google.android.material.textfield.TextInputLayout
import com.google.android.material.timepicker.MaterialTimePicker
import com.google.android.material.timepicker.TimeFormat
import com.google.firebase.Firebase
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import com.google.firebase.database.database
import java.text.ParseException
import java.text.SimpleDateFormat
import java.time.LocalTime
import java.time.ZoneId
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
class CInemaAdminManageShows : Fragment(), HorizontalCalendarAdapter.OnItemClickListener {
    private lateinit var recyclerView: RecyclerView
    private lateinit var tvDateMonth: TextView
    private lateinit var ivCalendarNext: ImageView
    private lateinit var ivCalendarPrevious: ImageView

    private var param1: String? = null
    private var param2: String? = null
    lateinit var binding: FragmentCInemaAdminManageShowsBinding
    lateinit var dialogView: View
    lateinit var alertDialog: AlertDialog
    lateinit var cinemaOwnerId: String
    lateinit var cinemaId: String
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



        cinemaOwnerId = null.toString()
        cinemaId = null.toString().toString()
        binding.btnOpenAddShowDialog.setOnClickListener {
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

        binding.recyclerView.layoutManager =
            LinearLayoutManager(requireContext(), LinearLayoutManager.HORIZONTAL, false)

        val calendarSetUp = HorizontalCalendarSetUp()
        val tvMonth =
            calendarSetUp.setUpCalendarAdapter(binding.recyclerView, this@CInemaAdminManageShows)
        tvDateMonth.text = tvMonth

        calendarSetUp.setUpCalendarPrevNextClickListener(
            ivCalendarNext,
            ivCalendarPrevious,
            this@CInemaAdminManageShows
        ) {
            tvDateMonth.text = it
        }


        loadInitialData()

        setUpRealTimeDatabase()


        return binding.root
    }

    private fun setUpRealTimeDatabase() {
        // Get a reference to the database
        val database = Firebase.database.reference

        // Set up a listener for changes to the "shows" node
        database.child("moviedb/showtb").addValueEventListener(object : ValueEventListener {
            override fun onDataChange(dataSnapshot: DataSnapshot) {
                // Get the current date in the required format
                val currentDate = getCurrentDateFormatted()
                // Simulate a click on the current date
                onItemClick(currentDate, "", "")
            }

            override fun onCancelled(databaseError: DatabaseError) {
                // This method is called if there is an error reading the data
                Log.e("TAG", "onCancelled: ", databaseError.toException())
            }
        })
    }

    private fun fetchCinemaOwnerId() {
        val userId = FirebaseAuth.getInstance().currentUser!!.uid

        val firebaseRestManager1 = FirebaseRestManager<CinemaAdminTb>()
        firebaseRestManager1.getAllItems(
            CinemaAdminTb::class.java,
            "moviedb/cinemaadmintb"
        ) { cinemaAdminTbs ->
            val cinemaAdmin = cinemaAdminTbs.find { it.userId == userId }
            val firebaseRestManager2 = FirebaseRestManager<CinemaOwnerTb>()
            firebaseRestManager2.getAllItems(
                CinemaOwnerTb::class.java,
                "moviedb/CinemaOwnerTb"
            ) { cinemaOwnerTbs ->
                val cinemaOwner =
                    cinemaOwnerTbs.find { it.cinemaOwnerId == cinemaAdmin?.cinemaOwnerId.toString() }
            }
        }

    }

    private fun loadLeasedMovies(recyclerView: RecyclerView) {
        val currentUser = FirebaseAuth.getInstance().currentUser
        currentUser?.let {
            val firebaseRestManager = FirebaseRestManager<CinemaAdminTb>()
            firebaseRestManager.getAllItems(
                CinemaAdminTb::class.java,
                "moviedb/cinemaadmintb"
            ) { cinemaAdminTbs ->
                val cinemaAdmin = cinemaAdminTbs.find { it.userId == currentUser.uid }
                cinemaAdmin?.let {
                    cinemaOwnerId = it.cinemaOwnerId.toString()

                    val cinemaOwnerId = it.cinemaOwnerId.toString()
                    loadCinemaOwner(cinemaOwnerId) { userId ->
                        loadLeasedMoviesForUser(userId) { movies ->
                            // Update RecyclerView with movies
                            // recyclerView.adapter = MoviesAdapter(movies)
                            Log.d("TAG", "loadLeasedMovies: waah re chl rha hai!! ${movies.size}")
                            val adapter =
                                OwnerMovieListAdapter(movies)// movies should be an arraylist
                            adapter.setOnItemClickListener(object :
                                OwnerMovieListAdapter.OnItemClickListener {
                                @RequiresApi(Build.VERSION_CODES.O)
                                override fun onItemClick(movie: MovieTB) {
                                    val yesOrNoLoadingHelper = YesOrNoLoadingHelper()
                                    yesOrNoLoadingHelper.showLoadingDialog(requireContext())
                                    yesOrNoLoadingHelper.hideCheckBox()
                                    yesOrNoLoadingHelper.updateText("Are you sure you want to add this movie , You won't be able to make any changes to it")
                                    val view = yesOrNoLoadingHelper.getView()
                                    val btnYes = view.findViewById<Button>(R.id.btn_yes)
                                    val btnNo = view.findViewById<Button>(R.id.btn_no)

                                    btnNo.setOnClickListener {
                                        alertDialog.dismiss()
                                    }

                                    btnYes.setOnClickListener {
                                        alertDialog.dismiss()
                                        yesOrNoLoadingHelper.dismissLoadingDialog()

                                        showAddShowDialog(movie)

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

    @RequiresApi(Build.VERSION_CODES.O)
    private fun showAddShowDialog(movie: MovieTB) {
        val dialogView2: View = layoutInflater.inflate(R.layout.custom_show_add_dialog, null)
        val alertDialog2: AlertDialog

        val dialogBuilder2 = AlertDialog.Builder(requireContext())
        dialogBuilder2.setView(dialogView2)

        alertDialog2 = dialogBuilder2.create()
        alertDialog2.show()

        val btnDate = dialogView2.findViewById<Button>(R.id.btnDate)
        val btnTime = dialogView2.findViewById<Button>(R.id.btnTime)
        val textView = dialogView2.findViewById<TextView>(R.id.textView)
        val addFinalShowBtn = dialogView2.findViewById<Button>(R.id.AddFinalShowBtn)
        val price = dialogView2.findViewById<TextInputLayout>(R.id.price)

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
        var newTimeText: String? = null

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

            val timePicker = MaterialTimePicker.Builder()
                .setTimeFormat(TimeFormat.CLOCK_12H)
                .setHour(currentDateTime.get(Calendar.HOUR_OF_DAY))
                .setMinute(currentDateTime.get(Calendar.MINUTE))
                .setTitleText("Select Movie Time")
                .setInputMode(MaterialTimePicker.INPUT_MODE_CLOCK)
                .build()

            timePicker.addOnPositiveButtonClickListener {
                val selectedHour = timePicker.hour
                val selectedMinute = timePicker.minute

                // Format the selected time
                val formattedSelectedTime =
                    String.format(Locale.getDefault(), "%02d:%02d", selectedHour, selectedMinute)

                // Add minutes to the selected time
                val additionalMinutes = movie.duration!!.toInt()
                val calendar = Calendar.getInstance().apply {
                    set(Calendar.HOUR_OF_DAY, selectedHour)
                    set(Calendar.MINUTE, selectedMinute)
                    add(Calendar.MINUTE, additionalMinutes)
                }

                // Format the new time
                val formattedNewTime = String.format(
                    Locale.getDefault(),
                    "%02d:%02d",
                    calendar.get(Calendar.HOUR_OF_DAY),
                    calendar.get(Calendar.MINUTE)
                )

                // Update the selected time text and new time text
                selectedTimeText = formattedSelectedTime
                newTimeText = formattedNewTime

                textView.text =
                    "Date: $selectedDateText\nSelected Time: $selectedTimeText\nEnd Time: $newTimeText"
            }
            timePicker.show(parentFragmentManager, "time")
        }

        addFinalShowBtn.setOnClickListener {
            Log.d("TAG", "showAddShowDialog: asnd,asndkjnajsdk")
            val firebaseRestManager = FirebaseRestManager<ShowTb>()
            firebaseRestManager.getAllItems(ShowTb::class.java, "moviedb/showtb") { showTbs ->
                val filteredShows =
                    showTbs.filter { it.cinemaId == cinemaId && it.showDate == selectedDateText }

                var isOverlap = false
                for (show in filteredShows) {
                    if (selectedTimeText == null || newTimeText == null) {
                        if (isTimeOverlap(
                                selectedTimeText, newTimeText!!, show.showStartTime,
                                show.showEndTime
                            )
                        ) {
                            isOverlap = true
                            break
                        }
                    }
                }

                if (isOverlap || newTimeText == null) {
                    val warningLoadingHelper = WarningLoadingHelper()
                    warningLoadingHelper.showLoadingDialog(requireContext())
                    warningLoadingHelper.updateText("Selected time overlaps with an existing show.")
                    warningLoadingHelper.hideButtons()

                    val handler = Handler(Looper.getMainLooper())
                    handler.postDelayed({
                        warningLoadingHelper.dismissLoadingDialog()
                        alertDialog2.dismiss()
                    }, 2000)
                } else {

                    if (price.editText?.text.toString().isEmpty()) {
                        price.error = "Enter price"
                    } else {
                        if (price.editText?.text.toString().toInt() < 1) {
                            price.error = "Enter valid price"
                        } else {
                            price.error = null
                            val firebaseRestManager2 = FirebaseRestManager<CinemaOwnerTb>()
                            firebaseRestManager2.getSingleItem(
                                CinemaOwnerTb::class.java,
                                "moviedb/CinemaOwnerTb",
                                cinemaOwnerId
                            ) { items ->
                                if (items != null) {
                                    cinemaId = items.cinemaId.toString()
                                    val firebaseRestManager = FirebaseRestManager<ShowTb>()
                                    val db = Firebase.database.getReference("moviedb/showtb")
                                    val id = db.push().key ?: return@getSingleItem

                                    val firebaseRestManager4 = FirebaseRestManager<CinemaAdminTb>()
                                    firebaseRestManager4.getAllItems(
                                        CinemaAdminTb::class.java,
                                        "moviedb/cinemaadmintb"
                                    ) { cinemaAdminTbs ->
                                        run {
                                            val cinemaAdmin =
                                                cinemaAdminTbs.find { it.userId == FirebaseAuth.getInstance().currentUser!!.uid }
                                            val tempData = ShowTb(
                                                id,
                                                cinemaId,
                                                cinemaAdmin!!.cinemaadminid.toString(),
                                                movie.mid!!,
                                                selectedDateText,
                                                selectedTimeText,
                                                newTimeText!!,
                                                price.editText?.text.toString()
                                            )

                                            firebaseRestManager.addItemWithCustomId(
                                                tempData,
                                                id,
                                                FirebaseDatabase.getInstance()
                                                    .getReference("moviedb/showtb")
                                            ) { success, message ->
                                                if (success) {
                                                    val successLoadingHelper =
                                                        SuccessLoadingHelper()
                                                    successLoadingHelper.showLoadingDialog(
                                                        requireContext()
                                                    )
                                                    successLoadingHelper.updateText("Show added successfully")
                                                    successLoadingHelper.hideButtons()

                                                    val handler = Handler(Looper.getMainLooper())
                                                    handler.postDelayed({
                                                        successLoadingHelper.dismissLoadingDialog()
                                                        alertDialog2.dismiss()
                                                    }, 2000)
                                                }
                                            }
                                        }

                                    }
                                }
                            }
                        }
                    }


                }
            }
        }

        alertDialog2.window?.attributes?.windowAnimations = R.style.DialogAnimation
    }


    private fun loadCinemaOwner(cinemaOwnerId: String, callback: (String?) -> Unit) {
        val firebaseRestManager = FirebaseRestManager<CinemaOwnerTb>()
        firebaseRestManager.getAllItems(
            CinemaOwnerTb::class.java,
            "moviedb/CinemaOwnerTb"
        ) { cinemaOwnerTbs ->
            val cinemaOwner = cinemaOwnerTbs.find { it.cinemaOwnerId == cinemaOwnerId }
            callback(cinemaOwner?.uid?.toString())
        }
    }

    private fun loadLeasedMoviesForUser(userId: String?, callback: (List<MovieTB>) -> Unit) {
        userId?.let {
            val firebaseRestManager = FirebaseRestManager<LeaseMovieTb>()
            firebaseRestManager.getAllItems(
                LeaseMovieTb::class.java,
                "moviedb/leasemovietb"
            ) { leaseMovieTbs ->
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

    @RequiresApi(Build.VERSION_CODES.O)
    private fun isTimeOverlap(
        newShowStartTime: String,
        newShowEndTime: String,
        existingShowStartTime: String,
        existingShowEndTime: String
    ): Boolean {
        val timeFormat = SimpleDateFormat("HH:mm", Locale.getDefault())

        return try {
            val newStart = timeFormat.parse(newShowStartTime)
            val newEnd = timeFormat.parse(newShowEndTime)
            val existingStart = timeFormat.parse(existingShowStartTime)
            val existingEnd = timeFormat.parse(existingShowEndTime)

            newStart.before(existingEnd) && existingStart.before(newEnd)
        } catch (e: ParseException) {
            e.printStackTrace()
            false
        }
    }


    private fun loadInitialData() {
        val firebaseRestManager1 = FirebaseRestManager<CinemaAdminTb>()
        firebaseRestManager1.getAllItems(
            CinemaAdminTb::class.java,
            "moviedb/cinemaadmintb"
        ) { cinemaAdminTbs ->
            val cinemaAdmin =
                cinemaAdminTbs.find { it.userId == FirebaseAuth.getInstance().currentUser!!.uid }
            cinemaAdmin?.let { it ->
                cinemaOwnerId = it.cinemaOwnerId.toString()
                val firebaseRestManager2 = FirebaseRestManager<CinemaOwnerTb>()
                firebaseRestManager2.getAllItems(
                    CinemaOwnerTb::class.java,
                    "moviedb/CinemaOwnerTb"
                ) { cinemaOwnerTbs ->
                    val cinemaOwner = cinemaOwnerTbs.find { it.cinemaOwnerId == cinemaOwnerId }
                    cinemaId = cinemaOwner?.cinemaId.toString()
                }
            }
        }
    }


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
    private fun getCurrentDateFormatted(): String {
        val currentDateTime = Calendar.getInstance(TimeZone.getTimeZone("Asia/Kolkata"))
        val dateFormat = SimpleDateFormat("dd MMM yyyy", Locale.getDefault())
        dateFormat.timeZone = TimeZone.getTimeZone("Asia/Kolkata")
        return dateFormat.format(currentDateTime.time)
    }

    override fun onItemClick(ddMmYy: String, dd: String, day: String) {
        // Parse the selected date from "01 May 2024" to "yyyy-MM-dd" format
        val selectedDate = SimpleDateFormat("dd MMM yyyy", Locale.getDefault()).parse(ddMmYy)
        val dateFormat = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
        val formattedSelectedDate = dateFormat.format(selectedDate)

        val firebaseRestManager = FirebaseRestManager<ShowTb>()
        firebaseRestManager.getAllItems(ShowTb::class.java, "moviedb/showtb") { showTbs ->
            val filteredShows =
                showTbs.filter { it.cinemaId == cinemaId && it.showDate == formattedSelectedDate }

            Log.d("TAG", "onItemClick: $filteredShows")
            // Set up the ShowAdapter with filtered shows and assign it to the RecyclerView
            val showAdapter = ShowAdapter(filteredShows)
            showAdapter.setOnItemClickListener(object : ShowAdapter.OnItemClickListener {
                @RequiresApi(Build.VERSION_CODES.O)
                override fun onItemClick(show: ShowTb) {
                    // Handle the click event here
                    Log.d("TAG", "onItemClick: $show")


                }
            })
            binding.ShowsHere.adapter = showAdapter
        }
    }

}
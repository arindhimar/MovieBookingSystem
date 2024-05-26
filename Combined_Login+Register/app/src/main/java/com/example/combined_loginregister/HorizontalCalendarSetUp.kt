package com.example.combined_loginregister

import android.widget.ImageView
import androidx.recyclerview.widget.LinearSnapHelper
import androidx.recyclerview.widget.RecyclerView
import androidx.recyclerview.widget.SnapHelper
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale

class HorizontalCalendarSetUp() {

    private val sdf = SimpleDateFormat("MMMM yyyy", Locale.ENGLISH)
    private val cal = Calendar.getInstance(Locale.ENGLISH)
    private val currentDate = Calendar.getInstance(Locale.ENGLISH)
    private val dates = ArrayList<Date>()
    private lateinit var adapter: HorizontalCalendarAdapter
    private val calendarList2 = ArrayList<CalendarDateModel>()

    /*
     * Set up click listener
     */
    fun setUpCalendarPrevNextClickListener(ivCalendarNext: ImageView, ivCalendarPrevious: ImageView, listener: HorizontalCalendarAdapter.OnItemClickListener, month : (String) -> Unit ) {
        ivCalendarNext.setOnClickListener {
            cal.add(Calendar.MONTH, 1)
            val monthDate = setUpCalendar(listener)
            month.invoke(monthDate)
        }
        ivCalendarPrevious.setOnClickListener {
            cal.add(Calendar.MONTH, -1)
            if (cal == currentDate) {
                val monthDate = setUpCalendar(listener)
                month.invoke(monthDate)
            } else {
                val monthDate = setUpCalendar(listener)
                month.invoke(monthDate)
            }
        }
    }


    /*
     * Setting up adapter for recyclerview
     */
    fun setUpCalendarAdapter(recyclerView: RecyclerView, listener : HorizontalCalendarAdapter.OnItemClickListener) : String {
        val snapHelper: SnapHelper = LinearSnapHelper()
        snapHelper.attachToRecyclerView(recyclerView)

        adapter = HorizontalCalendarAdapter() { calendarDateModel: CalendarDateModel, position: Int ->
            calendarList2.forEachIndexed { index, calendarModel ->
                calendarModel.isSelected = index == position
            }
        }
        adapter.setData(calendarList2)
        adapter.setOnItemClickListener(listener)
        recyclerView.adapter = adapter

        return setUpCalendar(listener)
    }

    /*
     * Function to setup calendar for every month
     */
<<<<<<< HEAD
    private fun setUpCalendar(listener: HorizontalCalendarAdapter.OnItemClickListener) : String {
        val calendarList = ArrayList<CalendarDateModel>()
        val calendar = Calendar.getInstance()
        val currentDate = Date()
        calendar.time = currentDate
        val maxDaysInMonth = cal.getActualMaximum(Calendar.DAY_OF_MONTH)
        dates.clear()
        calendar.set(Calendar.DAY_OF_MONTH, 1)
        while (dates.size < maxDaysInMonth) {
            val date = calendar.time
            val isEnabled = (calendar.time.time - currentDate.time) <= (7 * 24 * 60 * 60 * 1000) // 7 days in milliseconds
            dates.add(date)
            calendarList.add(CalendarDateModel(date, isEnabled))
            calendar.add(Calendar.DAY_OF_MONTH, 1)
        }
        calendarList2.clear()
        calendarList2.addAll(calendarList)
        adapter.setOnItemClickListener(listener)
        adapter.setData(calendarList)
        return sdf.format(cal.time)
    }
//    private fun setUpCalendar(listener: HorizontalCalendarAdapter.OnItemClickListener) : String {
//        val calendarList = ArrayList<CalendarDateModel>()
//        val monthCalendar = cal.clone() as Calendar
//        val maxDaysInMonth = cal.getActualMaximum(Calendar.DAY_OF_MONTH)
//        dates.clear()
//        monthCalendar.set(Calendar.DAY_OF_MONTH, 1)
//        while (dates.size < maxDaysInMonth) {
//            dates.add(monthCalendar.time)
//            calendarList.add(CalendarDateModel(monthCalendar.time))
//            monthCalendar.add(Calendar.DAY_OF_MONTH, 1)
=======
//    private fun setUpCalendar(listener: HorizontalCalendarAdapter.OnItemClickListener): String {
//        val calendarList = ArrayList<CalendarDateModel>()
//        val currentDate = Date()
//        val todayCalendar = Calendar.getInstance().apply {
//            time = currentDate
//            set(Calendar.HOUR_OF_DAY, 0)
//            set(Calendar.MINUTE, 0)
//            set(Calendar.SECOND, 0)
//            set(Calendar.MILLISECOND, 0)
//        }
//
//        val maxDaysInMonth = cal.getActualMaximum(Calendar.DAY_OF_MONTH)
//        dates.clear()
//        cal.set(Calendar.DAY_OF_MONTH, 1)
//
//        while (dates.size < maxDaysInMonth) {
//            val date = cal.time
//            val isEnabled = !date.before(todayCalendar.time)
//            dates.add(date)
//            calendarList.add(CalendarDateModel(date, isEnabled))
//            cal.add(Calendar.DAY_OF_MONTH, 1)
>>>>>>> 7249331f80923bcd6c1ebf26e26936377b1a2884
//        }
//
//        calendarList2.clear()
//        calendarList2.addAll(calendarList)
//        adapter.setOnItemClickListener(listener)
//        adapter.setData(calendarList)
//
//        return sdf.format(cal.time)
//    }

    private fun setUpCalendar(listener: HorizontalCalendarAdapter.OnItemClickListener) : String {
        val calendarList = ArrayList<CalendarDateModel>()
        val calendar = Calendar.getInstance()
        val currentDate = Date()
        calendar.time = currentDate
        val maxDaysInMonth = cal.getActualMaximum(Calendar.DAY_OF_MONTH)
        dates.clear()
        calendar.set(Calendar.DAY_OF_MONTH, 1)
        while (dates.size < maxDaysInMonth) {
            val date = calendar.time
            val isEnabled = (calendar.time.time - currentDate.time) <= (7 * 24 * 60 * 60 * 1000) // 7 days in milliseconds
            dates.add(date)
            calendarList.add(CalendarDateModel(date, isEnabled))
            calendar.add(Calendar.DAY_OF_MONTH, 1)
        }
        calendarList2.clear()
        calendarList2.addAll(calendarList)
        adapter.setOnItemClickListener(listener)
        adapter.setData(calendarList)
        return sdf.format(cal.time)
    }

}
package com.example.customdialogpicker

import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import java.time.LocalDate
import java.util.Calendar

class CalendarViewModel : ViewModel() {
    val selectedDate = MutableLiveData<LocalDate>()
    val selectedDates = MutableLiveData<List<LocalDate>>()
    val disabledDates = MutableLiveData<List<LocalDate>>()
    val boundaryDates = MutableLiveData<Pair<LocalDate, LocalDate>>()
    val firstDayOfWeek = MutableLiveData<Int>(Calendar.MONDAY)

    fun onDateSelected(date: LocalDate) {
        selectedDate.value = date
    }

    fun onMonthChanged(date: LocalDate) {
        // Handle month change
    }
}
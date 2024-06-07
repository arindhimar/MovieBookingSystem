package com.example.combined_loginregister

import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale


data class BookingTb(
    val bookingId: String = "",
    val paymentId: String = "",
    val bookingDate: String = "",
    val bookingTime: String = "",
    val showId: String = "",
    val userId: String = "",
    val bookedSeats: String = ""
) {
    fun getCombinedDateTime(): Date? {
        return try {
            val dateTimeString = "$bookingDate $bookingTime"
            val format = SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault())
            format.parse(dateTimeString)
        } catch (e: Exception) {
            null
        }
    }
}
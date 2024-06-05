package com.example.combined_loginregister

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.util.Log

class BookingReceiver : BroadcastReceiver() {
    override fun onReceive(context: Context?, intent: Intent?) {
        if (intent?.action == "com.example.BOOKING_CONFIRMED") {
            val bookingId = intent.getStringExtra("bookingId")
            // Handle the booking confirmation
            Log.d("BookingReceiver", "Booking confirmed with ID: $bookingId")
        }
    }
}

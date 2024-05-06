package com.example.seats_layout

data class Seat(
    val seatNumber: String,
    var isSelected: Boolean = false,
    val ticketPrice: Int = 100
)

package com.soumil.quickbook.models

data class User(
    var name: String = "",
    var email: String = "",
    var phone: String = "",
    var city: String = "",
    var bookingHistory: List<String> = emptyList(),
    var cardNumber: Long = -1L,
    var expiryDate: String = "",
    var cvv: Int = 0,
    var role: String = ""
    // Add more fields as needed
)

package com.soumil.quickbook.models
data class Manager(
    var name: String = "",
    var email: String = "",
    var phone: String = "",
    var city: String = "",
    var turf_count: Int = 0,
    var turfs_owned:List<String> = emptyList(),
    var cardNumber: Long = -1L,
    var expiryDate: String = "",
    var cvv: Int = 0,
    var role: String = ""
    // Add more fields as needed
)
package com.soumil.quickbook.models

import com.google.firebase.firestore.GeoPoint


data class Turf(
    var turf_uid: String = "",
    var manager_id: String = "",
    var name: String = "",
    var city: String = "",
    var price: Int = 0,
    var open_time: String = "",
    var close_time: String = "",
    var weekdays:List<String> = emptyList(),
    var games:List<String> = emptyList(),
    var pictures:List<String> = emptyList(),
    var bookings:List<String> = emptyList(),
    var rating_count:Int=0,
    var rating_sum:Int=0,
    val location: GeoPoint? = null,
    var latitude: String = "",
    var longitude: String = ""
    // Add more fields as needed
)
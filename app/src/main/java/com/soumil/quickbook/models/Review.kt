package com.soumil.quickbook.models

data class Review(
    val date: String, // Format: DD/MM/YYYY
    val userName: String,
    val userId: String,
    val turfId: String = "",
    val rating: Int =0,
    val review: String="",
    val turfName:String=""
){
    // No-argument constructor for Firestore
    constructor() : this("", "", "", "", 0, "")
}

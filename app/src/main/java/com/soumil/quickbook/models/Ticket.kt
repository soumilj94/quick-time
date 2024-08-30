package com.soumil.quickbook.models

data class Ticket(
    var turfId: String = "",
    var managerId: String = "",
    var userId: String="",

    var userName:String="",
    var turfName:String="",

    var game:String="",
    var players:String="",
    var slots:String="",
    var date:String="",

    var amount:String="",

    var razorpayPaymentID:String="",
    val timestamp: Long

){

    constructor() : this("", "", "", "", "", "", "", "", "", "", "", 0L)
}

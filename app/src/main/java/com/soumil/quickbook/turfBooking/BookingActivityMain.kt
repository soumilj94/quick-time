package com.soumil.quickbook.turfBooking

import android.content.Context
import android.content.SharedPreferences
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import com.soumil.quickbook.R

class BookingActivityMain : AppCompatActivity() {
    lateinit var sharedPreferences: SharedPreferences

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_booking_main)

        sharedPreferences = getSharedPreferences("user_prefs", Context.MODE_PRIVATE)
    }
}
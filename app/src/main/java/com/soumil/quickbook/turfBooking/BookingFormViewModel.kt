package com.soumil.quickbook.turfBooking

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.soumil.quickbook.models.Turf

class BookingFormViewModel : ViewModel() {

    private val auth = FirebaseAuth.getInstance()
    val userId = auth.currentUser?.uid

    private val _username = MutableLiveData<String>()
    val username: LiveData<String> get() = _username
    private val _userphone = MutableLiveData<String>()
    val userphone: LiveData<String> get() = _userphone

    private val _turf = MutableLiveData<Turf>()
    val turf: LiveData<Turf> get() = _turf

    private val _selectedGame = MutableLiveData<String>()
    val selectedGame: LiveData<String> get() = _selectedGame

    private val _playerCount = MutableLiveData<Int>()
    val playerCount: LiveData<Int> get() = _playerCount

    private val _bookedSlots = MutableLiveData<List<String>>()
    val bookedSlots: LiveData<List<String>> get() = _bookedSlots

    private val firestore = FirebaseFirestore.getInstance()

    fun setUserDetails(uname: String, uphone:String) {
        _username.value = uname
        _userphone.value = uphone
    }

    fun setTurf(turf: Turf) {
        _turf.value = turf
    }

    fun setSelectedGame(game: String) {
        _selectedGame.value = game
    }

    fun setSelectedPlayerCount(number: Int) {
        _playerCount.value = number
    }

    fun fetchBookedSlots(turfId: String, date: String) {
        firestore.collection("bookings")
            .whereEqualTo("turfId", turfId)
            .whereEqualTo("date", date)
            .get()
            .addOnSuccessListener { documents ->
                val bookedSlots = mutableListOf<String>()
                for (document in documents) {
                    val slot = document.getString("slots")
                    if (slot != null) {
                        bookedSlots.add(slot)
                    }
                }
                _bookedSlots.value = bookedSlots
            }
            .addOnFailureListener {
                _bookedSlots.value = emptyList()
            }
    }
}

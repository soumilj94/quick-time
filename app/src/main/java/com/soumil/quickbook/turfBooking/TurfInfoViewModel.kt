package com.soumil.quickbook.turfBooking

import android.content.Context
import android.widget.Toast
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.soumil.quickbook.models.City
import com.soumil.quickbook.models.Review
import com.soumil.quickbook.models.Turf

class TurfInfoViewModel : ViewModel() {


    val db = FirebaseFirestore.getInstance()
    private val auth = FirebaseAuth.getInstance()
    val userId = auth.currentUser?.uid



    private val _turf = MutableLiveData<Turf>()
    val turf: LiveData<Turf> get() = _turf

    fun fetchTurfDetails(turfId: String) {
        db.collection("turfs").document(turfId).get()
            .addOnSuccessListener { document ->
                if (document != null && document.exists()) {
                    val turf = document.toObject(Turf::class.java)
                    _turf.value = turf
                } else {
                    // Handle the case where the document does not exist
                    _turf.value = null
                }
            }
            .addOnFailureListener { exception ->
                // Handle the error
                _turf.value = null
            }
    }
    fun submitReview(review: Review, callback: (Boolean) -> Unit) {
        val reviewRef = db.collection("reviews").document(review.turfId)

        // Use an auto-generated ID for each review inside the turf-specific document
        reviewRef.collection("turfReviews").document().set(review)
            .addOnSuccessListener {
                callback(true)
            }
            .addOnFailureListener {
                callback(false)
            }
    }

    fun updateTurfRating(turfId: String, rating: Int, callback: (Boolean) -> Unit) {
        val turfRef = db.collection("turfs").document(turfId)
        turfRef.get().addOnSuccessListener { document ->
            if (document.exists()) {
                val turfData = document.toObject(Turf::class.java)
                if (turfData != null) {
                    turfData.rating_count += 1
                    turfData.rating_sum += rating
                    turfRef.set(turfData)
                        .addOnSuccessListener {
                            callback(true)
                        }
                        .addOnFailureListener { e ->
                            callback(false)
                        }
                }
            }
        }
    }
}

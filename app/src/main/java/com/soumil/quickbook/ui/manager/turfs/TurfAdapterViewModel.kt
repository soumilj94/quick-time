package com.soumil.quickbook.ui.manager.turfs

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.google.firebase.firestore.FirebaseFirestore

class TurfAdapterViewModel(private val turfUid: String): ViewModel() {
    private val db = FirebaseFirestore.getInstance()

    private val getTurfName = MutableLiveData<String>().apply {
        value = "Loading..."  // Default value while loading
    }
    val editTurfName: LiveData<String> get() = getTurfName

    fun getTurfName() {
        if (turfUid.isEmpty()) {
            getTurfName.value = "Invalid Turf ID"
            return
        }

        db.collection("turfs").document(turfUid).addSnapshotListener { document, error ->
            if (error != null) {
                getTurfName.value = "Error fetching name"
                return@addSnapshotListener
            }

            if (document != null && document.exists()) {
                val turfName = document.getString("name")
                getTurfName.value = turfName ?: "Unnamed Turf"
            } else {
                getTurfName.value = "Turf not found"
            }
        }
    }
}

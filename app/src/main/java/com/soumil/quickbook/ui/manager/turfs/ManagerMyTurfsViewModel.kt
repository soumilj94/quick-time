package com.soumil.quickbook.ui.manager.turfs

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.soumil.quickbook.models.Turf

class ManagerMyTurfsViewModel : ViewModel() {

    private val db = FirebaseFirestore.getInstance()
    private val auth = FirebaseAuth.getInstance()

    private val getManagerName = MutableLiveData<String>()
    val managerName: LiveData<String> get() = getManagerName

    private val getCityName = MutableLiveData<String>()
    val cityName: LiveData<String> get() = getCityName

    private val _turfs = MutableLiveData<List<Turf>>()
    val turfs: LiveData<List<Turf>> get() = _turfs

    private val _loading = MutableLiveData<Boolean>()
    val loading: LiveData<Boolean> get() = _loading

    init {
        fetchTurf()
    }

    fun getManagerName() {
        val currentManager = auth.currentUser

        if (currentManager != null) {
            val uid = currentManager.uid
            db.collection("managers").document(uid).addSnapshotListener { document, error ->
                if (error != null) {
                    return@addSnapshotListener
                }
                if (document != null && document.exists()) {
                    val managerName = document.getString("name")
                    getManagerName.value = managerName ?: "null"
                }
                if (document != null && document.exists()) {
                    val cityName = document.getString("city")
                    getCityName.value = cityName ?: "null"
                } else {
                    getManagerName.value = "null"
                    getCityName.value = "null"
                }
            }
        }
    }

    fun getCityName() {
        val currentManager = auth.currentUser

        if (currentManager != null) {
            val uid = currentManager.uid
            db.collection("managers").document(uid).addSnapshotListener { document, error ->
                if (error != null) {
                    return@addSnapshotListener
                }
                if (document != null && document.exists()) {
                    val cityName = document.getString("city")
                    getCityName.value = cityName ?: "null"
                } else {
                    getCityName.value = "null"
                }
            }
        }

    }

    private fun fetchTurf() {
        _loading.value = true

        val user = auth.currentUser ?: return
        val managerRef = db.collection("managers").document(user.uid)

        managerRef.get().addOnSuccessListener { document ->
            if (document != null && document.exists()) {
                val turfsOwned = document.get("turfs_owned") as? List<String> ?: emptyList() // DO NOT change the List<String> to List<*>
                fetchTurfDetails(turfsOwned)
            }
            else{
                _loading.value = false
            }
        }
            .addOnFailureListener {
                _loading.value = false
            }
    }

    private fun fetchTurfDetails(turfIds: List<String>) {
        val turfDetails = mutableListOf<Turf>()
        turfIds.forEach { turfId ->
            db.collection("turfs").document(turfId).get().addOnSuccessListener { document ->
                if (document != null && document.exists()) {
                    val turf = document.toObject(Turf::class.java)
                    turf?.let { turfDetails.add(it) }
                    if (turfDetails.size == turfIds.size) {
                        _turfs.value = turfDetails
                        _loading.value = false
                    }
                }
            }
                .addOnFailureListener {
                    _loading.value = false
                }
        }
        if (turfIds.isEmpty()){
            _loading.value = false
        }
    }
}
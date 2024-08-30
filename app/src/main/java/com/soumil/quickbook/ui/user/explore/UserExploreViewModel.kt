@file:Suppress("DEPRECATION")

package com.soumil.quickbook.ui.user.explore

import android.content.Context
import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore

class UserExploreViewModel(private val context: Context) {

    //    firestore initialization
    private var db = FirebaseFirestore.getInstance()
    private var auth = FirebaseAuth.getInstance()

    private val _userName = MutableLiveData<String>()
    val userName: LiveData<String> get() = _userName

    private val _cityName = MutableLiveData<String>()
    val cityName: LiveData<String> get() = _cityName

    fun getUserName(){
        val currentUser = auth.currentUser
        if (currentUser != null){
            val uid = currentUser.uid
            db.collection("users").document(uid).addSnapshotListener{ document, error ->
                if (error != null){
                    return@addSnapshotListener
                }
                if (document != null && document.exists()){
                    val userName = document.getString("name")
                    _userName.value = userName ?: ""
                }
                else{
                    _userName.value = "No document found"
                }
            }
        }
    }

    fun getCityName(){
        val currentUser = auth.currentUser
        if (currentUser != null){
            val uid = currentUser.uid
            db.collection("users").document(uid).addSnapshotListener{ document, error ->
                if (error != null){
                    Log.e("Firestore Error", error.message.toString())
                    return@addSnapshotListener
                }
                if (document != null && document.exists()){
                    val cityName = document.getString("city")
                    _cityName.value = cityName ?: "No city name found"
                }
                else{
                    _cityName.value = "No document found"
                }
            }
        }
    }
}
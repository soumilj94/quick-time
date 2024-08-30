package com.soumil.quickbook.ui.user.profile

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore

class UserProfileViewModel {
    val db = FirebaseFirestore.getInstance()
    val auth = FirebaseAuth.getInstance()
    private val currentUser = auth.currentUser

    private val getUserName = MutableLiveData<String>()
    val userName: LiveData<String> get() = getUserName

    private val getUserPhone = MutableLiveData<String>()
    val userPhone: LiveData<String> get() = getUserPhone

    private val getUserCity = MutableLiveData<String>()
    val userCity: LiveData<String> get() = getUserCity

    fun getUserName(){
        if (currentUser != null){
            val uid = currentUser.uid
            db.collection("users").document(uid).addSnapshotListener { document, error ->
                if (error != null){
                    return@addSnapshotListener
                }
                if (document != null && document.exists()){
                    val userName = document.getString("name")
                    getUserName.value = userName ?: "null"
                }
                else{
                    getUserName.value = "null"
                }
            }
        }
    }

    fun getUserPhone(){
        if (currentUser != null){
            val uid = currentUser.uid
            db.collection("users").document(uid).addSnapshotListener { document, error ->
                if (error != null){
                    return@addSnapshotListener
                }
                if (document != null && document.exists()){
                    val userPhone = document.getString("phone")
                    getUserPhone.value = userPhone ?: "null"
                }
                else{
                    getUserPhone.value = "null"
                }
            }
        }
    }

    fun getUserCity(){
        if (currentUser != null){
            val uid = currentUser.uid
            db.collection("users").document(uid).addSnapshotListener { document, error ->
                if (error != null){
                    return@addSnapshotListener
                }
                if (document != null && document.exists()){
                    val userCity = document.getString("city")
                    getUserCity.value = userCity ?: "null"
                }
                else{
                    getUserCity.value = "null"
                }
            }
        }
    }
}
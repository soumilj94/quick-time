package com.soumil.quickbook.ui.manager.profile

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore

class ManagerProfileViewModel : ViewModel() {

    val db = FirebaseFirestore.getInstance()
    val auth = FirebaseAuth.getInstance()
    private val currentManager = auth.currentUser

    private val getManagerName = MutableLiveData<String>()
    val managerName: LiveData<String> get() = getManagerName

    private val getManagerPhone = MutableLiveData<String>()
    val managerPhone: LiveData<String> get() = getManagerPhone

    private val getManagerCity = MutableLiveData<String>()
    val managerCity: LiveData<String> get() = getManagerCity



    fun getManagerName(){
        if (currentManager != null){
            val uid = currentManager.uid
            db.collection("managers").document(uid).addSnapshotListener { document, error ->
                if (error != null){
                    return@addSnapshotListener
                }
                if (document != null && document.exists()){
                    val managerName = document.getString("name")
                    getManagerName.value = managerName ?: "null"
                }
                else{
                    getManagerName.value = "null"
                }
            }
        }
    }

    fun getManagerPhone(){
        if (currentManager != null){
            val uid = currentManager.uid
            db.collection("managers").document(uid).addSnapshotListener { document, error ->
                if (error != null){
                    return@addSnapshotListener
                }
                if (document != null && document.exists()){
                    val userPhone = document.getString("phone")
                    getManagerPhone.value = userPhone ?: "null"
                }
                else{
                    getManagerPhone.value = "null"
                }
            }
        }
    }

    fun getManagerCity(){
        if (currentManager != null){
            val uid = currentManager.uid
            db.collection("managers").document(uid).addSnapshotListener { document, error ->
                if (error != null){
                    return@addSnapshotListener
                }
                if (document != null && document.exists()){
                    val userCity = document.getString("city")
                    getManagerCity.value = userCity ?: "null"
                }
                else{
                    getManagerCity.value = "null"
                }
            }
        }
    }
}
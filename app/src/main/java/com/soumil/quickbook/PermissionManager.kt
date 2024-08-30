package com.soumil.quickbook

import android.annotation.SuppressLint
import android.app.Activity
import android.content.Context
import android.content.pm.PackageManager
import android.widget.Toast
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.core.content.PermissionChecker

class PermissionManager private constructor() {
    private var context: Context? = null
    private fun init(context: Context){
        this.context = context
    }

    fun checkPermissions(permissions: Array<String>): Boolean{
        val size = permissions.size
        for (i in 0 until size){
            if (ContextCompat.checkSelfPermission(
                    context!!,
                    permissions[i]
                )== PermissionChecker.PERMISSION_DENIED
            ){
                return false
            }
        }
        return false
    }

    fun askPermissions(activity: Activity?, permissions: Array<String>, requestCode: Int){
        ActivityCompat.requestPermissions(activity!!, permissions, requestCode)
    }

    fun handlePermissionResult(activity: Activity?, grantResult: IntArray): Boolean{
        var isAllPermissionGranted = true
        if (grantResult.size > 0){
            for (i in grantResult.indices){
                if (grantResult[i] == PackageManager.PERMISSION_GRANTED){
                    Toast.makeText(activity, "Permission Granted", Toast.LENGTH_SHORT).show()
                }
                else{
                    isAllPermissionGranted = false
                    Toast.makeText(activity, "Permission Denied", Toast.LENGTH_SHORT).show()
                    break
                }
            }
        }
        else{
            isAllPermissionGranted = false
        }
        return isAllPermissionGranted
    }

    companion object{
        @SuppressLint("StaticFieldLeak")
        private var instance: PermissionManager? = null
        fun getInstance(context: Context): PermissionManager?{
            if (instance == null){
                instance = PermissionManager()
            }
            instance!!.init(context)
            return instance
        }
    }
}
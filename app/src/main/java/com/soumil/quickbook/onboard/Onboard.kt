package com.soumil.quickbook.onboard

import android.annotation.SuppressLint
import android.content.Context
import android.content.Intent
import android.content.SharedPreferences
import android.os.Build
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.View
import android.widget.Toast
import com.google.firebase.auth.FirebaseAuth
import com.soumil.quickbook.MainActivity
import com.soumil.quickbook.Manager_Dashboard
import com.soumil.quickbook.databinding.ActivityOnboardBinding
import com.soumil.quickbook.registration.owner.SigninOwnerActivity
import com.soumil.quickbook.registration.user.SigninUserActivity

@Suppress("DEPRECATION")
class
Onboard : AppCompatActivity() {
    // Initialization of the view binding, FirebaseAuth instance, and shared preferences
    private lateinit var binding: ActivityOnboardBinding
    private lateinit var auth: FirebaseAuth
    private lateinit var sharedPreferences: SharedPreferences
    @SuppressLint("ObsoleteSdkInt")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // Initialize FirebaseAuth instance
        auth = FirebaseAuth.getInstance()
        // Check if there is a currently logged-in user
        val currentUser = auth.currentUser

        if (currentUser != null) {
            // If a user is logged in, get the stored role from shared preferences
            sharedPreferences = getSharedPreferences("user_prefs", Context.MODE_PRIVATE)
            var role = sharedPreferences.getString("role", "")

            // Based on the role, direct the user to the appropriate activity
            when (role) {
                "user" -> startActivity(Intent(this, MainActivity::class.java))
                "manager" -> startActivity(Intent(this, Manager_Dashboard::class.java))
                else -> {
                    // Handle unknown role or default case
                    Toast.makeText(this, "Unknown role or no role found", Toast.LENGTH_SHORT).show()
                    return
                }
            }
            finish()
            return
        }

        window.statusBarColor = resources.getColor(android.R.color.transparent)
        // If the SDK version is >= Android Marshmallow, adjust the status bar appearance
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            window.decorView.systemUiVisibility = View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN or View.SYSTEM_UI_FLAG_LIGHT_STATUS_BAR
        }
        binding = ActivityOnboardBinding.inflate(layoutInflater)
        setContentView(binding.root)

        // Set an OnClickListener for the user button to navigate to the user sign-in activity
        binding.userBtn.setOnClickListener {
            val i = Intent(this, SigninUserActivity::class.java)
            startActivity(i)
        }

        // Set an OnClickListener for the owner button to navigate to the owner sign-in activity
        binding.ownerBtn.setOnClickListener{
            val i = Intent(this, SigninOwnerActivity::class.java)
            startActivity(i)
        }

    }
}

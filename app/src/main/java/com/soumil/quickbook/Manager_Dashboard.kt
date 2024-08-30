package com.soumil.quickbook

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.view.View
import android.view.animation.Animation
import android.view.animation.ScaleAnimation
import android.widget.Toast
import androidx.fragment.app.Fragment
import com.soumil.quickbook.databinding.ActivityManagerDashboardBinding
import com.soumil.quickbook.ui.manager.bookings.ManagerBookings
import com.soumil.quickbook.ui.manager.turfs.ManagerTurfs
import com.soumil.quickbook.ui.manager.profile.ManagerProfile

class Manager_Dashboard : AppCompatActivity() {

    lateinit var binding: ActivityManagerDashboardBinding
    private var doubleBackToExit = false

    private var selectedTab = 1

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        window.statusBarColor = resources.getColor(R.color.primary_blue)

        binding = ActivityManagerDashboardBinding.inflate(layoutInflater)
        setContentView(binding.root)

        loadFragment(ManagerTurfs())

        // manager-home-tab layout code for bottom navigation bar
        binding.myTurfsLayout.setOnClickListener {

            if (selectedTab != 1){

                binding.bookingText.visibility = View.GONE
                binding.profileText.visibility = View.GONE

                binding.bookingIcon.setImageResource(R.drawable.icons_booking_outline)
                binding.profileIcon.setImageResource(R.drawable.profile_outline)

                binding.bookingLayout.setBackgroundColor(resources.getColor(android.R.color.transparent))
                binding.profileLayout.setBackgroundColor(resources.getColor(android.R.color.transparent))

                // for home to be selected
                binding.myTurfsLayout.setBackgroundResource(R.drawable.rounded_selcted_nav_item_manager)
                binding.myTurfsIcon.setImageResource(R.drawable.icon_myturfs_filled)
                binding.myTurfsText.visibility = View.VISIBLE

                val animation = ScaleAnimation(
                    0.8f, 1.0f,
                    0.8f, 1.0f,
                    Animation.RELATIVE_TO_SELF, 0.5f,
                    Animation.RELATIVE_TO_SELF, 0.5f
                )
                animation.duration = 200
                animation.fillAfter = true
                binding.myTurfsIcon.startAnimation(animation)

                loadFragment(ManagerTurfs())

                selectedTab = 1

            }
        }

        //        for booking tab
        binding.bookingLayout.setOnClickListener {

            if (selectedTab != 2){

                binding.myTurfsText.visibility = View.GONE
                binding.profileText.visibility = View.GONE

                binding.myTurfsIcon.setImageResource(R.drawable.icon_myturfs_outline)
                binding.profileIcon.setImageResource(R.drawable.profile_outline)

                binding.myTurfsLayout.setBackgroundColor(resources.getColor(android.R.color.transparent))
                binding.profileLayout.setBackgroundColor(resources.getColor(android.R.color.transparent))

                // for home to be selected
                binding.bookingLayout.setBackgroundResource(R.drawable.rounded_selcted_nav_item_manager)
                binding.bookingIcon.setImageResource(R.drawable.icons_booking_filled)
                binding.bookingText.visibility = View.VISIBLE

                val animation = ScaleAnimation(
                    0.8f, 1.0f,
                    0.8f, 1.0f,
                    Animation.RELATIVE_TO_SELF, 0.5f,
                    Animation.RELATIVE_TO_SELF, 0.5f
                )
                animation.duration = 200
                animation.fillAfter = true
                binding.bookingIcon.startAnimation(animation)

                loadFragment(ManagerBookings())

                selectedTab = 2

            }
        }

        //        for profile tab
        binding.profileLayout.setOnClickListener {

            if (selectedTab != 3){

                binding.myTurfsText.visibility = View.GONE
                binding.bookingText.visibility = View.GONE

                binding.myTurfsIcon.setImageResource(R.drawable.icon_myturfs_outline)
                binding.bookingIcon.setImageResource(R.drawable.icons_booking_outline)

                binding.myTurfsLayout.setBackgroundColor(resources.getColor(android.R.color.transparent))
                binding.bookingLayout.setBackgroundColor(resources.getColor(android.R.color.transparent))

                // for home to be selected
                binding.profileLayout.setBackgroundResource(R.drawable.rounded_selcted_nav_item_manager)
                binding.profileIcon.setImageResource(R.drawable.profile_filled_manager)
                binding.profileText.visibility = View.VISIBLE

                val animation = ScaleAnimation(
                    0.8f, 1.0f,
                    0.8f, 1.0f,
                    Animation.RELATIVE_TO_SELF, 0.5f,
                    Animation.RELATIVE_TO_SELF, 0.5f
                )
                animation.duration = 200
                animation.fillAfter = true
                binding.profileIcon.startAnimation(animation)

                loadFragment(ManagerProfile())

                selectedTab = 3

            }
        }

    }

    private fun loadFragment(fragment: Fragment) {
        supportFragmentManager
            .beginTransaction()
            .replace(R.id.fragmentContainerManager, fragment)
            .commit()
    }

    @Deprecated("Deprecated in Java")
    override fun onBackPressed() {
        if (doubleBackToExit){
            super.onBackPressed()
            finishAffinity()
            return
        }
        this.doubleBackToExit = true
        Toast.makeText(this, "Press again to exit", Toast.LENGTH_SHORT).show()
        Handler(Looper.getMainLooper()).postDelayed(Runnable { doubleBackToExit = false }, 2000)
    }
}
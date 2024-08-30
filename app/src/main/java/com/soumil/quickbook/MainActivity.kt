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
import com.soumil.quickbook.databinding.ActivityMainBinding
import com.soumil.quickbook.ui.user.explore.UserExplore
import com.soumil.quickbook.ui.user.history.UserHistory
import com.soumil.quickbook.ui.user.profile.UserProfile

class MainActivity : AppCompatActivity(){

    lateinit var binding: ActivityMainBinding
    private var doubleBackToExit = false

    private var selectedTab = 1

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        // user-explore-tab layout code for bottom navigation bar
        loadFragment(UserExplore())
        binding.exploreLayout.setOnClickListener {

            if (selectedTab != 1){

                binding.historyText.visibility = View.GONE
                binding.profileText.visibility = View.GONE

                binding.historyIcon.setImageResource(R.drawable.history_outline)
                binding.profileIcon.setImageResource(R.drawable.profile_outline)

                binding.historyLayout.setBackgroundColor(resources.getColor(android.R.color.transparent))
                binding.profileLayout.setBackgroundColor(resources.getColor(android.R.color.transparent))

                // for home to be selected
                binding.exploreLayout.setBackgroundResource(R.drawable.rounded_selcted_nav_item)
                binding.homeIcon.setImageResource(R.drawable.home_filled)
                binding.exploreText.visibility = View.VISIBLE

                val animation = ScaleAnimation(
                    0.8f, 1.0f,
                    0.8f, 1.0f,
                    Animation.RELATIVE_TO_SELF, 0.5f,
                    Animation.RELATIVE_TO_SELF, 0.5f
                )
                animation.duration = 200
                animation.fillAfter = true
                binding.homeIcon.startAnimation(animation)

                loadFragment(UserExplore())

                selectedTab = 1

            }
        }

        // history-tab layout code
        binding.historyLayout.setOnClickListener {
            if (selectedTab != 2){

                binding.exploreText.visibility = View.GONE
                binding.profileText.visibility = View.GONE

                binding.homeIcon.setImageResource(R.drawable.home_outline)
                binding.profileIcon.setImageResource(R.drawable.profile_outline)

                binding.exploreLayout.setBackgroundColor(resources.getColor(android.R.color.transparent))
                binding.profileLayout.setBackgroundColor(resources.getColor(android.R.color.transparent))

                // for history to be selected
                binding.historyLayout.setBackgroundResource(R.drawable.rounded_selcted_nav_item)
                binding.historyIcon.setImageResource(R.drawable.history_filled)
                binding.historyText.visibility = View.VISIBLE

                val animation = ScaleAnimation(
                    0.8f, 1.0f,
                    0.8f, 1.0f,
                    Animation.RELATIVE_TO_SELF, 0.5f,
                    Animation.RELATIVE_TO_SELF, 0.5f
                )
                animation.duration = 200
                animation.fillAfter = true
                binding.historyLayout.startAnimation(animation)

                loadFragment(UserHistory())
                selectedTab = 2
            }
        }

        // profile-tab layout code
        binding.profileLayout.setOnClickListener {
            if (selectedTab != 3){

                binding.historyText.visibility = View.GONE
                binding.exploreText.visibility = View.GONE

                binding.historyIcon.setImageResource(R.drawable.history_outline)
                binding.homeIcon.setImageResource(R.drawable.home_outline)

                binding.historyLayout.setBackgroundColor(resources.getColor(android.R.color.transparent))
                binding.exploreLayout.setBackgroundColor(resources.getColor(android.R.color.transparent))

                // for home to be selected
                binding.profileLayout.setBackgroundResource(R.drawable.rounded_selcted_nav_item)
                binding.profileIcon.setImageResource(R.drawable.profile_filled)
                binding.profileText.visibility = View.VISIBLE

                val animation = ScaleAnimation(
                    0.8f, 1.0f,
                    0.8f, 1.0f,
                    Animation.RELATIVE_TO_SELF, 0.5f,
                    Animation.RELATIVE_TO_SELF, 0.5f
                )
                animation.duration = 200
                animation.fillAfter = true
                binding.profileLayout.startAnimation(animation)

                loadFragment(UserProfile())
                selectedTab = 3
            }
        }


        if (savedInstanceState == null) {
            loadFragment(UserExplore())
        }

    }

    private fun loadFragment(fragment: Fragment) {
        supportFragmentManager
            .beginTransaction()
            .replace(R.id.fragmentContainer, fragment)
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
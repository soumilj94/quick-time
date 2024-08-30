package com.soumil.quickbook.ui.user.explore

import android.app.Activity
import android.content.Intent
import android.os.Bundle
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.LinearLayout
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import com.google.android.material.snackbar.Snackbar
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase
import com.soumil.quickbook.R
import com.soumil.quickbook.TurfCardAdapter
import com.soumil.quickbook.databinding.FragmentUserExploreBinding
import com.soumil.quickbook.models.City
import com.soumil.quickbook.models.Turf
import com.soumil.quickbook.ui.user.explore.citySelect.CitySelect
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await

@Suppress("DEPRECATION")
class UserExplore : Fragment() {

    private lateinit var binding: FragmentUserExploreBinding
    private lateinit var userExploreViewModel: UserExploreViewModel
    private val db = Firebase.firestore
    private var turfsList: List<Turf> = emptyList()

//    filter items
    private lateinit var cricket: LinearLayout
    private lateinit var football: LinearLayout
    private lateinit var basketball: LinearLayout
    private lateinit var badminton: LinearLayout
    private lateinit var volleyball: LinearLayout
    private lateinit var swimming: LinearLayout

//    filter items textview
    private lateinit var cricketText: TextView
    private lateinit var footballText: TextView
    private lateinit var basketballText: TextView
    private lateinit var badmintonText: TextView
    private lateinit var volleyballText: TextView
    private lateinit var swimmingText: TextView

    private var selectedSport: String? = null


    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = FragmentUserExploreBinding.inflate(inflater, container, false)

        userExploreViewModel = UserExploreViewModel(requireContext())

//        filter buttons
        cricket = binding.sportFiltersBtn.sportCricket
        football = binding.sportFiltersBtn.sportFootball
        basketball = binding.sportFiltersBtn.sportBasketball
        badminton = binding.sportFiltersBtn.sportBadminton
        volleyball = binding.sportFiltersBtn.sportVolleyball
        swimming = binding.sportFiltersBtn.sportSwimming

//        filter buttons text
        cricketText = binding.sportFiltersBtn.cricketText
        footballText = binding.sportFiltersBtn.footballText
        basketballText = binding.sportFiltersBtn.basketballText
        badmintonText = binding.sportFiltersBtn.badmintonText
        volleyballText = binding.sportFiltersBtn.volleyballText
        swimmingText = binding.sportFiltersBtn.swimmingText


        // set city name from firestore
        userExploreViewModel.cityName.observe(viewLifecycleOwner) { city ->
            binding.cityLocationText.text = city
            fetchTurfIdsAndPopulateAdapter(city)
        }
        userExploreViewModel.getCityName()

        userExploreViewModel.userName.observe(viewLifecycleOwner){ name ->
            binding.userNameText.text = name
        }
        userExploreViewModel.getUserName()

        binding.addressContainer.setOnClickListener {
            val i = Intent(requireContext(), CitySelect::class.java)
            startActivityForResult(i, CITY_SELECT_REQUEST_CODE)
        }

        sportFilterListener()

        return binding.root
    }

    private fun sportFilterListener() {
        cricket.setOnClickListener { onSportFilterClick("Cricket", cricketText) }
        football.setOnClickListener { onSportFilterClick("Football", footballText) }
        basketball.setOnClickListener { onSportFilterClick("Basketball", basketballText) }
        badminton.setOnClickListener { onSportFilterClick("Badminton", badmintonText) }
        volleyball.setOnClickListener { onSportFilterClick("Volleyball", volleyballText) }
        swimming.setOnClickListener { onSportFilterClick("Swimming", swimmingText) }
    }

    private fun onSportFilterClick(sport: String, selectedTextView: TextView){
        if (selectedSport == sport){
            selectedSport = null
            resetSportFilterTextColor()
            updateRecyclerView(turfsList)
        }
        else{
            selectedSport = sport
            resetSportFilterTextColor()
            selectedTextView.setTextColor(resources.getColor(R.color.primary_green, null))
            filterRecyclerViewBySport(sport)
        }
    }

    private fun resetSportFilterTextColor() {
        cricketText.setTextColor(resources.getColor(R.color.black))
        footballText.setTextColor(resources.getColor(R.color.black))
        basketballText.setTextColor(resources.getColor(R.color.black))
        badmintonText.setTextColor(resources.getColor(R.color.black))
        volleyballText.setTextColor(resources.getColor(R.color.black))
        swimmingText.setTextColor(resources.getColor(R.color.black))
    }

    private fun filterRecyclerViewBySport(sport: String){
        val filterTurfs = turfsList.filter { it.games.contains(sport) }
        updateRecyclerView(filterTurfs)
    }

    private fun updateRecyclerView(turfsList: List<Turf>){
        val adapter = TurfCardAdapter(turfsList)
        binding.recyclerView.layoutManager = LinearLayoutManager(context, LinearLayoutManager.VERTICAL, false)
        binding.recyclerView.adapter = adapter

        if (turfsList.isEmpty()){
            binding.recyclerView.visibility = View.GONE
            binding.noTurfMessage.visibility = View.VISIBLE
            binding.noTurfMessageText.visibility = View.VISIBLE
        }
        else{
            binding.recyclerView.visibility = View.VISIBLE
            binding.noTurfMessage.visibility = View.GONE
            binding.noTurfMessageText.visibility = View.GONE
        }
    }

    private fun fetchTurfIdsAndPopulateAdapter(city: String) {
        lifecycleScope.launch {
            try {
                binding.progressBar.visibility = View.VISIBLE

                // Fetch the city document
                val cityDoc = db.collection("cities").document(city).get().await()
                val cityData = cityDoc.toObject(City::class.java)
                val turfIds = cityData?.turfs ?: emptyList()

                Log.d("UserExplore", "Turf IDs: $turfIds")

                // Fetch turf information for each turf ID
                val turfList = mutableListOf<Turf>()
                for (turfId in turfIds) {
                    val turfDoc = db.collection("turfs").document(turfId).get().await()
                    val turf = turfDoc.toObject(Turf::class.java)
                    if (turf != null){
                        turfList.add(turf.copy(turf_uid = turfId))
                    }
                }

                Log.d("UserExplore", "Fetched Turfs: $turfList")

                if (isAdded){
                    turfsList = turfList
                    updateRecyclerView(turfList)
//                    val adapter = TurfCardAdapter(turfList)
//                    binding.recyclerView.layoutManager = LinearLayoutManager(context, LinearLayoutManager.VERTICAL, false)
//                    binding.recyclerView.adapter = adapter
                }

            } catch (e: Exception) {
                Log.e("UserExplore", "Error fetching turf data", e)
                if (isAdded){
                    Snackbar.make(binding.root, "Error fetching turf data: ${e.message}", Snackbar.LENGTH_SHORT).show()
                }
            }
            finally {
                if (isAdded){
                    binding.progressBar.visibility = View.GONE
                }
            }
        }
    }

    @Deprecated("Deprecated in Java")
    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == CITY_SELECT_REQUEST_CODE && resultCode == AppCompatActivity.RESULT_OK){
            val addressText = data?.getStringExtra("addressText")
            addressText?.let {
                binding.cityLocationText.text = it
                fetchTurfIdsAndPopulateAdapter(it)
            }
        }
        if (resultCode == Activity.RESULT_OK){
            val selectedCity = data?.getStringExtra("selectedCity")
            selectedCity?.let{
                binding.cityLocationText.text = it
                fetchTurfIdsAndPopulateAdapter(it)
            }
        }
    }

    companion object {
        private const val CITY_SELECT_REQUEST_CODE = 100
    }

}

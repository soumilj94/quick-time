package com.soumil.quickbook.ui.user.explore.citySelect

import android.Manifest
import android.content.Context
import android.content.Intent
import android.content.SharedPreferences
import android.content.pm.PackageManager
import android.location.Geocoder
import android.location.Location
import android.location.LocationManager
import android.os.Bundle
import android.provider.Settings
import android.text.Editable
import android.text.TextWatcher
import android.widget.AdapterView
import android.widget.ArrayAdapter
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationServices
import com.google.android.material.snackbar.Snackbar
import com.soumil.quickbook.R
import com.soumil.quickbook.databinding.ActivityCitySelectBinding
import java.util.Locale

class CitySelect : AppCompatActivity() {

    private lateinit var binding: ActivityCitySelectBinding
    private lateinit var sp: SharedPreferences
    private lateinit var fusedLocationClient: FusedLocationProviderClient
    private lateinit var geocoder: Geocoder

    private val locationPermissionLauncher: ActivityResultLauncher<Array<String>> = registerForActivityResult(ActivityResultContracts.RequestMultiplePermissions()){ permissions ->
        val granted = permissions.entries.all { it.value }
        if (granted){
            checkLocationSettings()
        }
        else{
            Snackbar.make(binding.root, "Location permission is required", Snackbar.LENGTH_SHORT).show()
        }
    }

    private val locationSettingsLauncher: ActivityResultLauncher<Intent> = registerForActivityResult(
        ActivityResultContracts.StartActivityForResult()
    ){ result ->
        if (result.resultCode == RESULT_OK){
            showCoordinates()
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding = ActivityCitySelectBinding.inflate(layoutInflater)
        setContentView(binding.root)

        sp = getSharedPreferences("MyPrefs", Context.MODE_PRIVATE)
        fusedLocationClient = LocationServices.getFusedLocationProviderClient(this)
        geocoder = Geocoder(this, Locale.getDefault())

        val cities = resources.getStringArray(R.array.indian_cities)
        val sortedCities = cities.sortedArray()
        val adapter = ArrayAdapter(this, android.R.layout.simple_list_item_1, sortedCities)
        binding.cityListView.adapter = adapter

        binding.cityListView.onItemClickListener = AdapterView.OnItemClickListener { _, _, position, _ ->
            val selectedCity = adapter.getItem(position)
            saveCity(selectedCity)
            val resultIntent = Intent()
            resultIntent.putExtra("selectedCity", selectedCity)
            setResult(RESULT_OK, resultIntent)
            finish()
        }

        binding.searchField.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}

            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
                adapter.filter.filter(s)
            }

            override fun afterTextChanged(s: Editable?) {}
        })

        binding.getLocationBtn.setOnClickListener {
            if (ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED){
                checkLocationSettings()
            }
            else{
                locationPermissionLauncher.launch(
                    arrayOf(
                        Manifest.permission.ACCESS_FINE_LOCATION,
                        Manifest.permission.ACCESS_COARSE_LOCATION
                    )
                )
            }
        }
    }

    private fun checkLocationSettings() {
        val locationManager = getSystemService(Context.LOCATION_SERVICE) as LocationManager
        if (!locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER)) {
            val intent = Intent(Settings.ACTION_LOCATION_SOURCE_SETTINGS)
            locationSettingsLauncher.launch(intent)
        } else {
            showCoordinates()
            Snackbar.make(binding.root, "Location services are already enabled", Snackbar.LENGTH_SHORT).show()
        }
    }

    private fun showCoordinates() {
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED) {
            fusedLocationClient.lastLocation
                .addOnSuccessListener { location: Location? ->
                    if (location != null) {
                        getAddressFromLocation(location)
                    } else {
                        Snackbar.make(binding.root, "Unable to get location", Snackbar.LENGTH_SHORT).show()
                    }
                }
        } else {
            Snackbar.make(binding.root, "Location permission is required", Snackbar.LENGTH_SHORT).show()
        }
    }

    private fun getAddressFromLocation(location: Location){
        val latitude = location.latitude
        val longitude = location.longitude
        try{
            val addresses = geocoder.getFromLocation(latitude, longitude, 1)
            if (addresses?.isNotEmpty() == true){
                val address = addresses[0]
                val city = address.locality.trim()
                val addressText = city.trimIndent()
                sendAddressToFragment(addressText)
            }
            else{
                Snackbar.make(binding.root, "No address found for this location", Snackbar.LENGTH_SHORT).show()
            }
        }
        catch (e: Exception){
            e.printStackTrace()
            Snackbar.make(binding.root, "Error fetching address", Snackbar.LENGTH_SHORT).show()
        }
    }

    private fun sendAddressToFragment(addressText: String){
        val resultIntent = Intent()
        resultIntent.putExtra("addressText", addressText)
        setResult(RESULT_OK, resultIntent)
        finish()
    }

    private fun saveCity(selectedCity: String?) {
        val editor = sp.edit()
        editor.putString("selectedCity", selectedCity)
        editor.apply()
    }
}

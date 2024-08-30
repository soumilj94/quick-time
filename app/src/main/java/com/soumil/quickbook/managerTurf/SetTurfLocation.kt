package com.soumil.quickbook.managerTurf

import androidx.fragment.app.Fragment
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.navigation.fragment.findNavController
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.OnMapReadyCallback
import com.google.android.gms.maps.SupportMapFragment
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.Marker
import com.google.android.gms.maps.model.MarkerOptions
import com.soumil.quickbook.databinding.FragmentSetTurfLocationBinding
import com.soumil.quickbook.R

class SetTurfLocation : Fragment() {
    // Fragment for selection of Turf Location co-ordinates using Google Maps

    private lateinit var binding: FragmentSetTurfLocationBinding
    private lateinit var googleMap: GoogleMap
    private var marker: Marker? = null

    private val callback = OnMapReadyCallback { googleMap ->
        this.googleMap = googleMap

        googleMap.setOnMapClickListener { latLng ->
            marker?.remove() // Remove previous marker if any
            marker = googleMap.addMarker(MarkerOptions().position(latLng).title("Selected Location"))
            googleMap.animateCamera(CameraUpdateFactory.newLatLng(latLng))
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = FragmentSetTurfLocationBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        val mapFragment = childFragmentManager.findFragmentById(R.id.map) as SupportMapFragment?
        mapFragment?.getMapAsync(callback)

        binding.doneButton.setOnClickListener {
            marker?.let {
                val position = it.position
                val latitude = position.latitude
                val longitude = position.longitude

                // Create bundle with latitude and longitude
                val bundle = Bundle().apply {
                    putString("latitude", latitude.toString())
                    putString("longitude", longitude.toString())
                }


                findNavController().navigate(R.id.action_set_turf_location_to_turf_details, bundle)
                findNavController().popBackStack(R.id.set_turf_location, true)
            } ?: Toast.makeText(context, "Please drop a pin on the map", Toast.LENGTH_SHORT).show()
        }
    }
}

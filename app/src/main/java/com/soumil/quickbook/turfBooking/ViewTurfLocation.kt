package com.soumil.quickbook.turfBooking

import androidx.fragment.app.Fragment

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.navigation.fragment.findNavController

import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.OnMapReadyCallback
import com.google.android.gms.maps.SupportMapFragment
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.MarkerOptions
import com.soumil.quickbook.R
import com.soumil.quickbook.databinding.FragmentSetTurfLocationBinding
import com.soumil.quickbook.databinding.FragmentViewTurfLocationBinding

class ViewTurfLocation : Fragment() {

    private lateinit var binding: FragmentViewTurfLocationBinding
    private var latitude: String? = null
    private var longitude: String? = null
    private var name:String? = null

    private val callback = OnMapReadyCallback { googleMap ->
        /**
         * Manipulates the map once available.
         * This callback is triggered when the map is ready to be used.
         * This is where we can add markers or lines, add listeners or move the camera.
         * In this case, we just add a marker near Sydney, Australia.
         * If Google Play services is not installed on the device, the user will be prompted to
         * install it inside the SupportMapFragment. This method will only be triggered once the
         * user has installed Google Play services and returned to the app.
         */
        val loc = LatLng(latitude!!.toDouble(), longitude!!.toDouble())
        googleMap.addMarker(MarkerOptions().position(loc).title("$name"))
        googleMap.moveCamera(CameraUpdateFactory.newLatLng(loc))
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        binding = FragmentViewTurfLocationBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        arguments?.let {
            name = it.getString("name",null)
            latitude = it.getString("latitude", null)
            longitude = it.getString("longitude", null)

        val mapFragment = childFragmentManager.findFragmentById(R.id.map) as SupportMapFragment?
        mapFragment?.getMapAsync(callback)


            binding.doneButton.setOnClickListener {
                findNavController().navigate(R.id.action_viewTurfLocation_to_turfInfo)
                findNavController().popBackStack(R.id.viewTurfLocation, true)
            }




            //Toast.makeText(requireContext(),"$latitude  $longitude",Toast.LENGTH_SHORT).show()
        }
    }
}
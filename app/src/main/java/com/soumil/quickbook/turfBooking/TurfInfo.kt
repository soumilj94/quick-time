package com.soumil.quickbook.turfBooking

import android.content.Context
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.EditText
import android.widget.RatingBar
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.findNavController
import com.google.android.material.chip.Chip
import com.google.android.material.chip.ChipGroup
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.soumil.quickbook.R
import com.soumil.quickbook.databinding.FragmentTurfInfoBinding
import com.soumil.quickbook.models.Review
import com.soumil.quickbook.models.Turf
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

class   TurfInfo : Fragment() {

    private lateinit var binding: FragmentTurfInfoBinding
    private lateinit var viewModel: TurfInfoViewModel
    private lateinit var latitude: String
    private lateinit var longitude: String
    private lateinit var turfName:String
    private lateinit var currentTurf: Turf

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        binding = FragmentTurfInfoBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)
        viewModel = ViewModelProvider(this).get(TurfInfoViewModel::class.java)

        val sharedPreferences =
            requireActivity().getSharedPreferences("user_prefs", Context.MODE_PRIVATE)
        val username = sharedPreferences.getString("fullName", "")

        val turfId = activity?.intent?.getStringExtra("TURF_UID")

        turfId?.let {
            viewModel.fetchTurfDetails(it)
        }

        viewModel.turf.observe(viewLifecycleOwner, Observer { turf ->
            if (turf != null) {
                binding.infoName.text = turf.name
                binding.infoCity.text = turf.city
                binding.infoPrice.text = "₹" +turf.price.toString()
                if(turf.rating_count!=0) {
                    val rating: Double = turf.rating_sum.toDouble() / turf.rating_count
                    binding.infoRating.text =  String.format("%.1f", rating)
                }else{
                    binding.infoRating.text = "-"
                }
                binding.infoOpen.text = turf.open_time
                binding.infoClose.text = turf.close_time


                setChips(binding.gamesChips,turf.games)
                setChips(binding.daysChips,turf.weekdays)

                //binding.infoDays.text = turf.weekdays.joinToString(", ")
                //binding.infoGames.text = turf.games.joinToString(", ")
                latitude = turf.latitude
                longitude = turf.longitude
                turfName = turf.name
                currentTurf = turf

                binding.viewPagerImages.adapter = ImageSliderAdapter(turf.pictures)
                binding.progressBar.visibility = View.GONE

            } else {
                Toast.makeText(requireContext(),"error",Toast.LENGTH_SHORT).show()
            }
        })

        binding.textView9.setOnClickListener {
            val bundle = Bundle().apply {
                putString("turfId",turfId)
            }
            it.findNavController().navigate(R.id.action_turfInfo_to_viewReviews,bundle)
        }

        binding.viewMap.setOnClickListener {
            val bundle = Bundle().apply {
                putString("name",turfName)
                putString("latitude", latitude)
                putString("longitude", longitude)
            }
            it.findNavController().navigate(R.id.action_turfInfo_to_viewTurfLocation,bundle)
        }

        binding.addRating.setOnClickListener {
            val ratingDialog = LayoutInflater.from(context).inflate(R.layout.user_turf_review_dialog, null)
            val reviewInput = ratingDialog.findViewById<EditText>(R.id.userReview)
            val ratingBar = ratingDialog.findViewById<RatingBar>(R.id.ratingBar)

            val alertDialog = MaterialAlertDialogBuilder(requireContext())
                .setTitle("Rate us")
                .setView(ratingDialog)
                .setPositiveButton("Submit") { dialog, _ ->
                    val rating = ratingBar.rating.toInt()
                    val reviewText = reviewInput.text.toString()
                    if (rating > 0 && reviewText.isNotEmpty()) {
                        if (username != null && viewModel.userId != null && turfId != null) {
                            val currentDate = getCurrentDate()
                            val review = Review(
                                date = currentDate,
                                userName = username,
                                userId = viewModel.userId!!,
                                turfId = turfId,
                                rating = rating,
                                review = reviewText,
                                turfName = turfName
                            )

                            viewModel.submitReview(review) { success ->
                                if (success) {

                                    Toast.makeText(
                                        requireContext(),
                                        "Thanks for the feedback!",
                                        Toast.LENGTH_SHORT
                                    ).show()
                                } else {
                                    Toast.makeText(
                                        requireContext(),
                                        "Failed to submit review",
                                        Toast.LENGTH_SHORT
                                    ).show()
                                }
                            }

                            viewModel.updateTurfRating(turfId, rating) { success ->
                                if (success) {
                                    Toast.makeText(
                                        requireContext(),
                                        "Turf ratings updated",
                                        Toast.LENGTH_SHORT
                                    ).show()
                                } else {
                                    Toast.makeText(
                                        requireContext(),
                                        "Failed to update review",
                                        Toast.LENGTH_SHORT
                                    ).show()
                                }
                            }
                        } else {
                            Toast.makeText(
                                requireContext(),
                                "Error: User or Turf details missing",
                                Toast.LENGTH_SHORT
                            ).show()
                        }
                    }else {
                        Toast.makeText(requireContext(), "Please provide a rating and a review", Toast.LENGTH_SHORT).show()
                    }

                    dialog.dismiss()
                }
                .setNegativeButton("Cancel") { dialog, _ ->
                    dialog.dismiss()
                }
                .create()

            alertDialog.setOnShowListener {
                alertDialog.window?.setBackgroundDrawableResource(R.drawable.alert_dialog_background)
            }

            alertDialog.show()
        }


        binding.proceed.setOnClickListener {

            val bundle = Bundle().apply {
                putString("name", currentTurf.name)
                putString("city", currentTurf.city)
                putInt("price", currentTurf.price)
                putString("open_time", currentTurf.open_time)
                putString("close_time", currentTurf.close_time)
                putStringArrayList("weekdays", ArrayList(currentTurf.weekdays))
                putStringArrayList("games", ArrayList(currentTurf.games))
                putStringArrayList("pictures", ArrayList(currentTurf.pictures))
                putStringArrayList("bookings", ArrayList(currentTurf.bookings))
                putInt("rating_count",currentTurf.rating_count)
                putInt("rating_sum",currentTurf.rating_sum)
                putString("latitude", currentTurf.latitude)
                putString("longitude", currentTurf.longitude)
                putString("turf_uid", currentTurf.turf_uid)
                putString("manager_id", currentTurf.manager_id)
            }
            it.findNavController().navigate(R.id.action_turfInfo_to_bookingForm,bundle)
        }

    }

    private fun getCurrentDate(): String {
        val dateFormat = SimpleDateFormat("dd/MM/yyyy", Locale.getDefault())
        return dateFormat.format(Date())
    }
    private fun setChips(chipGroup: ChipGroup, days: List<String>?) {
        chipGroup.removeAllViews()
        days?.forEach { item ->
            val chip = Chip(chipGroup.context)
            chip.text = item
            chipGroup.addView(chip)
        }
    }

}

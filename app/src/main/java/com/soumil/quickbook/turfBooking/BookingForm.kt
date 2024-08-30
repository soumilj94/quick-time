package com.soumil.quickbook.turfBooking

import android.app.AlertDialog
import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.*
import androidx.core.content.ContextCompat
import androidx.core.view.children
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import com.google.firebase.firestore.FirebaseFirestore
import com.soumil.quickbook.R
import com.soumil.quickbook.databinding.FragmentBookingFormBinding
import com.soumil.quickbook.models.Turf
import java.text.SimpleDateFormat
import java.util.*

class BookingForm : Fragment() {

    private lateinit var binding: FragmentBookingFormBinding
    private lateinit var viewModel: BookingFormViewModel

    // Selected date and time slots
    private var selectedDate: String? = null
    private var selectedTimeSlots: List<String> = emptyList()

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        binding = FragmentBookingFormBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        viewModel = ViewModelProvider(this).get(BookingFormViewModel::class.java)

        // Set up date TextViews
        setupDateTextViews()

//        binding.backBtn.setOnClickListener { view.findNavController().navigateUp() }

        binding.day1Tv.setOnClickListener { selectDate(binding.day1Tv) }
        binding.day2Tv.setOnClickListener { selectDate(binding.day2Tv) }
        binding.day3Tv.setOnClickListener { selectDate(binding.day3Tv) }

        binding.backBtn.setOnClickListener {
            if (requireActivity().supportFragmentManager.backStackEntryCount > 0) {
                requireActivity().supportFragmentManager.popBackStack()
            } else {
                requireActivity().finish()
            }
        }

        binding.proceed.setOnClickListener { proceedToBooking() }

        // Fetch user details
        val sharedPreferences = requireActivity().getSharedPreferences("user_prefs", Context.MODE_PRIVATE)
        val username = sharedPreferences.getString("fullName", "default_value")
        val userphone = sharedPreferences.getString("phoneNumber", "default_value")

        if (username != null && userphone != null) {
            viewModel.setUserDetails(username, userphone)
        }

        viewModel.username.observe(viewLifecycleOwner) { name ->
            binding.userName.text = name
        }

        viewModel.userphone.observe(viewLifecycleOwner) { phone ->
            binding.userPhone.text = phone
        }

        // Fetch turf details from arguments
        arguments?.let { bundle ->
            val turf = Turf(
                name = bundle.getString("name") ?: "",
                city = bundle.getString("city") ?: "",
                price = bundle.getInt("price"),
                open_time = bundle.getString("open_time") ?: "",
                close_time = bundle.getString("close_time") ?: "",
                weekdays = bundle.getStringArrayList("weekdays") ?: emptyList(),
                games = bundle.getStringArrayList("games") ?: emptyList(),
                pictures = bundle.getStringArrayList("pictures") ?: emptyList(),
                bookings = bundle.getStringArrayList("bookings") ?: emptyList(),
                rating_count = bundle.getInt("rating_count"),
                rating_sum = bundle.getInt("rating_sum"),
                latitude = bundle.getString("latitude") ?: "",
                longitude = bundle.getString("longitude") ?: "",
                turf_uid = bundle.getString("turf_uid") ?: "",
                manager_id = bundle.getString("manager_id") ?: ""
            )
            viewModel.setTurf(turf)
        }

        viewModel.turf.observe(viewLifecycleOwner) {
            it?.let { turf ->
                val adapter = ArrayAdapter(requireContext(), android.R.layout.simple_spinner_item, turf.games)
                adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
                binding.gameSpinner.adapter = adapter

                viewModel.selectedGame.value?.let { selectedGame ->
                    val position = adapter.getPosition(selectedGame)
                    binding.gameSpinner.setSelection(position)
                }
            }

            binding.gameSpinner.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
                override fun onItemSelected(parent: AdapterView<*>, view: View?, position: Int, id: Long) {
                    val selectedGame = parent.getItemAtPosition(position).toString()
                    viewModel.setSelectedGame(selectedGame)
                }

                override fun onNothingSelected(parent: AdapterView<*>) {
                    // nothing is selected
                }
            }

            binding.players.addTextChangedListener(object : TextWatcher {
                override fun afterTextChanged(s: Editable?) {
                    val number = s.toString().toIntOrNull()
                    if (number != null && number in 2..12) {
                        viewModel.setSelectedPlayerCount(number)
                    } else {
                        Toast.makeText(
                            requireContext(),
                            "Please enter a number between 2 and 12",
                            Toast.LENGTH_SHORT
                        ).show()
                    }
                }

                override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
                override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {}
            })
        }
    }

    private fun setupDateTextViews() {
        val sdf = SimpleDateFormat("dd MMMM", Locale.getDefault())
        val today = Calendar.getInstance()

        binding.day1Tv.text = sdf.format(today.time)

        today.add(Calendar.DAY_OF_MONTH, 1)
        binding.day2Tv.text = sdf.format(today.time)

        today.add(Calendar.DAY_OF_MONTH, 1)
        binding.day3Tv.text = sdf.format(today.time)
    }

    private fun selectDate(selectedTextView: TextView) {
        // Reset all TextViews
        binding.day1Tv.setBackgroundColor(ContextCompat.getColor(requireContext(), android.R.color.transparent))
        binding.day2Tv.setBackgroundColor(ContextCompat.getColor(requireContext(), android.R.color.transparent))
        binding.day3Tv.setBackgroundColor(ContextCompat.getColor(requireContext(), android.R.color.transparent))

        // Highlight the selected TextView
        selectedTextView.setBackgroundColor(ContextCompat.getColor(requireContext(), R.color.light_green))

        // Save the selected date
        selectedDate = selectedTextView.text.toString()

        // Open the time slot selection dialog
        openTimeSlotDialog()
    }

    private fun openTimeSlotDialog() {
        val dialogView = layoutInflater.inflate(R.layout.dialog_time_slots, null)
        val timeSlotLayout = dialogView.findViewById<LinearLayout>(R.id.time_slot_layout)

        val turf = viewModel.turf.value
        val turfId = turf?.turf_uid
        val selectedDate = selectedDate

        if (turfId != null && selectedDate != null) {
            // Query Firestore for booked slots
            FirebaseFirestore.getInstance().collection("bookings")
                .whereEqualTo("turfId", turfId)
                .whereEqualTo("date", selectedDate)
                .get()
                .addOnSuccessListener { querySnapshot ->
                    val bookedSlots = mutableListOf<String>()

                    for (document in querySnapshot.documents) {
                        val slots = document.getString("slots")
                        slots?.split(", ")?.let { bookedSlots.addAll(it) }
                    }

                    // Get open and close time from bundle
                    val openTime = turf.open_time
                    val closeTime = turf.close_time

                    // Generate dynamic time slots based on open and close time
                    val timeSlots = generateTimeSlots(openTime, closeTime)

                    // Dynamically create checkboxes for each available time slot
                    timeSlots.forEach { slot ->
                        if (!bookedSlots.contains(slot)) {
                            val checkBox = CheckBox(requireContext())
                            checkBox.text = slot
                            timeSlotLayout.addView(checkBox)
                        }
                    }

                    AlertDialog.Builder(requireContext())
                        .setView(dialogView)
                        .setPositiveButton("OK") { _, _ ->
                            selectedTimeSlots = timeSlotLayout.children
                                .filter { (it as CheckBox).isChecked }
                                .map { (it as CheckBox).text.toString() }
                                .toList()
                        }
                        .setNegativeButton("Cancel", null)
                        .show()
                }
                .addOnFailureListener { e ->
                    Toast.makeText(requireContext(), "Failed to load time slots: ${e.message}", Toast.LENGTH_SHORT).show()
                }
        }
    }

    private fun generateTimeSlots(openTime: String, closeTime: String): List<String> {
        val slots = mutableListOf<String>()
        val formatter = SimpleDateFormat("HH:mm", Locale.getDefault())

        val open = Calendar.getInstance().apply {
            time = formatter.parse(openTime) ?: Date()
        }

        val close = Calendar.getInstance().apply {
            time = formatter.parse(closeTime) ?: Date()
        }

        while (open.before(close)) {
            val slotStart = formatter.format(open.time)
            open.add(Calendar.HOUR_OF_DAY, 1)
            val slotEnd = formatter.format(open.time)
            slots.add("$slotStart - $slotEnd")
        }

        return slots
    }


    private fun parseDate(dateString: String): Date? {
        return try {
            SimpleDateFormat("dd MMMM", Locale.getDefault()).parse(dateString)
        } catch (e: Exception) {
            null
        }
    }

    private fun proceedToBooking() {
        val userName = binding.userName.text.toString()
        val userPhone = binding.userPhone.text.toString()
        val playerCount = binding.players.text.toString().toIntOrNull()
        val selectedGame = viewModel.selectedGame.value
        val turf = viewModel.turf.value

        if (userName.isNotBlank() && userPhone.isNotBlank() && playerCount != null && selectedGame != null && selectedDate != null && selectedTimeSlots.isNotEmpty() && turf != null) {
            val intent = Intent(requireContext(), ConfirmActivity::class.java).apply {
                putExtra("userName", userName)
                putExtra("userPhone", userPhone)
                putExtra("playerCount", playerCount)
                putExtra("user_id",viewModel.userId)
                putExtra("selectedGame", selectedGame)
                putExtra("selectedDate", selectedDate)
                putStringArrayListExtra("selectedTimeSlots", ArrayList(selectedTimeSlots))

                putExtra("name", turf.name)
                putExtra("city", turf.city)
                putExtra("price", turf.price)
                putExtra("turf_id", turf.turf_uid)
                putExtra("manager_id", turf.manager_id)
            }
            startActivity(intent)
        } else {
            Toast.makeText(requireContext(), "Please fill all the details", Toast.LENGTH_SHORT).show()
        }
    }

}

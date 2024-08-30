package com.soumil.quickbook.ui.manager.bookings

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.recyclerview.widget.LinearLayoutManager
import com.google.android.material.chip.Chip
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.soumil.quickbook.TicketAdapter
import com.soumil.quickbook.databinding.FragmentManagerBookingsBinding
import com.soumil.quickbook.models.Ticket

class ManagerBookings : Fragment() {
    private lateinit var binding: FragmentManagerBookingsBinding
    private lateinit var ticketAdapter: TicketAdapter
    private val ticketList = mutableListOf<Ticket>()
    private val allTickets = mutableListOf<Ticket>()
    private val db = FirebaseFirestore.getInstance()
    private val auth = FirebaseAuth.getInstance()

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        binding = FragmentManagerBookingsBinding.inflate(inflater, container, false)

        setupRecyclerView()
        fetchManagerData()

        return binding.root
    }


   /* private fun setupRecyclerView() {
        ticketAdapter = TicketAdapter(ticketList)
        binding.managerTicketList.apply {
            layoutManager = LinearLayoutManager(requireContext())
            adapter = ticketAdapter
        }
    }

    private fun setupChipListener() {
        binding.chipGroup.setOnCheckedChangeListener { group, checkedId ->
            when (checkedId) {
                R.id.chipAll -> fetchAllBookingsForManager()
                // Add more chips and their listeners if needed
            }
        }
    }

    private fun fetchAllBookingsForManager() {
        ticketList.clear() // Clear the list to avoid duplicates
        val currentUser = auth.currentUser

        if (currentUser != null) {
            val managerId = currentUser.uid

            // Fetch the manager's document to get the turfs_owned field
            db.collection("managers").document(managerId).get()
                .addOnSuccessListener { document ->
                    if (document != null && document.exists()) {
                        val turfsOwned = document.get("turfs_owned") as? List<String>
                        if (turfsOwned != null) {
                            fetchBookingsForTurfs(turfsOwned)
                        }
                    }
                }
                .addOnFailureListener { exception ->
                    // Handle the error
                }
        }
    }

    private fun fetchBookingsForTurfs(turfsOwned: List<String>) {
        for (turfId in turfsOwned) {
            db.collection("turfs").document(turfId).get()
                .addOnSuccessListener { document ->
                    if (document != null && document.exists()) {
                        val bookingIds = document.get("bookings") as? List<String>
                        if (bookingIds != null) {
                            fetchTickets(bookingIds)
                        }
                    }
                }
                .addOnFailureListener { exception ->
                    // Handle the error
                }
        }
    }

    private fun fetchTickets(bookingIds: List<String>) {
        for (ticketId in bookingIds) {
            db.collection("bookings").document(ticketId).get()
                .addOnSuccessListener { document ->
                    if (document != null && document.exists()) {
                        val ticket = document.toObject(Ticket::class.java)
                        if (ticket != null) {
                            ticketList.add(ticket)
                            ticketList.sortByDescending { it.timestamp } // Sort by timestamp to show latest first
                            ticketAdapter.notifyDataSetChanged()
                        }
                    }
                }
                .addOnFailureListener { exception ->
                    // Handle the error
                }
        }
    }
}*/

    private fun setupRecyclerView() {
        ticketAdapter = TicketAdapter(ticketList)
        binding.managerTicketList.apply {
            layoutManager = LinearLayoutManager(requireContext())
            adapter = ticketAdapter
        }
    }

    private fun fetchManagerData() {
        val currentUser = auth.currentUser

        if (currentUser != null) {
            val managerId = currentUser.uid

            db.collection("managers").document(managerId).get()
                .addOnSuccessListener { document ->
                    if (document != null && document.exists()) {
                        val turfsOwned = document.get("turfs_owned") as? List<String>
                        if (turfsOwned != null) {
                            setupTurfChips(turfsOwned)
                            fetchAllTickets(turfsOwned)
                        }
                    }
                }
                .addOnFailureListener { exception ->
                    // Handle the error
                    Toast.makeText(requireContext(), "Failed to fetch manager data", Toast.LENGTH_SHORT).show()
                }
        }
    }

    private fun setupTurfChips(turfIds: List<String>) {
        val chipGroup = binding.chipGroup

        // Add "All" chip to show all tickets by default
        val allChip = Chip(requireContext()).apply {
            text = "All"
            isCheckable = true
            isChecked = true // Default to showing all tickets
            setOnClickListener {
                showAllTickets()
            }
        }
        chipGroup.addView(allChip)

        // Fetch and add chips for each turf
        for (turfId in turfIds) {
            db.collection("turfs").document(turfId).get()
                .addOnSuccessListener { document ->
                    if (isAdded){
                    if (document != null && document.exists()) {
                        val turfName = document.getString("name") ?: turfId // Use turf name or ID if name is not available
                        val turfChip = Chip(requireContext()).apply {
                            text = turfName
                            isCheckable = true
                            setOnClickListener {
                                filterTicketsByTurf(turfId)
                            }
                        }
                        chipGroup.addView(turfChip)
                    }
                }}
                .addOnFailureListener { exception ->
                    // Handle the error
                    if (isAdded){
                        Toast.makeText(requireContext(), "Error fetching turf data", Toast.LENGTH_SHORT).show()
                    }
                }
        }
    }

    private fun fetchAllTickets(turfIds: List<String>) {
        for (turfId in turfIds) {
            db.collection("turfs").document(turfId).get()
                .addOnSuccessListener { document ->
                    val bookings = document.get("bookings") as? List<String>
                    if (bookings != null) {
                        fetchTickets(bookings)
                    }
                }
                .addOnFailureListener { exception ->
                    // Handle the error
                }
        }
    }

    private fun fetchTickets(bookingHistory: List<String>) {
        for (ticketId in bookingHistory) {
            db.collection("bookings").document(ticketId).get()
                .addOnSuccessListener { document ->
                    if (document != null && document.exists()) {
                        val ticket = document.toObject(Ticket::class.java)
                        if (ticket != null) {
                            allTickets.add(ticket)
                            ticketList.add(ticket) // Add to the main list initially
                            ticketAdapter.notifyDataSetChanged()
                        }
                    }
                    checkIfNoBookings()
                }
                .addOnFailureListener { exception ->
                    // Handle the error
                }
        }
    }

    private fun checkIfNoBookings() {
        if (ticketList.isEmpty()){
            binding.noTurfMessage.visibility = View.VISIBLE
            binding.noTurfMessageText.visibility = View.VISIBLE
        }
        else{
            binding.noTurfMessage.visibility = View.GONE
            binding.noTurfMessageText.visibility = View.GONE
        }
    }

    private fun showAllTickets() {
        ticketList.clear()
        ticketList.addAll(allTickets) // Restore the full list
        ticketAdapter.notifyDataSetChanged()
        checkIfNoBookings()
    }

    private fun filterTicketsByTurf(turfId: String) {
        ticketList.clear()
        ticketList.addAll(allTickets.filter { it.turfId == turfId }) // Filter tickets by selected turfId
        ticketAdapter.notifyDataSetChanged()
        checkIfNoBookings()
    }
}


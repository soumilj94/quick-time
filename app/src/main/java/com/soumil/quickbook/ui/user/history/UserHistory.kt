package com.soumil.quickbook.ui.user.history

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.LinearLayoutManager
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.soumil.quickbook.TicketAdapter
import com.soumil.quickbook.databinding.FragmentUserHistoryBinding
import com.soumil.quickbook.models.Ticket

class UserHistory : Fragment() {

    private lateinit var binding: FragmentUserHistoryBinding
    private lateinit var ticketAdapter: TicketAdapter
    private val ticketList = mutableListOf<Ticket>()
    private val db = FirebaseFirestore.getInstance()
    private val auth = FirebaseAuth.getInstance()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        binding = FragmentUserHistoryBinding.inflate(inflater, container, false)

        setupRecyclerView()
        fetchBookingHistory()

        return binding.root
    }

    private fun setupRecyclerView() {
        ticketAdapter = TicketAdapter(ticketList)
        binding.userTicketList.apply {
            layoutManager = LinearLayoutManager(requireContext())
            adapter = ticketAdapter
        }
    }

    private fun fetchBookingHistory() {
        val currentUser = auth.currentUser

        if (currentUser != null) {
            val userId = currentUser.uid

            db.collection("users").document(userId).get()
                .addOnSuccessListener { document ->
                    if (document != null && document.exists()) {
                        val bookingHistory = document.get("bookingHistory") as? List<String>
                        if (bookingHistory != null) {
                            fetchTickets(bookingHistory)
                        }
                        else{
                            handleNoData()
                        }
                    }
                    else{
                        handleNoData()
                    }
                }
                .addOnFailureListener { exception ->
                    // Handle the error
                    handleNoData()
                }
        }
    }

   /* private fun fetchTickets(bookingHistory: List<String>) {
        for (ticketId in bookingHistory) {
            db.collection("bookings").document(ticketId).get()
                .addOnSuccessListener { document ->
                    if (document != null && document.exists()) {
                        val ticket = document.toObject(Ticket::class.java)
                        if (ticket != null) {
                            ticketList.add(ticket)
                            ticketAdapter.notifyDataSetChanged()
                        }
                    }
                }
                .addOnFailureListener { exception ->
                    // Handle the error
                }
        }
    }*/
   private fun fetchTickets(bookingHistory: List<String>) {
       var ticketsFetched = 0

       if (bookingHistory.isEmpty()){
           handleNoData()
           return
       }

       for (ticketId in bookingHistory) {
           db.collection("bookings").document(ticketId).get()
               .addOnSuccessListener { document ->
                   if (document != null && document.exists()) {
                       val ticket = document.toObject(Ticket::class.java)
                       if (ticket != null) {
                           ticketList.add(ticket)
                       }
                   }

                   ticketsFetched++

                   // After all tickets have been fetched, sort and update the adapter
                   if (ticketsFetched == bookingHistory.size) {
                       sortTicketsByTimestamp()
                       ticketAdapter.notifyDataSetChanged()
                       handleNoData()
                   }
               }
               .addOnFailureListener { exception ->
                   ticketsFetched++

                   // Handle the error
                   if (ticketsFetched == bookingHistory.size) {
                       sortTicketsByTimestamp()
                       ticketAdapter.notifyDataSetChanged()
                       handleNoData()
                   }
               }
       }
   }

    private fun handleNoData() {
        if (ticketList.isEmpty()){
            binding.noTurfMessage.visibility = View.VISIBLE
            binding.noTurfMessageText.visibility = View.VISIBLE
        }
        else{
            binding.noTurfMessage.visibility = View.GONE
            binding.noTurfMessageText.visibility = View.GONE
        }
    }

    private fun sortTicketsByTimestamp() {
        ticketList.sortByDescending { it.timestamp }
    }

}
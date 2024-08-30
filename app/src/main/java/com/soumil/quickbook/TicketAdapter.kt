package com.soumil.quickbook

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.soumil.quickbook.models.Review
import com.soumil.quickbook.models.Ticket

class TicketAdapter(private val ticketList: List<Ticket>) : RecyclerView.Adapter<TicketAdapter.TicketViewHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): TicketViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.ticket_card, parent, false)
        return TicketViewHolder(view)
    }

    override fun onBindViewHolder(holder: TicketViewHolder, position: Int) {
        val ticket = ticketList[position]
        holder.bind(ticket)
    }

    override fun getItemCount(): Int {
        return ticketList.size
    }

    class TicketViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        private val turfName: TextView = itemView.findViewById(R.id.turfName)
        private val ticketDate: TextView = itemView.findViewById(R.id.bookingDate)
        private val userName: TextView = itemView.findViewById(R.id.bookedBy)
        private val players: TextView = itemView.findViewById(R.id.playerCount)
        private val game: TextView = itemView.findViewById(R.id.game)
        private val slots: TextView = itemView.findViewById(R.id.slot)
        private val amount: TextView = itemView.findViewById(R.id.totalAmt)


        fun bind(ticket: Ticket) {
            turfName.text = ticket.turfName
            ticketDate.text = ticket.date
            userName.text = ticket.userName
            players.text = ticket.players
            game.text = ticket.game
            slots.text = ticket.slots
            amount.text = ticket.amount

        }
    }
}
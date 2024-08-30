package com.soumil.quickbook

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView

data class SportItem(val title: String)

class SportCardAdapter(
    private val sportList: List<SportItem>,
    private val itemClickListener: (SportItem) -> Unit
) : RecyclerView.Adapter<SportCardAdapter.CardViewHolder>() {

    class CardViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val cardTitle: TextView = itemView.findViewById(R.id.sportName)

        fun bind(sportItem: SportItem, clickListener: (SportItem) -> Unit) {
            cardTitle.text = sportItem.title
            itemView.setOnClickListener { clickListener(sportItem) }
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): CardViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.sport_card, parent, false)
        return CardViewHolder(view)
    }

    override fun onBindViewHolder(holder: CardViewHolder, position: Int) {
        val sportItem = sportList[position]
        holder.bind(sportItem, itemClickListener)
    }

    override fun getItemCount(): Int {
        return sportList.size
    }
}

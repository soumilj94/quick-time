package com.soumil.quickbook

import android.content.Intent
import android.graphics.drawable.Drawable
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ProgressBar
import android.widget.TextView
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.bumptech.glide.load.resource.bitmap.DownsampleStrategy
import com.bumptech.glide.request.RequestOptions
import com.bumptech.glide.request.target.CustomTarget
import com.bumptech.glide.request.transition.Transition
import com.soumil.quickbook.turfBooking.BookingActivityMain
import com.soumil.quickbook.models.Turf

class TurfCardAdapter(private val cardList: List<Turf>) : RecyclerView.Adapter<TurfCardAdapter.CardViewHolder>() {

    class CardViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val turfImage: ConstraintLayout = itemView.findViewById(R.id.turfImageBg)
        val progressBar: ProgressBar = itemView.findViewById(R.id.progressBar)
        val cardTitle: TextView = itemView.findViewById(R.id.cardTitle)
        val cardRating: TextView = itemView.findViewById(R.id.cardRating)
        val cardOpeningTime: TextView = itemView.findViewById(R.id.cardOpeningTime)
        val cardClosingTime: TextView = itemView.findViewById(R.id.cardClosingTime)
        val cardPrice: TextView = itemView.findViewById(R.id.cardPrice)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): CardViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.turf_card, parent, false)
        return CardViewHolder(view)
    }

    override fun onBindViewHolder(holder: CardViewHolder, position: Int) {
        val cardItem = cardList[position]
        holder.cardTitle.text = cardItem.name

        if(cardItem.rating_count!=0) {
            val rating: Double = cardItem.rating_sum.toDouble() / cardItem.rating_count
            holder.cardRating.text = String.format("%.1f", rating)
        }else{
            holder.cardRating.text = "-"
        }
        holder.cardOpeningTime.text = cardItem.open_time
        holder.cardClosingTime.text = cardItem.close_time
        holder.cardPrice.text = "₹" + cardItem.price.toString()

        holder.progressBar.visibility = View.VISIBLE

        if (cardItem.pictures.isNotEmpty()) {
            val context = holder.turfImage.context
            Glide.with(context)
                .load(cardItem.pictures[0])
                .apply(RequestOptions()
                    .override(400,200)
                    .downsample(DownsampleStrategy.AT_LEAST)
                    .centerCrop())
                .into(object : CustomTarget<Drawable>() {
                    override fun onResourceReady(resource: Drawable, transition: Transition<in Drawable>?) {
                        holder.turfImage.background = resource
                    }
                    override fun onLoadCleared(placeholder: Drawable?) {
                        holder.progressBar.visibility = View.GONE
                    }
                })
        }
        else{
            holder.progressBar.visibility = View.GONE
        }

        holder.itemView.setOnClickListener {
            val context = holder.itemView.context
            val intent = Intent(context, BookingActivityMain::class.java).apply {
                putExtra("TURF_UID", cardItem.turf_uid)
            }
            context.startActivity(intent)
        }
    }

    override fun getItemCount(): Int {
        return cardList.size
    }
}
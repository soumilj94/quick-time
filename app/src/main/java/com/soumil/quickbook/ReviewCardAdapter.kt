package com.soumil.quickbook

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.soumil.quickbook.models.Review

class ReviewCardAdapter(private val reviewList: List<Review>) :RecyclerView.Adapter<ReviewCardAdapter.ReviewViewHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ReviewViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.review_card, parent, false)
        return ReviewViewHolder(view)
    }

    override fun onBindViewHolder(holder: ReviewViewHolder, position: Int) {
        val review = reviewList[position]
        holder.bind(review)
    }

    override fun getItemCount(): Int {
        return reviewList.size
    }

    class ReviewViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        private val userNameTextView: TextView = itemView.findViewById(R.id.reviewName)
        private val ratingTextView: TextView = itemView.findViewById(R.id.reviewRating)
        private val dateTextView: TextView = itemView.findViewById(R.id.reviewDate)
        private val reviewTextView: TextView = itemView.findViewById(R.id.reviewText)

        fun bind(review: Review) {
            userNameTextView.text = review.userName
            ratingTextView.text = review.rating.toString()
            dateTextView.text = review.date
            reviewTextView.text = review.review
        }
    }
}
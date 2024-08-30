package com.soumil.quickbook.turfBooking

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import com.google.firebase.firestore.FirebaseFirestore
import com.soumil.quickbook.ReviewCardAdapter
import com.soumil.quickbook.databinding.FragmentViewReviewsBinding
import com.soumil.quickbook.models.Review

class ViewReviews : Fragment() {

    private lateinit var binding: FragmentViewReviewsBinding
    private lateinit var reviewAdapter: ReviewCardAdapter
    private val reviewList = mutableListOf<Review>()
    private val db = FirebaseFirestore.getInstance()
    private var turfId: String = ""

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = FragmentViewReviewsBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)

        arguments?.let { bundle ->
            turfId = bundle.getString("turfId") ?: ""
        }

        if (turfId.isNotEmpty()) {
            fetchReviews()
        }

        setupRecyclerView()
    }

    private fun setupRecyclerView() {
        reviewAdapter = ReviewCardAdapter(reviewList)
        binding.reviewList.apply {
            layoutManager = LinearLayoutManager(context)
            adapter = reviewAdapter
        }
    }

    private fun fetchReviews() {
        db.collection("reviews").document(turfId)
            .collection("turfReviews")
            .get()
            .addOnSuccessListener { documents ->
                for (document in documents) {
                    val review = document.toObject(Review::class.java)
                    reviewList.add(review)
                }
                reviewAdapter.notifyDataSetChanged()
            }
            .addOnFailureListener {
                Toast.makeText(requireContext(),"Error fetching reviews!",Toast.LENGTH_SHORT).show()
            }
    }
}

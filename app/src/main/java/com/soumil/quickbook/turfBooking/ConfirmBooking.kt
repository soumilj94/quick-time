package com.soumil.quickbook.turfBooking

import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import com.razorpay.Checkout
import com.razorpay.PaymentResultListener
import com.soumil.quickbook.R
import com.soumil.quickbook.databinding.FragmentConfirmBookingBinding
import org.json.JSONObject

class ConfirmBooking : Fragment(), PaymentResultListener {
    private lateinit var binding: FragmentConfirmBookingBinding
    private var amount: Int = 0

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        binding = FragmentConfirmBookingBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        // Retrieve data from the bundle
        arguments?.let { bundle ->
            val userName = bundle.getString("userName")
            val userPhone = bundle.getString("userPhone")
            val playerCount = bundle.getInt("playerCount")
            val selectedGame = bundle.getString("selectedGame")
            val selectedDate = bundle.getString("selectedDate")
            val selectedTimeSlots = bundle.getStringArrayList("selectedTimeSlots")
            val turfName = bundle.getString("name")
            val turfCity = bundle.getString("city")
            val price = bundle.getInt("price")

            binding.userName.text = userName
            binding.userPhone.text = userPhone
            binding.players.text = playerCount.toString()
            binding.game.text = selectedGame
            binding.date.text = selectedDate

            if (selectedTimeSlots != null) {
                amount = playerCount * price * selectedTimeSlots.size
                binding.amount.text = "₹ $amount"
                binding.slot.text = selectedTimeSlots.joinToString(", ")
            }
        }

        // Initialize Razorpay
        val checkout = Checkout()
        checkout.setKeyID("rzp_test_W1oT8Zcu6E3Jrk") // Replace with your Razorpay Key ID

        // Set on click listener for Pay Now button
        binding.proceed.setOnClickListener {
            startPayment(checkout)
        }
    }

    private fun startPayment(checkout: Checkout) {
        try {
            val activity = requireActivity()
            val options = JSONObject()
            options.put("name", "Turf Booking")
            options.put("description", "Booking Payment")
            options.put("currency", "INR")
            options.put("amount", amount * 100) // amount in paise (1 INR = 100 paise)

            val prefill = JSONObject()
            prefill.put("email", "user@example.com")
            prefill.put("contact", binding.userPhone.text.toString())

            options.put("prefill", prefill)

            checkout.open(activity, options)

        } catch (e: Exception) {
            Toast.makeText(requireActivity(), "Error in payment: " + e.message, Toast.LENGTH_LONG).show()
            e.printStackTrace()
        }
    }

    // Handle Razorpay Payment Success
    override fun onPaymentSuccess(razorpayPaymentID: String?) {
        Toast.makeText(requireContext(), "Payment Successful: $razorpayPaymentID", Toast.LENGTH_LONG).show()

        // Navigate to SuccessfulActivity
        val intent = Intent(requireContext(), SuccessfulActivity::class.java)
        intent.putExtra("paymentId", razorpayPaymentID)
        startActivity(intent)
    }

    // Handle Razorpay Payment Failure
    override fun onPaymentError(code: Int, response: String?) {
        Toast.makeText(requireContext(), "Payment Failed: $response", Toast.LENGTH_LONG).show()
    }
}

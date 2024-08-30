package com.soumil.quickbook.turfBooking

import android.content.Intent
import android.os.Bundle
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.util.rangeTo
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase
import com.razorpay.Checkout
import com.razorpay.PaymentResultListener
import com.soumil.quickbook.R
import com.soumil.quickbook.databinding.ActivityConfirmBinding
import com.soumil.quickbook.models.Ticket
import com.soumil.quickbook.models.Turf
import com.soumil.quickbook.models.User

import org.json.JSONObject
import java.text.SimpleDateFormat
import java.util.*

class ConfirmActivity : AppCompatActivity(), PaymentResultListener {
    private lateinit var binding: ActivityConfirmBinding


    private var userId: String? = ""
    private var userName: String? = ""
    private var userPhone: String? = ""

    private var playerCount: Int = 0
    private var selectedGame: String? = ""
    private var selectedDate: String? = ""
    private var selectedTimeSlots: ArrayList<String>? = null

    private var managerId: String? = ""
    private var turfId: String? = ""
    private var turfName: String? = ""
    private var turfCity: String? = ""
    private var price: Int = 0
    private var amount: Int = 0

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding = ActivityConfirmBinding.inflate(layoutInflater)
        setContentView(binding.root)

        // Retrieve data from the intent
        userId = intent.getStringExtra("user_id")
        userName = intent.getStringExtra("userName")
        userPhone = intent.getStringExtra("userPhone")
        playerCount = intent.getIntExtra("playerCount", 0)
        selectedGame = intent.getStringExtra("selectedGame")
        selectedDate = intent.getStringExtra("selectedDate")
        selectedTimeSlots = intent.getStringArrayListExtra("selectedTimeSlots")
        managerId = intent.getStringExtra("manager_id")
        turfId = intent.getStringExtra("turf_id")
        turfName = intent.getStringExtra("name")
        turfCity = intent.getStringExtra("city")
        price = intent.getIntExtra("price", 0)

        // Set data to views
        binding.userName.text = userName
        binding.userPhone.text = userPhone
        binding.players.text = playerCount.toString()
        binding.game.text = selectedGame
        binding.date.text = selectedDate
        if (selectedTimeSlots != null) {
            amount = playerCount * price * selectedTimeSlots!!.size
            binding.amount.text = "₹ $amount"
            binding.slot.text = selectedTimeSlots!!.joinToString(", ")
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
            val options = JSONObject().apply {
                put("name", "Turf Booking")
                put("description", "Booking Payment")
                put("currency", "INR")
                put("amount", amount * 100) // amount in paise (1 INR = 100 paise)

                val prefill = JSONObject().apply {
                    put("email", "user@example.com")
                    put("contact", binding.userPhone.text.toString())
                }
                put("prefill", prefill)
            }
            checkout.open(this, options)

        } catch (e: Exception) {
            Toast.makeText(this, "Error in payment: " + e.message, Toast.LENGTH_LONG).show()
            e.printStackTrace()
        }
    }

    // Handle Razorpay Payment Success
    override fun onPaymentSuccess(razorpayPaymentID: String?) {
       /* Toast.makeText(this, "Payment Successful: $razorpayPaymentID", Toast.LENGTH_LONG).show()

        // Get current date
        val dateFormat = SimpleDateFormat("dd MMMM yyyy", Locale.getDefault())
        val paymentDate = dateFormat.format(Date())

        // Navigate to SuccessfulActivity
        val intent = Intent(this, SuccessfulActivity::class.java).apply {
            putExtra("userName", binding.userName.text.toString())
            putExtra("userPhone", binding.userPhone.text.toString())
            putExtra("playerCount", binding.players.text.toString().toInt())
            putExtra("selectedGame", binding.game.text.toString())
            putExtra("selectedDate", binding.date.text.toString())
            putExtra("selectedTimeSlots", intent.getStringArrayListExtra("selectedTimeSlots"))
            putExtra("turfName", intent.getStringExtra("name"))
            putExtra("turfCity", intent.getStringExtra("city"))
            putExtra("price", binding.amount.text.toString().replace("₹ ", "").toInt())
            putExtra("paymentId", razorpayPaymentID)
            putExtra("paymentDate", paymentDate)
        }*/

        val ticket = Ticket(
            turfId = turfId ?: "",
            managerId = managerId?:"",
            userId = userId?:"",
            userName = userName?:"",
            turfName = turfName?:"",
            game = selectedGame?:"",
            players = playerCount.toString(),
            slots = selectedTimeSlots!!.joinToString(", "),
            date = selectedDate?:"",
            amount = amount.toString(),
            razorpayPaymentID = razorpayPaymentID?:"",
            timestamp = System.currentTimeMillis()
        )

        saveTicketDetails(ticket);

       // startActivity(intent)
        //finish() // Close the ConfirmActivity
    }

    // Handle Razorpay Payment Failure
    override fun onPaymentError(code: Int, response: String?) {
        Toast.makeText(this, "Payment Failed: $response", Toast.LENGTH_LONG).show()
    }

    private fun saveTicketDetails(ticket: Ticket) {
        val db = FirebaseFirestore.getInstance()
        val razorpayPaymentID = ticket.razorpayPaymentID

        if (razorpayPaymentID.isNotEmpty()) {
            val bookRef = db.collection("bookings").document(razorpayPaymentID)
            bookRef.set(ticket)
                .addOnSuccessListener {
                    // Handle success, maybe show a confirmation message
                    Toast.makeText(this, "Booking saved successfully!", Toast.LENGTH_SHORT).show()
                    val turfRef = db.collection("turfs").document(ticket.turfId)
                    turfRef.get().addOnSuccessListener { document ->
                        if (document.exists()) {
                            val turfData = document.toObject(Turf::class.java)
                            if (turfData != null) {
                                turfData.bookings += razorpayPaymentID
                                turfRef.set(turfData)
                                    .addOnSuccessListener {
                                        Toast.makeText(this, "Saved to turf", Toast.LENGTH_SHORT)
                                            .show()
                                    }
                                    .addOnFailureListener { e ->
                                        Toast.makeText(
                                            this,
                                            "Error: ${e.message}",
                                            Toast.LENGTH_SHORT
                                        ).show()
                                    }
                            }

                        }
                    }
                    Toast.makeText(this, "Booking saved successfully!", Toast.LENGTH_SHORT).show()
                    val userRef = db.collection("users").document(ticket.userId)
                    userRef.get().addOnSuccessListener { document ->
                        if (document.exists()) {
                            val userData = document.toObject(User::class.java)
                            if (userData != null) {
                                userData.bookingHistory += razorpayPaymentID
                                userRef.set(userData)
                                    .addOnSuccessListener {
                                        Toast.makeText(this, "Saved to user", Toast.LENGTH_SHORT)
                                            .show()
                                    }
                                    .addOnFailureListener { e ->
                                        Toast.makeText(
                                            this,
                                            "Error: ${e.message}",
                                            Toast.LENGTH_SHORT
                                        ).show()
                                    }
                            }
                        }
                    }
                }
                .addOnFailureListener { e ->
                    // Handle failure, maybe show an error message
                    Toast.makeText(this, "Error saving booking: ${e.message}", Toast.LENGTH_SHORT)
                        .show()
                }
        } else {
            Toast.makeText(this, "Invalid Razorpay Payment ID", Toast.LENGTH_SHORT).show()
        }

        val intent = Intent(this, SuccessfulActivity::class.java)

        startActivity(intent)


    }
}

package com.soumil.quickbook.registration.user

import android.annotation.SuppressLint
import android.content.Context
import android.content.SharedPreferences
import com.soumil.quickbook.R
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Patterns
import android.widget.ArrayAdapter
import android.widget.Toast
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase
import com.soumil.quickbook.databinding.ActivitySignupUserBinding
import com.soumil.quickbook.models.User


class SignupUserActivity : AppCompatActivity() {
    private lateinit var binding: ActivitySignupUserBinding
    private lateinit var auth: FirebaseAuth
    private lateinit var sharedPreferences: SharedPreferences


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivitySignupUserBinding.inflate(layoutInflater)
        setContentView(binding.root)

        auth = FirebaseAuth.getInstance()
        sharedPreferences = getSharedPreferences("user_prefs", Context.MODE_PRIVATE)

        binding.backBtn.setOnClickListener { finish() }

        binding.signinLink.setOnClickListener { finish() }

        fullNameFocusListener()
        emailFocusListener()
        passwordFocusListener()
        phoneFocusListener()
        setupCityAutoComplete()

        binding.signinBtn.setOnClickListener { signInBtn() }
    }

    private fun fullNameFocusListener() {
        binding.fullNameTextInput.setOnFocusChangeListener { _, focused ->
            if (!focused) {
                binding.fullNameField.helperText = checkName()
            }
        }
    }

    private fun checkName(): String? {
        val userName = binding.fullNameTextInput.text.toString().trim()
        if (userName.length < 5) {
            return "Please enter your full name!"
        }
        return null
    }

    private fun emailFocusListener() {
        binding.emailTextInput.setOnFocusChangeListener { _, focused ->
            if (!focused) {
                binding.emailField.helperText = validateEmail()
            }
        }
    }

    private fun validateEmail(): String? {
        val emailText = binding.emailTextInput.text.toString()
        if (!Patterns.EMAIL_ADDRESS.matcher(emailText).matches()) {
            return "Invalid Email Address!"
        }
        return null
    }

    private fun passwordFocusListener() {
        binding.passwordTextInput.setOnFocusChangeListener { _, focused ->
            if (!focused) {
                binding.passwordField.helperText = validatePassword()
            }
        }
    }

    private fun validatePassword(): String? {
        val passwordText = binding.passwordTextInput.text.toString()
        if (passwordText.length < 8) {
            return "Minimum 8 characters required"
        }
        return null
    }

    private fun phoneFocusListener() {
        binding.phoneTextInput.setOnFocusChangeListener { _, focused ->
            if (!focused) {
                binding.phoneField.helperText = validatePhoneNumber()
            }
        }
    }

    private fun validatePhoneNumber(): String? {
        val phoneText = binding.phoneTextInput.text.toString()
        val phonePattern = "^[6-9]\\d{9}\$"  // Regex for Indian phone numbers starting with 6-9 and 10 digits long
        if (!phoneText.matches(phonePattern.toRegex())) {
            return "Invalid Phone Number!"
        }
        return null
    }

    private fun setupCityAutoComplete() {
        val cities = resources.getStringArray(R.array.indian_cities)
        val adapter = ArrayAdapter(this, android.R.layout.simple_dropdown_item_1line, cities)
        binding.cityTextInput.setAdapter(adapter)
        binding.cityTextInput.threshold = 1 // Start suggesting from the first character
    }

    private fun validateCity(selectedCity: String): String? {
        val validCities = resources.getStringArray(R.array.indian_cities)
        return if (selectedCity in validCities) null else "Please select a valid city!"
    }

    @SuppressLint("SetTextI18n")
    private fun signInBtn() {
        val userName = binding.fullNameTextInput.text.toString()
        val validEmail = binding.emailTextInput.text.toString()
        val validPassword = binding.passwordTextInput.text.toString()
        val selectedCity = binding.cityTextInput.text.toString()


        if (checkName() != null || validateEmail() != null || validatePassword() != null || validatePhoneNumber() != null) {
            binding.fullNameField.helperText = checkName()
            binding.emailField.helperText = validateEmail()
            binding.passwordField.helperText = validatePassword()
            binding.phoneField.helperText = validatePhoneNumber()
            binding.cityField.helperText = validateCity(selectedCity)

            Toast.makeText(this, "Please fill out all fields correctly", Toast.LENGTH_SHORT).show()
            return
        } else {
            auth.createUserWithEmailAndPassword(validEmail, validPassword).addOnCompleteListener(this) { task ->
                if (task.isSuccessful) {
                    binding.signinBtn.text = "Processing"
                    val user = auth.currentUser
                    user?.let {
                        val fullName = binding.fullNameTextInput.text.toString()
                        val email = validEmail
                        val phoneNumber = binding.phoneTextInput.text.toString()
                        val city = binding.cityTextInput.text.toString()
                        val uid = it.uid

                        val db = Firebase.firestore
                        val userDocRef = db.collection("users").document(uid)
                        val newUser = User(fullName, email, phoneNumber, city, emptyList(),-1L,"",-1,"user")
                        userDocRef.set(newUser)
                            .addOnSuccessListener {
                                saveUserToSharedPreferences("user", fullName, email, phoneNumber, city)
                                Toast.makeText(this, "Registration Successful!", Toast.LENGTH_SHORT).show()
                                finish()
                            }
                            .addOnFailureListener { e ->
                                Toast.makeText(this, "Failed to store user data: ${e.message}", Toast.LENGTH_SHORT).show()
                            }
                    }
                }  else {
                    Toast.makeText(this, "Registration Failed! ${task.exception?.message}", Toast.LENGTH_SHORT).show()
                }

            }
        }

    }

    private fun saveUserToSharedPreferences(role: String, fullName: String, email: String, phoneNumber: String, city: String) {
        val editor = sharedPreferences.edit()
        editor.putString("role", role)
        editor.putString("fullName", fullName)
        editor.putString("email", email)
        editor.putString("phoneNumber", phoneNumber)
        editor.putString("city", city)
        editor.apply()
    }
}

package com.soumil.quickbook.registration.user
import android.annotation.SuppressLint
import android.content.Context
import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.util.Patterns
import android.widget.Toast
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.firestore.FirebaseFirestore
import com.soumil.quickbook.MainActivity
import com.soumil.quickbook.databinding.ActivitySigninUserBinding
import android.content.SharedPreferences
import android.view.LayoutInflater
import androidx.appcompat.app.AlertDialog
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.google.android.material.textfield.TextInputEditText
import com.soumil.quickbook.R

class SigninUserActivity : AppCompatActivity() {
    private lateinit var binding: ActivitySigninUserBinding
    private lateinit var auth: FirebaseAuth
    private lateinit var db: FirebaseFirestore
    private lateinit var sharedPreferences: SharedPreferences

    override fun onStart() {
        super.onStart()
        val currentUser: FirebaseUser? = auth.currentUser
        if (currentUser != null) {
            startActivity(Intent(this, MainActivity::class.java))
            finishAffinity()
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivitySigninUserBinding.inflate(layoutInflater)
        setContentView(binding.root)

        auth = FirebaseAuth.getInstance()
        db = FirebaseFirestore.getInstance()
        sharedPreferences = getSharedPreferences("user_prefs", Context.MODE_PRIVATE)

        binding.backBtn.setOnClickListener {
            finish()
        }

        binding.forgotPassLink.setOnClickListener {
            passwordReset()
        }

        binding.createAcc.setOnClickListener {
            val i = Intent(this, SignupUserActivity::class.java)
            startActivity(i)
        }

        emailFocusListener()
        passwordFocusListener()

        binding.signinBtn.setOnClickListener { signInBtn() }
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

    @SuppressLint("SetTextI18n")
    private fun signInBtn() {
        val validEmail = binding.emailTextInput.text.toString()
        val validPassword = binding.passwordTextInput.text.toString()

        if (validEmail.isEmpty() || validPassword.isEmpty()) {
            Toast.makeText(this, "Please enter valid details!", Toast.LENGTH_SHORT).show()
        } else {
            auth.signInWithEmailAndPassword(validEmail, validPassword)
                .addOnCompleteListener { task ->
                    if (task.isSuccessful) {
                        binding.signinBtn.text = "Processing"
                        val currentUser = auth.currentUser
                        val uid = currentUser?.uid
                        if (uid != null) {
                            db.collection("users").document(uid)
                                .addSnapshotListener { document, error ->
                                    if (error != null) {
                                        Log.e("Firestore Error", error.message.toString())
                                        return@addSnapshotListener
                                    }
                                    if (document != null && document.exists()) {
                                        val role = document.getString("role")
                                        val fullName = document.getString("name")
                                        val email = document.getString("email")
                                        val phone = document.getString("phone")
                                        val city = document.getString("city")
                                        when (role) {
                                            "user" -> {
                                                if (fullName != null && email != null && phone != null && city != null) {
                                                    saveUserToSharedPreferences(
                                                        "user",
                                                        fullName,
                                                        email,
                                                        phone,
                                                        city
                                                    )
                                                }

                                                Toast.makeText(
                                                    this,
                                                    "Login Successful!",
                                                    Toast.LENGTH_SHORT
                                                ).show()
                                                startActivity(
                                                    Intent(
                                                        this,
                                                        MainActivity::class.java
                                                    )
                                                )
                                                finishAffinity()
                                            }

                                            else -> {
                                                Toast.makeText(
                                                    this,
                                                    "Invalid Credentials!",
                                                    Toast.LENGTH_SHORT
                                                ).show()
                                                binding.signinBtn.text = "Sign In"
                                            }

                                        }
                                    } else {
                                        Toast.makeText(
                                            this,
                                            "Invalid Credentials!",
                                            Toast.LENGTH_SHORT
                                        ).show()
                                        binding.signinBtn.text = "Sign In"
                                    }
                                }
                        }
                    } else {
                        Toast.makeText(
                            this,
                            "Something went wrong!${task.exception?.message}",
                            Toast.LENGTH_SHORT
                        ).show()
                    }
                }
        }
    }

    private fun saveUserToSharedPreferences(
        role: String,
        fullName: String,
        email: String,
        phoneNumber: String,
        city: String
    ) {
        val editor = sharedPreferences.edit()
        editor.putString("role", role)
        editor.putString("fullName", fullName)
        editor.putString("email", email)
        editor.putString("phoneNumber", phoneNumber)
        editor.putString("city", city)
        editor.apply()
    }

    private fun passwordReset() {
        val passDialog = LayoutInflater.from(this).inflate(R.layout.forgot_password_dialog_box, null)

        val newEmail = passDialog.findViewById<TextInputEditText>(R.id.inputEmail)

        val alertDialog = MaterialAlertDialogBuilder(this)
            .setView(passDialog)
            .setTitle("Forgot Password")
            .setPositiveButton("Send", null)
            .setNegativeButton("Cancel") { dialog, _ ->
                dialog.dismiss()
            }
            .create()

        alertDialog.setOnShowListener {
            alertDialog.getButton(AlertDialog.BUTTON_POSITIVE).setOnClickListener {
                val input = newEmail.text.toString()

                if (input.isEmpty()) {
                    Toast.makeText(this, "Fill in email address!", Toast.LENGTH_SHORT).show()
                } else if (!Patterns.EMAIL_ADDRESS.matcher(input).matches()) {
                    Toast.makeText(this, "Invalid email", Toast.LENGTH_SHORT).show()
                } else {
                    auth.sendPasswordResetEmail(input)
                        .addOnCompleteListener { task ->
                            if (task.isSuccessful) {
                                Toast.makeText(
                                    this,
                                    "Password reset email sent! Check your inbox.",
                                    Toast.LENGTH_LONG
                                ).show()
                                alertDialog.dismiss()
                            } else {
                                Toast.makeText(
                                    this,
                                    "Error: ${task.exception?.message}",
                                    Toast.LENGTH_SHORT
                                ).show()
                            }
                        }
                }
            }
        }
        alertDialog.window?.setBackgroundDrawableResource(R.drawable.alert_dialog_background)
        alertDialog.show()
    }

}
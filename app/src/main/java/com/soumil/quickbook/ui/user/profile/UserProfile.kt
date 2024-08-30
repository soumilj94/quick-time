package com.soumil.quickbook.ui.user.profile

import android.content.Intent
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ArrayAdapter
import android.widget.AutoCompleteTextView
import android.widget.Button
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.google.android.material.textfield.TextInputEditText
import com.soumil.quickbook.R
import com.soumil.quickbook.databinding.FragmentUserProfileBinding
import com.soumil.quickbook.onboard.Onboard

class UserProfile : Fragment() {

    private lateinit var viewModel: UserProfileViewModel
    private lateinit var binding: FragmentUserProfileBinding

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = FragmentUserProfileBinding.inflate(inflater, container, false)
        viewModel = UserProfileViewModel()

        viewModel.userName.observe(viewLifecycleOwner){name ->
            binding.userName.text = name
        }
        viewModel.getUserName()

        binding.editProfileBtn.setOnClickListener {
            editProfileBtn()
        }

        binding.logOutBtn.setOnClickListener {
            logOutBtn()
        }

        binding.changePassBtn.setOnClickListener {
            changePassBtn()
        }

        binding.helpBtn.setOnClickListener {
            aboutUsBtn()
        }

        return binding.root
    }

    private fun editProfileBtn() {
        val editProfileDialog = LayoutInflater.from(context).inflate(R.layout.edit_profile_details_dialog, null)

        val editText = editProfileDialog.findViewById<TextInputEditText>(R.id.newUserName)
        editText.setText(binding.userName.text)

        val editPhone = editProfileDialog.findViewById<TextInputEditText>(R.id.newPhoneNumber)
        viewModel.userPhone.observe(viewLifecycleOwner){ phone ->
            editPhone.setText(phone)
        }
        viewModel.getUserPhone()

        val editCity = editProfileDialog.findViewById<AutoCompleteTextView>(R.id.newCityName)
        viewModel.userCity.observe(viewLifecycleOwner){ city ->
            editCity.setText(city)
        }
        viewModel.getUserCity()

        val cities = resources.getStringArray(R.array.indian_cities)
        val adapter = ArrayAdapter(requireContext(), android.R.layout.simple_dropdown_item_1line, cities)
        editCity.setAdapter(adapter)

/*      verification of selected city from dropdown menu is not done.
        user can pass any string and it'll be accepted as a city name.
        need to add a check for that.
 */

        val alertDialog = MaterialAlertDialogBuilder(requireContext())
            .setView(editProfileDialog)
            .setTitle("Edit Profile Details")
            .setPositiveButton("Save"){dialog, _ ->
                val newName = editText.text.toString()
                val newPhone = editPhone.text.toString()
                val newCity = editCity.text.toString()

                val userId = viewModel.auth.currentUser?.uid
                val firestore = viewModel.db

                userId?.let {
                    firestore.collection("users").document(it)
                        .update("name", newName)
                        .addOnSuccessListener { viewModel.getUserName() }

                    firestore.collection("users").document(it)
                        .update("phone", newPhone)
                        .addOnSuccessListener { viewModel.getUserPhone() }

                    firestore.collection("users").document(it)
                        .update("city", newCity)
                        .addOnSuccessListener {
                            viewModel.getUserCity()
                        }
                    Toast.makeText(requireContext(), "Profile Settings updated!", Toast.LENGTH_SHORT).show()
                }
                dialog.dismiss()
            }
            .setNegativeButton("Cancel"){dialog, _ ->
                dialog.dismiss()
            }
            .create()
        alertDialog.setOnShowListener {
//            need more customization here using custom drawable resource file
            alertDialog.window?.setBackgroundDrawableResource(android.R.color.white)
        }
        alertDialog.show()
    }


    private fun logOutBtn() {
        val confirmDialog = LayoutInflater.from(context).inflate(R.layout.confirm_dialog_box, null)
        val noBtn = confirmDialog.findViewById<Button>(R.id.noBtn)
        val yesBtn = confirmDialog.findViewById<Button>(R.id.yesBtn)

        val alertDialog = AlertDialog.Builder(requireContext())
            .setView(confirmDialog)
            .create()

        alertDialog.window?.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))

        noBtn.setOnClickListener {
            alertDialog.dismiss()
        }

        yesBtn.setOnClickListener {
            viewModel.auth.signOut()
            val i = Intent(requireContext(), Onboard::class.java)
            i.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
            startActivity(i)
            activity?.finishAffinity()
            alertDialog.dismiss()
        }

        alertDialog.show()
    }

    private fun changePassBtn() {
        val passDialog = LayoutInflater.from(context).inflate(R.layout.change_password_dialog_box, null)

        val newPass = passDialog.findViewById<TextInputEditText>(R.id.newPassword)
        val confirmPass = passDialog.findViewById<TextInputEditText>(R.id.retypeNewPassword)

        val alertDialog = MaterialAlertDialogBuilder(requireContext())
            .setView(passDialog)
            .setTitle("Change Account Password")
            .setPositiveButton("Save", null)
            .setNegativeButton("Cancel") { dialog, _ ->
                dialog.dismiss()
            }
            .create()

        alertDialog.setOnShowListener {
            alertDialog.getButton(AlertDialog.BUTTON_POSITIVE).setOnClickListener {
                val newPassword = newPass.text.toString()
                val confirmPassword = confirmPass.text.toString()

                if (newPassword.isEmpty() || confirmPassword.isEmpty()) {
                    Toast.makeText(requireContext(), "Fill in both fields!", Toast.LENGTH_SHORT).show()
                } else if (newPassword.length < 8 || confirmPassword.length < 8) {
                    Toast.makeText(requireContext(), "Enter at least 8 characters", Toast.LENGTH_SHORT).show()
                } else if (newPassword != confirmPassword) {
                    Toast.makeText(requireContext(), "Passwords do not match", Toast.LENGTH_SHORT).show()
                } else {
                    val user = viewModel.auth.currentUser
                    user?.updatePassword(newPassword)?.addOnCompleteListener { task ->
                        if (task.isSuccessful) {
                            Toast.makeText(requireContext(), "Password updated. Please log in again!", Toast.LENGTH_SHORT).show()
                            viewModel.auth.signOut()
                            val i = Intent(requireContext(), Onboard::class.java)
                            i.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
                            startActivity(i)
                            activity?.finishAffinity()
                            alertDialog.dismiss()
                        } else {
                            Toast.makeText(requireContext(), "Error updating password", Toast.LENGTH_SHORT).show()
                        }
                    }
                }
            }
        }

        alertDialog.window?.setBackgroundDrawableResource(android.R.color.white)
        alertDialog.show()
    }


    private fun aboutUsBtn() {
        val helpDialog = LayoutInflater.from(context).inflate(R.layout.about_us_dialog_box, null)

        val alertDialog = AlertDialog.Builder(requireContext())
            .setView(helpDialog)
            .create()

        alertDialog.window?.setBackgroundDrawable(ColorDrawable(Color.WHITE))
        alertDialog.show()
    }

}
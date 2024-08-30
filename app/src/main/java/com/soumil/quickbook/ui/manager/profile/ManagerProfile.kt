package com.soumil.quickbook.ui.manager.profile

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
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.chip.Chip
import com.google.android.material.chip.ChipGroup
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.google.android.material.textfield.TextInputEditText
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.soumil.quickbook.R
import com.soumil.quickbook.ReviewCardAdapter
import com.soumil.quickbook.TicketAdapter
import com.soumil.quickbook.databinding.FragmentManagerProfileBinding
import com.soumil.quickbook.models.Review
import com.soumil.quickbook.models.Ticket
import com.soumil.quickbook.onboard.Onboard

class ManagerProfile : Fragment() {

    private lateinit var viewModel: ManagerProfileViewModel
    private lateinit var binding: FragmentManagerProfileBinding


    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {

        binding = FragmentManagerProfileBinding.inflate(inflater, container, false)
        viewModel = ManagerProfileViewModel()

        viewModel.managerName.observe(viewLifecycleOwner){name ->
            binding.userName.text = name
        }
        viewModel.getManagerName()

        binding.editProfileBtn.setOnClickListener {
            editProfileBtn()
        }

        binding.logOutBtn.setOnClickListener {
            logOutBtn()
        }

        binding.changePassBtn.setOnClickListener {
            changePassBtn()
        }

        binding.viewReview.setOnClickListener {
            loadReviews()
        }

        binding.helpBtn.setOnClickListener {
            aboutUsBtn()
        }


        return binding.root
    }

    private fun editProfileBtn(){
        val editProfileDialog = LayoutInflater.from(context).inflate(R.layout.edit_profile_details_dialog, null)

        val editText = editProfileDialog.findViewById<TextInputEditText>(R.id.newUserName)
        editText.setText(binding.userName.text)

        val editPhone = editProfileDialog.findViewById<TextInputEditText>(R.id.newPhoneNumber)
        viewModel.managerPhone.observe(viewLifecycleOwner){ phone ->
            editPhone.setText(phone)
        }
        viewModel.getManagerPhone()

        val editCity = editProfileDialog.findViewById<AutoCompleteTextView>(R.id.newCityName)
        viewModel.managerCity.observe(viewLifecycleOwner){ city ->
            editCity.setText(city)
        }

        val cities = resources.getStringArray(R.array.indian_cities)
        val adapter = ArrayAdapter(requireContext(), android.R.layout.simple_dropdown_item_1line, cities)
        editCity.setAdapter(adapter)

/*      verification of selected city from dropdown menu is not done.
        manager can pass any string and it'll be accepted as a city name.
        need to add a check for that.
 */

        viewModel.getManagerCity()

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

                    firestore.collection("managers").document(it)
                        .update("name", newName)
                        .addOnSuccessListener { viewModel.getManagerName() }

                    firestore.collection("managers").document(it)
                        .update("phone", newPhone)
                        .addOnSuccessListener { viewModel.getManagerPhone() }

                    firestore.collection("managers").document(it)
                        .update("city", newCity)
                        .addOnSuccessListener {
                            viewModel.getManagerCity()
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
            alertDialog.window?.setBackgroundDrawableResource(R.drawable.alert_dialog_background)

            val positiveButton = alertDialog.getButton(AlertDialog.BUTTON_POSITIVE)
            positiveButton.setTextColor(ContextCompat.getColor(requireContext(), R.color.primary_blue))

            val negativeButton = alertDialog.getButton(AlertDialog.BUTTON_NEGATIVE)
            negativeButton.setTextColor(ContextCompat.getColor(requireContext(), R.color.primary_blue))
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
            alertDialog.window?.setBackgroundDrawableResource(R.drawable.alert_dialog_background)

            val positiveButton = alertDialog.getButton(AlertDialog.BUTTON_POSITIVE)
            positiveButton.setTextColor(ContextCompat.getColor(requireContext(), R.color.primary_blue))

            val negativeButton = alertDialog.getButton(AlertDialog.BUTTON_NEGATIVE)
            negativeButton.setTextColor(ContextCompat.getColor(requireContext(), R.color.primary_blue))

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

    private fun loadReviews() {
        val reviewAdapter: ReviewCardAdapter
        val reviewList = mutableListOf<Review>()
        val allReviews = mutableListOf<Review>()
        val db = FirebaseFirestore.getInstance()
        val auth = FirebaseAuth.getInstance()
        val managerId = auth.currentUser?.uid ?: return

        // Inflate the dialog layout
        val helpDialog = LayoutInflater.from(context).inflate(R.layout.manager_view_reviews, null)
        val recyclerView = helpDialog.findViewById<RecyclerView>(R.id.reviewList) // Ensure this matches your dialog layout
        val chipGroup = helpDialog.findViewById<ChipGroup>(R.id.reviewChipGroup) // Ensure this matches your dialog layout

        reviewAdapter = ReviewCardAdapter(reviewList)
        recyclerView.layoutManager = LinearLayoutManager(requireContext())
        recyclerView.adapter = reviewAdapter

        // Show the dialog
        val alertDialog = AlertDialog.Builder(requireContext())
            .setView(helpDialog)
            .create()


        alertDialog.window?.setBackgroundDrawable(ColorDrawable(Color.WHITE))
        alertDialog.show()

        // Fetch manager data to get turfs_owned
        db.collection("managers").document(managerId).get()
            .addOnSuccessListener { document ->
                if (document != null && document.exists()) {
                    val turfsOwned = document.get("turfs_owned") as? List<String> ?: return@addOnSuccessListener
                    fetchTurfNamesAndSetupChips(turfsOwned, chipGroup, reviewList, allReviews, reviewAdapter)
                    fetchAllReviews(turfsOwned, allReviews, reviewList, reviewAdapter)
                }
            }
            .addOnFailureListener { exception ->
                // Handle the error
            }
    }

    // Setup the chips dynamically for each turf and the "All" chip
    private fun fetchTurfNamesAndSetupChips(
        turfsOwned: List<String>,
        chipGroup: ChipGroup,
        reviewList: MutableList<Review>,
        allReviews: MutableList<Review>,
        reviewAdapter: ReviewCardAdapter
    ) {
        val db = FirebaseFirestore.getInstance()
        val turfNamesMap = mutableMapOf<String, String>()

        // Fetch the name for each turf UID
        turfsOwned.forEach { turfUid ->
            db.collection("turfs").document(turfUid).get()
                .addOnSuccessListener { document ->
                    if (document != null && document.exists()) {
                        val turfName = document.getString("name") ?: turfUid
                        turfNamesMap[turfUid] = turfName

                        // Set up the chip after fetching all names
                        if (turfNamesMap.size == turfsOwned.size) {
                            setupChipsWithNames(chipGroup, turfNamesMap, reviewList, allReviews, reviewAdapter)
                        }
                    }
                }
                .addOnFailureListener {
                    // Handle any error
                }
        }
    }

    private fun setupChipsWithNames(
        chipGroup: ChipGroup,
        turfNamesMap: Map<String, String>,
        reviewList: MutableList<Review>,
        allReviews: MutableList<Review>,
        reviewAdapter: ReviewCardAdapter
    ) {
        // Add "All" chip
        val allChip = Chip(chipGroup.context).apply {
            text = "All"
            isCheckable = true
            setOnClickListener {
                reviewList.clear()
                reviewList.addAll(allReviews)
                reviewAdapter.notifyDataSetChanged()
            }
        }
        chipGroup.addView(allChip)

        // Add chips for each turf
        turfNamesMap.forEach { (turfUid, turfName) ->
            val chip = Chip(chipGroup.context).apply {
                text = turfName
                isCheckable = true
                setOnClickListener {
                    reviewList.clear()
                    reviewList.addAll(allReviews.filter { it.turfId == turfUid })
                    reviewAdapter.notifyDataSetChanged()
                }
            }
            chipGroup.addView(chip)
        }
    }

    // Fetch reviews for all turfs owned by the manager
    private fun fetchAllReviews(
        turfsOwned: List<String>,
        allReviews: MutableList<Review>,
        reviewList: MutableList<Review>,
        reviewAdapter: ReviewCardAdapter
    ) {
        val db = FirebaseFirestore.getInstance()

        for (turfId in turfsOwned) {
            db.collection("reviews").document(turfId).collection("turfReviews").get()
                .addOnSuccessListener { result ->
                    for (document in result) {
                        val review = document.toObject(Review::class.java)
                        allReviews.add(review)
                    }
                    // By default, show all reviews
                    reviewList.clear()
                    reviewList.addAll(allReviews)
                    reviewAdapter.notifyDataSetChanged()
                }
                .addOnFailureListener { exception ->
                    // Handle the error
                }
        }
    }


}
package com.soumil.quickbook.ui.manager.turfs

import android.app.TimePickerDialog
import android.view.LayoutInflater
import android.view.ViewGroup
import android.widget.Button
import android.widget.CheckBox
import android.widget.GridLayout
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import androidx.databinding.BindingAdapter
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.bumptech.glide.load.resource.bitmap.DownsampleStrategy
import com.bumptech.glide.request.RequestOptions
import com.google.android.material.chip.Chip
import com.google.android.material.chip.ChipGroup
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.google.android.material.textfield.TextInputEditText
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.soumil.quickbook.R
import com.soumil.quickbook.databinding.ListItemTurfBinding
import com.soumil.quickbook.models.Turf
import java.util.Calendar
import java.util.Locale

class TurfAdapter : ListAdapter<Turf, TurfAdapter.TurfViewHolder>(TurfDiffCallback()) {
    // Turf Car Recycler View Adapter for handling all task related to
    // Turf Data fetching, editing & updating details of the Turf.

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): TurfViewHolder {
        val binding = ListItemTurfBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return TurfViewHolder(binding)
    }

    override fun onBindViewHolder(holder: TurfViewHolder, position: Int) {
        val turf = getItem(position)
        holder.bind(turf)
    }

    class TurfViewHolder(private val binding: ListItemTurfBinding) : RecyclerView.ViewHolder(binding.root) {

        private lateinit var nameEditText: TextInputEditText
        private lateinit var priceEditText: TextInputEditText
        private lateinit var openTimeEditText: TextView
        private lateinit var closeTimeEditText: TextView
        private val turfImageView: ImageView = itemView.findViewById(R.id.turfImageView)

        private val editDetailsBtn: Button = itemView.findViewById(R.id.editDetailsBtn)

        private val firestore = FirebaseFirestore.getInstance()

        init {
            editDetailsBtn.setOnClickListener {
                editDetails()
            }
        }

        private fun editDetails() {
            val turfId = binding.turf?.turf_uid ?: return
            val turfRef = firestore.collection("turfs").document(turfId)

            turfRef.get()
                .addOnSuccessListener { document ->
                    if (document != null && document.exists()) {
                        val name = document.getString("name") ?: ""
                        val price = document.getLong("price")?.toInt() ?: 0
                        val openTime = document.getString("open_time") ?: ""
                        val closeTime = document.getString("close_time") ?: ""
                        val weekdays = document.get("weekdays") as? List<String> ?: emptyList()
                        val sportsOffered = document.get("games") as? List<String> ?: emptyList()

                        val dialogView = LayoutInflater.from(itemView.context).inflate(R.layout.edit_turf_details_dialog, null)
                        val confirmDeleteDialog = LayoutInflater.from(itemView.context).inflate(R.layout.confirm_dialog_box, null)

                        dialogView.parent?.let { parent ->
                            (parent as ViewGroup).removeView(dialogView)
                        }

                        confirmDeleteDialog.parent?.let { parent ->
                            (parent as ViewGroup).removeView(confirmDeleteDialog)
                        }

                        nameEditText = dialogView.findViewById(R.id.newTurfName)
                        priceEditText = dialogView.findViewById(R.id.newTurfPrice)
                        openTimeEditText = dialogView.findViewById(R.id.openTimeSet)
                        closeTimeEditText = dialogView.findViewById(R.id.closeTimeSet)

                        val daysCheckBoxLayout = dialogView.findViewById<GridLayout>(R.id.daysCheckboxLayout)
                        val sportOfferedBoxLayout = dialogView.findViewById<GridLayout>(R.id.sportCheckboxLayout)

                        val mondayCheckBox = daysCheckBoxLayout.findViewById<CheckBox>(R.id.monday)
                        val tuesdayCheckBox = daysCheckBoxLayout.findViewById<CheckBox>(R.id.tuesday)
                        val wednesdayCheckBox = daysCheckBoxLayout.findViewById<CheckBox>(R.id.wednesday)
                        val thursdayCheckBox = daysCheckBoxLayout.findViewById<CheckBox>(R.id.thursday)
                        val fridayCheckBox = daysCheckBoxLayout.findViewById<CheckBox>(R.id.friday)
                        val saturdayCheckBox = daysCheckBoxLayout.findViewById<CheckBox>(R.id.saturday)
                        val sundayCheckBox = daysCheckBoxLayout.findViewById<CheckBox>(R.id.sunday)

                        mondayCheckBox.isChecked = weekdays.contains("Monday")
                        tuesdayCheckBox.isChecked = weekdays.contains("Tuesday")
                        wednesdayCheckBox.isChecked = weekdays.contains("Wednesday")
                        thursdayCheckBox.isChecked = weekdays.contains("Thursday")
                        fridayCheckBox.isChecked = weekdays.contains("Friday")
                        saturdayCheckBox.isChecked = weekdays.contains("Saturday")
                        sundayCheckBox.isChecked = weekdays.contains("Sunday")

                        val cricket = sportOfferedBoxLayout.findViewById<CheckBox>(R.id.cricket)
                        val football = sportOfferedBoxLayout.findViewById<CheckBox>(R.id.football)
                        val badminton = sportOfferedBoxLayout.findViewById<CheckBox>(R.id.badminton)
                        val basketball = sportOfferedBoxLayout.findViewById<CheckBox>(R.id.basketball)
                        val volleyball = sportOfferedBoxLayout.findViewById<CheckBox>(R.id.volleyball)
                        val swimming = sportOfferedBoxLayout.findViewById<CheckBox>(R.id.swimming)

                        cricket.isChecked = sportsOffered.contains("Cricket")
                        football.isChecked = sportsOffered.contains("Football")
                        badminton.isChecked = sportsOffered.contains("Badminton")
                        basketball.isChecked = sportsOffered.contains("Basketball")
                        volleyball.isChecked = sportsOffered.contains("Volleyball")
                        swimming.isChecked = sportsOffered.contains("Swimming")

                        val updateButton = dialogView.findViewById<Button>(R.id.updateDetailsBtn)
                        val deleteTurfBtn = dialogView.findViewById<Button>(R.id.deleteBtn)

                        nameEditText.setText(name)
                        priceEditText.setText(price.toString())
                        openTimeEditText.text = openTime
                        closeTimeEditText.text = closeTime

                        openTimeEditText.setOnClickListener {
                            showTimePickerDialog(true)
                        }
                        closeTimeEditText.setOnClickListener {
                            showTimePickerDialog(false)
                        }

                        val dialogBox = MaterialAlertDialogBuilder(itemView.context)
                            .setTitle("Edit Details")
                            .setView(dialogView)
                            .create()
                        dialogBox.window?.setBackgroundDrawableResource(R.drawable.alert_dialog_background)
                        dialogBox.show()

                        updateButton?.setOnClickListener {
                            val updatedName = nameEditText.text.toString()
                            val updatedPrice = priceEditText.text.toString().toIntOrNull() ?: 0
                            val updatedOpenTime = openTimeEditText.text.toString()
                            val updatedCloseTime = closeTimeEditText.text.toString()

                            val selectedWeekdays = mutableListOf<String>()
                            if (mondayCheckBox.isChecked) selectedWeekdays.add("Monday")
                            if (tuesdayCheckBox.isChecked) selectedWeekdays.add("Tuesday")
                            if (wednesdayCheckBox.isChecked) selectedWeekdays.add("Wednesday")
                            if (thursdayCheckBox.isChecked) selectedWeekdays.add("Thursday")
                            if (fridayCheckBox.isChecked) selectedWeekdays.add("Friday")
                            if (saturdayCheckBox.isChecked) selectedWeekdays.add("Saturday")
                            if (sundayCheckBox.isChecked) selectedWeekdays.add("Sunday")

                            val selectedSports = mutableListOf<String>()
                            if (cricket.isChecked) selectedSports.add("Cricket")
                            if (football.isChecked) selectedSports.add("Football")
                            if (badminton.isChecked) selectedSports.add("Badminton")
                            if (basketball.isChecked) selectedSports.add("Basketball")
                            if (volleyball.isChecked) selectedSports.add("Volleyball")
                            if (swimming.isChecked) selectedSports.add("Swimming")

                            val updatedTurf = mapOf(
                                "name" to updatedName,
                                "price" to updatedPrice,
                                "open_time" to updatedOpenTime,
                                "close_time" to updatedCloseTime,
                                "weekdays" to selectedWeekdays,
                                "games" to selectedSports
                            )

                            turfRef.update(updatedTurf)
                                .addOnSuccessListener {
                                    Toast.makeText(itemView.context, "Turf details updated successfully", Toast.LENGTH_SHORT).show()
                                    dialogBox.dismiss()
                                }
                                .addOnFailureListener { exception ->
                                    Toast.makeText(itemView.context, "Error updating details: ${exception.message}", Toast.LENGTH_SHORT).show()
                                }

                        }

                        deleteTurfBtn.setOnClickListener {
                            val confirmDialogBuilder = MaterialAlertDialogBuilder(itemView.context)
                                .setTitle("Are you sure?")
                                .setMessage("This action will delete your Turf!")

                                .setNegativeButton("No"){dialog, _ ->
                                    dialog.dismiss()
                                }

                                .setPositiveButton("Delete"){dialog, _->
                                    val currentUserId = FirebaseAuth.getInstance().currentUser?.uid
                                    if (currentUserId == null) {
                                        Toast.makeText(itemView.context, "User not logged in", Toast.LENGTH_SHORT).show()
                                        dialog.dismiss()
                                        return@setPositiveButton
                                    }
                                    val managerRef = firestore.collection("managers").document(currentUserId)


                                    managerRef.get()
                                        .addOnSuccessListener { managerDoc->
                                            if (managerDoc != null && managerDoc.exists()){
                                                val turfsOwned = managerDoc.get("turfs_owned") as? List<String> ?: emptyList()
                                                val turfCount = managerDoc.getLong("turf_count")?.toInt() ?: 0
                                                val updatedTurfsOwned = turfsOwned.toMutableList().apply {
                                                    remove(turfId)
                                                }

                                                val updatedTurfCount = turfCount - 1

                                                managerRef.update("turfs_owned", updatedTurfsOwned, "turf_count",updatedTurfCount)
                                                    .addOnSuccessListener {
                                                        turfRef.delete()
                                                            .addOnSuccessListener{
                                                                Toast.makeText(itemView.context, "Turf Deleted Successfully!", Toast.LENGTH_SHORT).show()
                                                            }
                                                            .addOnFailureListener { exception->
                                                                Toast.makeText(itemView.context, "Error deleting turf: ${exception.message}", Toast.LENGTH_SHORT).show()
                                                            }
                                                    }
                                                    .addOnFailureListener { exception->
                                                        Toast.makeText(itemView.context, "Error updating manager: ${exception.message}", Toast.LENGTH_SHORT).show()
                                                    }
                                            }
                                            else{
                                                Toast.makeText(itemView.context, "Manager not found", Toast.LENGTH_SHORT).show()
                                            }
                                        }
                                        .addOnFailureListener { exception->
                                            Toast.makeText(itemView.context, "Error fetching manager details: ${exception.message}", Toast.LENGTH_SHORT).show()
                                        }
                                    dialog.dismiss()
                                }
                                .create()
                            confirmDialogBuilder.show()
                        }

                    } else {
                        Toast.makeText(itemView.context, "Turf details not found", Toast.LENGTH_SHORT).show()
                    }
                }
                .addOnFailureListener { exception ->
                    Toast.makeText(itemView.context, "Error fetching details: ${exception.message}", Toast.LENGTH_SHORT).show()
                }
        }

        private fun showTimePickerDialog(isOpenTime: Boolean) {
            val calendar = Calendar.getInstance()
            val hour = calendar.get(Calendar.HOUR_OF_DAY)
            val minute = calendar.get(Calendar.MINUTE)

            val timePickerDialog = TimePickerDialog(
                itemView.context,
                { _, selectedHour, selectedMinute ->
                    val formattedTime =
                        String.format(Locale.getDefault(), "%02d:%02d", selectedHour, selectedMinute)
                    if (isOpenTime) {
                        openTimeEditText.text = formattedTime
                    } else {
                        closeTimeEditText.text = formattedTime
                    }
                }, hour, minute, true
            )
            timePickerDialog.show()
        }

        fun bind(turf: Turf) {
            val firstImageUrl = turf.pictures.firstOrNull()
            if (firstImageUrl != null){
                Glide.with(itemView.context)
                    .load(firstImageUrl)
                    .apply(
                        RequestOptions()
                            .override(800, 400)
                            .downsample(DownsampleStrategy.AT_LEAST)
                            .centerCrop())
                    .into(turfImageView)
            }
            binding.turf = turf
            binding.executePendingBindings()
        }
    }

    class TurfDiffCallback : DiffUtil.ItemCallback<Turf>() {
        override fun areItemsTheSame(oldItem: Turf, newItem: Turf): Boolean {
            return oldItem.name == newItem.name
        }

        override fun areContentsTheSame(oldItem: Turf, newItem: Turf): Boolean {
            return oldItem == newItem
        }
    }

}

@BindingAdapter("games")
fun setChips(chipGroup: ChipGroup, games: List<String>?){
    chipGroup.removeAllViews()
    games?.forEach{ game ->
        val chip = Chip(chipGroup.context)
        chip.text = game
        chipGroup.addView(chip)
    }
}

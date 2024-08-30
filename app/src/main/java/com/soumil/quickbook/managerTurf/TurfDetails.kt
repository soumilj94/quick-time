package com.soumil.quickbook.managerTurf

import android.annotation.SuppressLint
import android.app.TimePickerDialog
import android.content.Intent
import android.graphics.Bitmap
import android.graphics.drawable.BitmapDrawable
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.provider.MediaStore
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ArrayAdapter
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity.RESULT_OK
import androidx.fragment.app.Fragment
import androidx.navigation.findNavController
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.GeoPoint
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase
import com.google.firebase.storage.ktx.storage
import com.soumil.quickbook.PermissionManager
import com.soumil.quickbook.R
import com.soumil.quickbook.databinding.FragmentTurfDetailsBinding
import com.soumil.quickbook.models.City
import com.soumil.quickbook.models.Manager
import com.soumil.quickbook.models.Turf
import com.soumil.quickbook.models.Game
import java.io.ByteArrayOutputStream
import java.util.*

class TurfDetails : Fragment() {
    // Fragment for adding creating a new turf from the Turf Manager Screen

    private lateinit var binding: FragmentTurfDetailsBinding
    private lateinit var auth: FirebaseAuth
    private var permissionManager: PermissionManager? = null

    // Permission handling for reading external media storage to get images
    // from device's gallery if Android Version > 13
    private val permission = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
        arrayOf(android.Manifest.permission.READ_MEDIA_IMAGES)
    } else {
        arrayOf(android.Manifest.permission.READ_EXTERNAL_STORAGE)
    }

    private val PICK_IMAGE_REQUEST_CODE = 200
    private var selectedImageView: Int? = null

    private var openTime: String? = null
    private var closeTime: String? = null
    private var latitude: String? = null
    private var longitude: String? = null

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        binding = FragmentTurfDetailsBinding.inflate(inflater, container, false)
        permissionManager = PermissionManager.getInstance(requireContext())

        binding.firstImage.setOnClickListener {
            selectedImageView = R.id.firstImage
            handleImageClick()
        }

        binding.secondImage.setOnClickListener {
            selectedImageView = R.id.secondImage
            handleImageClick()
        }

        binding.thirdImage.setOnClickListener {
            selectedImageView = R.id.thirdImage
            handleImageClick()
        }

        return binding.root
    }

    private val requestPermissionLauncher = registerForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { isGranted: Boolean ->
        if (isGranted) {
            Log.d("TurfDetails", "Permissions granted, picking image now.")
            pickSingleImage()
        } else {
            Toast.makeText(requireContext(), "Permission Denied.", Toast.LENGTH_SHORT).show()
        }
    }

    private fun handleImageClick() {
        Log.d("TurfDetails", "Image Clicked: $selectedImageView")
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            requestPermissionLauncher.launch(android.Manifest.permission.READ_MEDIA_IMAGES)
        } else {
            requestPermissionLauncher.launch(android.Manifest.permission.READ_EXTERNAL_STORAGE)
        }
    }

    private fun pickSingleImage() {
        val i = Intent(Intent.ACTION_PICK, MediaStore.Images.Media.INTERNAL_CONTENT_URI)
        startActivityForResult(i, PICK_IMAGE_REQUEST_CODE)
    }

    @Deprecated("Deprecated in Java")
    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        if (requestCode == PICK_IMAGE_REQUEST_CODE && resultCode == RESULT_OK && data != null) {
            val uri: Uri? = data.data
            uri?.let {
                when (selectedImageView) {
                    R.id.firstImage -> binding.firstImage.setImageURI(uri)
                    R.id.secondImage -> binding.secondImage.setImageURI(uri)
                    R.id.thirdImage -> binding.thirdImage.setImageURI(uri)
                }
            }
        }
        super.onActivityResult(requestCode, resultCode, data)
    }

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray
    ) {
        if (requestCode == PICK_IMAGE_REQUEST_CODE && permissionManager!!.handlePermissionResult(
                requireActivity(),
                grantResults
            )
        ) {
            pickSingleImage()
        }
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        auth = FirebaseAuth.getInstance()
        //sharedPreferences = requireActivity().getSharedPreferences("user_prefs", Context.MODE_PRIVATE)


        //latitude = requireArguments().getFloat("latitude")
        //longitude = requireArguments().getFloat("longitude")
        arguments?.let {
            latitude = it.getString("latitude", null)
            longitude = it.getString("longitude", null)
        }

        setupCityAutoComplete()
        setupPricePicker()

        binding.locateButton.setOnClickListener {
            it.findNavController().navigate(R.id.action_turf_details_to_set_turf_location)
        }

        binding.submitButton.setOnClickListener { validateAndSave() }

        binding.setOpenTime.setOnClickListener {
            showTimePickerDialog(true)
        }

        binding.setCloseTime.setOnClickListener {
            showTimePickerDialog(false)
        }
    }

    private fun validateSelectedImages(): String? {
        val firstImage = binding.firstImage.drawable
        val secondImage = binding.secondImage.drawable
        val thirdImage = binding.thirdImage.drawable

        return if (firstImage == null || secondImage == null || thirdImage == null) {
            "Please select 3 images to continue"
        } else {
            null
        }
    }

    //    function to upload the turf images to firebase cloud storage
    private fun uploadImagesToFirebase(turf: Turf) {
        val storage = Firebase.storage
        val storageRef = storage.reference
        val turfUid = auth.currentUser?.uid

        val imageUris = listOf(
            Pair(binding.firstImage.drawable, "first_image.jpg"),
            Pair(binding.secondImage.drawable, "second_image.jpg"),
            Pair(binding.thirdImage.drawable, "third_image.jpg"),
        )
        val uploadedImageUrls = mutableListOf<String>()

        imageUris.forEachIndexed { index, pair ->
            val drawable = pair.first
            val imageName = pair.second

            val bitmap = (drawable as BitmapDrawable).bitmap
            val baos = ByteArrayOutputStream()
            bitmap.compress(Bitmap.CompressFormat.JPEG, 100, baos)
            val data = baos.toByteArray()

            val imageRef = storageRef.child("turfs/$turfUid/$imageName")
            val uploadTask = imageRef.putBytes(data)

            uploadTask.addOnSuccessListener { snapshot ->
                imageRef.downloadUrl.addOnSuccessListener { uri ->
                    uploadedImageUrls.add(uri.toString())

                    if (uploadedImageUrls.size == 3) {
                        turf.pictures = uploadedImageUrls
                        saveTurfDetailsToFirestore(turf)
                    }
                }.addOnFailureListener { exception ->
                    Toast.makeText(
                        requireContext(),
                        "Failed to upload image: ${exception.message}",
                        Toast.LENGTH_SHORT
                    ).show()
                    binding.submitButton.text = "Submit"
                }
            }
        }
    }

    private fun setupCityAutoComplete() {
        val cities = resources.getStringArray(R.array.indian_cities)
        val adapter =
            ArrayAdapter(requireContext(), android.R.layout.simple_dropdown_item_1line, cities)
        binding.turfCity.setAdapter(adapter)
        binding.turfCity.threshold = 1 // Start suggesting from the first character
    }


    private fun setupPricePicker() {
        val turfPrice = binding.pricePicker
    }

    private fun showTimePickerDialog(isOpenTime: Boolean) {
        val calendar = Calendar.getInstance()
        val hour = calendar.get(Calendar.HOUR_OF_DAY)
        val minute = calendar.get(Calendar.MINUTE)


        val timePickerDialog = TimePickerDialog(
            requireContext(),
            { _, selectedHour, selectedMinute ->
                val formattedTime =
                    String.format(Locale.getDefault(), "%02d:%02d", selectedHour, selectedMinute)
                if (isOpenTime) {
                    openTime = formattedTime
                    binding.setOpenTime.text = formattedTime
                } else {
                    closeTime = formattedTime
                    binding.setCloseTime.text = formattedTime
                }
            }, hour, minute, true
        )
        timePickerDialog.show()
    }

    private fun validateAndSave() {
        val turfName = binding.turfNameTextInput
        val turfNameText = turfName.text.toString().trim()
        val city = binding.turfCity.text.toString()
        val turfPriceText = binding.pricePicker.text.toString().trim()
        val weekDays = getSelectedDays()
        val games = getSelectedGames()
        val timeValidationResult = validateTimes()

        val pricePerHour = try {
            turfPriceText.toInt()
        } catch (e: NumberFormatException) {
            -1
        }


        if (validateTurfName(turfNameText) != null || validateCity(city) != null || validatePrice(
                pricePerHour
            ) != null || validateDaysSelection() || validateGamesSelection() || timeValidationResult != null || validateSelectedImages() != null
        )// ||validateLocation() != null
        {
            Toast.makeText(
                requireContext(),
                "Fill all details properly!",
                Toast.LENGTH_SHORT
            ).show()
            return
        } else {
            binding.submitButton.text = "Processing"
            val latitudeDouble = latitude?.toDoubleOrNull() ?: 0.0
            val longitudeDouble = longitude?.toDoubleOrNull() ?: 0.0
            val geoPoint = GeoPoint(latitudeDouble, longitudeDouble)

            val turf = Turf(
                manager_id = auth.currentUser?.uid ?: "",
                name = turfNameText,
                city = city,
                price = pricePerHour,
                open_time = openTime ?: "",
                close_time = closeTime ?: "",
                weekdays = weekDays,
                games = games,
                rating_count = 0,
                rating_sum = 0,
                latitude = latitude ?: "",
                longitude = longitude ?: "",
                location = geoPoint
            )
            Log.e("priceChecker", pricePerHour.toString())
            uploadImagesToFirebase(turf)
        }
    }

    private fun validateTurfName(turfName: String): String? {
        return if (turfName.length <= 5) "Turf name must be more than 5 characters long." else null
    }

    private fun validateCity(city: String): String? {
        val validCities = resources.getStringArray(R.array.indian_cities)
        return if (city !in validCities) "Please select a valid city!" else null
    }

    private fun validatePrice(price: Int): String? {
        return if (price < 100 || price % 50 != 0) "Price must be between 100 and 1000, in multiples of 50." else null
    }

    private fun validateDaysSelection(): Boolean {
        val checkBoxes = listOf(
            binding.daysCheckbox.monday,
            binding.daysCheckbox.tuesday,
            binding.daysCheckbox.wednesday,
            binding.daysCheckbox.thursday,
            binding.daysCheckbox.friday,
            binding.daysCheckbox.saturday,
            binding.daysCheckbox.sunday
        )

        return !(checkBoxes.any { it.isChecked })
    }

    private fun validateGamesSelection(): Boolean {
        val checkBoxes = listOf(
            binding.sportsCheckbox.cricket,
            binding.sportsCheckbox.football,
            binding.sportsCheckbox.basketball,
            binding.sportsCheckbox.volleyball,
            binding.sportsCheckbox.badminton,
            binding.sportsCheckbox.swimming,
        )

        return !(checkBoxes.any { it.isChecked })
    }


    private fun getSelectedDays(): List<String> {
        val days = mutableListOf<String>()
        if (binding.daysCheckbox.monday.isChecked) days.add("Monday")
        if (binding.daysCheckbox.tuesday.isChecked) days.add("Tuesday")
        if (binding.daysCheckbox.wednesday.isChecked) days.add("Wednesday")
        if (binding.daysCheckbox.thursday.isChecked) days.add("Thursday")
        if (binding.daysCheckbox.friday.isChecked) days.add("Friday")
        if (binding.daysCheckbox.saturday.isChecked) days.add("Saturday")
        if (binding.daysCheckbox.sunday.isChecked) days.add("Sunday")
        return days
    }

    private fun getSelectedGames(): List<String> {
        val games = mutableListOf<String>()
        if (binding.sportsCheckbox.football.isChecked) games.add("Football")
        if (binding.sportsCheckbox.cricket.isChecked) games.add("Cricket")
        if (binding.sportsCheckbox.badminton.isChecked) games.add("Badminton")
        if (binding.sportsCheckbox.basketball.isChecked) games.add("Basketball")
        if (binding.sportsCheckbox.volleyball.isChecked) games.add("Volleyball")
        if (binding.sportsCheckbox.swimming.isChecked) games.add("Swimming")
        return games
    }

    private fun validateTimes(): String? {
        if (openTime == null || closeTime == null) {
            return "Please set both opening and closing times."
        }

        val openTimeParts = openTime!!.split(":").map { it.toInt() }
        val closeTimeParts = closeTime!!.split(":").map { it.toInt() }

        val openTimeInMinutes = openTimeParts[0] * 60 + openTimeParts[1]
        val closeTimeInMinutes = closeTimeParts[0] * 60 + closeTimeParts[1]

        return if (closeTimeInMinutes <= openTimeInMinutes) {
            "Closing time must be greater than opening time."
        } else {
            null
        }
    }

    @SuppressLint("SetTextI18n")
    private fun saveTurfDetailsToFirestore(turf: Turf) {
        val db = Firebase.firestore
        val managerId = auth.currentUser?.uid // Assuming the manager ID is the current user's UID
        val city = turf.city // Assuming the city name is part of the Turf object


        db.collection("turfs")
            .add(turf)
            .addOnSuccessListener { documentReference ->
                val generatedTurfId = documentReference.id
                Toast.makeText(requireContext(), "Turf details saved!", Toast.LENGTH_SHORT).show()
                
                // Update turf with the generated ID
                val turfRef = db.collection("turfs").document(generatedTurfId)
                turfRef.get().addOnSuccessListener { document ->
                    if (document.exists()) {
                        val turfData = document.toObject(Turf::class.java)
                        if (turfData != null) {
                            turfData.turf_uid = generatedTurfId
                            turfRef.set(turfData)
                                .addOnSuccessListener {
                                    Toast.makeText(
                                        requireContext(),
                                        "Turf uid saved",
                                        Toast.LENGTH_SHORT
                                    ).show()
                                    //requireActivity().finish()
                                }
                                .addOnFailureListener { e ->
                                    Toast.makeText(
                                        requireContext(),
                                        "Error: ${e.message}",
                                        Toast.LENGTH_SHORT
                                    ).show()
                                    binding.submitButton.text = "Submit"
                                }
                        }
                    } else {
                        Toast.makeText(
                            requireContext(),
                            "Turf uid added successfully!",
                            Toast.LENGTH_SHORT
                        ).show()
                    }
                    // Fetch the current manager data
                    db.collection("managers").document(managerId!!)
                        .get()
                        .addOnSuccessListener { document ->
                            if (document != null) {
                                val manager = document.toObject(Manager::class.java)
                                if (manager != null) {
                                    manager.turfs_owned = manager.turfs_owned + generatedTurfId
                                    manager.turf_count = manager.turf_count + 1

                                    db.collection("managers").document(managerId)
                                        .set(manager)
                                        .addOnSuccessListener {
                                            Toast.makeText(
                                                requireContext(),
                                                "Manager details updated successfully!",
                                                Toast.LENGTH_SHORT
                                            ).show()
                                            // Update city with new turf
                                            val cityRef = db.collection("cities").document(city)

                                            cityRef.get().addOnSuccessListener { document ->
                                                if (document.exists()) {
                                                    // Document exists, update the array of turfs
                                                    val cityData =
                                                        document.toObject(City::class.java)
                                                    if (cityData != null) {
                                                        cityData.turfs =
                                                            cityData.turfs + generatedTurfId
                                                        cityData.turfCount += 1
                                                        cityRef.set(cityData)
                                                            .addOnSuccessListener {
                                                                Toast.makeText(
                                                                    requireContext(),
                                                                    "City updated with new turf!",
                                                                    Toast.LENGTH_SHORT
                                                                ).show()
                                                                //requireActivity().finish()
                                                            }
                                                            .addOnFailureListener { e ->
                                                                Toast.makeText(
                                                                    requireContext(),
                                                                    "Error updating city: ${e.message}",
                                                                    Toast.LENGTH_SHORT
                                                                ).show()
                                                                binding.submitButton.text = "Submit"
                                                            }
                                                    }
                                                } else {
                                                    // Document does not exist, create a new one
                                                    val newCityData = City(
                                                        turfCount = 1,
                                                        turfs = listOf(generatedTurfId)
                                                    )
                                                    cityRef.set(newCityData)
                                                        .addOnSuccessListener {
                                                            Toast.makeText(
                                                                requireContext(),
                                                                "City created with new turf!",
                                                                Toast.LENGTH_SHORT
                                                            ).show()
                                                            //requireActivity().finish()
                                                        }
                                                        .addOnFailureListener { e ->
                                                            Toast.makeText(
                                                                requireContext(),
                                                                "Error creating city: ${e.message}",
                                                                Toast.LENGTH_SHORT
                                                            ).show()
                                                            binding.submitButton.text = "Submit"
                                                        }
                                                }
                                            }.addOnFailureListener { e ->
                                                Toast.makeText(
                                                    requireContext(),
                                                    "Error checking city: ${e.message}",
                                                    Toast.LENGTH_SHORT
                                                ).show()
                                                binding.submitButton.text = "Submit"
                                            }

                                            // Update games with new turf
                                            val selectedGames = getSelectedGames()

                                            selectedGames.forEach { game ->
                                                val gameRef = db.collection("games").document(game)

                                                gameRef.get().addOnSuccessListener { document ->
                                                    if (document.exists()) {
                                                        // Document exists, update the array of turfs
                                                        val gameData =
                                                            document.toObject(Game::class.java)
                                                        if (gameData != null) {
                                                            gameData.turfs =
                                                                gameData.turfs + generatedTurfId
                                                            gameData.turfCount += 1
                                                            gameRef.set(gameData)
                                                                .addOnSuccessListener {
                                                                    Toast.makeText(
                                                                        requireContext(),
                                                                        "Game updated with new turf!",
                                                                        Toast.LENGTH_SHORT
                                                                    ).show()
                                                                    requireActivity().finish()
                                                                }
                                                                .addOnFailureListener { e ->
                                                                    Toast.makeText(
                                                                        requireContext(),
                                                                        "Error updating game: ${e.message}",
                                                                        Toast.LENGTH_SHORT
                                                                    ).show()
                                                                    binding.submitButton.text =
                                                                        "Submit"
                                                                }
                                                        }
                                                    } else {
                                                        // Document does not exist, create a new one
                                                        val newGameData = Game(
                                                            turfCount = 1,
                                                            turfs = listOf(generatedTurfId)
                                                        )
                                                        gameRef.set(newGameData)
                                                            .addOnSuccessListener {
                                                                Toast.makeText(
                                                                    requireContext(),
                                                                    "Game created with new turf!",
                                                                    Toast.LENGTH_SHORT
                                                                ).show()
                                                                requireActivity().finish()
                                                            }
                                                            .addOnFailureListener { e ->
                                                                Toast.makeText(
                                                                    requireContext(),
                                                                    "Error creating game: ${e.message}",
                                                                    Toast.LENGTH_SHORT
                                                                ).show()
                                                                binding.submitButton.text = "Submit"
                                                            }
                                                    }
                                                }.addOnFailureListener { e ->
                                                    Toast.makeText(
                                                        requireContext(),
                                                        "Error checking game: ${e.message}",
                                                        Toast.LENGTH_SHORT
                                                    ).show()
                                                    binding.submitButton.text = "Submit"
                                                }
                                            }
                                        }
                                        .addOnFailureListener { e ->
                                            Toast.makeText(
                                                requireContext(),
                                                "Error updating manager details: ${e.message}",
                                                Toast.LENGTH_SHORT
                                            ).show()
                                            binding.submitButton.text = "Submit"
                                        }
                                }
                            }
                        }
                        .addOnFailureListener { e ->
                            Toast.makeText(
                                requireContext(),
                                "Error fetching manager details: ${e.message}",
                                Toast.LENGTH_SHORT
                            ).show()
                            binding.submitButton.text = "Submit"
                        }

                }
                    .addOnFailureListener { e ->
                        Toast.makeText(
                            requireContext(),
                            "Error saving turf details: ${e.message}",
                            Toast.LENGTH_SHORT
                        ).show()
                        binding.submitButton.text = "Submit"
                    }
            }
    }

}

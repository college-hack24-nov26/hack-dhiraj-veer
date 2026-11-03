
package com.example.pikrakshak

import android.Manifest
import android.annotation.SuppressLint
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.core.content.ContextCompat
import com.google.android.gms.location.LocationServices
import kotlin.math.*

private val StorageGreen = Color(0xFF14532D)
private val StorageBackground = Color(0xFFF0FDF4)

@Composable
fun NearbyStorageScreen(
    onBack: () -> Unit
) {
    val context = LocalContext.current

    var facilities by remember {
        mutableStateOf<List<StorageFacility>>(emptyList())
    }

    var isLoading by remember { mutableStateOf(true) }
    var errorMessage by remember { mutableStateOf("") }

    var farmerLatitude by remember { mutableStateOf<Double?>(null) }
    var farmerLongitude by remember { mutableStateOf<Double?>(null) }

    var locationMessage by remember {
        mutableStateOf("Enable location to find nearest storage.")
    }

    // Booking form state
    var bookingFacility by remember {
        mutableStateOf<StorageFacility?>(null)
    }
    var quantityText by remember { mutableStateOf("") }
    var farmerPhone by remember { mutableStateOf("") }
    var bookingNotes by remember { mutableStateOf("") }
    var bookingMessage by remember { mutableStateOf("") }
    var isBooking by remember { mutableStateOf(false) }
    var bookingSubmitted by remember { mutableStateOf(false) }

    val fusedLocationClient = remember {
        LocationServices.getFusedLocationProviderClient(context)
    }

    // Get farmer's current phone location
    @SuppressLint("MissingPermission")
    fun getFarmerLocation() {
        val fineGranted = ContextCompat.checkSelfPermission(
            context,
            Manifest.permission.ACCESS_FINE_LOCATION
        ) == PackageManager.PERMISSION_GRANTED

        val coarseGranted = ContextCompat.checkSelfPermission(
            context,
            Manifest.permission.ACCESS_COARSE_LOCATION
        ) == PackageManager.PERMISSION_GRANTED

        if (!fineGranted && !coarseGranted) {
            locationMessage = "Please allow location permission."
            return
        }

        try {
            fusedLocationClient.lastLocation
                .addOnSuccessListener { location ->
                    if (location != null) {
                        farmerLatitude = location.latitude
                        farmerLongitude = location.longitude
                        locationMessage = "Your location is detected."
                    } else {
                        locationMessage =
                            "Location unavailable. Turn on GPS and try again."
                    }
                }
                .addOnFailureListener { exception ->
                    locationMessage =
                        exception.localizedMessage ?: "Unable to get location."
                }
        } catch (e: SecurityException) {
            locationMessage = "Location permission is required."
        }
    }

    // Request location permission
    val locationPermissionLauncher =
        rememberLauncherForActivityResult(
            ActivityResultContracts.RequestMultiplePermissions()
        ) { permissions ->
            if (
                permissions[Manifest.permission.ACCESS_FINE_LOCATION] == true ||
                permissions[Manifest.permission.ACCESS_COARSE_LOCATION] == true
            ) {
                getFarmerLocation()
            } else {
                locationMessage = "Location permission denied."
            }
        }

    // Load active facilities from Firebase
    DisposableEffect(Unit) {
        val registration = StorageRepository()
            .listenToActiveFacilities(
                onResult = { result ->
                    facilities = result
                    isLoading = false
                    errorMessage = ""
                },
                onError = { error ->
                    errorMessage = error
                    isLoading = false
                }
            )

        onDispose {
            registration.remove()
        }
    }

    // Calculate distance using Haversine formula
    fun calculateDistance(
        lat1: Double,
        lon1: Double,
        lat2: Double,
        lon2: Double
    ): Double {
        val earthRadius = 6371.0
        val dLat = Math.toRadians(lat2 - lat1)
        val dLon = Math.toRadians(lon2 - lon1)

        val a = sin(dLat / 2).pow(2) +
                cos(Math.toRadians(lat1)) *
                cos(Math.toRadians(lat2)) *
                sin(dLon / 2).pow(2)

        val c = 2 * atan2(sqrt(a), sqrt(1 - a))
        return earthRadius * c
    }

    // Sort facilities from nearest to farthest
    val sortedFacilities = remember(
        facilities,
        farmerLatitude,
        farmerLongitude
    ) {
        if (farmerLatitude != null && farmerLongitude != null) {
            facilities.sortedBy { facility ->
                if (
                    facility.latitude in -90.0..90.0 &&
                    facility.longitude in -180.0..180.0 &&
                    (facility.latitude != 0.0 || facility.longitude != 0.0)
                ) {
                    calculateDistance(
                        farmerLatitude!!,
                        farmerLongitude!!,
                        facility.latitude,
                        facility.longitude
                    )
                } else {
                    Double.MAX_VALUE
                }
            }
        } else {
            facilities
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(StorageBackground)
            .padding(18.dp)
    ) {
        Text(
            text = "🏬 Nearby Storage",
            style = MaterialTheme.typography.headlineMedium,
            fontWeight = FontWeight.Bold,
            color = StorageGreen
        )

        Spacer(Modifier.height(6.dp))

        Text(
            text = "Find and book storage facilities nearest to your location.",
            color = Color.DarkGray
        )

        Spacer(Modifier.height(12.dp))

        Button(onClick = onBack) {
            Text("← Back to Dashboard")
        }

        Spacer(Modifier.height(12.dp))

        OutlinedButton(
            onClick = {
                val fineGranted = ContextCompat.checkSelfPermission(
                    context,
                    Manifest.permission.ACCESS_FINE_LOCATION
                ) == PackageManager.PERMISSION_GRANTED

                val coarseGranted = ContextCompat.checkSelfPermission(
                    context,
                    Manifest.permission.ACCESS_COARSE_LOCATION
                ) == PackageManager.PERMISSION_GRANTED

                if (fineGranted || coarseGranted) {
                    getFarmerLocation()
                } else {
                    locationPermissionLauncher.launch(
                        arrayOf(
                            Manifest.permission.ACCESS_FINE_LOCATION,
                            Manifest.permission.ACCESS_COARSE_LOCATION
                        )
                    )
                }
            },
            modifier = Modifier.fillMaxWidth()
        ) {
            Text("📍 Find Storage Near Me")
        }

        Text(
            text = locationMessage,
            color = StorageGreen,
            style = MaterialTheme.typography.bodySmall
        )

        Spacer(Modifier.height(12.dp))

        when {
            isLoading -> {
                CircularProgressIndicator(color = StorageGreen)
                Text("Loading storage facilities...")
            }

            errorMessage.isNotBlank() -> {
                Text(
                    text = "Unable to load storage: $errorMessage",
                    color = MaterialTheme.colorScheme.error
                )
                Text("Check your internet connection and Firestore rules.")
            }

            facilities.isEmpty() -> {
                Text("No active storage facilities found.")
                Text("Check that a facility has isActive set to true.")
            }

            else -> {
                Text(
                    text = "${facilities.size} active storage facilities",
                    fontWeight = FontWeight.SemiBold,
                    color = StorageGreen
                )

                if (farmerLatitude != null) {
                    Text(
                        text = "Sorted by nearest distance",
                        color = StorageGreen,
                        style = MaterialTheme.typography.bodySmall
                    )
                }

                Spacer(Modifier.height(8.dp))

                LazyColumn(
                    modifier = Modifier.weight(1f),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    items(
                        items = sortedFacilities,
                        key = { it.id }
                    ) { facility ->

                        val hasValidLocation =
                            facility.latitude in -90.0..90.0 &&
                                    facility.longitude in -180.0..180.0 &&
                                    (facility.latitude != 0.0 ||
                                            facility.longitude != 0.0)

                        val distance = if (
                            farmerLatitude != null &&
                            farmerLongitude != null &&
                            hasValidLocation
                        ) {
                            calculateDistance(
                                farmerLatitude!!,
                                farmerLongitude!!,
                                facility.latitude,
                                facility.longitude
                            )
                        } else {
                            null
                        }

                        Card(
                            modifier = Modifier.fillMaxWidth(),
                            shape = RoundedCornerShape(18.dp),
                            colors = CardDefaults.cardColors(
                                containerColor = Color.White
                            ),
                            elevation = CardDefaults.cardElevation(
                                defaultElevation = 3.dp
                            )
                        ) {
                            Column(
                                modifier = Modifier.padding(16.dp)
                            ) {
                                Text(
                                    text = facility.name,
                                    style = MaterialTheme.typography.titleLarge,
                                    fontWeight = FontWeight.Bold,
                                    color = StorageGreen
                                )

                                Spacer(Modifier.height(6.dp))

                                Text(
                                    text = facility.storageType.ifBlank {
                                        "Storage facility"
                                    }
                                )

                                Text(
                                    text = listOf(
                                        facility.address,
                                        facility.city,
                                        facility.state
                                    )
                                        .filter { it.isNotBlank() }
                                        .joinToString(", ")
                                        .ifBlank { "Address not available" }
                                )

                                Spacer(Modifier.height(10.dp))

                                if (distance != null) {
                                    Text(
                                        text = "📍 Distance: ${
                                            String.format("%.2f", distance)
                                        } km",
                                        color = StorageGreen,
                                        fontWeight = FontWeight.Bold
                                    )
                                } else if (farmerLatitude == null) {
                                    Text(
                                        "Tap Find Storage Near Me to calculate distance."
                                    )
                                } else {
                                    Text("Distance unavailable for this facility.")
                                }

                                Spacer(Modifier.height(8.dp))

                                Text(
                                    "Available capacity: ${facility.availableCapacity} kg"
                                )

                                Text(
                                    "Price: ₹${facility.pricePerUnit} per kg"
                                )

                                // Call Storage Owner
                                if (facility.phone.isNotBlank()) {
                                    Spacer(Modifier.height(8.dp))

                                    Text("Contact: ${facility.phone}")

                                    Spacer(Modifier.height(8.dp))

                                    Button(
                                        onClick = {
                                            val intent = Intent(
                                                Intent.ACTION_DIAL,
                                                Uri.parse("tel:${facility.phone}")
                                            )
                                            context.startActivity(intent)
                                        },
                                        modifier = Modifier.fillMaxWidth()
                                    ) {
                                        Text("📞 Call Storage Owner")
                                    }
                                }

                                // Book Storage button
                                Spacer(Modifier.height(8.dp))

                                Button(
                                    onClick = {
                                        bookingFacility = facility
                                        quantityText = ""
                                        farmerPhone = ""
                                        bookingNotes = ""
                                        bookingMessage = ""
                                        bookingSubmitted = false
                                    },
                                    modifier = Modifier.fillMaxWidth(),
                                    enabled = facility.availableCapacity > 0
                                ) {
                                    Text("📦 Book Storage")
                                }
                            }
                        }
                    }
                }
            }
        }
    }

    // Storage booking form
    bookingFacility?.let { facility ->

        AlertDialog(
            onDismissRequest = {
                if (!isBooking) {
                    bookingFacility = null
                }
            },
            title = {
                Text(
                    text = if (bookingSubmitted) {
                        "Booking Request Submitted"
                    } else {
                        "Book ${facility.name}"
                    }
                )
            },
            text = {
                Column(
                    verticalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    if (bookingSubmitted) {
                        Text(
                            "Your booking request has been saved successfully. " +
                                    "Its current status is Pending."
                        )
                    } else {
                        Text(
                            "Available capacity: ${facility.availableCapacity} kg"
                        )

                        Text(
                            "Price: ₹${facility.pricePerUnit} per kg"
                        )

                        OutlinedTextField(
                            value = quantityText,
                            onValueChange = { quantityText = it },
                            label = { Text("Required quantity (kg)") },
                            keyboardOptions = KeyboardOptions(
                                keyboardType = KeyboardType.Decimal
                            ),
                            singleLine = true,
                            modifier = Modifier.fillMaxWidth()
                        )

                        OutlinedTextField(
                            value = farmerPhone,
                            onValueChange = { farmerPhone = it },
                            label = { Text("Your phone number") },
                            keyboardOptions = KeyboardOptions(
                                keyboardType = KeyboardType.Phone
                            ),
                            singleLine = true,
                            modifier = Modifier.fillMaxWidth()
                        )

                        OutlinedTextField(
                            value = bookingNotes,
                            onValueChange = { bookingNotes = it },
                            label = { Text("Notes (optional)") },
                            modifier = Modifier.fillMaxWidth()
                        )
                    }

                    if (bookingMessage.isNotBlank()) {
                        Text(
                            text = bookingMessage,
                            color = MaterialTheme.colorScheme.error
                        )
                    }
                }
            },
            confirmButton = {
                if (bookingSubmitted) {
                    TextButton(
                        onClick = {
                            bookingFacility = null
                        }
                    ) {
                        Text("Done")
                    }
                } else {
                    Button(
                        enabled = !isBooking,
                        onClick = {
                            val quantity = quantityText.toDoubleOrNull()

                            when {
                                quantity == null || quantity <= 0 -> {
                                    bookingMessage =
                                        "Please enter a valid quantity."
                                }

                                quantity > facility.availableCapacity -> {
                                    bookingMessage =
                                        "Quantity exceeds available capacity."
                                }

                                farmerPhone.isBlank() -> {
                                    bookingMessage =
                                        "Please enter your phone number."
                                }

                                else -> {
                                    isBooking = true
                                    bookingMessage = ""

                                    StorageBookingRepository().createBooking(
                                        facility = facility,
                                        quantityKg = quantity,
                                        farmerPhone = farmerPhone.trim(),
                                        notes = bookingNotes.trim(),
                                        onSuccess = {
                                            isBooking = false
                                            bookingSubmitted = true
                                            bookingMessage = ""
                                        },
                                        onError = { error ->
                                            isBooking = false
                                            bookingMessage = error
                                        }
                                    )
                                }
                            }
                        }
                    ) {
                        if (isBooking) {
                            CircularProgressIndicator(
                                modifier = Modifier.size(18.dp),
                                strokeWidth = 2.dp
                            )
                        } else {
                            Text("Submit Booking")
                        }
                    }
                }
            },
            dismissButton = {
                if (!bookingSubmitted) {
                    TextButton(
                        enabled = !isBooking,
                        onClick = {
                            bookingFacility = null
                        }
                    ) {
                        Text("Cancel")
                    }
                }
            }
        )
    }
}

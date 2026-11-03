
package com.example.pikrakshak

import android.Manifest
import android.annotation.SuppressLint
import android.content.pm.PackageManager
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.core.content.ContextCompat
import com.google.android.gms.location.LocationServices

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddStorageScreen(
    onBack: () -> Unit,
    onFacilityAdded: () -> Unit
) {
    val context = LocalContext.current
    val repository = remember { StorageRepository() }
    val fusedLocationClient = remember {
        LocationServices.getFusedLocationProviderClient(context)
    }

    var name by remember { mutableStateOf("") }
    var ownerName by remember { mutableStateOf("") }
    var address by remember { mutableStateOf("") }
    var city by remember { mutableStateOf("") }
    var state by remember { mutableStateOf("Maharashtra") }
    var storageType by remember { mutableStateOf("Cold Storage") }
    var totalCapacity by remember { mutableStateOf("") }
    var availableCapacity by remember { mutableStateOf("") }
    var pricePerUnit by remember { mutableStateOf("") }
    var phone by remember { mutableStateOf("") }

    var latitude by remember { mutableStateOf<Double?>(null) }
    var longitude by remember { mutableStateOf<Double?>(null) }
    var locationMessage by remember { mutableStateOf("Location not captured") }
    var message by remember { mutableStateOf("") }
    var isSaving by remember { mutableStateOf(false) }

    val storageTypes = listOf(
        "Cold Storage",
        "Grain Warehouse",
        "Fruit Storage",
        "Vegetable Storage",
        "General Warehouse"
    )

    @SuppressLint("MissingPermission")
    fun captureLocation() {
        val fineGranted = ContextCompat.checkSelfPermission(
            context, Manifest.permission.ACCESS_FINE_LOCATION
        ) == PackageManager.PERMISSION_GRANTED

        val coarseGranted = ContextCompat.checkSelfPermission(
            context, Manifest.permission.ACCESS_COARSE_LOCATION
        ) == PackageManager.PERMISSION_GRANTED

        if (!fineGranted && !coarseGranted) {
            locationMessage = "Location permission is required."
            return
        }

        try {
            fusedLocationClient.lastLocation
                .addOnSuccessListener { location ->
                    if (location != null) {
                        latitude = location.latitude
                        longitude = location.longitude
                        locationMessage = "Location captured successfully"
                    } else {
                        locationMessage = "Location unavailable. Turn on GPS and try again."
                    }
                }
                .addOnFailureListener {
                    locationMessage = it.localizedMessage ?: "Unable to get location."
                }
        } catch (e: SecurityException) {
            locationMessage = "Location permission is required."
        }
    }

    val locationPermissionLauncher =
        rememberLauncherForActivityResult(
            ActivityResultContracts.RequestMultiplePermissions()
        ) { permissions ->
            if (
                permissions[Manifest.permission.ACCESS_FINE_LOCATION] == true ||
                permissions[Manifest.permission.ACCESS_COARSE_LOCATION] == true
            ) {
                captureLocation()
            } else {
                locationMessage = "Please allow location permission."
            }
        }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(20.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        TextButton(onClick = onBack) {
            Text("← Back")
        }

        Text(
            text = "Add Storage Facility",
            style = MaterialTheme.typography.headlineSmall
        )

        Text(
            "Register your warehouse or cold storage so farmers can find it."
        )

        @Composable
        fun inputField(
            label: String,
            value: String,
            onValueChange: (String) -> Unit
        ) {
            OutlinedTextField(
                value = value,
                onValueChange = onValueChange,
                label = { Text(label) },
                modifier = Modifier.fillMaxWidth(),
                singleLine = true,
                shape = RoundedCornerShape(12.dp)
            )
        }

        inputField("Facility Name", name) { name = it }
        inputField("Owner Name", ownerName) { ownerName = it }
        inputField("Address", address) { address = it }
        inputField("City", city) { city = it }
        inputField("State", state) { state = it }

        Text("Storage Type", style = MaterialTheme.typography.titleMedium)

        var expanded by remember { mutableStateOf(false) }

        ExposedDropdownMenuBox(
            expanded = expanded,
            onExpandedChange = { expanded = !expanded }
        ) {
            OutlinedTextField(
                value = storageType,
                onValueChange = {},
                readOnly = true,
                label = { Text("Select Storage Type") },
                trailingIcon = {
                    ExposedDropdownMenuDefaults.TrailingIcon(expanded)
                },
                modifier = Modifier
                    .menuAnchor()
                    .fillMaxWidth()
            )

            ExposedDropdownMenu(
                expanded = expanded,
                onDismissRequest = { expanded = false }
            ) {
                storageTypes.forEach { type ->
                    DropdownMenuItem(
                        text = { Text(type) },
                        onClick = {
                            storageType = type
                            expanded = false
                        }
                    )
                }
            }
        }

        inputField("Total Capacity (kg)", totalCapacity) {
            totalCapacity = it
        }

        inputField("Available Capacity (kg)", availableCapacity) {
            availableCapacity = it
        }

        inputField("Price Per Kg (₹)", pricePerUnit) {
            pricePerUnit = it
        }

        inputField("Contact Number", phone) { phone = it }

        HorizontalDivider()

        Text("Facility GPS Location", style = MaterialTheme.typography.titleMedium)

        Button(
            onClick = {
                val fineGranted = ContextCompat.checkSelfPermission(
                    context, Manifest.permission.ACCESS_FINE_LOCATION
                ) == PackageManager.PERMISSION_GRANTED

                val coarseGranted = ContextCompat.checkSelfPermission(
                    context, Manifest.permission.ACCESS_COARSE_LOCATION
                ) == PackageManager.PERMISSION_GRANTED

                if (fineGranted || coarseGranted) {
                    captureLocation()
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
            Text("📍 Capture Current Location")
        }

        Text(locationMessage)

        if (latitude != null && longitude != null) {
            Text("Latitude: $latitude")
            Text("Longitude: $longitude")
        }

        if (message.isNotBlank()) {
            Text(
                text = message,
                color = MaterialTheme.colorScheme.primary
            )
        }

        Button(
            onClick = {
                val total = totalCapacity.toDoubleOrNull()
                val available = availableCapacity.toDoubleOrNull()
                val price = pricePerUnit.toDoubleOrNull()

                when {
                    name.isBlank() || ownerName.isBlank() ||
                            address.isBlank() || city.isBlank() ||
                            state.isBlank() || phone.isBlank() -> {
                        message = "Please fill all required fields."
                    }

                    total == null || available == null || price == null ||
                            total <= 0 || available < 0 ||
                            available > total || price < 0 -> {
                        message = "Please enter valid capacity and price values."
                    }

                    latitude == null || longitude == null -> {
                        message = "Please capture the facility GPS location."
                    }

                    else -> {
                        isSaving = true
                        message = "Saving facility..."

                        val facility = StorageFacility(
                            name = name.trim(),
                            ownerName = ownerName.trim(),
                            address = address.trim(),
                            city = city.trim(),
                            state = state.trim(),
                            storageType = storageType,
                            totalCapacity = total,
                            availableCapacity = available,
                            pricePerUnit = price,
                            phone = phone.trim(),
                            latitude = latitude!!,
                            longitude = longitude!!
                        )

                        repository.addFacility(
                            facility = facility,
                            onSuccess = {
                                isSaving = false
                                message = "Storage facility added successfully!"
                                onFacilityAdded()
                            },
                            onError = { error ->
                                isSaving = false
                                message = error
                            }
                        )
                    }
                }
            },
            enabled = !isSaving,
            modifier = Modifier.fillMaxWidth()
        ) {
            Text(if (isSaving) "Saving..." else "Save Storage Facility")
        }

        Spacer(modifier = Modifier.height(20.dp))
    }
}

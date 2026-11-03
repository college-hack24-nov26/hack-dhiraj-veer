
package com.example.pikrakshak

import android.Manifest
import android.content.Context
import android.content.pm.PackageManager
import android.location.Location
import android.os.Handler
import android.os.Looper
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.Alignment
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.content.ContextCompat
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationServices
import com.google.android.gms.location.Priority
import java.net.HttpURLConnection
import java.net.URL
import org.json.JSONObject

@Suppress("UNUSED_PARAMETER")
@Composable
fun ShelfLifeScreen(
    onBack: () -> Unit = {},
    photoPath: String? = null
) {
    val context = LocalContext.current
    val green = Color(0xFF14532D)

    val fusedLocationClient = remember {
        LocationServices.getFusedLocationProviderClient(context)
    }

    var selectedCrop by remember {
        mutableStateOf(CropType.TOMATO)
    }

    var daysText by remember { mutableStateOf("1") }
    var temperatureText by remember { mutableStateOf("25") }
    var humidityText by remember { mutableStateOf("65") }

    var selectedStorage by remember {
        mutableStateOf(StorageType.ROOM_TEMPERATURE)
    }

    var selectedMaturity by remember {
        mutableStateOf(MaturityLevel.MATURE)
    }

    var selectedCondition by remember {
        mutableStateOf(ProduceCondition.GOOD)
    }

    var result by remember {
        mutableStateOf<ShelfLifeResult?>(null)
    }

    var errorMessage by remember { mutableStateOf("") }
    var saveMessage by remember { mutableStateOf("") }
    var isSaving by remember { mutableStateOf(false) }
    var isLoadingWeather by remember { mutableStateOf(false) }
    var weatherMessage by remember { mutableStateOf("") }

    fun loadWeather() {
        fetchWeather(
            context = context,
            fusedLocationClient = fusedLocationClient,
            onLoading = { isLoadingWeather = it },
            onSuccess = { temperature, humidity ->
                temperatureText = temperature.toString()
                humidityText = humidity.toString()
                weatherMessage = "Weather updated using your location."
                result = null
                errorMessage = ""
                saveMessage = ""
            },
            onError = { message ->
                weatherMessage = message
            }
        )
    }

    val locationPermissionLauncher =
        rememberLauncherForActivityResult(
            ActivityResultContracts.RequestMultiplePermissions()
        ) { permissions ->

            val granted =
                permissions[Manifest.permission.ACCESS_FINE_LOCATION] == true ||
                        permissions[Manifest.permission.ACCESS_COARSE_LOCATION] == true

            if (granted) {
                loadWeather()
            } else {
                weatherMessage = "Location permission was not granted."
            }
        }

    fun useLocationWeather() {
        val fineGranted = ContextCompat.checkSelfPermission(
            context,
            Manifest.permission.ACCESS_FINE_LOCATION
        ) == PackageManager.PERMISSION_GRANTED

        val coarseGranted = ContextCompat.checkSelfPermission(
            context,
            Manifest.permission.ACCESS_COARSE_LOCATION
        ) == PackageManager.PERMISSION_GRANTED

        if (fineGranted || coarseGranted) {
            loadWeather()
        } else {
            locationPermissionLauncher.launch(
                arrayOf(
                    Manifest.permission.ACCESS_FINE_LOCATION,
                    Manifest.permission.ACCESS_COARSE_LOCATION
                )
            )
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(20.dp),
        verticalArrangement = Arrangement.spacedBy(14.dp)
    ) {

        TextButton(onClick = onBack) {
            Text("← Back to Home")
        }

        Text(
            text = "Shelf-Life Estimator",
            fontSize = 26.sp,
            fontWeight = FontWeight.Bold,
            color = green
        )

        Text(
            "Estimate the remaining shelf life of your harvested produce."
        )

        // CROP SELECTION

        Text(
            "Select crop",
            fontWeight = FontWeight.SemiBold
        )

        EnumDropdown(
            label = selectedCrop.displayName,
            items = CropType.entries,
            itemLabel = { it.displayName },
            onSelected = {
                selectedCrop = it
                result = null
                saveMessage = ""
            }
        )

        // DAYS SINCE HARVEST

        OutlinedTextField(
            value = daysText,
            onValueChange = {
                daysText = it
                result = null
                saveMessage = ""
            },
            label = { Text("Days since harvest") },
            keyboardOptions = KeyboardOptions(
                keyboardType = KeyboardType.Number
            ),
            singleLine = true,
            modifier = Modifier.fillMaxWidth()
        )

        // LOCAL WEATHER

        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(16.dp),
            colors = CardDefaults.cardColors(
                containerColor = Color(0xFFF0FDF4)
            )
        ) {
            Column(
                modifier = Modifier.padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {

                Text(
                    "LOCAL WEATHER",
                    fontWeight = FontWeight.Bold,
                    color = green
                )

                Text(
                    "Get outdoor temperature and humidity using your phone's location."
                )

                Button(
                    onClick = { useLocationWeather() },
                    enabled = !isLoadingWeather,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    if (isLoadingWeather) {
                        CircularProgressIndicator(
                            modifier = Modifier.size(18.dp),
                            strokeWidth = 2.dp
                        )
                        Spacer(Modifier.width(8.dp))
                        Text("Getting weather...")
                    } else {
                        Text("📍 Use My Location")
                    }
                }

                if (weatherMessage.isNotBlank()) {
                    Text(
                        text = weatherMessage,
                        color = if (
                            weatherMessage.startsWith("Weather updated")
                        ) {
                            green
                        } else {
                            MaterialTheme.colorScheme.error
                        },
                        fontSize = 13.sp
                    )
                }
            }
        }

        // TEMPERATURE

        OutlinedTextField(
            value = temperatureText,
            onValueChange = {
                temperatureText = it
                result = null
                saveMessage = ""
            },
            label = { Text("Temperature (°C)") },
            keyboardOptions = KeyboardOptions(
                keyboardType = KeyboardType.Decimal
            ),
            singleLine = true,
            modifier = Modifier.fillMaxWidth()
        )

        // HUMIDITY

        OutlinedTextField(
            value = humidityText,
            onValueChange = {
                humidityText = it
                result = null
                saveMessage = ""
            },
            label = { Text("Humidity (%)") },
            keyboardOptions = KeyboardOptions(
                keyboardType = KeyboardType.Decimal
            ),
            singleLine = true,
            modifier = Modifier.fillMaxWidth()
        )

        // STORAGE TYPE

        Text(
            "Storage type",
            fontWeight = FontWeight.SemiBold
        )

        EnumDropdown(
            label = selectedStorage.label,
            items = StorageType.entries,
            itemLabel = { it.label },
            onSelected = {
                selectedStorage = it
                result = null
                saveMessage = ""
            }
        )

        // MATURITY LEVEL

        Text(
            "Maturity level",
            fontWeight = FontWeight.SemiBold
        )

        EnumDropdown(
            label = selectedMaturity.label,
            items = MaturityLevel.entries,
            itemLabel = { it.label },
            onSelected = {
                selectedMaturity = it
                result = null
                saveMessage = ""
            }
        )

        // PHYSICAL CONDITION

        Text(
            "Physical condition",
            fontWeight = FontWeight.SemiBold
        )

        EnumDropdown(
            label = selectedCondition.label,
            items = ProduceCondition.entries,
            itemLabel = { it.label },
            onSelected = {
                selectedCondition = it
                result = null
                saveMessage = ""
            }
        )

        // CALCULATE SHELF LIFE

        Button(
            onClick = {

                val days = daysText.toIntOrNull()
                val temperature = temperatureText.toDoubleOrNull()
                val humidity = humidityText.toDoubleOrNull()

                when {

                    days == null || days < 0 -> {
                        result = null
                        errorMessage =
                            "Enter valid days since harvest."
                    }

                    temperature == null ||
                            temperature !in -10.0..60.0 -> {
                        result = null
                        errorMessage =
                            "Temperature must be -10°C to 60°C."
                    }

                    humidity == null ||
                            humidity !in 0.0..100.0 -> {
                        result = null
                        errorMessage =
                            "Humidity must be between 0 and 100%."
                    }

                    else -> {
                        try {
                            result = ShelfLifeCalculator.calculate(
                                crop = selectedCrop,
                                daysSinceHarvest = days,
                                temperatureCelsius = temperature,
                                refrigerated = selectedStorage !=
                                        StorageType.ROOM_TEMPERATURE,
                                humidityPercent = humidity,
                                storageType = selectedStorage,
                                maturity = selectedMaturity,
                                condition = selectedCondition
                            )

                            errorMessage = ""
                            saveMessage = ""

                        } catch (_: IllegalArgumentException) {
                            result = null
                            errorMessage =
                                "Please check your input values."
                        }
                    }
                }
            },
            modifier = Modifier.fillMaxWidth()
        ) {
            Text("Calculate Shelf Life")
        }

        if (errorMessage.isNotBlank()) {
            Text(
                text = errorMessage,
                color = MaterialTheme.colorScheme.error
            )
        }

        // RESULT AND FIREBASE SAVE

        result?.let { estimate ->

            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(
                    containerColor = Color(0xFFF0FDF4)
                )
            ) {
                Column(
                    modifier = Modifier.padding(18.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {

                    Text(
                        "Your Estimate",
                        fontSize = 22.sp,
                        fontWeight = FontWeight.Bold,
                        color = green
                    )

                    Text(
                        "${estimate.remainingDays} days",
                        fontSize = 32.sp,
                        fontWeight = FontWeight.Bold,
                        color = green
                    )

                    Text(
                        "Estimated total shelf life: " +
                                "${estimate.estimatedTotalDays} days"
                    )

                    Text(
                        "Spoilage risk: ${estimate.risk.name}",
                        fontWeight = FontWeight.Bold,
                        color = when (estimate.risk) {
                            SpoilageRisk.LOW -> Color(0xFF15803D)
                            SpoilageRisk.MEDIUM -> Color(0xFFB45309)
                            SpoilageRisk.HIGH -> Color(0xFFB91C1C)
                        }
                    )

                    Text(estimate.recommendation)
                }
            }

            // SMART RECOMMENDATIONS

            SmartRecommendationCard(result = estimate)

            // SAVE COMPLETE RESULT TO FIREBASE

            Button(
                onClick = {

                    val days = daysText.toIntOrNull()
                    val temperature = temperatureText.toDoubleOrNull()
                    val humidity = humidityText.toDoubleOrNull()

                    when {

                        days == null || days < 0 -> {
                            saveMessage =
                                "Please enter valid days since harvest."
                        }

                        temperature == null ||
                                temperature !in -10.0..60.0 -> {
                            saveMessage = "Please enter valid temperature."
                        }

                        humidity == null ||
                                humidity !in 0.0..100.0 -> {
                            saveMessage = "Please enter valid humidity."
                        }

                        else -> {

                            isSaving = true
                            saveMessage = ""

                            ShelfLifeRepository.saveResult(
                                crop = selectedCrop,
                                daysSinceHarvest = days,
                                temperatureCelsius = temperature,
                                refrigerated = selectedStorage !=
                                        StorageType.ROOM_TEMPERATURE,
                                humidityPercent = humidity,
                                storageType = selectedStorage,
                                maturity = selectedMaturity,
                                condition = selectedCondition,
                                result = estimate,

                                onSuccess = {
                                    isSaving = false
                                    saveMessage =
                                        "Result saved successfully to Firebase!"
                                },

                                onError = { message ->
                                    isSaving = false
                                    saveMessage = "Save failed: $message"
                                }
                            )
                        }
                    }
                },
                enabled = !isSaving,
                modifier = Modifier.fillMaxWidth()
            ) {

                if (isSaving) {
                    CircularProgressIndicator(
                        modifier = Modifier.size(18.dp),
                        strokeWidth = 2.dp
                    )
                    Spacer(Modifier.width(8.dp))
                    Text("Saving...")
                } else {
                    Text("Save Result to Firebase")
                }
            }

            if (saveMessage.isNotBlank()) {
                Text(
                    text = saveMessage,
                    color = if (
                        saveMessage.startsWith("Result saved")
                    ) {
                        green
                    } else {
                        MaterialTheme.colorScheme.error
                    }
                )
            }
        }

        Text(
            "Important: This is a preliminary rule-based estimate, " +
                    "not a validated AI prediction. Outdoor weather is " +
                    "not the same as storage temperature. Inspect produce " +
                    "before selling or consuming.",
            fontSize = 12.sp,
            color = Color.Gray
        )
    }
}

// REUSABLE ENUM DROPDOWN

@Composable
private fun <T> EnumDropdown(
    label: String,
    items: List<T>,
    itemLabel: (T) -> String,
    onSelected: (T) -> Unit
) {
    var expanded by remember {
        mutableStateOf(false)
    }

    Column {

        OutlinedButton(
            onClick = { expanded = true },
            modifier = Modifier.fillMaxWidth()
        ) {
            Text("$label  ▼")
        }

        DropdownMenu(
            expanded = expanded,
            onDismissRequest = { expanded = false }
        ) {
            items.forEach { item ->

                DropdownMenuItem(
                    text = { Text(itemLabel(item)) },
                    onClick = {
                        onSelected(item)
                        expanded = false
                    }
                )
            }
        }
    }
}

// LOCATION + WEATHER RETRIEVAL

private fun fetchWeather(
    context: Context,
    fusedLocationClient: FusedLocationProviderClient,
    onLoading: (Boolean) -> Unit,
    onSuccess: (Double, Double) -> Unit,
    onError: (String) -> Unit
) {

    // Check location permission before accessing location.

    val fineGranted = ContextCompat.checkSelfPermission(
        context,
        Manifest.permission.ACCESS_FINE_LOCATION
    ) == PackageManager.PERMISSION_GRANTED

    val coarseGranted = ContextCompat.checkSelfPermission(
        context,
        Manifest.permission.ACCESS_COARSE_LOCATION
    ) == PackageManager.PERMISSION_GRANTED

    if (!fineGranted && !coarseGranted) {
        onLoading(false)
        onError("Please allow location permission.")
        return
    }

    onLoading(true)

    try {

        fusedLocationClient.getCurrentLocation(
            Priority.PRIORITY_BALANCED_POWER_ACCURACY,
            null
        ).addOnSuccessListener { location: Location? ->

            if (location == null) {
                onLoading(false)
                onError(
                    "Could not get your location. Turn on phone Location and try again."
                )
                return@addOnSuccessListener
            }

            Thread {

                try {

                    val url = URL(
                        "https://api.open-meteo.com/v1/forecast" +
                                "?latitude=${location.latitude}" +
                                "&longitude=${location.longitude}" +
                                "&current=temperature_2m,relative_humidity_2m"
                    )

                    val connection =
                        url.openConnection() as HttpURLConnection

                    try {
                        connection.connectTimeout = 15000
                        connection.readTimeout = 15000
                        connection.requestMethod = "GET"

                        val response = connection.inputStream
                            .bufferedReader()
                            .use { it.readText() }

                        val json = JSONObject(response)
                        val current = json.getJSONObject("current")

                        val temperature =
                            current.getDouble("temperature_2m")

                        val humidity =
                            current.getDouble("relative_humidity_2m")

                        Handler(Looper.getMainLooper()).post {
                            onLoading(false)
                            onSuccess(temperature, humidity)
                        }

                    } finally {
                        connection.disconnect()
                    }

                } catch (_: Exception) {

                    Handler(Looper.getMainLooper()).post {
                        onLoading(false)
                        onError(
                            "Weather unavailable. Check your internet and try again."
                        )
                    }
                }

            }.start()

        }.addOnFailureListener {
            onLoading(false)
            onError("Unable to get location. Please try again.")
        }

    } catch (_: SecurityException) {
        onLoading(false)
        onError("Please allow location permission.")
    }
}

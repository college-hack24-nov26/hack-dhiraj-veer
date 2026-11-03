
package com.example.pikrakshak

import android.graphics.BitmapFactory
import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

@Composable
fun DiseaseDetectionScreen(
    onBack: () -> Unit
) {
    val context = LocalContext.current

    var selectedImageUri by remember {
        mutableStateOf<Uri?>(null)
    }

    var selectedBitmap by remember {
        mutableStateOf<android.graphics.Bitmap?>(null)
    }

    var message by remember {
        mutableStateOf("Select a crop photo to get started.")
    }

    var isLoading by remember {
        mutableStateOf(false)
    }

    val imagePicker = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent()
    ) { uri ->
        selectedImageUri = uri
        selectedBitmap = null

        if (uri != null) {
            isLoading = true
            message = "Loading selected image..."
        } else {
            message = "No image selected."
        }
    }

    LaunchedEffect(selectedImageUri) {
        val uri = selectedImageUri

        if (uri != null) {
            isLoading = true

            selectedBitmap = withContext(Dispatchers.IO) {
                try {
                    context.contentResolver.openInputStream(uri)?.use {
                        BitmapFactory.decodeStream(it)
                    }
                } catch (exception: Exception) {
                    null
                }
            }

            isLoading = false

            message = if (selectedBitmap != null) {
                "Image loaded successfully."
            } else {
                "Could not load this image. Please select another photo."
            }
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFFF0FDF4))
            .verticalScroll(rememberScrollState())
            .padding(20.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        TextButton(onClick = onBack) {
            Text("← Back")
        }

        Text(
            "Crop Disease Detection 📷",
            style = MaterialTheme.typography.headlineSmall,
            color = Color(0xFF14532D)
        )

        Text(
            "Select a clear photograph of a crop leaf or affected plant.",
            color = Color.DarkGray
        )

        Card(
            modifier = Modifier.fillMaxWidth(),
            colors = CardDefaults.cardColors(
                containerColor = Color.White
            )
        ) {
            Column(
                modifier = Modifier.padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                if (selectedBitmap != null) {
                    Image(
                        bitmap = selectedBitmap!!.asImageBitmap(),
                        contentDescription = "Selected crop photograph",
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(260.dp),
                        contentScale = ContentScale.Fit
                    )
                } else {
                    Text(
                        "🌿",
                        style = MaterialTheme.typography.displayLarge
                    )

                    Text("Your selected crop photo will appear here.")
                }

                Button(
                    onClick = {
                        imagePicker.launch("image/*")
                    },
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text("Select Crop Photo")
                }
            }
        }

        if (isLoading) {
            Text("Please wait while the image loads...")
        }

        Text(
            message,
            color = Color(0xFF14532D)
        )

        Card(
            modifier = Modifier.fillMaxWidth(),
            colors = CardDefaults.cardColors(
                containerColor = Color.White
            )
        ) {
            Column(
                modifier = Modifier.padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Text(
                    "Disease Analysis",
                    style = MaterialTheme.typography.titleLarge,
                    color = Color(0xFF14532D)
                )

                Text(
                    "HA PART PENDING AHE AI MODEL CONNECT KELELA NAHII...!!"
                )

                Text(
                    "After model integration, this section can display the " +
                            "detected disease, confidence score, and suggested treatment."
                )
            }
        }

        Text(
            "Important: This version does not diagnose plant diseases. " +
                    "Use expert agricultural advice before applying any treatment.",
            style = MaterialTheme.typography.bodySmall,
            color = Color.DarkGray
        )
    }
}


package com.example.pikrakshak

import android.Manifest
import android.content.pm.PackageManager
import android.graphics.BitmapFactory
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.camera.core.CameraSelector
import androidx.camera.core.ImageCapture
import androidx.camera.core.ImageCaptureException
import androidx.camera.core.Preview
import androidx.camera.lifecycle.ProcessCameraProvider
import androidx.camera.view.PreviewView
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalLifecycleOwner
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.core.content.ContextCompat
import java.io.File

@Composable
fun ProduceCameraScreen(
    onBack: () -> Unit = {},
    onContinue: (String) -> Unit = {}
) {
    val context = LocalContext.current
    val lifecycleOwner = LocalLifecycleOwner.current

    var hasPermission by remember {
        mutableStateOf(
            ContextCompat.checkSelfPermission(
                context,
                Manifest.permission.CAMERA
            ) == PackageManager.PERMISSION_GRANTED
        )
    }

    var imageCapture by remember {
        mutableStateOf<ImageCapture?>(null)
    }

    var photoFile by remember {
        mutableStateOf<File?>(null)
    }

    var message by remember {
        mutableStateOf("")
    }

    var cameraReady by remember {
        mutableStateOf(false)
    }

    var checkingPhoto by remember {
        mutableStateOf(false)
    }

    var photoCheckMessage by remember {
        mutableStateOf("")
    }

    val permissionLauncher =
        rememberLauncherForActivityResult(
            contract = ActivityResultContracts.RequestPermission()
        ) { granted ->
            hasPermission = granted

            if (!granted) {
                message =
                    "Camera permission is required to take a photo."
            } else {
                message = ""
            }
        }

    LaunchedEffect(Unit) {
        if (!hasPermission) {
            permissionLauncher.launch(Manifest.permission.CAMERA)
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        OutlinedButton(onClick = onBack) {
            Text("← Back")
        }

        Text(
            text = "Camera Inspection",
            style = MaterialTheme.typography.headlineMedium
        )

        Text(
            "Take a clear photo of one harvested fruit or vegetable."
        )

        if (photoFile != null && photoFile!!.exists()) {
            val currentPhoto = photoFile!!

            val bitmap = remember(currentPhoto.absolutePath) {
                BitmapFactory.decodeFile(currentPhoto.absolutePath)
            }

            if (bitmap != null) {
                Image(
                    bitmap = bitmap.asImageBitmap(),
                    contentDescription = "Captured produce photo",
                    modifier = Modifier
                        .fillMaxWidth()
                        .weight(1f)
                        .clip(RoundedCornerShape(18.dp)),
                    contentScale = ContentScale.Fit
                )
            } else {
                Text("Photo captured, but preview could not be loaded.")
            }

            Card(
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(
                    modifier = Modifier.padding(12.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Text("Photo captured successfully!")

                    Text(
                        "Tap Continue to check whether the photo " +
                                "contains a supported fruit or vegetable."
                    )

                    Button(
                        onClick = {
                            if (!checkingPhoto) {
                                checkingPhoto = true
                                photoCheckMessage =
                                    "Checking photo... Please wait."
                                message = ""

                                detectProduceFromPhoto(
                                    context = context,
                                    photoPath = currentPhoto.absolutePath
                                ) { detection ->
                                    checkingPhoto = false
                                    photoCheckMessage = detection.message

                                    if (detection.crop != null) {
                                        // A supported crop was recognized.
                                        onContinue(currentPhoto.absolutePath)
                                    } else {
                                        // Keep the user on this screen.
                                        // Do not open the estimator.
                                        photoCheckMessage =
                                            detection.message +
                                                    "\nPlease capture a supported " +
                                                    "fruit or vegetable photo."
                                    }
                                }
                            }
                        },
                        modifier = Modifier.fillMaxWidth(),
                        enabled = !checkingPhoto
                    ) {
                        Text(
                            if (checkingPhoto) {
                                "Checking Photo..."
                            } else {
                                "Continue to Estimator →"
                            }
                        )
                    }

                    if (photoCheckMessage.isNotBlank()) {
                        Text(
                            text = photoCheckMessage,
                            color = when {
                                checkingPhoto -> Color.Gray
                                photoCheckMessage.startsWith(
                                    "Crop identified:"
                                ) -> Color(0xFF15803D)
                                else -> Color(0xFFB91C1C)
                            }
                        )
                    }

                    OutlinedButton(
                        onClick = {
                            photoFile = null
                            message = ""
                            photoCheckMessage = ""
                            checkingPhoto = false
                        },
                        modifier = Modifier.fillMaxWidth(),
                        enabled = !checkingPhoto
                    ) {
                        Text("Retake Photo")
                    }
                }
            }
        } else if (hasPermission) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f),
                contentAlignment = Alignment.Center
            ) {
                AndroidView(
                    modifier = Modifier.fillMaxSize(),
                    factory = { viewContext ->
                        PreviewView(viewContext).apply {
                            scaleType =
                                PreviewView.ScaleType.FILL_CENTER

                            val cameraProviderFuture =
                                ProcessCameraProvider.getInstance(viewContext)

                            cameraProviderFuture.addListener(
                                {
                                    try {
                                        val cameraProvider =
                                            cameraProviderFuture.get()

                                        val preview = Preview.Builder()
                                            .build()
                                            .also {
                                                it.surfaceProvider =
                                                    surfaceProvider
                                            }

                                        val capture = ImageCapture.Builder()
                                            .setCaptureMode(
                                                ImageCapture
                                                    .CAPTURE_MODE_MINIMIZE_LATENCY
                                            )
                                            .build()

                                        cameraProvider.unbindAll()

                                        cameraProvider.bindToLifecycle(
                                            lifecycleOwner,
                                            CameraSelector.DEFAULT_BACK_CAMERA,
                                            preview,
                                            capture
                                        )

                                        imageCapture = capture
                                        cameraReady = true
                                        message = ""
                                    } catch (e: Exception) {
                                        message =
                                            "Could not start camera: " +
                                                    (e.message ?: "Unknown error")
                                    }
                                },
                                ContextCompat.getMainExecutor(viewContext)
                            )
                        }
                    }
                )
            }

            Button(
                onClick = {
                    val capture = imageCapture

                    if (capture == null || !cameraReady) {
                        message =
                            "Camera is still starting. Please wait."
                    } else {
                        val outputFile = File(
                            context.cacheDir,
                            "pikrakshak_${System.currentTimeMillis()}.jpg"
                        )

                        val outputOptions =
                            ImageCapture.OutputFileOptions.Builder(
                                outputFile
                            ).build()

                        capture.takePicture(
                            outputOptions,
                            ContextCompat.getMainExecutor(context),
                            object : ImageCapture.OnImageSavedCallback {
                                override fun onImageSaved(
                                    output: ImageCapture.OutputFileResults
                                ) {
                                    photoFile = outputFile
                                    message = ""
                                    photoCheckMessage = ""
                                }

                                override fun onError(
                                    exception: ImageCaptureException
                                ) {
                                    message =
                                        "Photo failed: " +
                                                (exception.message
                                                    ?: "Please try again.")
                                }
                            }
                        )
                    }
                },
                modifier = Modifier.fillMaxWidth(),
                enabled = cameraReady
            ) {
                Text("📸 Capture Produce Photo")
            }
        } else {
            Text("Allow camera permission to use this feature.")

            Button(
                onClick = {
                    permissionLauncher.launch(Manifest.permission.CAMERA)
                },
                modifier = Modifier.fillMaxWidth()
            ) {
                Text("Grant Camera Permission")
            }
        }

        if (message.isNotBlank()) {
            Text(
                text = message,
                color = MaterialTheme.colorScheme.error
            )
        }
    }

    DisposableEffect(lifecycleOwner) {
        onDispose {
            imageCapture = null
            cameraReady = false

            val providerFuture =
                ProcessCameraProvider.getInstance(context)

            providerFuture.addListener(
                {
                    try {
                        providerFuture.get().unbindAll()
                    } catch (_: Exception) {
                        // The camera may already have been released.
                    }
                },
                ContextCompat.getMainExecutor(context)
            )
        }
    }
}

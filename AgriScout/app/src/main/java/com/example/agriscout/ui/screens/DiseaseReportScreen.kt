package com.example.agriscout.ui.screens

import android.Manifest
import android.content.Context
import android.util.Log
import android.view.ViewGroup
import androidx.camera.core.*
import androidx.camera.lifecycle.ProcessCameraProvider
import androidx.camera.view.PreviewView
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalLifecycleOwner
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.core.content.ContextCompat
import coil.compose.AsyncImage
import com.example.agriscout.ui.viewmodel.AgriViewModel
import com.google.accompanist.permissions.ExperimentalPermissionsApi
import com.google.accompanist.permissions.isGranted
import com.google.accompanist.permissions.rememberMultiplePermissionsState
import com.google.android.gms.location.LocationServices
import com.google.common.util.concurrent.ListenableFuture
import java.io.File
import java.text.SimpleDateFormat
import java.util.*

@OptIn(ExperimentalPermissionsApi::class, ExperimentalMaterial3Api::class)
@Composable
fun DiseaseReportScreen(
    farmId: Long,
    viewModel: AgriViewModel,
    onNavigateBack: () -> Unit
) {
    val context = LocalContext.current
    val snackbarHostState = remember { SnackbarHostState() }
    val scope = rememberCoroutineScope()

    // Form state
    var diseaseName by remember { mutableStateOf("") }
    var notes by remember { mutableStateOf("") }
    var imagePath by remember { mutableStateOf("") }
    var latitude by remember { mutableStateOf(0.0) }
    var longitude by remember { mutableStateOf(0.0) }
    var locationText by remember { mutableStateOf("Not captured yet") }
    var isSaved by remember { mutableStateOf(false) }

    // Camera
    var imageCaptureUseCase by remember { mutableStateOf<ImageCapture?>(null) }

    val permissionsState = rememberMultiplePermissionsState(
        permissions = listOf(
            Manifest.permission.CAMERA,
            Manifest.permission.ACCESS_FINE_LOCATION
        )
    )

    LaunchedEffect(Unit) {
        if (!permissionsState.allPermissionsGranted) {
            permissionsState.launchMultiplePermissionRequest()
        }
    }

    Scaffold(
        snackbarHost = { SnackbarHost(snackbarHostState) },
        topBar = {
            TopAppBar(
                title = { Text("Disease Report — Farm #$farmId") },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Back")
                    }
                }
            )
        }
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .verticalScroll(rememberScrollState())
        ) {
            // Camera preview or captured image
            if (permissionsState.permissions[0].status.isGranted) {
                Box(modifier = Modifier
                    .fillMaxWidth()
                    .height(280.dp)) {
                    
                    if (imagePath.isEmpty()) {
                        CameraPreviewWithCapture(
                            modifier = Modifier.fillMaxSize(),
                            onImageCaptureReady = { imageCaptureUseCase = it }
                        )
                    } else {
                        AsyncImage(
                            model = File(imagePath),
                            contentDescription = "Captured Photo",
                            modifier = Modifier.fillMaxSize(),
                            contentScale = ContentScale.Crop
                        )
                    }

                    // Captured indicator
                    if (imagePath.isNotEmpty()) {
                        Surface(
                            modifier = Modifier
                                .align(Alignment.TopEnd)
                                .padding(8.dp),
                            color = Color(0xFF2E7D32),
                            shape = MaterialTheme.shapes.small
                        ) {
                            Text(
                                "✓ Photo captured",
                                modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                                color = Color.White,
                                fontSize = 12.sp
                            )
                        }
                    }
                }
            } else {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(200.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Text("Camera & location permissions required")
                        Spacer(Modifier.height(8.dp))
                        Button(onClick = { permissionsState.launchMultiplePermissionRequest() }) {
                            Text("Grant Permissions")
                        }
                    }
                }
            }

            // Form fields
            Column(
                modifier = Modifier.padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Text("Report Details", fontWeight = FontWeight.Bold, fontSize = 18.sp)

                OutlinedTextField(
                    value = diseaseName,
                    onValueChange = { diseaseName = it },
                    label = { Text("Disease / Issue Name") },
                    placeholder = { Text("e.g. Leaf Blight, Fungal Infection") },
                    modifier = Modifier.fillMaxWidth()
                )

                OutlinedTextField(
                    value = notes,
                    onValueChange = { notes = it },
                    label = { Text("Field Notes") },
                    placeholder = { Text("Describe symptoms, affected area, severity...") },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(120.dp),
                    maxLines = 5
                )

                // GPS section
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(
                        containerColor = MaterialTheme.colorScheme.surfaceVariant
                    )
                ) {
                    Column(modifier = Modifier.padding(12.dp)) {
                        Text("GPS Location", fontWeight = FontWeight.SemiBold)
                        Spacer(Modifier.height(4.dp))
                        Text(locationText, fontSize = 13.sp,
                            color = MaterialTheme.colorScheme.onSurfaceVariant)
                        Spacer(Modifier.height(8.dp))
                        Button(
                            onClick = {
                                getLocation(context) { lat, lon ->
                                    latitude = lat
                                    longitude = lon
                                    locationText = "Lat: %.5f, Lon: %.5f".format(lat, lon)
                                }
                            },
                            enabled = permissionsState.permissions[1].status.isGranted
                        ) {
                            Text("Get GPS Location")
                        }
                    }
                }

                // Capture photo button
                Button(
                    onClick = {
                        if (imagePath.isEmpty()) {
                            capturePhoto(context, imageCaptureUseCase) { path ->
                                imagePath = path
                            }
                        } else {
                            imagePath = "" // Allow retake
                        }
                    },
                    modifier = Modifier.fillMaxWidth(),
                    enabled = permissionsState.permissions[0].status.isGranted
                ) {
                    Text(if (imagePath.isEmpty()) "📷 Capture Photo" else "🔄 Retake Photo")
                }

                Spacer(modifier = Modifier.height(8.dp))

                // Save button
                Button(
                    onClick = {
                        if (diseaseName.isBlank()) {
                            return@Button
                        }
                        viewModel.insertReport(
                            farmId = farmId,
                            diseaseName = diseaseName,
                            notes = notes,
                            imagePath = imagePath,
                            lat = latitude,
                            lon = longitude
                        )
                        isSaved = true
                        onNavigateBack()
                    },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(50.dp),
                    enabled = diseaseName.isNotBlank() && !isSaved,
                    colors = ButtonDefaults.buttonColors(
                        containerColor = MaterialTheme.colorScheme.primary
                    )
                ) {
                    Text("Save Report", fontSize = 16.sp)
                }
            }
        }
    }
}

@Composable
fun CameraPreviewWithCapture(
    modifier: Modifier = Modifier,
    onImageCaptureReady: (ImageCapture) -> Unit
) {
    val context = LocalContext.current
    val lifecycleOwner = LocalLifecycleOwner.current
    val cameraProviderFuture: ListenableFuture<ProcessCameraProvider> = remember(context) {
        ProcessCameraProvider.getInstance(context)
    }

    AndroidView(
        factory = { ctx ->
            val previewView = PreviewView(ctx).apply {
                layoutParams = ViewGroup.LayoutParams(
                    ViewGroup.LayoutParams.MATCH_PARENT,
                    ViewGroup.LayoutParams.MATCH_PARENT
                )
            }

            cameraProviderFuture.addListener({
                val cameraProvider = cameraProviderFuture.get()

                val preview = Preview.Builder().build().also {
                    it.setSurfaceProvider(previewView.surfaceProvider)
                }

                val imageCapture = ImageCapture.Builder()
                    .setCaptureMode(ImageCapture.CAPTURE_MODE_MINIMIZE_LATENCY)
                    .build()

                onImageCaptureReady(imageCapture)

                try {
                    cameraProvider.unbindAll()
                    cameraProvider.bindToLifecycle(
                        lifecycleOwner,
                        CameraSelector.DEFAULT_BACK_CAMERA,
                        preview,
                        imageCapture
                    )
                } catch (e: Exception) {
                    Log.e("CameraPreview", "Binding failed", e)
                }
            }, ContextCompat.getMainExecutor(ctx))

            previewView
        },
        modifier = modifier
    )
}

fun capturePhoto(context: Context, imageCapture: ImageCapture?, onCaptured: (String) -> Unit) {
    imageCapture ?: return

    val photoFile = File(
        context.filesDir,
        SimpleDateFormat("yyyyMMdd_HHmmss", Locale.US).format(Date()) + ".jpg"
    )

    val outputOptions = ImageCapture.OutputFileOptions.Builder(photoFile).build()

    imageCapture.takePicture(
        outputOptions,
        ContextCompat.getMainExecutor(context),
        object : ImageCapture.OnImageSavedCallback {
            override fun onImageSaved(output: ImageCapture.OutputFileResults) {
                onCaptured(photoFile.absolutePath)
                Log.d("Camera", "Photo saved: ${photoFile.absolutePath}")
            }
            override fun onError(exception: ImageCaptureException) {
                Log.e("Camera", "Capture failed: ${exception.message}")
            }
        }
    )
}

fun getLocation(context: Context, onResult: (Double, Double) -> Unit) {
    try {
        val fusedClient = LocationServices.getFusedLocationProviderClient(context)
        fusedClient.lastLocation.addOnSuccessListener { location ->
            if (location != null) {
                onResult(location.latitude, location.longitude)
            }
        }
    } catch (e: SecurityException) {
        Log.e("GPS", "Location permission not granted: ${e.message}")
    }
}

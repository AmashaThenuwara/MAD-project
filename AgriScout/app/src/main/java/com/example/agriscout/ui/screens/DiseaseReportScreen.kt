package com.example.agriscout.ui.screens

import android.Manifest
import android.content.Context
import android.content.Intent
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
import androidx.compose.material.icons.filled.MyLocation
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
import com.example.agriscout.data.local.entity.CropEntity
import com.example.agriscout.data.local.entity.FarmEntity
import com.example.agriscout.ui.viewmodel.AgriViewModel
import com.example.agriscout.util.PdfGenerator
import com.example.agriscout.ui.components.AnimatedButton
import com.example.agriscout.ui.components.ShinyCard
import com.google.accompanist.permissions.ExperimentalPermissionsApi
import com.google.accompanist.permissions.isGranted
import com.google.accompanist.permissions.rememberMultiplePermissionsState
import com.google.android.gms.location.LocationServices
import com.google.android.gms.location.Priority
import com.google.common.util.concurrent.ListenableFuture
import java.io.File
import java.text.SimpleDateFormat
import java.util.*

@OptIn(ExperimentalPermissionsApi::class, ExperimentalMaterial3Api::class)
@Composable
fun DiseaseReportScreen(
    farmId: Long = 0L,
    viewModel: AgriViewModel,
    onNavigateBack: () -> Unit
) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()

    // Data
    val farmsList by viewModel.farmsList.collectAsState()

    // Selection State
    var selectedFarm by remember { mutableStateOf<FarmEntity?>(null) }
    var selectedCrop by remember { mutableStateOf<CropEntity?>(null) }
    var cropsForSelectedFarm by remember { mutableStateOf<List<CropEntity>>(emptyList()) }

    // Pre-select farm if farmId is passed from navigation
    LaunchedEffect(farmsList, farmId) {
        if (farmId != 0L && selectedFarm == null) {
            selectedFarm = farmsList.find { it.farmId == farmId }
        }
    }

    // Update crops when farm changes
    LaunchedEffect(selectedFarm?.farmId) {
        if (selectedFarm != null) {
            viewModel.getCropsForFarm(selectedFarm!!.farmId).collect { list ->
                cropsForSelectedFarm = list
                selectedCrop = null // Reset crop selection
            }
        } else {
            cropsForSelectedFarm = emptyList()
        }
    }

    // Form state
    var diseaseName by remember { mutableStateOf("") }
    var notes by remember { mutableStateOf("") }
    var imagePath by remember { mutableStateOf("") }
    var latitude by remember { mutableStateOf(0.0) }
    var longitude by remember { mutableStateOf(0.0) }
    var isLocating by remember { mutableStateOf(false) }

    var imageCaptureUseCase by remember { mutableStateOf<ImageCapture?>(null) }

    val permissionsState = rememberMultiplePermissionsState(
        permissions = listOf(
            Manifest.permission.CAMERA,
            Manifest.permission.ACCESS_FINE_LOCATION,
            Manifest.permission.ACCESS_COARSE_LOCATION
        )
    )

    LaunchedEffect(Unit) {
        if (!permissionsState.allPermissionsGranted) {
            permissionsState.launchMultiplePermissionRequest()
        }
    }

    // Dropdown Expansions
    var cropExpanded by remember { mutableStateOf(false) }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Disease Report") },
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
            // 1. Crop Selection
            if (imagePath.isEmpty()) {
                ExposedDropdownMenuBox(
                    expanded = cropExpanded,
                    onExpandedChange = { if (selectedFarm != null) cropExpanded = !cropExpanded },
                    modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp)
                ) {
                    OutlinedTextField(
                        value = selectedCrop?.commonName ?: "Select Crop",
                        onValueChange = {},
                        readOnly = true,
                        label = { Text("Select Crop") },
                        enabled = selectedFarm != null,
                        trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = cropExpanded) },
                        modifier = Modifier.menuAnchor().fillMaxWidth()
                    )
                    ExposedDropdownMenu(expanded = cropExpanded, onDismissRequest = { cropExpanded = false }) {
                        cropsForSelectedFarm.forEach { crop ->
                            DropdownMenuItem(
                                text = { Text(crop.commonName) },
                                onClick = {
                                    selectedCrop = crop
                                    cropExpanded = false
                                }
                            )
                        }
                    }
                }
            } else {
                // Display simple text when image is captured to save space
                Text(
                    text = "Reporting for: ${selectedCrop?.commonName ?: ""}",
                    fontSize = 16.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp)
                )
            }

            // Rest of UI (Camera, Inputs) - Only enabled if crop selected
            if (selectedCrop != null) {
                // Camera Preview
                if (permissionsState.permissions[0].status.isGranted) {
                    Box(modifier = Modifier.fillMaxWidth().height(280.dp)) {
                        if (imagePath.isEmpty()) {
                            CameraPreviewWithCapture(
                                modifier = Modifier.fillMaxSize(),
                                onImageCaptureReady = { imageCaptureUseCase = it }
                            )
                        } else {
                            AsyncImage(
                                model = if (imagePath.startsWith("http")) imagePath else File(imagePath),
                                contentDescription = "Captured Photo",
                                modifier = Modifier.fillMaxSize(),
                                contentScale = ContentScale.Crop
                            )
                        }
                    }
                }

                Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
                    OutlinedTextField(
                        value = diseaseName, onValueChange = { diseaseName = it },
                        label = { Text("Disease Name") }, modifier = Modifier.fillMaxWidth()
                    )
                    OutlinedTextField(
                        value = notes, onValueChange = { notes = it },
                        label = { Text("Notes") }, modifier = Modifier.fillMaxWidth().height(100.dp), maxLines = 4
                    )

                    // GPS & Camera Buttons
                    AnimatedButton(
                        onClick = {
                            if (imagePath.isEmpty()) {
                                capturePhoto(context, imageCaptureUseCase) { path -> imagePath = path }
                            } else {
                                imagePath = ""
                            }
                        },
                        modifier = Modifier.fillMaxWidth()
                    ) { Text(if (imagePath.isEmpty()) "Capture Photo" else "Retake") }

                    // GPS Card Layout matching Farm Register Page
                    ShinyCard(
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Column(modifier = Modifier.padding(16.dp)) {
                            Text("GPS Coordinates", fontWeight = FontWeight.Bold, fontSize = 14.sp)
                            Spacer(modifier = Modifier.height(8.dp))
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Column {
                                    Text("Lat: ${if (latitude != 0.0) "%.6f".format(latitude) else "Not set"}", fontSize = 13.sp)
                                    Text("Lon: ${if (longitude != 0.0) "%.6f".format(longitude) else "Not set"}", fontSize = 13.sp)
                                }
                                AnimatedButton(
                                    onClick = {
                                        isLocating = true
                                        getLocation(context) { lat, lon ->
                                            latitude = lat
                                            longitude = lon
                                            isLocating = false
                                        }
                                    },
                                    enabled = !isLocating
                                ) {
                                    if (isLocating) {
                                        CircularProgressIndicator(modifier = Modifier.size(18.dp), strokeWidth = 2.dp, color = MaterialTheme.colorScheme.onPrimary)
                                    } else {
                                        Icon(Icons.Default.MyLocation, contentDescription = null, modifier = Modifier.size(18.dp))
                                        Spacer(Modifier.width(8.dp))
                                        Text("Capture Location")
                                    }
                                }
                            }
                        }
                    }

                    AnimatedButton(
                        onClick = {
                            // Save Logic
                            if (diseaseName.isNotBlank() && selectedFarm != null && selectedCrop != null) {
                                viewModel.insertReport(
                                    farmId = selectedFarm!!.farmId,
                                    cropId = selectedCrop!!.cropId,
                                    diseaseName = diseaseName,
                                    notes = notes,
                                    imagePath = imagePath,
                                    lat = latitude,
                                    lon = longitude
                                )

                                // Generate PDF
                                val uri = PdfGenerator.createDiseaseReport(
                                    context,
                                    farmName = selectedFarm!!.farmName,
                                    farmerName = selectedFarm!!.farmerName,
                                    locationName = selectedFarm!!.locationName,
                                    cropName = selectedCrop!!.commonName,
                                    diseaseName = diseaseName,
                                    notes = notes,
                                    imagePath = imagePath,
                                    latitude = latitude,
                                    longitude = longitude
                                )
                                if (uri != null) {
                                    val intent = Intent(Intent.ACTION_SEND).apply {
                                        type = "application/pdf"
                                        putExtra(Intent.EXTRA_STREAM, uri)
                                        addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
                                    }
                                    context.startActivity(Intent.createChooser(intent, "Share Disease Report PDF"))
                                }

                                onNavigateBack()
                            }
                        },
                        modifier = Modifier.fillMaxWidth(),
                        enabled = diseaseName.isNotBlank()
                    ) { Text("Save Report & Generate PDF") }
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
    val cameraProviderFuture = remember { ProcessCameraProvider.getInstance(context) }

    AndroidView(
        factory = { ctx ->
            val previewView = PreviewView(ctx).apply {
                layoutParams = ViewGroup.LayoutParams(
                    ViewGroup.LayoutParams.MATCH_PARENT,
                    ViewGroup.LayoutParams.MATCH_PARENT
                )
            }

            val preview = Preview.Builder().build().also {
                it.setSurfaceProvider(previewView.surfaceProvider)
            }

            val imageCapture = ImageCapture.Builder()
                .build()

            try {
                cameraProviderFuture.get().unbindAll()
                cameraProviderFuture.get().bindToLifecycle(
                    lifecycleOwner,
                    CameraSelector.DEFAULT_BACK_CAMERA,
                    preview,
                    imageCapture
                )
                onImageCaptureReady(imageCapture)
            } catch (exc: Exception) {
                Log.e("Camera", "Use case binding failed", exc)
            }

            previewView
        },
        modifier = modifier
    )
}

fun capturePhoto(context: Context, imageCapture: ImageCapture?, onImageSaved: (String) -> Unit) {
    if (imageCapture == null) return

    val photoFile = File(
        context.externalCacheDir,
        SimpleDateFormat("yyyyMMdd-HHmmss", Locale.US).format(System.currentTimeMillis()) + ".jpg"
    )

    val outputOptions = ImageCapture.OutputFileOptions.Builder(photoFile).build()

    imageCapture.takePicture(
        outputOptions,
        ContextCompat.getMainExecutor(context),
        object : ImageCapture.OnImageSavedCallback {
            override fun onImageSaved(outputFileResults: ImageCapture.OutputFileResults) {
                onImageSaved(photoFile.absolutePath)
            }

            override fun onError(exception: ImageCaptureException) {
                Log.e("Camera", "Photo capture failed: ${exception.message}", exception)
            }
        }
    )
}

fun getLocation(context: Context, onLocationCaptured: (Double, Double) -> Unit) {
    val fusedLocationClient = LocationServices.getFusedLocationProviderClient(context)
    try {
        fusedLocationClient.getCurrentLocation(Priority.PRIORITY_HIGH_ACCURACY, null).addOnSuccessListener { location ->
            if (location != null) {
                onLocationCaptured(location.latitude, location.longitude)
            } else {
                fusedLocationClient.lastLocation.addOnSuccessListener { lastLoc ->
                    if (lastLoc != null) {
                        onLocationCaptured(lastLoc.latitude, lastLoc.longitude)
                    } else {
                        android.widget.Toast.makeText(context, "Could not capture location. Ensure GPS is enabled.", android.widget.Toast.LENGTH_SHORT).show()
                        onLocationCaptured(0.0, 0.0) // Reset to stop loading spinner
                    }
                }.addOnFailureListener {
                    android.widget.Toast.makeText(context, "Failed to get location: ${it.message}", android.widget.Toast.LENGTH_SHORT).show()
                    onLocationCaptured(0.0, 0.0)
                }
            }
        }.addOnFailureListener {
            android.widget.Toast.makeText(context, "Failed to capture current location: ${it.message}", android.widget.Toast.LENGTH_SHORT).show()
            onLocationCaptured(0.0, 0.0)
        }
    } catch (e: SecurityException) {
        Log.e("Location", "Permission not granted")
        android.widget.Toast.makeText(context, "Location permission not granted", android.widget.Toast.LENGTH_SHORT).show()
        onLocationCaptured(0.0, 0.0)
    }
}

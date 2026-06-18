package com.example.agriscout.ui.screens

import android.Manifest
import androidx.camera.core.ImageCapture
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import com.example.agriscout.ui.theme.ShinyGradientStart
import com.example.agriscout.ui.theme.ShinyGradientEnd
import com.example.agriscout.ui.viewmodel.AgriViewModel
import kotlinx.coroutines.launch
import com.example.agriscout.ui.components.ShinyCard
import com.example.agriscout.ui.components.AnimatedButton
import com.google.accompanist.permissions.ExperimentalPermissionsApi
import com.google.accompanist.permissions.isGranted
import com.google.accompanist.permissions.rememberMultiplePermissionsState
import java.io.File

@OptIn(ExperimentalMaterial3Api::class, ExperimentalPermissionsApi::class)
@Composable
fun AddEditCropScreen(
    farmId: Long,
    cropId: Long?,
    viewModel: AgriViewModel,
    onNavigateBack: () -> Unit,
    onNavigateToCropDetail: (Long) -> Unit
) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    
    var commonName by remember { mutableStateOf("") }
    var scientificName by remember { mutableStateOf("") }
    var category by remember { mutableStateOf("") }
    var origin by remember { mutableStateOf("") }
    var soilType by remember { mutableStateOf("") }
    var fertilizerType by remember { mutableStateOf("Organic") }
    var imagePath by remember { mutableStateOf("") }

    val categories = listOf("Vegetable", "Fruit", "Grain", "Legume", "Other")
    val soilTypes = listOf("Loamy", "Clay", "Sandy", "Silty", "Peaty", "Chalky")
    
    var isEditing by remember { mutableStateOf(false) }

    // Dropdown state
    var soilDropdownExpanded by remember { mutableStateOf(false) }

    // Camera state
    var imageCaptureUseCase by remember { mutableStateOf<ImageCapture?>(null) }
    val permissionsState = rememberMultiplePermissionsState(
        permissions = listOf(
            Manifest.permission.CAMERA
        )
    )

    LaunchedEffect(Unit) {
        if (!permissionsState.allPermissionsGranted) {
            permissionsState.launchMultiplePermissionRequest()
        }
    }

    LaunchedEffect(cropId) {
        if (cropId != null) {
            isEditing = true
            val crop = viewModel.getCropById(cropId)
            crop?.let {
                commonName = it.commonName
                scientificName = it.scientificName
                category = it.category
                origin = it.origin
                soilType = it.soilType
                fertilizerType = it.fertilizerType
                imagePath = it.imagePath
            }
        }
    }

    val primaryGradient = Brush.horizontalGradient(
        colors = listOf(ShinyGradientStart, ShinyGradientEnd)
    )

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(if (isEditing) "Edit Crop" else "Add New Crop", fontWeight = FontWeight.Bold) },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = MaterialTheme.colorScheme.primaryContainer)
            )
        }
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .background(MaterialTheme.colorScheme.background)
                .verticalScroll(rememberScrollState())
        ) {
            
            Text(
                text = if (isEditing) "Edit Crop Details" else "Add New Crop Details", 
                fontSize = 24.sp, 
                fontWeight = FontWeight.Bold, 
                color = MaterialTheme.colorScheme.primary,
                modifier = Modifier.padding(start = 24.dp, top = 24.dp, bottom = 8.dp)
            )

            ShinyCard(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 24.dp, vertical = 8.dp)
            ) {
                Column(
                    modifier = Modifier.padding(24.dp),
                    verticalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    
                    // Camera / Image Section
                    Text("Crop Image", style = MaterialTheme.typography.labelLarge, color = MaterialTheme.colorScheme.primary)
                    if (permissionsState.permissions.isNotEmpty() && permissionsState.permissions[0].status.isGranted) {
                        Box(modifier = Modifier
                            .fillMaxWidth()
                            .height(200.dp)
                            .shadow(4.dp, RoundedCornerShape(12.dp))
                            .clip(RoundedCornerShape(12.dp))
                        ) {
                            if (imagePath.isEmpty()) {
                                CameraPreviewWithCapture(
                                    modifier = Modifier.fillMaxSize(),
                                    onImageCaptureReady = { imageCaptureUseCase = it }
                                )
                            } else {
                                AsyncImage(
                                    model = File(imagePath),
                                    contentDescription = "Crop Image",
                                    modifier = Modifier.fillMaxSize(),
                                    contentScale = ContentScale.Crop
                                )
                            }
                        }
                        
                        AnimatedButton(
                            onClick = {
                                if (imagePath.isEmpty()) {
                                    capturePhoto(context, imageCaptureUseCase) { path -> imagePath = path }
                                } else {
                                    imagePath = "" // Reset to capture again
                                }
                            },
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Text(if (imagePath.isEmpty()) "Capture Photo" else "Retake Photo")
                        }
                    } else {
                        Text("Camera permission required to capture images.", color = MaterialTheme.colorScheme.error)
                    }

                    HorizontalDivider(modifier = Modifier.padding(vertical = 8.dp))

                    OutlinedTextField(
                        value = commonName,
                        onValueChange = { commonName = it },
                        label = { Text("Common Name") },
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(12.dp)
                    )
                    OutlinedTextField(
                        value = scientificName,
                        onValueChange = { scientificName = it },
                        label = { Text("Scientific Name") },
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(12.dp)
                    )
                    
                    Text("Category", style = MaterialTheme.typography.labelLarge, color = MaterialTheme.colorScheme.primary)
                    Row(
                        modifier = Modifier.fillMaxWidth(), 
                        horizontalArrangement = Arrangement.spacedBy(4.dp),
                    ) {
                        categories.forEach { cat ->
                            FilterChip(
                                selected = category == cat,
                                onClick = { category = cat },
                                label = { Text(cat, fontSize = 10.sp) },
                                shape = RoundedCornerShape(16.dp)
                            )
                        }
                    }

                    OutlinedTextField(
                        value = origin,
                        onValueChange = { origin = it },
                        label = { Text("Origin / Native Region") },
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(12.dp)
                    )
                    
                    // Soil Type Dropdown
                    ExposedDropdownMenuBox(
                        expanded = soilDropdownExpanded,
                        onExpandedChange = { soilDropdownExpanded = !soilDropdownExpanded },
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        OutlinedTextField(
                            value = soilType.ifEmpty { "Select Soil Type" },
                            onValueChange = {},
                            readOnly = true,
                            label = { Text("Soil Type") },
                            trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = soilDropdownExpanded) },
                            modifier = Modifier.menuAnchor().fillMaxWidth(),
                            shape = RoundedCornerShape(12.dp)
                        )
                        ExposedDropdownMenu(
                            expanded = soilDropdownExpanded,
                            onDismissRequest = { soilDropdownExpanded = false }
                        ) {
                            soilTypes.forEach { type ->
                                DropdownMenuItem(
                                    text = { Text(type) },
                                    onClick = {
                                        soilType = type
                                        soilDropdownExpanded = false
                                    }
                                )
                            }
                        }
                    }
                    
                    Text("Fertilizer", style = MaterialTheme.typography.labelLarge, color = MaterialTheme.colorScheme.primary)
                    Row(horizontalArrangement = Arrangement.spacedBy(16.dp)) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            RadioButton(selected = fertilizerType == "Organic", onClick = { fertilizerType = "Organic" })
                            Text("Organic")
                        }
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            RadioButton(selected = fertilizerType == "Inorganic", onClick = { fertilizerType = "Inorganic" })
                            Text("Inorganic")
                        }
                    }

                    Spacer(modifier = Modifier.height(16.dp))

                    AnimatedButton(
                        onClick = {
                            if (commonName.isNotBlank()) {
                                scope.launch {
                                    if (isEditing && cropId != null) {
                                        val crop = viewModel.getCropById(cropId)
                                        crop?.let {
                                            viewModel.updateCrop(
                                                it.copy(
                                                    commonName = commonName,
                                                    scientificName = scientificName,
                                                    category = category,
                                                    origin = origin,
                                                    soilType = soilType,
                                                    fertilizerType = fertilizerType,
                                                    imagePath = imagePath
                                                )
                                            )
                                        }
                                        onNavigateBack()
                                    } else {
                                        val newCropId = viewModel.insertCrop(
                                            farmId = farmId,
                                            commonName = commonName,
                                            scientificName = scientificName,
                                            category = category,
                                            origin = origin,
                                            soilType = soilType,
                                            fertilizerType = fertilizerType,
                                            imagePath = imagePath
                                        )
                                        onNavigateToCropDetail(newCropId)
                                    }
                                }
                            }
                        },
                        modifier = Modifier.fillMaxWidth().height(56.dp),
                        enabled = commonName.isNotBlank()
                    ) {
                        Text(if (isEditing) "Save Changes" else "Save Crop", fontSize = 16.sp)
                    }
                }
            }
            
            Spacer(modifier = Modifier.height(24.dp))
        }
    }
}

package com.example.agriscout.ui.screens

import android.Manifest
import android.content.Context
import android.content.pm.PackageManager
import android.location.Location
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
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
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.content.ContextCompat
import com.example.agriscout.ui.viewmodel.AgriViewModel
import com.google.android.gms.location.LocationServices
import kotlinx.coroutines.launch
import com.example.agriscout.ui.components.ShinyCard
import com.example.agriscout.ui.components.AnimatedButton

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddFarmScreen(
    viewModel: AgriViewModel,
    onNavigateBack: () -> Unit,
    onNavigateToFarmDetails: (Long) -> Unit
) {
    var farmName by remember { mutableStateOf("") }
    var farmerName by remember { mutableStateOf("") }
    var locationName by remember { mutableStateOf("") }
    var landSize by remember { mutableStateOf("") }
    var locationDetails by remember { mutableStateOf("") }
    
    var latitude by remember { mutableStateOf(0.0) }
    var longitude by remember { mutableStateOf(0.0) }
    var isLocating by remember { mutableStateOf(false) }

    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    val fusedLocationClient = remember { LocationServices.getFusedLocationProviderClient(context) }

    val locationPermissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestMultiplePermissions()
    ) { permissions ->
        val granted = permissions.getOrDefault(Manifest.permission.ACCESS_FINE_LOCATION, false) ||
                      permissions.getOrDefault(Manifest.permission.ACCESS_COARSE_LOCATION, false)
        if (granted) {
            getCurrentLocation(context, fusedLocationClient) { lat, lon ->
                latitude = lat
                longitude = lon
                isLocating = false
            }
        } else {
            isLocating = false
            Toast.makeText(context, "Location permission denied", Toast.LENGTH_SHORT).show()
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Register New Farm", fontWeight = FontWeight.Bold) },
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
                .padding(24.dp)
                .verticalScroll(rememberScrollState()),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            Text("General Details", fontSize = 18.sp, fontWeight = FontWeight.Bold)

            OutlinedTextField(
                value = farmName,
                onValueChange = { farmName = it },
                label = { Text("Farm Name") },
                modifier = Modifier.fillMaxWidth()
            )

            OutlinedTextField(
                value = farmerName,
                onValueChange = { farmerName = it },
                label = { Text("Farmer / Manager Name") },
                modifier = Modifier.fillMaxWidth()
            )

            OutlinedTextField(
                value = locationName,
                onValueChange = { locationName = it },
                label = { Text("Regional Location") },
                modifier = Modifier.fillMaxWidth()
            )

            OutlinedTextField(
                value = landSize,
                onValueChange = { landSize = it },
                label = { Text("Size of Land (Hectares)") },
                modifier = Modifier.fillMaxWidth()
            )

            OutlinedTextField(
                value = locationDetails,
                onValueChange = { locationDetails = it },
                label = { Text("Physical Address / Landmark") },
                modifier = Modifier.fillMaxWidth()
            )

            // Location Capture Section
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
                                if (ContextCompat.checkSelfPermission(context, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED) {
                                    getCurrentLocation(context, fusedLocationClient) { lat, lon ->
                                        latitude = lat
                                        longitude = lon
                                        isLocating = false
                                    }
                                } else {
                                    locationPermissionLauncher.launch(
                                        arrayOf(Manifest.permission.ACCESS_FINE_LOCATION, Manifest.permission.ACCESS_COARSE_LOCATION)
                                    )
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

            Spacer(modifier = Modifier.height(16.dp))

            AnimatedButton(
                onClick = {
                    if (farmName.isNotBlank() && farmerName.isNotBlank()) {
                        scope.launch {
                            val farmId = viewModel.insertFarm(
                                name = farmName,
                                farmer = farmerName,
                                location = locationName,
                                landSize = landSize.toDoubleOrNull() ?: 0.0,
                                details = locationDetails,
                                lat = latitude,
                                lon = longitude
                            )
                            // Navigate to farm details to add crops
                            onNavigateToFarmDetails(farmId)
                        }
                    }
                },
                modifier = Modifier.fillMaxWidth().height(50.dp),
                enabled = farmName.isNotBlank() && farmerName.isNotBlank()
            ) {
                Text("Save & Add Crops", fontSize = 16.sp)
            }
        }
    }
}

private fun getCurrentLocation(
    context: Context,
    fusedLocationClient: com.google.android.gms.location.FusedLocationProviderClient,
    onLocationReceived: (Double, Double) -> Unit
) {
    try {
        fusedLocationClient.lastLocation.addOnSuccessListener { location: Location? ->
            if (location != null) {
                onLocationReceived(location.latitude, location.longitude)
            } else {
                Toast.makeText(context, "Unable to get location. Is GPS on?", Toast.LENGTH_LONG).show()
            }
        }
    } catch (e: SecurityException) {
        e.printStackTrace()
    }
}

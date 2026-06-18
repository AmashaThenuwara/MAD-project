package com.example.agriscout.ui.screens

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.agriscout.ui.viewmodel.AgriViewModel
import com.example.agriscout.ui.components.AnimatedButton

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun EditFarmScreen(
    farmId: Long,
    viewModel: AgriViewModel,
    onNavigateBack: () -> Unit
) {
    val farmState by viewModel.getFarmById(farmId).collectAsState(initial = null)

    var farmName by remember { mutableStateOf("") }
    var farmerName by remember { mutableStateOf("") }
    var locationName by remember { mutableStateOf("") }
    var landSize by remember { mutableStateOf("") }
    var locationDetails by remember { mutableStateOf("") }

    LaunchedEffect(farmState) {
        farmState?.let { farm ->
            farmName = farm.farmName
            farmerName = farm.farmerName
            locationName = farm.locationName
            landSize = farm.landSize.toString()
            locationDetails = farm.locationDetails
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Edit Farm Details", fontWeight = FontWeight.Bold) },
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
                label = { Text("Location Details / Coordinates") },
                modifier = Modifier.fillMaxWidth()
            )

            Spacer(modifier = Modifier.height(16.dp))

            AnimatedButton(
                onClick = {
                    val currentFarm = farmState
                    if (currentFarm != null && farmName.isNotBlank() && farmerName.isNotBlank()) {
                        viewModel.updateFarm(
                            currentFarm.copy(
                                farmName = farmName,
                                farmerName = farmerName,
                                locationName = locationName,
                                landSize = landSize.toDoubleOrNull() ?: 0.0,
                                locationDetails = locationDetails,
                                isSynced = false // Reset sync status on edit
                            )
                        )
                        onNavigateBack()
                    }
                },
                modifier = Modifier.fillMaxWidth().height(50.dp),
                enabled = farmName.isNotBlank() && farmerName.isNotBlank()
            ) {
                Text("Update Farm", fontSize = 16.sp)
            }
        }
    }
}

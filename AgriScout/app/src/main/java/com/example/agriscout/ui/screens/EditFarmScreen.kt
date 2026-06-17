package com.example.agriscout.ui.screens

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.agriscout.ui.viewmodel.AgriViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun EditFarmScreen(
    farmId: Long,
    viewModel: AgriViewModel,
    onNavigateBack: () -> Unit
) {
    val farmsList by viewModel.farmsList.collectAsState()
    val farm = farmsList.find { it.farmId == farmId }

    if (farm == null) {
        LaunchedEffect(Unit) {
            onNavigateBack()
        }
        return
    }

    var farmName by remember { mutableStateOf(farm.farmName) }
    var farmerName by remember { mutableStateOf(farm.farmerName) }
    var locationName by remember { mutableStateOf(farm.locationName) }
    var cropType by remember { mutableStateOf(farm.cropType) }
    var growthStage by remember { mutableStateOf(farm.growthStage) }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Edit Farm Details", fontWeight = FontWeight.Bold) },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
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
            Text("Update Details", fontSize = 18.sp, fontWeight = FontWeight.Bold)

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
                label = { Text("Regional Location String") },
                modifier = Modifier.fillMaxWidth()
            )

            HorizontalDivider(modifier = Modifier.padding(vertical = 8.dp))
            Text("Crop Profiling", fontSize = 18.sp, fontWeight = FontWeight.Bold)

            OutlinedTextField(
                value = cropType,
                onValueChange = { cropType = it },
                label = { Text("Crop Variant") },
                modifier = Modifier.fillMaxWidth()
            )

            OutlinedTextField(
                value = growthStage,
                onValueChange = { growthStage = it },
                label = { Text("Current Growth Phase") },
                modifier = Modifier.fillMaxWidth()
            )

            Spacer(modifier = Modifier.height(16.dp))

            Button(
                onClick = {
                    if (farmName.isNotBlank() && farmerName.isNotBlank()) {
                        viewModel.updateFarm(
                            farm.copy(
                                farmName = farmName,
                                farmerName = farmerName,
                                locationName = locationName,
                                cropType = cropType,
                                growthStage = growthStage,
                                isSynced = false
                            )
                        )
                        onNavigateBack()
                    }
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(50.dp),
                enabled = farmName.isNotBlank() && farmerName.isNotBlank()
            ) {
                Text("Update Farm Info", fontSize = 16.sp)
            }
        }
    }
}

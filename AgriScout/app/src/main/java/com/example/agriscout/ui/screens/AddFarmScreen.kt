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

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddFarmScreen(
    viewModel: AgriViewModel,
    onNavigateBack: () -> Unit
) {
    var farmName by remember { mutableStateOf("") }
    var farmerName by remember { mutableStateOf("") }
    var locationName by remember { mutableStateOf("") }
    var cropType by remember { mutableStateOf("") }
    var growthStage by remember { mutableStateOf("") }

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
                label = { Text("Regional Location String") },
                modifier = Modifier.fillMaxWidth()
            )

            Divider(modifier = Modifier.padding(vertical = 8.dp))
            Text("Initial Crop Profiling", fontSize = 18.sp, fontWeight = FontWeight.Bold)

            OutlinedTextField(
                value = cropType,
                onValueChange = { cropType = it },
                label = { Text("Crop Variant (e.g., Rice, Maize)") },
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
                        viewModel.insertFarm(
                            name = farmName,
                            farmer = farmerName,
                            location = locationName,
                            cropType = cropType,
                            stage = growthStage
                        )
                        onNavigateBack() // Pop back to main dashboard list view
                    }
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(50.dp),
                enabled = farmName.isNotBlank() && farmerName.isNotBlank()
            ) {
                Text("Save to Local Device Database", fontSize = 16.sp)
            }
        }
    }
}
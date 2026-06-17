package com.example.agriscout.ui.screens

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.material.icons.filled.*
import com.example.agriscout.data.local.entity.FarmEntity
import com.example.agriscout.ui.viewmodel.AgriViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun FarmListScreen(
    viewModel: AgriViewModel,
    onNavigateToAddFarm: () -> Unit,
    onNavigateToEditFarm: (Long) -> Unit,
    onNavigateToReport: (Long) -> Unit,
    onNavigateToWeather: () -> Unit
) {
    // Collect active farms from database
    val farmsList by viewModel.farmsList.collectAsState()
    var farmToDelete by remember { mutableStateOf<FarmEntity?>(null) }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Active Farms", fontWeight = FontWeight.Bold) },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = MaterialTheme.colorScheme.primaryContainer),
                actions = {
                    IconButton(onClick = onNavigateToWeather) {
                        Icon(imageVector = Icons.Default.Cloud, contentDescription = "Weather")
                    }
                }
            )
        },
        floatingActionButton = {
            FloatingActionButton(onClick = onNavigateToAddFarm, containerColor = MaterialTheme.colorScheme.primaryContainer) {
                Icon(Icons.Default.Add, contentDescription = "Add Farm")
            }
        }
    ) { innerPadding ->
        if (farmsList.isEmpty()) {
            Box(modifier = Modifier.fillMaxSize().padding(innerPadding), contentAlignment = Alignment.Center) {
                Text("No farms tracked yet. Tap '+' to begin.", color = MaterialTheme.colorScheme.onSurfaceVariant)
            }
        } else {
            LazyColumn(
                modifier = Modifier.fillMaxSize().padding(innerPadding).padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                items(farmsList) { farm ->
                    FarmCard(
                        farm = farm,
                        onEditClick = { onNavigateToEditFarm(farm.farmId) },
                        onDeleteClick = { farmToDelete = farm },
                        onReportClick = { onNavigateToReport(farm.farmId) }
                    )
                }
            }
        }

        // Deletion confirmation dialog
        farmToDelete?.let { farm ->
            AlertDialog(
                onDismissRequest = { farmToDelete = null },
                title = { Text("Delete Farm") },
                text = { Text("Are you sure you want to delete '${farm.farmName}'?") },
                confirmButton = {
                    TextButton(onClick = { viewModel.deleteFarm(farm); farmToDelete = null }, colors = ButtonDefaults.textButtonColors(contentColor = MaterialTheme.colorScheme.error)) {
                        Text("Delete")
                    }
                },
                dismissButton = {
                    TextButton(onClick = { farmToDelete = null }) { Text("Cancel") }
                }
            )
        }
    }
}

@Composable
fun FarmCard(
    farm: FarmEntity,
    onEditClick: () -> Unit,
    onDeleteClick: () -> Unit,
    onReportClick: () -> Unit
) {
    Card(modifier = Modifier.fillMaxWidth(), elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)) {
        Column(modifier = Modifier.fillMaxWidth().padding(16.dp)) {
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                Text(text = farm.farmName, fontSize = 20.sp, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.primary)
                Row {
                    IconButton(onClick = onEditClick) { Icon(Icons.Default.Edit, contentDescription = "Edit", tint = MaterialTheme.colorScheme.primary) }
                    IconButton(onClick = onDeleteClick) { Icon(Icons.Default.Delete, contentDescription = "Delete", tint = MaterialTheme.colorScheme.error) }
                }
            }
            Text(text = "Manager: ${farm.farmerName}", fontSize = 14.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(Icons.Default.LocationOn, contentDescription = null, modifier = Modifier.size(16.dp))
                Spacer(modifier = Modifier.width(4.dp))
                Text(text = farm.locationName, fontSize = 14.sp)
            }
            Button(onClick = onReportClick, modifier = Modifier.align(Alignment.End)) {
                Text("Log Disease Report")
            }
        }
    }
}

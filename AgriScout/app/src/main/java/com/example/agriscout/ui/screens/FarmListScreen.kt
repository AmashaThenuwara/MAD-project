// FarmListScreen.kt
// Main screen — shows all tracked farms, with quick access to Add Farm,
// Disease Report (per farm), and the global Weather screen.

package com.example.agriscout.ui.screens

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Cloud
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.agriscout.data.local.entity.FarmEntity
import com.example.agriscout.ui.viewmodel.AgriViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun FarmListScreen(
    viewModel: AgriViewModel,
    onNavigateToAddFarm: () -> Unit,
    onNavigateToReport: (Long) -> Unit,
    onNavigateToWeather: () -> Unit   // NEW — navigates to the global WeatherScreen
) {
    // Collect the dynamic Room database list state flow
    val farmsList by viewModel.farmsList.collectAsState()

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Agri Scout - Active Farms", fontWeight = FontWeight.Bold) },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.primaryContainer
                ),
                actions = {
                    // Weather button — top-right corner of the app bar
                    IconButton(onClick = onNavigateToWeather) {
                        Icon(
                            imageVector = Icons.Default.Cloud,
                            contentDescription = "Weather"
                        )
                    }
                }
            )
        },
        floatingActionButton = {
            FloatingActionButton(
                onClick = onNavigateToAddFarm,
                containerColor = MaterialTheme.colorScheme.primaryContainer
            ) {
                Icon(Icons.Default.Add, contentDescription = "Add Farm")
            }
        }
    ) { innerPadding ->
        if (farmsList.isEmpty()) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(innerPadding),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = "No farms tracked yet. Tap '+' to begin.",
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    fontSize = 16.sp
                )
            }
        } else {
            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(innerPadding)
                    .padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                items(farmsList) { farm ->
                    FarmCard(
                        farm = farm,
                        onReportClick = { onNavigateToReport(farm.farmId) }
                    )
                }
            }
        }
    }
}

@Composable
fun FarmCard(farm: FarmEntity, onReportClick: () -> Unit) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
        ) {
            Text(
                text = farm.farmName,
                fontSize = 20.sp,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.primary
            )
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = "Manager: ${farm.farmerName}",
                fontSize = 14.sp,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            Spacer(modifier = Modifier.height(8.dp))
            Row(
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(
                    Icons.Default.LocationOn,
                    contentDescription = "Location Pin",
                    tint = MaterialTheme.colorScheme.secondary,
                    modifier = Modifier.size(16.dp)
                )
                Spacer(modifier = Modifier.width(4.dp))
                Text(
                    text = farm.locationName,
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Medium
                )
            }
            Spacer(modifier = Modifier.height(12.dp))
            Button(
                onClick = onReportClick,
                modifier = Modifier.align(Alignment.End)
            ) {
                Text("Log Disease Report")
            }
        }
    }
}
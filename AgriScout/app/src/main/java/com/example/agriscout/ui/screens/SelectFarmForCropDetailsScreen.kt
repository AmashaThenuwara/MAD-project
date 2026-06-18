package com.example.agriscout.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
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
import com.example.agriscout.ui.components.ShinyCard
import com.example.agriscout.ui.theme.ShinyGradientStart
import com.example.agriscout.ui.theme.ShinyGradientEnd
import com.example.agriscout.ui.viewmodel.AgriViewModel
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SelectFarmForCropDetailsScreen(
    viewModel: AgriViewModel,
    onNavigateBack: () -> Unit,
    onFarmSelected: (Long) -> Unit
) {
    val farmsList by viewModel.farmsList.collectAsState()

    val primaryGradient = Brush.horizontalGradient(
        colors = listOf(ShinyGradientStart, ShinyGradientEnd)
    )

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Select a Farm", fontWeight = FontWeight.Bold, color = Color.White) },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back", tint = Color.White)
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = Color.Transparent
                ),
                modifier = Modifier.background(primaryGradient)
            )
        }
    ) { innerPadding ->
        if (farmsList.isEmpty()) {
            Box(modifier = Modifier.fillMaxSize().padding(innerPadding), contentAlignment = Alignment.Center) {
                Text("No farms tracked yet.", color = MaterialTheme.colorScheme.onSurfaceVariant)
            }
        } else {
            LazyColumn(
                modifier = Modifier.fillMaxSize().padding(innerPadding).padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                items(farmsList) { farm ->
                    SimpleFarmCard(
                        farm = farm,
                        onClick = { onFarmSelected(farm.farmId) }
                    )
                }
            }
        }
    }
}

@Composable
fun SimpleFarmCard(
    farm: FarmEntity,
    onClick: () -> Unit
) {
    ShinyCard(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onClick() }
    ) {
        Column(modifier = Modifier.fillMaxWidth().padding(16.dp)) {
            Text(text = farm.farmName, fontSize = 20.sp, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.primary)
            Spacer(modifier = Modifier.height(4.dp))
            Text(text = "Manager: ${farm.farmerName}", fontSize = 14.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(Icons.Default.LocationOn, contentDescription = null, modifier = Modifier.size(16.dp), tint = MaterialTheme.colorScheme.primary)
                Spacer(modifier = Modifier.width(4.dp))
                Text(text = farm.locationName, fontSize = 14.sp, color = MaterialTheme.colorScheme.onSurface)
            }
        }
    }
}

package com.example.agriscout.ui.screens

import androidx.compose.animation.animateContentSize
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Eco
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Landscape
import androidx.compose.material.icons.filled.Public
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import coil.compose.AsyncImage
import com.example.agriscout.data.local.entity.CropEntity
import com.example.agriscout.ui.components.ShinyCard
import com.example.agriscout.ui.theme.NeonGreen
import com.example.agriscout.ui.theme.ShinyGradientStart
import com.example.agriscout.ui.theme.ShinyGradientEnd
import com.example.agriscout.ui.viewmodel.AgriViewModel
import java.io.File

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun FarmCropsDetailedListScreen(
    farmId: Long,
    viewModel: AgriViewModel,
    onNavigateBack: () -> Unit
) {
    val farm by viewModel.getFarmById(farmId).collectAsState(initial = null)
    val crops by viewModel.getCropsForFarm(farmId).collectAsState(initial = emptyList())

    val primaryGradient = Brush.horizontalGradient(
        colors = listOf(ShinyGradientStart, ShinyGradientEnd)
    )

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(farm?.farmName?.let { "$it Crops" } ?: "Crops List", fontWeight = FontWeight.Bold, color = Color.White) },
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
        if (crops.isEmpty()) {
            Box(modifier = Modifier.fillMaxSize().padding(innerPadding), contentAlignment = Alignment.Center) {
                Text("No crops added for this farm.", color = MaterialTheme.colorScheme.onSurfaceVariant)
            }
        } else {
            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(innerPadding)
                    .padding(horizontal = 16.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                item { Spacer(modifier = Modifier.height(8.dp)) }
                items(crops) { crop ->
                    DetailedCropCard(crop = crop)
                }
                item { Spacer(modifier = Modifier.height(24.dp)) }
            }
        }
    }
}

@Composable
fun DetailedCropCard(crop: CropEntity) {
    ShinyCard(
        modifier = Modifier
            .fillMaxWidth()
            .animateContentSize()
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            // Image Section
            if (crop.imagePath.isNotEmpty()) {
                AsyncImage(
                    model = if (crop.imagePath.startsWith("http")) crop.imagePath else File(crop.imagePath),
                    contentDescription = "Crop Image",
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(180.dp)
                        .shadow(4.dp, RoundedCornerShape(12.dp))
                        .clip(RoundedCornerShape(12.dp)),
                    contentScale = ContentScale.Crop
                )
            } else {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(120.dp)
                        .shadow(4.dp, RoundedCornerShape(12.dp))
                        .clip(RoundedCornerShape(12.dp))
                        .background(MaterialTheme.colorScheme.surface),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        Icons.Default.Eco,
                        contentDescription = null,
                        modifier = Modifier.size(48.dp),
                        tint = NeonGreen.copy(alpha = 0.5f)
                    )
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Header Section
            Text(
                text = crop.commonName,
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onSurface
            )
            if (crop.scientificName.isNotEmpty()) {
                Text(
                    text = crop.scientificName,
                    style = MaterialTheme.typography.bodyMedium,
                    fontStyle = FontStyle.Italic,
                    color = MaterialTheme.colorScheme.primary
                )
            }

            Spacer(modifier = Modifier.height(16.dp))
            HorizontalDivider(color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.1f))
            Spacer(modifier = Modifier.height(8.dp))

            // Details
            DetailItem(icon = Icons.Default.Info, label = "Category", value = crop.category)
            DetailItem(icon = Icons.Default.Public, label = "Origin", value = crop.origin)
            DetailItem(icon = Icons.Default.Landscape, label = "Soil Type", value = crop.soilType)
            DetailItem(icon = Icons.Default.Eco, label = "Fertilizer", value = crop.fertilizerType)
        }
    }
}

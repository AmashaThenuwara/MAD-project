package com.example.agriscout.ui.screens

import androidx.compose.animation.animateContentSize
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Eco
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Landscape
import androidx.compose.material.icons.filled.Public
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import com.example.agriscout.ui.theme.NeonGreen
import com.example.agriscout.ui.theme.ShinyGradientStart
import com.example.agriscout.ui.theme.ShinyGradientEnd
import com.example.agriscout.ui.viewmodel.AgriViewModel
import com.example.agriscout.ui.components.ShinyCard
import java.io.File

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CropDetailScreen(
    cropId: Long,
    viewModel: AgriViewModel,
    onNavigateBack: () -> Unit,
    onNavigateToEditCrop: (Long, Long) -> Unit
) {
    var crop by remember { mutableStateOf<com.example.agriscout.data.local.entity.CropEntity?>(null) }
    var showDeleteConfirmDialog by remember { mutableStateOf(false) }

    LaunchedEffect(cropId) {
        crop = viewModel.getCropById(cropId)
    }

    val primaryGradient = Brush.horizontalGradient(
        colors = listOf(ShinyGradientStart, ShinyGradientEnd)
    )

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(crop?.commonName ?: "Crop Details", fontWeight = FontWeight.Bold) },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                    }
                },
                actions = {
                    crop?.let { currentCrop ->
                        IconButton(onClick = { onNavigateToEditCrop(currentCrop.farmId, currentCrop.cropId) }) {
                            Icon(Icons.Default.Edit, contentDescription = "Edit Crop")
                        }
                        IconButton(onClick = { showDeleteConfirmDialog = true }) {
                            Icon(Icons.Default.Delete, contentDescription = "Delete Crop")
                        }
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = MaterialTheme.colorScheme.primaryContainer)
            )
        }
    ) { padding ->
        if (crop != null) {
            Column(
                modifier = Modifier
                    .padding(padding)
                    .fillMaxSize()
                    .background(MaterialTheme.colorScheme.background)
                    .verticalScroll(rememberScrollState())
            ) {
                // Image Section
                Box(modifier = Modifier.fillMaxWidth().padding(16.dp)) {
                    if (crop!!.imagePath.isNotEmpty()) {
                        AsyncImage(
                            model = File(crop!!.imagePath),
                            contentDescription = "Crop Image",
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(250.dp)
                                .shadow(8.dp, RoundedCornerShape(16.dp))
                                .clip(RoundedCornerShape(16.dp)),
                            contentScale = ContentScale.Crop
                        )
                    } else {
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(200.dp)
                                .shadow(8.dp, RoundedCornerShape(16.dp))
                                .clip(RoundedCornerShape(16.dp))
                                .background(MaterialTheme.colorScheme.surface),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                Icons.Default.Eco,
                                contentDescription = null,
                                modifier = Modifier.size(72.dp),
                                tint = NeonGreen.copy(alpha = 0.5f)
                            )
                        }
                    }
                }

                ShinyCard(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp, vertical = 8.dp)
                        .animateContentSize()
                ) {
                    Column(modifier = Modifier.padding(24.dp)) {
                        // Header Section
                        Text(
                            text = crop!!.commonName,
                            style = MaterialTheme.typography.headlineMedium,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                        if (crop!!.scientificName.isNotEmpty()) {
                            Text(
                                text = crop!!.scientificName,
                                style = MaterialTheme.typography.titleMedium,
                                fontStyle = FontStyle.Italic,
                                color = MaterialTheme.colorScheme.primary
                            )
                        }

                        Spacer(modifier = Modifier.height(24.dp))
                        HorizontalDivider(modifier = Modifier.padding(bottom = 16.dp), color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.1f))

                        // Details
                        DetailItem(
                            icon = Icons.Default.Info,
                            label = "Category",
                            value = crop!!.category
                        )
                        DetailItem(
                            icon = Icons.Default.Public,
                            label = "Origin / Native Region",
                            value = crop!!.origin
                        )
                        DetailItem(
                            icon = Icons.Default.Landscape,
                            label = "Soil Type",
                            value = crop!!.soilType
                        )
                        DetailItem(
                            icon = Icons.Default.Eco,
                            label = "Fertilizer Type",
                            value = crop!!.fertilizerType
                        )
                    }
                }
                Spacer(modifier = Modifier.height(24.dp))
            }
        } else {
            Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                CircularProgressIndicator(color = MaterialTheme.colorScheme.primary)
            }
        }
    }

    if (showDeleteConfirmDialog) {
        AlertDialog(
            onDismissRequest = { showDeleteConfirmDialog = false },
            title = { Text("Delete Crop", fontWeight = FontWeight.Bold) },
            text = { Text("Are you sure you want to delete this crop?") },
            confirmButton = {
                TextButton(onClick = {
                    crop?.let { viewModel.deleteCrop(it) }
                    showDeleteConfirmDialog = false
                    onNavigateBack()
                }) {
                    Text("Delete", color = MaterialTheme.colorScheme.error, fontWeight = FontWeight.Bold)
                }
            },
            dismissButton = {
                TextButton(onClick = { showDeleteConfirmDialog = false }) {
                    Text("Cancel")
                }
            },
            shape = RoundedCornerShape(16.dp),
            containerColor = MaterialTheme.colorScheme.surface
        )
    }
}

@Composable
fun DetailItem(icon: ImageVector, label: String, value: String) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 12.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Box(
            modifier = Modifier
                .size(40.dp)
                .background(NeonGreen.copy(alpha = 0.15f), RoundedCornerShape(10.dp)),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                icon,
                contentDescription = null,
                modifier = Modifier.size(24.dp),
                tint = ShinyGradientEnd
            )
        }
        Spacer(modifier = Modifier.width(16.dp))
        Column {
            Text(label, style = MaterialTheme.typography.labelMedium, color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f))
            Text(
                if (value.isNotEmpty()) value else "Not specified",
                style = MaterialTheme.typography.bodyLarge,
                fontWeight = FontWeight.Medium,
                color = MaterialTheme.colorScheme.onSurface
            )
        }
    }
}

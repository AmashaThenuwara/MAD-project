package com.example.agriscout.ui.screens

import android.content.Intent
import android.widget.Toast
import androidx.compose.animation.animateContentSize
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material.icons.filled.PictureAsPdf
import androidx.compose.material.icons.filled.Share
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.agriscout.ui.theme.ShinyGradientStart
import com.example.agriscout.ui.theme.ShinyGradientEnd
import com.example.agriscout.ui.viewmodel.AgriViewModel
import com.example.agriscout.util.CsvGenerator
import com.example.agriscout.util.PdfGenerator
import com.example.agriscout.ui.components.ShinyCard

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun FarmDetailsScreen(
    farmId: Long,
    viewModel: AgriViewModel,
    onNavigateBack: () -> Unit,
    onNavigateToCropDetail: (Long) -> Unit,
    onNavigateToAddCrop: (Long) -> Unit,
    onNavigateToEditFarm: (Long) -> Unit
) {
    val context = LocalContext.current
    val farm by viewModel.getFarmById(farmId).collectAsState(initial = null)
    val crops by viewModel.getCropsForFarm(farmId).collectAsState(initial = emptyList())

    var showDeleteConfirmDialog by remember { mutableStateOf(false) }
    var expandedMenu by remember { mutableStateOf(false) }

    val primaryGradient = Brush.horizontalGradient(
        colors = listOf(ShinyGradientStart, ShinyGradientEnd)
    )

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(farm?.farmName ?: "Farm Details", fontWeight = FontWeight.Bold, color = Color.White) },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Back", tint = Color.White)
                    }
                },
                actions = {
                    IconButton(onClick = { onNavigateToEditFarm(farmId) }) {
                        Icon(Icons.Default.Edit, contentDescription = "Edit Farm", tint = Color.White)
                    }
                    IconButton(onClick = { showDeleteConfirmDialog = true }) {
                        Icon(Icons.Default.Delete, contentDescription = "Delete Farm", tint = Color.White)
                    }
                    Box {
                        IconButton(onClick = { expandedMenu = true }) {
                            Icon(Icons.Default.MoreVert, contentDescription = "More", tint = Color.White)
                        }
                        DropdownMenu(
                            expanded = expandedMenu,
                            onDismissRequest = { expandedMenu = false },
                            modifier = Modifier.background(MaterialTheme.colorScheme.surface)
                        ) {
                            DropdownMenuItem(
                                text = { Text("Export Detailed PDF") },
                                onClick = {
                                    expandedMenu = false
                                    farm?.let { f ->
                                        val path = PdfGenerator.createFarmReport(context, f, crops)
                                        val uri = androidx.core.content.FileProvider.getUriForFile(context, "${context.packageName}.fileprovider", java.io.File(path))
                                        val intent = Intent(Intent.ACTION_SEND).apply {
                                            type = "application/pdf"
                                            putExtra(Intent.EXTRA_STREAM, uri)
                                            addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
                                        }
                                        context.startActivity(Intent.createChooser(intent, "Share PDF Report"))
                                    }
                                },
                                leadingIcon = { Icon(Icons.Default.PictureAsPdf, contentDescription = null) }
                            )
                            DropdownMenuItem(
                                text = { Text("Export Detailed CSV") },
                                onClick = {
                                    expandedMenu = false
                                    farm?.let { f ->
                                        val path = CsvGenerator.createFarmCsvReport(context, f, crops)
                                        val uri = androidx.core.content.FileProvider.getUriForFile(context, "${context.packageName}.fileprovider", java.io.File(path))
                                        val intent = Intent(Intent.ACTION_SEND).apply {
                                            type = "text/csv"
                                            putExtra(Intent.EXTRA_STREAM, uri)
                                            addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
                                        }
                                        context.startActivity(Intent.createChooser(intent, "Share CSV Report"))
                                    }
                                },
                                leadingIcon = { Icon(Icons.Default.Share, contentDescription = null) }
                            )
                        }
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = Color.Transparent
                ),
                modifier = Modifier.background(primaryGradient)
            )
        },
        floatingActionButton = {
            FloatingActionButton(
                onClick = { onNavigateToAddCrop(farmId) },
                containerColor = Color.Transparent,
                elevation = FloatingActionButtonDefaults.elevation(8.dp),
                modifier = Modifier
                    .background(primaryGradient, RoundedCornerShape(16.dp))
                    .shadow(8.dp, RoundedCornerShape(16.dp))
            ) {
                Icon(Icons.Default.Add, contentDescription = "Add Crop", tint = Color.White)
            }
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .padding(padding)
                .fillMaxSize()
                .background(MaterialTheme.colorScheme.background)
        ) {
            if (farm != null) {
                ShinyCard(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp)
                        .animateContentSize()
                ) {
                    Column(modifier = Modifier.padding(20.dp)) {
                        Text(text = "Size: ${farm!!.landSize} Hectares", style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold)
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(text = "Location: ${farm!!.locationName}", style = MaterialTheme.typography.bodyLarge, color = MaterialTheme.colorScheme.primary)
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(text = "Details: ${farm!!.locationDetails}", style = MaterialTheme.typography.bodyMedium)
                    }
                }

                Text(
                    "Associated Crops", 
                    modifier = Modifier.padding(horizontal = 20.dp, vertical = 8.dp), 
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.primary
                )

                LazyColumn(
                    contentPadding = PaddingValues(horizontal = 16.dp, vertical = 8.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    items(crops) { crop ->
                        ShinyCard(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable { onNavigateToCropDetail(crop.cropId) }
                                .animateContentSize()
                        ) {
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(16.dp),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = androidx.compose.ui.Alignment.CenterVertically
                            ) {
                                Column {
                                    Text(text = crop.commonName, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                                    Spacer(modifier = Modifier.height(4.dp))
                                    Text(text = "${crop.category} • ${crop.scientificName}", style = MaterialTheme.typography.bodySmall, color = Color.Gray)
                                }
                            }
                        }
                    }
                }
            }
        }
    }

    if (showDeleteConfirmDialog) {
        AlertDialog(
            onDismissRequest = { showDeleteConfirmDialog = false },
            title = { Text("Delete Farm", fontWeight = FontWeight.Bold) },
            text = { Text("Are you sure you want to delete this farm? This will also delete all associated crops.") },
            confirmButton = {
                TextButton(onClick = {
                    farm?.let { viewModel.deleteFarm(it) }
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

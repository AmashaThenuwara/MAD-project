package com.example.agriscout.ui.screens

import android.content.Intent
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material.icons.filled.PictureAsPdf
import androidx.compose.material.icons.filled.Share
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import com.example.agriscout.data.local.entity.DiseaseReportEntity
import com.example.agriscout.ui.viewmodel.AgriViewModel
import com.example.agriscout.util.ReportExporter
import java.io.File
import java.text.SimpleDateFormat
import java.util.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ReportsListScreen(
    viewModel: AgriViewModel,
    onNavigateBack: () -> Unit
) {
    val reports by viewModel.allReports.collectAsState()
    val farms by viewModel.farmsList.collectAsState()
    val context = LocalContext.current
    var showMenu by remember { mutableStateOf(false) }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("All Disease Reports", fontWeight = FontWeight.Bold) },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Back")
                    }
                },
                actions = {
                    IconButton(onClick = { showMenu = !showMenu }) {
                        Icon(Icons.Default.MoreVert, contentDescription = "Export Options")
                    }
                    DropdownMenu(
                        expanded = showMenu,
                        onDismissRequest = { showMenu = false }
                    ) {
                        DropdownMenuItem(
                            text = { Text("Export Detailed PDF") },
                            onClick = {
                                showMenu = false
                                // Export reports to PDF
                                val uri = ReportExporter.exportToPdf(context, reports, farms)
                                if (uri != null) {
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
                                showMenu = false
                                // Export reports to CSV
                                val uri = ReportExporter.exportToCsv(context, reports, farms)
                                if (uri != null) {
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
            )
        }
    ) { padding ->
        if (reports.isEmpty()) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(padding),
                contentAlignment = Alignment.Center
            ) {
                Text("No disease reports yet.", color = MaterialTheme.colorScheme.onSurfaceVariant)
            }
        } else {
            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(padding)
                    .padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                items(reports) { report ->
                    ReportCard(report)
                }
            }
        }
    }
}

@Composable
fun ReportCard(report: DiseaseReportEntity) {
    val dateStr = remember(report.date) {
        SimpleDateFormat("dd MMM yyyy, HH:mm", Locale.getDefault()).format(Date(report.date))
    }

    Card(
        modifier = Modifier.fillMaxWidth(),
        elevation = CardDefaults.cardElevation(2.dp)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                // Display disease image
                if (report.imagePath.isNotEmpty()) {
                    AsyncImage(
                        model = File(report.imagePath),
                        contentDescription = "Disease Photo",
                        modifier = Modifier
                            .size(100.dp)
                            .clip(RoundedCornerShape(8.dp)),
                        contentScale = ContentScale.Crop
                    )
                }

                Column(modifier = Modifier.weight(1f)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(report.diseaseName, fontWeight = FontWeight.Bold, fontSize = 16.sp)
                        Surface(
                            color = if (report.syncStatus == "SYNCED")
                                Color(0xFF2E7D32) else Color(0xFFF57F17),
                            shape = MaterialTheme.shapes.small
                        ) {
                            Text(
                                text = report.syncStatus,
                                modifier = Modifier.padding(horizontal = 8.dp, vertical = 2.dp),
                                color = Color.White,
                                fontSize = 11.sp
                            )
                        }
                    }
                    Spacer(Modifier.height(4.dp))
                    Text("Farm ID: ${report.farmId} • $dateStr",
                        fontSize = 12.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                    if (report.notes.isNotBlank()) {
                        Spacer(Modifier.height(6.dp))
                        Text(report.notes, fontSize = 13.sp, maxLines = 3)
                    }
                    if (report.latitude != 0.0) {
                        Spacer(Modifier.height(4.dp))
                        Text(
                            "📍 Lat: %.4f, Lon: %.4f".format(report.latitude, report.longitude),
                            fontSize = 12.sp,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            }
        }
    }
}

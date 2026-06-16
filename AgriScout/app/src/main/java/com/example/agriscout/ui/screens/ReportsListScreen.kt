package com.example.agriscout.ui.screens

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.agriscout.data.local.entity.DiseaseReportEntity
import com.example.agriscout.ui.viewmodel.AgriViewModel
import java.text.SimpleDateFormat
import java.util.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ReportsListScreen(
    viewModel: AgriViewModel,
    onNavigateBack: () -> Unit
) {
    val reports by viewModel.allReports.collectAsState()

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("All Disease Reports", fontWeight = FontWeight.Bold) },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Back")
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
                Text(report.notes, fontSize = 13.sp)
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
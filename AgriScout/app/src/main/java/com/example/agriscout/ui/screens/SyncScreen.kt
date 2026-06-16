package com.example.agriscout.ui.screens

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.asFlow
import androidx.work.OneTimeWorkRequestBuilder
import androidx.work.WorkInfo
import androidx.work.WorkManager
import com.example.agriscout.data.sync.SyncWorker
import com.example.agriscout.ui.viewmodel.AgriViewModel
import kotlinx.coroutines.flow.collectLatest
import java.util.UUID

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SyncScreen(
    viewModel: AgriViewModel,
    onNavigateBack: () -> Unit
) {
    val context = LocalContext.current
    val hasUnsynced by viewModel.hasUnsyncedData.collectAsState()
    val pendingReports by viewModel.pendingSyncCount.collectAsState()
    val syncedCount by viewModel.syncedCount.collectAsState()
    val totalReports by viewModel.totalReportCount.collectAsState()

    var syncStatus by remember { mutableStateOf("Ready to sync data") }
    var isSyncing by remember { mutableStateOf(false) }
    var activeWorkId by remember { mutableStateOf<UUID?>(null) }

    // Observe WorkManager state properly
    LaunchedEffect(activeWorkId) {
        activeWorkId?.let { id ->
            WorkManager.getInstance(context)
                .getWorkInfoByIdLiveData(id)
                .asFlow()
                .collectLatest { info ->
                    when (info?.state) {
                        WorkInfo.State.SUCCEEDED -> {
                            isSyncing = false
                            syncStatus = "✅ Sync completed successfully!"
                            activeWorkId = null
                        }
                        WorkInfo.State.FAILED -> {
                            isSyncing = false
                            syncStatus = "❌ Sync failed. Check internet or Firebase rules."
                            activeWorkId = null
                        }
                        WorkInfo.State.RUNNING -> {
                            syncStatus = "⏳ Uploading data..."
                        }
                        WorkInfo.State.ENQUEUED -> {
                            syncStatus = "⏳ Waiting in queue..."
                        }
                        else -> {}
                    }
                }
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Cloud Synchronization", fontWeight = FontWeight.Bold) },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Back")
                    }
                }
            )
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(24.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            Text("Connection Status", fontSize = 20.sp, fontWeight = FontWeight.Bold)

            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.surfaceVariant
                )
            ) {
                Column(
                    modifier = Modifier.padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    SyncInfoRow("Database Reports", totalReports.toString())
                    SyncInfoRow("Cloud Synced", syncedCount.toString())
                    SyncInfoRow("Pending Uploads", pendingReports.toString())
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            if (isSyncing) {
                CircularProgressIndicator(
                    modifier = Modifier.size(48.dp),
                    strokeWidth = 4.dp
                )
            }

            Text(
                text = syncStatus,
                color = if (syncStatus.contains("✅")) Color(0xFF2E7D32) else MaterialTheme.colorScheme.onSurfaceVariant,
                fontSize = 15.sp,
                fontWeight = if (syncStatus.contains("✅")) FontWeight.Bold else FontWeight.Normal,
                modifier = Modifier.padding(horizontal = 8.dp)
            )

            Spacer(modifier = Modifier.weight(1f))

            Button(
                onClick = {
                    isSyncing = true
                    val workRequest = OneTimeWorkRequestBuilder<SyncWorker>().build()
                    activeWorkId = workRequest.id
                    WorkManager.getInstance(context).enqueue(workRequest)
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(56.dp),
                enabled = !isSyncing && hasUnsynced,
                shape = RoundedCornerShape(12.dp)
            ) {
                Text(
                    if (!hasUnsynced) "Everything Synced" else "Sync with Firebase",
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Bold
                )
            }
        }
    }
}

@Composable
fun SyncInfoRow(label: String, value: String) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(label, color = MaterialTheme.colorScheme.onSurfaceVariant, fontSize = 14.sp)
        Text(value, fontWeight = FontWeight.Bold, fontSize = 16.sp, color = MaterialTheme.colorScheme.primary)
    }
}

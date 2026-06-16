package com.example.agriscout.ui.screens

import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.work.OneTimeWorkRequestBuilder
import androidx.work.WorkInfo
import androidx.work.WorkManager
import com.example.agriscout.data.sync.SyncWorker
import com.example.agriscout.ui.viewmodel.AgriViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SyncScreen(
    viewModel: AgriViewModel,
    onNavigateBack: () -> Unit
) {
    val context = LocalContext.current
    val pendingSync by viewModel.pendingSyncCount.collectAsState()
    val syncedCount by viewModel.syncedCount.collectAsState()
    val totalReports by viewModel.totalReportCount.collectAsState()

    var syncStatus by remember { mutableStateOf("Tap the button to sync data") }
    var isSyncing by remember { mutableStateOf(false) }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Sync Data", fontWeight = FontWeight.Bold) },
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
            Text("Sync Status", fontSize = 20.sp, fontWeight = FontWeight.Bold)

            Card(modifier = Modifier.fillMaxWidth()) {
                Column(
                    modifier = Modifier.padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    SyncInfoRow("Total Reports", totalReports.toString())
                    SyncInfoRow("Synced to Firebase", syncedCount.toString())
                    SyncInfoRow("Pending Upload", pendingSync.toString())
                }
            }

            Spacer(modifier = Modifier.height(8.dp))

            Text(
                text = syncStatus,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                fontSize = 14.sp
            )

            if (isSyncing) {
                CircularProgressIndicator()
            }

            Button(
                onClick = {
                    isSyncing = true
                    syncStatus = "Syncing..."
                    val workRequest = OneTimeWorkRequestBuilder<SyncWorker>().build()
                    val workManager = WorkManager.getInstance(context)
                    workManager.enqueue(workRequest)
                    workManager.getWorkInfoByIdLiveData(workRequest.id)
                        .observeForever { info ->
                            when (info?.state) {
                                WorkInfo.State.SUCCEEDED -> {
                                    isSyncing = false
                                    syncStatus = "✅ Sync completed successfully!"
                                }
                                WorkInfo.State.FAILED -> {
                                    isSyncing = false
                                    syncStatus = "❌ Sync failed. Check internet connection."
                                }
                                WorkInfo.State.RUNNING -> {
                                    syncStatus = "⏳ Uploading data..."
                                }
                                else -> {}
                            }
                        }
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(50.dp),
                enabled = !isSyncing && pendingSync > 0
            ) {
                Text(
                    if (pendingSync == 0) "Nothing to sync" else "Sync Now ($pendingSync pending)",
                    fontSize = 16.sp
                )
            }
        }
    }
}

@Composable
fun SyncInfoRow(label: String, value: String) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Text(label, color = MaterialTheme.colorScheme.onSurfaceVariant)
        Text(value, fontWeight = FontWeight.SemiBold)
    }
}
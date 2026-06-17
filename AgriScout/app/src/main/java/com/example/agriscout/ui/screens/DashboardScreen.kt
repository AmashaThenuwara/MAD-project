package com.example.agriscout.ui.screens

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.agriscout.data.firebase.FirebaseAuthManager
import com.example.agriscout.ui.viewmodel.AgriViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DashboardScreen(
    viewModel: AgriViewModel,
    onNavigateToFarms: () -> Unit,
    onNavigateToReports: () -> Unit,
    onNavigateToWeather: () -> Unit,
    onNavigateToSync: () -> Unit,
    onNavigateToProfile: () -> Unit,
    onLogout: () -> Unit
) {
    val authManager = remember { FirebaseAuthManager() }
    val totalFarms by viewModel.totalFarmCount.collectAsState()
    val totalReports by viewModel.totalReportCount.collectAsState()
    val pendingSync by viewModel.pendingSyncCount.collectAsState()
    val synced by viewModel.syncedCount.collectAsState()

    val userEmail = authManager.currentUser?.email ?: "Officer"

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("AgriScout Dashboard", fontWeight = FontWeight.Bold) },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.primaryContainer
                ),
                actions = {
                    IconButton(onClick = onNavigateToProfile) {
                        Icon(Icons.Default.Person, contentDescription = "Profile")
                    }
                    IconButton(onClick = {
                        authManager.logout()
                        onLogout()
                    }) {
                        Icon(Icons.Default.ExitToApp, contentDescription = "Logout")
                    }
                }
            )
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            Text(
                text = "Welcome, $userEmail",
                fontSize = 16.sp,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )

            // Statistical overview section
            Text("Overview", fontWeight = FontWeight.SemiBold, fontSize = 18.sp)

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                StatCard("Farms", totalFarms.toString(), Icons.Default.Home, Modifier.weight(1f))
                StatCard("Reports", totalReports.toString(), Icons.Default.Warning, Modifier.weight(1f))
            }
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                StatCard("Synced", synced.toString(), Icons.Default.CheckCircle, Modifier.weight(1f))
                StatCard("Pending Sync", pendingSync.toString(), Icons.Default.Refresh, Modifier.weight(1f))
            }

            Spacer(modifier = Modifier.height(8.dp))
            Text("Quick Actions", fontWeight = FontWeight.SemiBold, fontSize = 18.sp)

            // Primary navigation options
            ActionCard("My Farms", "View and manage farms", Icons.Default.Home, onNavigateToFarms)
            ActionCard("Disease Reports", "View all disease reports", Icons.Default.Warning, onNavigateToReports)
            ActionCard("Weather", "Check farm weather conditions", Icons.Default.Info, onNavigateToWeather)
            ActionCard("Sync Data", "Upload local data to Firebase", Icons.Default.Refresh, onNavigateToSync)
        }
    }
}

@Composable
fun StatCard(title: String, value: String, icon: ImageVector, modifier: Modifier = Modifier) {
    Card(modifier = modifier, elevation = CardDefaults.cardElevation(2.dp)) {
        Column(
            modifier = Modifier.padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Icon(icon, contentDescription = null, tint = MaterialTheme.colorScheme.primary, modifier = Modifier.size(28.dp))
            Spacer(modifier = Modifier.height(8.dp))
            Text(value, fontSize = 28.sp, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.primary)
            Text(title, fontSize = 12.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
        }
    }
}

@Composable
fun ActionCard(title: String, subtitle: String, icon: ImageVector, onClick: () -> Unit) {
    Card(
        modifier = Modifier.fillMaxWidth().clickable { onClick() },
        elevation = CardDefaults.cardElevation(2.dp)
    ) {
        Row(
            modifier = Modifier.padding(16.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            Icon(icon, contentDescription = null, tint = MaterialTheme.colorScheme.primary, modifier = Modifier.size(32.dp))
            Column(modifier = Modifier.weight(1f)) {
                Text(title, fontWeight = FontWeight.SemiBold, fontSize = 16.sp)
                Text(subtitle, fontSize = 13.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
            }
            Icon(Icons.Default.ArrowForward, contentDescription = null, tint = MaterialTheme.colorScheme.onSurfaceVariant)
        }
    }
}

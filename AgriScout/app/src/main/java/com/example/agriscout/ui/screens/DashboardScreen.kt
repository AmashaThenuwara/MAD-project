package com.example.agriscout.ui.screens

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ExitToApp
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
import com.example.agriscout.ui.components.ShinyCard

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DashboardScreen(
    viewModel: AgriViewModel,
    onNavigateToFarms: () -> Unit,
    onNavigateToReports: () -> Unit,
    onNavigateToWeather: () -> Unit,
    onNavigateToSelectFarmForCrops: () -> Unit,
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
    
    // Get display name from Firebase Auth, fallback to formatted email if not set
    val displayName = authManager.currentUser?.displayName.takeIf { !it.isNullOrBlank() }
        ?: if (userEmail != "Officer") {
            userEmail.substringBefore("@")
                .replace(Regex("[^a-zA-Z]"), " ")
                .split(" ")
                .filter { it.isNotBlank() }
                .joinToString(" ") { it.replaceFirstChar { char -> char.uppercase() } }
        } else {
            "Officer"
        }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("AGRISCOUT", fontWeight = FontWeight.ExtraBold, letterSpacing = 2.sp) },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.background,
                    titleContentColor = MaterialTheme.colorScheme.primary
                ),
                actions = {
                    IconButton(onClick = onNavigateToProfile) {
                        Icon(Icons.Default.Person, contentDescription = "Profile", tint = MaterialTheme.colorScheme.primary)
                    }
                    IconButton(onClick = {
                        authManager.logout()
                        onLogout()
                    }) {
                        Icon(Icons.AutoMirrored.Filled.ExitToApp, contentDescription = "Logout", tint = MaterialTheme.colorScheme.primary)
                    }
                }
            )
        }
    ) { padding ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(horizontal = 16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            item {
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    text = "Welcome back,",
                    fontSize = 16.sp,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Text(
                    text = displayName,
                    fontSize = 24.sp,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.primary
                )
                Spacer(modifier = Modifier.height(24.dp))
            }

            item {
                // Hero Card for Farms
                BentoCard(
                    title = "Registered Farms",
                    subtitle = "Manage fields and crops",
                    icon = Icons.Default.Home,
                    onClick = onNavigateToFarms,
                    modifier = Modifier.fillMaxWidth().height(140.dp),
                    isHero = true
                )
            }

            item {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    BentoCard(
                        title = "Diseases",
                        subtitle = "Track issues",
                        icon = Icons.Default.Warning,
                        onClick = onNavigateToReports,
                        modifier = Modifier.weight(1f).height(140.dp)
                    )
                    BentoCard(
                        title = "Weather",
                        subtitle = "Forecasts",
                        icon = Icons.Default.Info,
                        onClick = onNavigateToWeather,
                        modifier = Modifier.weight(1f).height(140.dp)
                    )
                }
                Spacer(modifier = Modifier.height(16.dp))
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    BentoCard(
                        title = "Crop Details",
                        subtitle = "View by farm",
                        icon = Icons.Default.Eco,
                        onClick = onNavigateToSelectFarmForCrops,
                        modifier = Modifier.weight(1f).height(140.dp)
                    )
                    Spacer(modifier = Modifier.weight(1f))
                }
            }

            item {
                Text("System Status", fontWeight = FontWeight.SemiBold, fontSize = 18.sp, color = MaterialTheme.colorScheme.primary)
                Spacer(modifier = Modifier.height(12.dp))
                
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    StatBox("Farms", totalFarms.toString(), Modifier.weight(1f))
                    StatBox("Reports", totalReports.toString(), Modifier.weight(1f))
                    StatBox("Pending", pendingSync.toString(), Modifier.weight(1f).clickable { onNavigateToSync() })
                }
            }

            item {
                Spacer(modifier = Modifier.height(8.dp))
                BentoCard(
                    title = "Sync Data",
                    subtitle = "$synced items synced to cloud",
                    icon = Icons.Default.Refresh,
                    onClick = onNavigateToSync,
                    modifier = Modifier.fillMaxWidth().height(90.dp)
                )
                Spacer(modifier = Modifier.height(24.dp))
            }
        }
    }
}

@Composable
fun BentoCard(title: String, subtitle: String, icon: ImageVector, onClick: () -> Unit, modifier: Modifier = Modifier, isHero: Boolean = false) {
    ShinyCard(
        modifier = modifier.clickable { onClick() }
    ) {
        Box(modifier = Modifier.fillMaxSize().padding(16.dp)) {
            Icon(
                icon, 
                contentDescription = null, 
                tint = MaterialTheme.colorScheme.primary.copy(alpha = if (isHero) 0.8f else 0.5f), 
                modifier = Modifier
                    .size(if (isHero) 64.dp else 48.dp)
                    .align(if (isHero) Alignment.CenterEnd else Alignment.TopEnd)
            )
            Column(
                modifier = Modifier.align(Alignment.BottomStart)
            ) {
                Text(title, fontWeight = FontWeight.Bold, fontSize = if (isHero) 24.sp else 18.sp, color = MaterialTheme.colorScheme.primary)
                Text(subtitle, fontSize = 13.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
            }
        }
    }
}

@Composable
fun StatBox(title: String, value: String, modifier: Modifier = Modifier) {
    ShinyCard(modifier = modifier) {
        Column(
            modifier = Modifier.padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(value, fontSize = 28.sp, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.primary)
            Text(title, fontSize = 12.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
        }
    }
}

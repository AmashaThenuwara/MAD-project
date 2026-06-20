package com.example.agriscout.ui.screens

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Person
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.agriscout.data.firebase.FirebaseAuthManager
import com.example.agriscout.ui.viewmodel.AgriViewModel
import com.example.agriscout.ui.components.AnimatedButton
import com.example.agriscout.ui.components.ShinyCard

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ProfileScreen(
    viewModel: AgriViewModel,
    onNavigateBack: () -> Unit,
    onLogout: () -> Unit
) {
    val authManager = remember { FirebaseAuthManager() }
    val user = authManager.currentUser

    val totalFarms by viewModel.totalFarmCount.collectAsState()
    val totalReports by viewModel.totalReportCount.collectAsState()
    val synced by viewModel.syncedCount.collectAsState()

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Profile", fontWeight = FontWeight.Bold) },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Back")
                    }
                }
            )
        }
    ) { padding ->
        Column(
            modifier = Modifier.fillMaxSize().padding(padding).padding(24.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            Spacer(modifier = Modifier.height(16.dp))

            Surface(
                modifier = Modifier.size(90.dp),
                shape = CircleShape,
                color = MaterialTheme.colorScheme.primaryContainer
            ) {
                Box(contentAlignment = Alignment.Center) {
                    Icon(Icons.Default.Person, contentDescription = null, modifier = Modifier.size(48.dp), tint = MaterialTheme.colorScheme.onPrimaryContainer)
                }
            }

            val displayTitle = user?.displayName.takeIf { !it.isNullOrBlank() }
                ?: user?.email?.substringBefore("@")?.replace(".", " ")?.split(" ")?.joinToString(" ") { it.replaceFirstChar { char -> char.uppercase() } }
                ?: "Unknown Officer"
            
            Text(text = displayTitle, fontSize = 22.sp, fontWeight = FontWeight.Bold)
            Text(text = user?.email ?: "No Email", fontSize = 16.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
            Text(text = "UID: ${user?.uid?.take(12)}...", fontSize = 12.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)

            HorizontalDivider(modifier = Modifier.padding(vertical = 8.dp))

            Text("Activity Summary", fontWeight = FontWeight.SemiBold, fontSize = 16.sp, modifier = Modifier.align(Alignment.Start))

            ShinyCard(modifier = Modifier.fillMaxWidth()) {
                Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    SyncInfoRow("Farms Registered", totalFarms.toString())
                    SyncInfoRow("Disease Reports Filed", totalReports.toString())
                    SyncInfoRow("Reports Synced", synced.toString())
                }
            }

            Spacer(modifier = Modifier.weight(1f))

            AnimatedButton(
                onClick = { authManager.logout(); onLogout() },
                modifier = Modifier.fillMaxWidth().height(50.dp)
            ) {
                Text("Sign Out", fontSize = 16.sp)
            }
        }
    }
}

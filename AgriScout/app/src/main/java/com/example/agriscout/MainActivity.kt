// MainActivity.kt
// Entry point of AgriScout.
// Responsibilities:
//   1. Checks if user is already logged in (Firebase) — skips login if so
//   2. Sets up NavHost with ALL screens (login, dashboard, farm list, add farm,
//      disease report, reports list, weather, sync, profile)
//   3. Schedules SyncWorker via WorkManager on app start

package com.example.agriscout

import android.app.Application
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import androidx.work.*
import com.example.agriscout.data.firebase.FirebaseAuthManager
import com.example.agriscout.data.sync.SyncWorker
import com.example.agriscout.ui.navigation.Screen
import com.example.agriscout.ui.screens.*
import com.example.agriscout.ui.theme.AgriScoutTheme
import com.example.agriscout.ui.viewmodel.AgriViewModel
import java.util.concurrent.TimeUnit

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // Schedule background sync worker — runs every 15 minutes when internet is available
        scheduleSyncWorker()

        setContent {
            AgriScoutTheme {
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background
                ) {
                    AppNavigation()
                }
            }
        }
    }

    // WorkManager: schedules SyncWorker to run periodically with network constraint.
    // KEEP_EXISTING means if already scheduled, don't reschedule on every app open.
    private fun scheduleSyncWorker() {
        val constraints = Constraints.Builder()
            .setRequiredNetworkType(NetworkType.CONNECTED) // only run when online
            .build()

        val syncRequest = PeriodicWorkRequestBuilder<SyncWorker>(
            15, TimeUnit.MINUTES   // minimum interval WorkManager allows
        )
            .setConstraints(constraints)
            .build()

        WorkManager.getInstance(this).enqueueUniquePeriodicWork(
            "agriscout_sync",              // unique name — prevents duplicate workers
            ExistingPeriodicWorkPolicy.KEEP,
            syncRequest
        )
    }
}

@Composable
fun AppNavigation() {
    val navController = rememberNavController()
    val context = LocalContext.current

    // Shared ViewModel across all screens
    val agriViewModel: AgriViewModel = viewModel(
        factory = androidx.lifecycle.ViewModelProvider.AndroidViewModelFactory
            .getInstance(context.applicationContext as Application)
    )

    // Check Firebase login state to decide the start destination
    // NOTE: Screen.Splash exists in Screen.kt but isn't wired here yet —
    // share SplashScreen.kt if you want it to own this decision instead.
    val authManager = FirebaseAuthManager()
    val startScreen = if (authManager.isLoggedIn) Screen.Dashboard.route else Screen.Login.route

    NavHost(
        navController = navController,
        startDestination = startScreen   // skip login if already authenticated
    ) {

        // Login / Register screen
        composable(Screen.Login.route) {
            LoginScreen(
                onLoginSuccess = {
                    // Replace login screen with Dashboard (can't go back to login)
                    navController.navigate(Screen.Dashboard.route) {
                        popUpTo(Screen.Login.route) { inclusive = true }
                    }
                }
            )
        }

        // Dashboard — main hub screen after login
        composable(Screen.Dashboard.route) {
            DashboardScreen(
                viewModel = agriViewModel,
                onNavigateToFarms = { navController.navigate(Screen.FarmList.route) },
                onNavigateToReports = { navController.navigate(Screen.ReportsList.route) },
                onNavigateToWeather = { navController.navigate(Screen.Weather.route) },
                onNavigateToSync = { navController.navigate(Screen.Sync.route) },
                onNavigateToProfile = { navController.navigate(Screen.Profile.route) },
                onLogout = {
                    navController.navigate(Screen.Login.route) {
                        popUpTo(0) { inclusive = true } // clear entire back stack on logout
                    }
                }
            )
        }

        // Farm list
        composable(Screen.FarmList.route) {
            FarmListScreen(
                viewModel = agriViewModel,
                onNavigateToAddFarm = { navController.navigate(Screen.AddFarm.route) },
                onNavigateToReport = { farmId ->
                    navController.navigate(Screen.DiseaseReport.createRoute(farmId))
                },
                onNavigateToWeather = { navController.navigate(Screen.Weather.route) }
            )
        }

        // Add farm form
        composable(Screen.AddFarm.route) {
            AddFarmScreen(
                viewModel = agriViewModel,
                onNavigateBack = { navController.popBackStack() }
            )
        }

        // Disease report screen — receives farmId as nav argument
        composable(
            route = Screen.DiseaseReport.route,
            arguments = listOf(navArgument("farmId") { type = NavType.LongType })
        ) { backStackEntry ->
            val farmId = backStackEntry.arguments?.getLong("farmId") ?: 0L
            DiseaseReportScreen(
                farmId = farmId,
                viewModel = agriViewModel,
                onNavigateBack = { navController.popBackStack() }
            )
        }

        // Reports list — all disease reports across every farm
        composable(Screen.ReportsList.route) {
            ReportsListScreen(
                viewModel = agriViewModel,
                onNavigateBack = { navController.popBackStack() }
            )
        }

        // Weather screen
        composable(Screen.Weather.route) {
            WeatherScreen(
                viewModel = agriViewModel,
                onNavigateBack = { navController.popBackStack() }
            )
        }

        // Sync screen
        composable(Screen.Sync.route) {
            SyncScreen(
                viewModel = agriViewModel,
                onNavigateBack = { navController.popBackStack() }
            )
        }

        // Profile screen
        composable(Screen.Profile.route) {
            ProfileScreen(
                viewModel = agriViewModel,
                onNavigateBack = { navController.popBackStack() },
                onLogout = {
                    navController.navigate(Screen.Login.route) {
                        popUpTo(0) { inclusive = true }
                    }
                }
            )
        }
    }
}

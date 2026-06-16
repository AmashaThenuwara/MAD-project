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
import com.example.agriscout.data.firebase.FirebaseAuthManager
import com.example.agriscout.ui.navigation.Screen
import com.example.agriscout.ui.screens.*
import com.example.agriscout.ui.theme.AgriScoutTheme
import com.example.agriscout.ui.viewmodel.AgriViewModel

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
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
}

@Composable
fun AppNavigation() {
    val navController = rememberNavController()
    val context = LocalContext.current
    val agriViewModel: AgriViewModel = viewModel(
        factory = androidx.lifecycle.ViewModelProvider.AndroidViewModelFactory
            .getInstance(context.applicationContext as Application)
    )

    val authManager = FirebaseAuthManager()
    // If already logged in, skip login screen
    val startDestination = if (authManager.isLoggedIn)
        Screen.Dashboard.route else Screen.Splash.route

    NavHost(navController = navController, startDestination = startDestination) {

        composable(Screen.Splash.route) {
            SplashScreen(onNavigateNext = {
                val dest = if (authManager.isLoggedIn)
                    Screen.Dashboard.route else Screen.Login.route
                navController.navigate(dest) {
                    popUpTo(Screen.Splash.route) { inclusive = true }
                }
            })
        }

        composable(Screen.Login.route) {
            LoginScreen(onLoginSuccess = {
                navController.navigate(Screen.Dashboard.route) {
                    popUpTo(Screen.Login.route) { inclusive = true }
                }
            })
        }

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
                        popUpTo(Screen.Dashboard.route) { inclusive = true }
                    }
                }
            )
        }

        composable(Screen.FarmList.route) {
            FarmListScreen(
                viewModel = agriViewModel,
                onNavigateToAddFarm = { navController.navigate(Screen.AddFarm.route) },
                onNavigateToReport = { farmId ->
                    navController.navigate(Screen.DiseaseReport.createRoute(farmId))
                }
            )
        }

        composable(Screen.AddFarm.route) {
            AddFarmScreen(
                viewModel = agriViewModel,
                onNavigateBack = { navController.popBackStack() }
            )
        }

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

        composable(Screen.ReportsList.route) {
            ReportsListScreen(
                viewModel = agriViewModel,
                onNavigateBack = { navController.popBackStack() }
            )
        }

        composable(Screen.Weather.route) {
            WeatherScreen(
                viewModel = agriViewModel,
                onNavigateBack = { navController.popBackStack() }
            )
        }

        composable(Screen.Sync.route) {
            SyncScreen(
                viewModel = agriViewModel,
                onNavigateBack = { navController.popBackStack() }
            )
        }

        composable(Screen.Profile.route) {
            ProfileScreen(
                viewModel = agriViewModel,
                onNavigateBack = { navController.popBackStack() },
                onLogout = {
                    navController.navigate(Screen.Login.route) {
                        popUpTo(Screen.Dashboard.route) { inclusive = true }
                    }
                }
            )
        }
    }
}
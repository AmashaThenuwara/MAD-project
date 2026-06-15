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
import com.example.agriscout.ui.navigation.Screen
import com.example.agriscout.ui.screens.AddFarmScreen
import com.example.agriscout.ui.screens.DiseaseReportScreen
import com.example.agriscout.ui.screens.FarmListScreen
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

    // Instantiate our shared local architecture view model factory
    val context = LocalContext.current
    val agriViewModel: AgriViewModel = viewModel(
        factory = androidx.lifecycle.ViewModelProvider.AndroidViewModelFactory.getInstance(context.applicationContext as Application)
    )

    NavHost(
        navController = navController,
        startDestination = Screen.FarmList.route
    ) {
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
                onNavigateBack = { navController.popBackStack() }
            )
        }
    }
}

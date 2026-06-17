package com.example.agriscout.ui.navigation

sealed class Screen(val route: String) {
    object Splash : Screen("splash_screen")
    object Login : Screen("login_screen")
    object Dashboard : Screen("dashboard_screen")
    object FarmList : Screen("farm_list_screen")
    object AddFarm : Screen("add_farm_screen")
    object EditFarm : Screen("edit_farm_screen/{farmId}") {
        fun createRoute(farmId: Long) = "edit_farm_screen/$farmId"
    }
    object DiseaseReport : Screen("disease_report_screen/{farmId}") {
        fun createRoute(farmId: Long) = "disease_report_screen/$farmId"
    }
    object ReportsList : Screen("reports_list_screen")
    object Weather : Screen("weather_screen")
    object Profile : Screen("profile_screen")
    object Sync : Screen("sync_screen")
}
package com.example.agriscout.ui.navigation

sealed class Screen(val route: String) {
    object Splash : Screen("splash_screen")
    object Login : Screen("login_screen")
    object Dashboard : Screen("dashboard_screen")
    object FarmList : Screen("farm_list_screen")
    object AddFarm : Screen("add_farm_screen")
    object FarmDetails : Screen("farm_details_screen/{farmId}") {
        fun createRoute(farmId: Long) = "farm_details_screen/$farmId"
    }
    object EditFarm : Screen("edit_farm_screen/{farmId}") {
        fun createRoute(farmId: Long) = "edit_farm_screen/$farmId"
    }
    object CropDetail : Screen("crop_detail_screen/{cropId}") {
        fun createRoute(cropId: Long) = "crop_detail_screen/$cropId"
    }
    object AddEditCrop : Screen("add_edit_crop_screen/{farmId}?cropId={cropId}") {
        fun createRoute(farmId: Long, cropId: Long? = null): String {
            return if (cropId != null) "add_edit_crop_screen/$farmId?cropId=$cropId"
            else "add_edit_crop_screen/$farmId"
        }
    }
    object DiseaseReport : Screen("disease_report_screen/{farmId}") {
        fun createRoute(farmId: Long) = "disease_report_screen/$farmId"
    }
    object ReportsList : Screen("reports_list_screen")
    object Weather : Screen("weather_screen")
    object Profile : Screen("profile_screen")
    object Sync : Screen("sync_screen")
}

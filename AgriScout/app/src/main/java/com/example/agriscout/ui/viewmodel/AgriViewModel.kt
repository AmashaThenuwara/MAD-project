package com.example.agriscout.ui.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.agriscout.data.local.AppDatabase
import com.example.agriscout.data.local.entity.DiseaseReportEntity
import com.example.agriscout.data.local.entity.FarmEntity
import com.example.agriscout.data.repository.DiseaseReportRepository
import com.example.agriscout.data.repository.FarmRepository
import com.example.agriscout.data.repository.WeatherRepository
import com.example.agriscout.data.repository.WeatherResult
import com.example.agriscout.data.network.WeatherResponse
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

class AgriViewModel(application: Application) : AndroidViewModel(application) {

    private val db = AppDatabase.getDatabase(application)
    private val farmRepo = FarmRepository(db.farmDao())
    private val reportRepo = DiseaseReportRepository(db.diseaseReportDao())
    private val weatherRepo = WeatherRepository()

    // --- Farm state ---
    val farmsList: StateFlow<List<FarmEntity>> = farmRepo.allFarms
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val totalFarmCount: StateFlow<Int> = farmRepo.totalFarmCount
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), 0)

    // --- Disease Report state ---
    val allReports: StateFlow<List<DiseaseReportEntity>> = reportRepo.allReports
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val totalReportCount: StateFlow<Int> = reportRepo.totalReportCount
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), 0)

    val pendingSyncCount: StateFlow<Int> = reportRepo.pendingSyncCount
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), 0)

    val syncedCount: StateFlow<Int> = reportRepo.syncedCount
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), 0)

    // --- Weather state ---
    private val _weatherState = MutableStateFlow<WeatherResult>(WeatherResult.Loading)
    val weatherState: StateFlow<WeatherResult> = _weatherState

    // --- Farm operations ---
    fun insertFarm(name: String, farmer: String, location: String, cropType: String, stage: String) {
        viewModelScope.launch {
            farmRepo.insertFarm(
                FarmEntity(
                    farmName = name,
                    farmerName = farmer,
                    locationName = location,
                    cropType = cropType,
                    growthStage = stage
                )
            )
        }
    }

    fun deleteFarm(farm: FarmEntity) {
        viewModelScope.launch { farmRepo.deleteFarm(farm) }
    }

    // --- Disease Report operations ---
    fun insertReport(farmId: Long, diseaseName: String, notes: String,
                     imagePath: String, latitude: Double, longitude: Double) {
        viewModelScope.launch {
            reportRepo.insertReport(
                DiseaseReportEntity(
                    farmId = farmId,
                    diseaseName = diseaseName,
                    notes = notes,
                    imagePath = imagePath,
                    latitude = latitude,
                    longitude = longitude
                )
            )
        }
    }

    fun deleteReport(report: DiseaseReportEntity) {
        viewModelScope.launch { reportRepo.deleteReport(report) }
    }

    fun getReportsForFarm(farmId: Long) = reportRepo.getReportsForFarm(farmId)

    // --- Weather operations ---
    fun fetchWeather(latitude: Double, longitude: Double) {
        viewModelScope.launch {
            _weatherState.value = WeatherResult.Loading
            _weatherState.value = weatherRepo.getWeather(latitude, longitude)
        }
    }
}
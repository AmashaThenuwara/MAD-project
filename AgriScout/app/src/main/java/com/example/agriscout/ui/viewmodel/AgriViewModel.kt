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
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch

class AgriViewModel(application: Application) : AndroidViewModel(application) {

    private val db = AppDatabase.getDatabase(application)
    private val farmRepo = FarmRepository(db.farmDao())
    private val reportRepo = DiseaseReportRepository(db.diseaseReportDao())
    private val weatherRepo = WeatherRepository()

    // Farm data stream
    val farmsList: StateFlow<List<FarmEntity>> = farmRepo.allFarms
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val totalFarmCount: StateFlow<Int> = farmRepo.totalFarmCount
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), 0)

    // Disease report data stream
    val allReports: StateFlow<List<DiseaseReportEntity>> = reportRepo.allReports
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val totalReportCount: StateFlow<Int> = reportRepo.totalReportCount
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), 0)

    // Calculate total synced records
    val syncedCount: StateFlow<Int> = combine(
        farmRepo.allFarms.map { it.count { f -> f.isSynced } },
        reportRepo.syncedCount
    ) { f, r -> f + r }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), 0)

    // Calculate total pending sync records
    val pendingSyncCount: StateFlow<Int> = combine(
        farmRepo.allFarms.map { it.count { f -> !f.isSynced } },
        reportRepo.pendingSyncCount
    ) { f, r -> f + r }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), 0)

    val hasUnsyncedData: StateFlow<Boolean> = pendingSyncCount.map { it > 0 }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), false)

    private val _weatherState = MutableStateFlow<WeatherResult>(WeatherResult.Loading)
    val weatherState: StateFlow<WeatherResult> = _weatherState

    fun insertFarm(name: String, farmer: String, location: String, crop: String, stage: String) {
        viewModelScope.launch {
            farmRepo.insertFarm(
                FarmEntity(
                    farmName = name,
                    farmerName = farmer,
                    locationName = location,
                    cropType = crop,
                    growthStage = stage
                )
            )
        }
    }

    fun updateFarm(farm: FarmEntity) {
        viewModelScope.launch {
            farmRepo.updateFarm(farm)
        }
    }

    fun deleteFarm(farm: FarmEntity) {
        viewModelScope.launch {
            farmRepo.deleteFarm(farm)
        }
    }

    fun insertReport(farmId: Long, diseaseName: String, notes: String, imagePath: String, lat: Double, lon: Double) {
        viewModelScope.launch {
            reportRepo.insertReport(
                DiseaseReportEntity(
                    farmId = farmId,
                    diseaseName = diseaseName,
                    notes = notes,
                    imagePath = imagePath,
                    latitude = lat,
                    longitude = lon
                )
            )
        }
    }

    fun updateReport(report: DiseaseReportEntity) {
        viewModelScope.launch {
            reportRepo.updateReport(report)
        }
    }

    fun deleteReport(report: DiseaseReportEntity) {
        viewModelScope.launch {
            reportRepo.deleteReport(report)
        }
    }

    fun fetchWeather(lat: Double, lon: Double) {
        viewModelScope.launch {
            _weatherState.value = WeatherResult.Loading
            _weatherState.value = weatherRepo.getWeather(lat, lon)
        }
    }
}

package com.example.agriscout.ui.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.agriscout.data.local.AppDatabase
import com.example.agriscout.data.local.entity.CropEntity
import com.example.agriscout.data.local.entity.DiseaseReportEntity
import com.example.agriscout.data.local.entity.FarmEntity
import com.example.agriscout.data.repository.CropRepository
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
    private val cropRepo = CropRepository(db.cropDao())
    private val weatherRepo = WeatherRepository()

    val farmsList: StateFlow<List<FarmEntity>> = farmRepo.allFarms
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val allCrops: StateFlow<List<CropEntity>> = cropRepo.allCrops
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val totalFarmCount: StateFlow<Int> = farmRepo.totalFarmCount
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), 0)

    val allReports: StateFlow<List<DiseaseReportEntity>> = reportRepo.allReports
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val totalReportCount: StateFlow<Int> = reportRepo.totalReportCount
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), 0)

    // Sync Counts
    val syncedCount: StateFlow<Int> = combine(
        farmRepo.allFarms.map { it.count { f -> f.isSynced } },
        cropRepo.allCrops.map { it.count { c -> c.isSynced } },
        reportRepo.syncedCount
    ) { f, c, r -> f + c + r }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), 0)

    val pendingSyncCount: StateFlow<Int> = combine(
        farmRepo.allFarms.map { it.count { f -> !f.isSynced } },
        cropRepo.allCrops.map { it.count { c -> !c.isSynced } },
        reportRepo.pendingSyncCount
    ) { f, c, r -> f + c + r }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), 0)

    val hasUnsyncedData: StateFlow<Boolean> = pendingSyncCount.map { it > 0 }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), false)

    private val _weatherState = MutableStateFlow<WeatherResult>(WeatherResult.Loading)
    val weatherState: StateFlow<WeatherResult> = _weatherState

    // --- Farm Methods ---
    suspend fun insertFarm(name: String, farmer: String, location: String, landSize: Double, details: String, lat: Double = 0.0, lon: Double = 0.0): Long {
        return farmRepo.insertFarm(
            FarmEntity(
                farmName = name,
                farmerName = farmer,
                locationName = location,
                landSize = landSize,
                locationDetails = details,
                latitude = lat,
                longitude = lon
            )
        )
    }

    fun getFarmById(id: Long): Flow<FarmEntity?> = farmRepo.getFarmById(id)
    fun updateFarm(farm: FarmEntity) = viewModelScope.launch { farmRepo.updateFarm(farm.copy(isSynced = false)) }
    
    fun deleteFarm(farm: FarmEntity) = viewModelScope.launch { 
        farmRepo.deleteFarm(farm)
        try {
            com.example.agriscout.data.firebase.FirestoreManager().deleteFarm(farm.farmId)
        } catch (e: Exception) {
            // Ignore if offline
        }
    }

    // --- Crop Methods ---
    fun getCropsForFarm(farmId: Long): Flow<List<CropEntity>> = cropRepo.getCropsForFarm(farmId)

    suspend fun insertCrop(
        farmId: Long,
        commonName: String,
        scientificName: String,
        category: String,
        origin: String,
        soilType: String,
        fertilizerType: String,
        imagePath: String = ""
    ): Long {
        return cropRepo.insertCrop(
            CropEntity(
                farmId = farmId,
                commonName = commonName,
                scientificName = scientificName,
                category = category,
                origin = origin,
                soilType = soilType,
                fertilizerType = fertilizerType,
                imagePath = imagePath,
                isSynced = false
            )
        )
    }

    fun updateCrop(crop: CropEntity) = viewModelScope.launch { cropRepo.updateCrop(crop.copy(isSynced = false)) }
    
    fun deleteCrop(crop: CropEntity) = viewModelScope.launch { 
        cropRepo.deleteCrop(crop)
        try {
            com.example.agriscout.data.firebase.FirestoreManager().deleteCrop(crop.cropId)
        } catch (e: Exception) {
            // Ignore if offline
        }
    }

    suspend fun getCropById(id: Long): CropEntity? = cropRepo.getCropById(id)

    // --- Report Methods ---
    fun insertReport(farmId: Long, cropId: Long, diseaseName: String, notes: String, imagePath: String, lat: Double, lon: Double) {
        viewModelScope.launch {
            reportRepo.insertReport(
                DiseaseReportEntity(
                    farmId = farmId,
                    cropId = cropId,
                    diseaseName = diseaseName,
                    notes = notes,
                    imagePath = imagePath,
                    latitude = lat,
                    longitude = lon
                )
            )
        }
    }

    fun updateReport(report: DiseaseReportEntity) = viewModelScope.launch { reportRepo.updateReport(report.copy(syncStatus = "PENDING")) }
    fun deleteReport(report: DiseaseReportEntity) = viewModelScope.launch { reportRepo.deleteReport(report) }

    // --- Weather ---
    fun fetchWeather(lat: Double, lon: Double) {
        viewModelScope.launch {
            _weatherState.value = WeatherResult.Loading
            _weatherState.value = weatherRepo.getWeather(lat, lon)
        }
    }
}

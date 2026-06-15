package com.example.agriscout.ui.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.agriscout.data.local.AppDatabase
import com.example.agriscout.data.local.entity.FarmEntity
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

class AgriViewModel(application: Application) : AndroidViewModel(application) {
    private val farmDao = AppDatabase.getDatabase(application).farmDao()

    val farmsList: StateFlow<List<FarmEntity>> = farmDao.getAllFarms()
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = emptyList()
        )

    fun insertFarm(name: String, farmer: String, location: String, cropType: String, stage: String) {
        viewModelScope.launch {
            val newFarm = FarmEntity(
                farmName = name,
                farmerName = farmer,
                locationName = location,
                cropType = cropType,
                growthStage = stage
            )
            farmDao.insertFarm(newFarm)
        }
    }
}

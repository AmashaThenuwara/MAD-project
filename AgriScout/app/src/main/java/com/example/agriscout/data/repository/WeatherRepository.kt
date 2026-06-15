package com.example.agriscout.data.repository

import com.example.agriscout.BuildConfig
import com.example.agriscout.data.network.RetrofitInstance
import com.example.agriscout.data.network.WeatherResponse

sealed class WeatherResult {
    data class Success(val data: WeatherResponse) : WeatherResult()
    data class Error(val message: String) : WeatherResult()
    object Loading : WeatherResult()
}

class WeatherRepository {
    suspend fun getWeather(latitude: Double, longitude: Double): WeatherResult {
        return try {
            val response = RetrofitInstance.weatherApi.getCurrentWeather(
                latitude = latitude,
                longitude = longitude,
                apiKey = BuildConfig.WEATHER_API_KEY
            )
            WeatherResult.Success(response)
        } catch (e: Exception) {
            WeatherResult.Error(e.message ?: "Unknown error fetching weather")
        }
    }
}

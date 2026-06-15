package com.example.agriscout.data.network

import com.google.gson.annotations.SerializedName

data class WeatherResponse(
    @SerializedName("name") val cityName: String,
    @SerializedName("main") val main: MainData,
    @SerializedName("weather") val weather: List<WeatherDescription>,
    @SerializedName("wind") val wind: WindData,
    @SerializedName("sys") val sys: SysData
)

data class MainData(
    @SerializedName("temp") val tempKelvin: Double,
    @SerializedName("feels_like") val feelsLikeKelvin: Double,
    @SerializedName("humidity") val humidity: Int,
    @SerializedName("pressure") val pressure: Int
) {
    val tempCelsius: Double get() = tempKelvin - 273.15
    val feelsLikeCelsius: Double get() = feelsLikeKelvin - 273.15
}

data class WeatherDescription(
    @SerializedName("main") val main: String,
    @SerializedName("description") val description: String,
    @SerializedName("icon") val icon: String
)

data class WindData(
    @SerializedName("speed") val speed: Double,
    @SerializedName("deg") val degree: Int
)

data class SysData(
    @SerializedName("country") val country: String
)

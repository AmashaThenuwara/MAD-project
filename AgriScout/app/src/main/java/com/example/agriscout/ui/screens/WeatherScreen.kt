package com.example.agriscout.ui.screens

import android.Manifest
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.agriscout.data.repository.WeatherResult
import com.example.agriscout.ui.viewmodel.AgriViewModel
import com.google.accompanist.permissions.ExperimentalPermissionsApi
import com.google.accompanist.permissions.isGranted
import com.google.accompanist.permissions.rememberMultiplePermissionsState
import com.google.android.gms.location.LocationServices
import com.google.android.gms.location.Priority
import com.example.agriscout.ui.components.AnimatedButton
import com.example.agriscout.ui.components.ShinyCard

@OptIn(ExperimentalPermissionsApi::class, ExperimentalMaterial3Api::class)
@Composable
fun WeatherScreen(
    viewModel: AgriViewModel,
    onNavigateBack: () -> Unit
) {
    val context = LocalContext.current
    val locationPermissions = rememberMultiplePermissionsState(
        listOf(
            Manifest.permission.ACCESS_FINE_LOCATION,
            Manifest.permission.ACCESS_COARSE_LOCATION
        )
    )
    val isLocationGranted = locationPermissions.permissions.any { it.status.isGranted }

    // Collect weather state from ViewModel
    val weatherState by viewModel.weatherState.collectAsState()

    // Fetch weather using device GPS
    fun fetchWeatherForCurrentLocation() {
        if (!isLocationGranted) {
            locationPermissions.launchMultiplePermissionRequest()
            return
        }
        val fusedLocationClient = LocationServices.getFusedLocationProviderClient(context)
        try {
            fusedLocationClient.getCurrentLocation(Priority.PRIORITY_HIGH_ACCURACY, null)
                .addOnSuccessListener { location ->
                    if (location != null) {
                        viewModel.fetchWeather(location.latitude, location.longitude)
                    }
                }
        } catch (e: SecurityException) {
            // Permission denied at runtime
        }
    }

    // Auto-fetch when screen opens or permissions are granted
    LaunchedEffect(isLocationGranted) {
        if (isLocationGranted) {
            fetchWeatherForCurrentLocation()
        } else {
            locationPermissions.launchMultiplePermissionRequest()
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Weather at Farm Location", fontWeight = FontWeight.Bold) },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Back")
                    }
                },
                actions = {
                    // Refresh button to re-fetch weather
                    IconButton(onClick = { fetchWeatherForCurrentLocation() }) {
                        Icon(Icons.Default.Refresh, contentDescription = "Refresh")
                    }
                }
            )
        }
    ) { innerPadding ->
        Box(
            modifier = Modifier.fillMaxSize().padding(innerPadding).padding(24.dp),
            contentAlignment = Alignment.Center
        ) {
            when (val state = weatherState) {
                is WeatherResult.Loading -> {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        CircularProgressIndicator()
                        Spacer(modifier = Modifier.height(12.dp))
                        Text("Fetching weather...", color = MaterialTheme.colorScheme.onSurfaceVariant)
                    }
                }

                is WeatherResult.Error -> {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Text("Failed to load weather", fontSize = 18.sp, fontWeight = FontWeight.Bold)
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(state.message, color = MaterialTheme.colorScheme.error, fontSize = 13.sp)
                        Spacer(modifier = Modifier.height(16.dp))
                        AnimatedButton(onClick = { fetchWeatherForCurrentLocation() }) {
                            Text("Retry")
                        }
                    }
                }

                is WeatherResult.Success -> {
                    val data = state.data
                    Column(
                        modifier = Modifier.fillMaxWidth(),
                        verticalArrangement = Arrangement.spacedBy(16.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        // Location name
                        Text(
                            text = "${data.cityName}, ${data.sys.country}",
                            fontSize = 22.sp,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.primary
                        )

                        // Temperature — converted from Kelvin to Celsius in WeatherModels.kt
                        Text(
                            text = "${"%.1f".format(data.main.tempCelsius)}°C",
                            fontSize = 48.sp,
                            fontWeight = FontWeight.Bold
                        )

                        Text(
                            text = "Feels like ${"%.1f".format(data.main.feelsLikeCelsius)}°C",
                            fontSize = 16.sp,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )

                        // Weather description
                        if (data.weather.isNotEmpty()) {
                            Text(
                                text = data.weather[0].description.replaceFirstChar { it.uppercase() },
                                fontSize = 18.sp
                            )
                        }

                        Divider(modifier = Modifier.padding(vertical = 8.dp))

                        // Detail cards
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceEvenly
                        ) {
                            WeatherDetailCard(label = "Humidity", value = "${data.main.humidity}%")
                            WeatherDetailCard(label = "Pressure", value = "${data.main.pressure} hPa")
                            WeatherDetailCard(label = "Wind", value = "${"%.1f".format(data.wind.speed)} m/s")
                        }
                    }
                }
            }
        }
    }
}

// Reusable card for one weather stat
@Composable
fun WeatherDetailCard(label: String, value: String) {
    ShinyCard(
        modifier = Modifier.padding(4.dp)
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(text = value, fontSize = 18.sp, fontWeight = FontWeight.Bold)
            Text(text = label, fontSize = 12.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
        }
    }
}
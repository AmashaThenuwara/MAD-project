package com.example.agriscout.ui.screens

import android.Manifest
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.platform.LocalContext
import com.example.agriscout.data.repository.WeatherResult
import com.example.agriscout.ui.viewmodel.AgriViewModel
import com.google.accompanist.permissions.ExperimentalPermissionsApi
import com.google.accompanist.permissions.isGranted
import com.google.accompanist.permissions.rememberPermissionState

@OptIn(ExperimentalMaterial3Api::class, ExperimentalPermissionsApi::class)
@Composable
fun WeatherScreen(
    viewModel: AgriViewModel,
    onNavigateBack: () -> Unit
) {
    val context = LocalContext.current
    val weatherState by viewModel.weatherState.collectAsState()
    val locationPermission = rememberPermissionState(Manifest.permission.ACCESS_FINE_LOCATION)

    LaunchedEffect(locationPermission.status.isGranted) {
        if (locationPermission.status.isGranted) {
            getLocation(context) { lat, lon ->
                viewModel.fetchWeather(lat, lon)
            }
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Weather Conditions", fontWeight = FontWeight.Bold) },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Back")
                    }
                }
            )
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(24.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            if (!locationPermission.status.isGranted) {
                Text("Location permission needed to fetch weather")
                Spacer(Modifier.height(12.dp))
                Button(onClick = { locationPermission.launchPermissionRequest() }) {
                    Text("Grant Permission")
                }
            } else {
                when (val state = weatherState) {
                    is WeatherResult.Loading -> {
                        CircularProgressIndicator()
                        Spacer(Modifier.height(16.dp))
                        Text("Fetching weather data...")
                    }
                    is WeatherResult.Error -> {
                        Text("⚠️ ${state.message}",
                            color = MaterialTheme.colorScheme.error)
                        Spacer(Modifier.height(16.dp))
                        Button(onClick = {
                            getLocation(context) { lat, lon ->
                                viewModel.fetchWeather(lat, lon)
                            }
                        }) { Text("Retry") }
                    }
                    is WeatherResult.Success -> {
                        val data = state.data
                        Text(
                            text = "📍 ${data.cityName}, ${data.sys.country}",
                            fontSize = 20.sp,
                            fontWeight = FontWeight.Bold
                        )
                        Spacer(Modifier.height(8.dp))
                        Text(
                            text = data.weather.firstOrNull()?.description?.replaceFirstChar
                            { it.uppercase() } ?: "",
                            fontSize = 16.sp,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        Spacer(Modifier.height(32.dp))

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceEvenly
                        ) {
                            WeatherInfoCard("🌡️ Temperature",
                                "%.1f°C".format(data.main.tempCelsius))
                            WeatherInfoCard("💧 Humidity",
                                "${data.main.humidity}%")
                        }
                        Spacer(Modifier.height(16.dp))
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceEvenly
                        ) {
                            WeatherInfoCard("🌬️ Wind Speed",
                                "${data.wind.speed} m/s")
                            WeatherInfoCard("🌡️ Feels Like",
                                "%.1f°C".format(data.main.feelsLikeCelsius))
                        }
                        Spacer(Modifier.height(24.dp))
                        Button(onClick = {
                            getLocation(context) { lat, lon ->
                                viewModel.fetchWeather(lat, lon)
                            }
                        }) { Text("Refresh") }
                    }
                }
            }
        }
    }
}

@Composable
fun WeatherInfoCard(label: String, value: String) {
    Card(elevation = CardDefaults.cardElevation(2.dp)) {
        Column(
            modifier = Modifier.padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(value, fontSize = 22.sp, fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.primary)
            Spacer(Modifier.height(4.dp))
            Text(label, fontSize = 12.sp,
                color = MaterialTheme.colorScheme.onSurfaceVariant)
        }
    }
}
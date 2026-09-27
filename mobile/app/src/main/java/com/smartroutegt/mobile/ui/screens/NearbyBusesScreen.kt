package com.smartroutegt.mobile.ui.screens

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Card
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.compose.material3.OutlinedButton
import android.Manifest
import android.content.pm.PackageManager
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.platform.LocalContext
import androidx.core.content.ContextCompat
import com.google.android.gms.location.LocationServices
import androidx.annotation.RequiresPermission
import androidx.compose.material3.Button
import androidx.compose.runtime.rememberCoroutineScope
import kotlinx.coroutines.launch
import com.smartroutegt.mobile.data.remote.ApiClient
import com.google.gson.Gson
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll

@RequiresPermission(
    anyOf = [
        Manifest.permission.ACCESS_FINE_LOCATION,
        Manifest.permission.ACCESS_COARSE_LOCATION
    ]
)

@Composable
fun NearbyBusesScreen(
    onBack: () -> Unit
) {

    val context = LocalContext.current

    val fusedLocationClient = remember {
        LocationServices.getFusedLocationProviderClient(context)
    }

    var locationPermissionGranted by remember {
        mutableStateOf(
            ContextCompat.checkSelfPermission(
                context,
                Manifest.permission.ACCESS_FINE_LOCATION
            ) == PackageManager.PERMISSION_GRANTED
        )
    }

    val locationPermissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestPermission()
    ) { granted ->
        locationPermissionGranted = granted
    }
    var passengerLatitude by remember {
        mutableStateOf<Double?>(null)
    }

    var passengerLongitude by remember {
        mutableStateOf<Double?>(null)
    }

    var locationError by remember {
        mutableStateOf<String?>(null)
    }

    var isLoadingBus by remember {
        mutableStateOf(false)
    }

    var nearestBus by remember {
        mutableStateOf<com.smartroutegt.mobile.data.remote.BusLocationDto?>(null)
    }

    var busesWithLocation by remember {
        mutableStateOf<List<com.smartroutegt.mobile.data.remote.BusMonitoringDto>>(emptyList())
    }

    var busSearchError by remember {
        mutableStateOf<String?>(null)
    }

    val coroutineScope = rememberCoroutineScope()

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(24.dp)
    ) {

        OutlinedButton(
            onClick = onBack
        ) {
            Text(text = "← Volver al menú")
        }

        Text(
            text = "Buses cercanos",
            modifier = Modifier.padding(top = 16.dp)
        )
        Button(
            onClick = {
                if (!locationPermissionGranted) {
                    locationPermissionLauncher.launch(
                        Manifest.permission.ACCESS_FINE_LOCATION
                    )
                } else {
                    locationError = null

                    fusedLocationClient.lastLocation
                        .addOnSuccessListener { location ->
                            if (location != null) {
                                passengerLatitude = location.latitude
                                passengerLongitude = location.longitude

                                coroutineScope.launch {
                                    isLoadingBus = true
                                    busSearchError = null
                                    nearestBus = null
                                    busesWithLocation = emptyList()

                                    try {
                                        val response = ApiClient.nearestBusApiService.getNearestBus(
                                            latitud = location.latitude,
                                            longitud = location.longitude
                                        )

                                        if (response.isSuccessful) {
                                            val responseText = response.body()?.string()

                                            nearestBus = if (responseText.isNullOrBlank()) {
                                                null
                                            } else {
                                                Gson().fromJson(
                                                    responseText,
                                                    com.smartroutegt.mobile.data.remote.BusLocationDto::class.java
                                                )
                                            }
                                        } else {
                                            busSearchError = "No se pudo consultar el bus más cercano"
                                        }

                                        val monitoringBuses =
                                            ApiClient.busMonitoringApiService.getBuses()

                                        busesWithLocation = monitoringBuses.filter { bus ->
                                            bus.latitud != null && bus.longitud != null
                                        }

                                    } catch (e: Exception) {
                                        busSearchError = "Error: ${e.message}"
                                    } finally {
                                        isLoadingBus = false
                                    }
                                }
                            }else {
                                locationError = "No se pudo obtener la ubicación actual"
                            }
                        }
                        .addOnFailureListener {
                            locationError = "No se pudo obtener la ubicación actual"
                        }
                }
            },
            modifier = Modifier
                .fillMaxWidth()
                .padding(top = 24.dp)
        ) {
            Text(text = "Obtener mi ubicación")
        }

        if (passengerLatitude != null && passengerLongitude != null) {
            Text(
                text = "Ubicación obtenida: $passengerLatitude, $passengerLongitude",
                modifier = Modifier.padding(top = 16.dp)
            )
        }

        if (passengerLatitude != null && passengerLongitude != null) {
            NearbyBusMap(
                passengerLatitude = passengerLatitude!!,
                passengerLongitude = passengerLongitude!!,
                nearestBus = nearestBus,
                busesWithLocation = busesWithLocation,
                modifier = Modifier.padding(top = 16.dp)
            )
        }

        locationError?.let { error ->
            Text(
                text = error,
                modifier = Modifier.padding(top = 16.dp)
            )
        }

        when {
            isLoadingBus -> {
                Text(
                    text = "Buscando bus cercano...",
                    modifier = Modifier.padding(top = 24.dp)
                )
            }

            busSearchError != null -> {
                Text(
                    text = busSearchError ?: "",
                    modifier = Modifier.padding(top = 24.dp)
                )
            }

            passengerLatitude != null &&
                    passengerLongitude != null &&
                    nearestBus == null -> {
                Text(
                    text = "No hay buses disponibles en este momento",
                    modifier = Modifier.padding(top = 24.dp)
                )
            }

            nearestBus != null -> {
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(top = 24.dp)
                ) {
                    Column(
                        modifier = Modifier.padding(16.dp)
                    ) {
                        Text(text = "Bus: ${nearestBus!!.bus.codigo}")
                        Text(text = "Placa: ${nearestBus!!.bus.placa}")
                        Text(text = "Velocidad: %.1f km/h".format(nearestBus!!.velocidad))
                        Text(
                            text = "Ubicación: ${nearestBus!!.latitud}, ${nearestBus!!.longitud}"
                        )
                    }
                }
            }
        }
    }
}
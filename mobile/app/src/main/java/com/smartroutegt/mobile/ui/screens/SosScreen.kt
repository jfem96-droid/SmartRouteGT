package com.smartroutegt.mobile.ui.screens

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
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
import androidx.compose.runtime.rememberCoroutineScope
import kotlinx.coroutines.launch
import com.smartroutegt.mobile.data.remote.ApiClient
import com.smartroutegt.mobile.data.remote.PassengerAlertRequest

@Composable
fun SosScreen(
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

    var isSendingSos by remember {
        mutableStateOf(false)
    }

    var sosMessage by remember {
        mutableStateOf<String?>(null)
    }

    var sosError by remember {
        mutableStateOf<String?>(null)
    }

    val coroutineScope = rememberCoroutineScope()

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(24.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {

        OutlinedButton(
            onClick = onBack
        ) {
            Text(text = "← Volver al menú")
        }

        Text(
            text = "SOS",
            modifier = Modifier.padding(top = 16.dp)
        )

        Text(
            text = "Usa esta opción solo en una situación de emergencia.",
            modifier = Modifier.padding(top = 24.dp)
        )

        Button(
            onClick = {
                sosMessage = null
                sosError = null

                if (!locationPermissionGranted) {
                    locationPermissionLauncher.launch(
                        Manifest.permission.ACCESS_FINE_LOCATION
                    )
                } else {
                    fusedLocationClient.lastLocation
                        .addOnSuccessListener { location ->

                            coroutineScope.launch {
                                isSendingSos = true

                                try {
                                    val alert = PassengerAlertRequest(
                                        tipo = "SOS",
                                        descripcion = "Alerta SOS enviada desde la aplicación móvil",
                                        latitud = location?.latitude,
                                        longitud = location?.longitude
                                    )

                                    val response =
                                        ApiClient.passengerAlertApiService.createAlert(alert)

                                    sosMessage =
                                        "Alerta SOS enviada correctamente. ID: ${response.id}"

                                } catch (e: Exception) {
                                    sosError = "No se pudo enviar la alerta SOS"
                                } finally {
                                    isSendingSos = false
                                }
                            }
                        }
                        .addOnFailureListener {
                            sosError = "No se pudo obtener la ubicación"
                        }
                }
            },

            modifier = Modifier
                .fillMaxWidth()
                .padding(top = 24.dp)
        ) {
            Text(text = "Enviar alerta SOS")
        }

        when {
            isSendingSos -> {
                Text(
                    text = "Enviando alerta SOS...",
                    modifier = Modifier.padding(top = 16.dp)
                )
            }

            sosMessage != null -> {
                Text(
                    text = sosMessage ?: "",
                    modifier = Modifier.padding(top = 16.dp)
                )
            }

            sosError != null -> {
                Text(
                    text = sosError ?: "",
                    modifier = Modifier.padding(top = 16.dp)
                )
            }
        }
    }
    }
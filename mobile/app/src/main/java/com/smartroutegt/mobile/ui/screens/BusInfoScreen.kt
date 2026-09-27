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
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import com.smartroutegt.mobile.data.remote.ApiClient
import com.smartroutegt.mobile.data.remote.BusMonitoringDto
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll

@Composable
fun BusInfoScreen(
    onBack: () -> Unit
) {
    var buses by remember {
        mutableStateOf<List<BusMonitoringDto>>(emptyList())
    }

    var isLoading by remember {
        mutableStateOf(true)
    }

    var loadError by remember {
        mutableStateOf<String?>(null)
    }

    LaunchedEffect(Unit) {
        try {
            buses = ApiClient.busMonitoringApiService.getBuses()
            loadError = null
        } catch (e: Exception) {
            buses = emptyList()
            loadError = "No se pudo cargar la información de los buses"
        } finally {
            isLoading = false
        }
    }

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
            text = "Información del bus",
            modifier = Modifier.padding(top = 16.dp)
        )

        when {
            isLoading -> {
                Text(
                    text = "Cargando información de buses...",
                    modifier = Modifier.padding(top = 24.dp)
                )
            }

            loadError != null -> {
                Text(
                    text = loadError ?: "",
                    modifier = Modifier.padding(top = 24.dp)
                )
            }

            buses.isEmpty() -> {
                Text(
                    text = "No hay buses disponibles",
                    modifier = Modifier.padding(top = 24.dp)
                )
            }

            else -> {
                buses.forEach { bus ->
                    Card(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(top = 16.dp)
                    ) {
                        Column(
                            modifier = Modifier.padding(16.dp)
                        ) {
                            Text(text = "Bus: ${bus.codigo}")
                            Text(text = "Placa: ${bus.placa}")
                            Text(text = "Ruta: ${bus.ruta ?: "Sin ruta asignada"}")
                            Text(text = "Piloto: ${bus.piloto ?: "Sin piloto asignado"}")
                            Text(text = "Estado: ${if (bus.activo) "Activo" else "Inactivo"}")
                            Text(
                                text = "Velocidad: ${
                                    bus.velocidad?.let { "%.1f km/h".format(it) }
                                        ?: "Sin información"
                                }"
                            )
                        }
                    }
                }
            }
        }
    }
}
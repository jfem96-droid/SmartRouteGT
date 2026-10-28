package com.smartroutegt.mobile.ui.screens

import com.smartroutegt.mobile.ui.theme.SmartRoutePalette

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.smartroutegt.mobile.data.remote.ApiClient
import com.smartroutegt.mobile.data.remote.BusMonitoringDto
import kotlinx.coroutines.delay

private val fleetBlue = SmartRoutePalette.Panel
private val fleetBackground = SmartRoutePalette.Night

@Composable
fun BusInfoScreen(onBack: () -> Unit) {
    var buses by remember { mutableStateOf<List<BusMonitoringDto>>(emptyList()) }
    var loading by remember { mutableStateOf(true) }
    var error by remember { mutableStateOf<String?>(null) }

    LaunchedEffect(Unit) {
        while (true) {
            try {
                buses = ApiClient.busMonitoringApiService.getBuses()
                error = null
            } catch (e: Exception) {
                error = "No se pudo actualizar la flota: ${e.message}"
            } finally {
                loading = false
            }
            delay(10_000)
        }
    }

    Column(
        Modifier
            .fillMaxSize()
            .background(fleetBackground)
            .verticalScroll(rememberScrollState())
    ) {
        PassengerV3TopBar(
            "Buses en tiempo real",
            "Estado de la flota",
            onBack
        )

        Column(
            Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            Card(
                colors = CardDefaults.cardColors(containerColor = fleetBlue),
                shape = RoundedCornerShape(20.dp)
            ) {
                Column(
                    Modifier.fillMaxWidth().padding(20.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Text(
                        "Flota SmartRoute GT",
                        color = Color.White,
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.Bold
                    )
                    Text(
                        "${buses.size} buses registrados en el monitoreo",
                        color = Color.White.copy(alpha = 0.9f)
                    )
                    Text(
                        "Actualización automática cada 10 segundos",
                        color = Color.White.copy(alpha = 0.75f),
                        style = MaterialTheme.typography.bodySmall
                    )
                }
            }

            if (loading) {
                LinearProgressIndicator(Modifier.fillMaxWidth())
            }

            if (error != null) {
                Text(
                    error.orEmpty(),
                    color = MaterialTheme.colorScheme.error
                )
            }

            if (!loading && buses.isEmpty()) {
                Text("No hay buses disponibles.", color = SmartRoutePalette.Muted)
            }

            buses.forEach { bus ->
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(18.dp),
                    colors = CardDefaults.cardColors(containerColor = SmartRoutePalette.Panel),
                    elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
                ) {
                    Column(
                        Modifier.padding(18.dp),
                        verticalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        Row(
                            Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Text(
                                bus.codigo,
                                color = SmartRoutePalette.Mint,
                                style = MaterialTheme.typography.titleLarge,
                                fontWeight = FontWeight.Bold
                            )
                            Surface(
                                color = if (bus.activo)
                                    Color(0xFF113F3B)
                                else Color(0xFF4C2634),
                                shape = RoundedCornerShape(50.dp)
                            ) {
                                Text(
                                    if (bus.activo) "Activo" else "Inactivo",
                                    color = if (bus.activo)
                                        SmartRoutePalette.Mint
                                    else SmartRoutePalette.Error,
                                    modifier = Modifier.padding(
                                        horizontal = 12.dp,
                                        vertical = 6.dp
                                    ),
                                    style = MaterialTheme.typography.labelMedium
                                )
                            }
                        }

                        Text(
                            bus.ruta ?: "Sin ruta asignada",
                            fontWeight = FontWeight.SemiBold,
                            color = SmartRoutePalette.Text
                        )
                        Text(
                            "Placa: ${bus.placa}",
                            color = SmartRoutePalette.Muted
                        )
                        Text(
                            "Piloto: ${bus.piloto ?: "Sin piloto asignado"}",
                            color = SmartRoutePalette.Muted
                        )

                        HorizontalDivider(color = SmartRoutePalette.Border)

                        Text(
                            "Velocidad reportada",
                            style = MaterialTheme.typography.labelMedium,
                            color = SmartRoutePalette.Muted
                        )
                        Text(
                            bus.velocidad?.let {
                                "%.1f km/h".format(it)
                            } ?: "Sin información",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold,
                            color = SmartRoutePalette.Mint
                        )
                    }
                }
            }
        }
    }
}

package com.smartroutegt.mobile.ui.screens

import androidx.compose.foundation.layout.Arrangement
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
import androidx.compose.material3.MaterialTheme

@Composable
fun PassengerHomeScreen(
    onSearchRoute: () -> Unit,
    onNearbyBuses: () -> Unit,
    onBusInfo: () -> Unit,
    onEta: () -> Unit,
    onSos: () -> Unit,
    onDriverReport: () -> Unit,
    onRouteReport: () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(24.dp),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {

        Text(
            text = "Menú del pasajero",
            style = MaterialTheme.typography.headlineMedium
        )

        Button(
            onClick = onSearchRoute,
            modifier = Modifier
                .fillMaxWidth()
                .padding(top = 24.dp)
        ) {
            Text(text = "Buscar ruta")
        }

        Button(
            onClick = onNearbyBuses,
            modifier = Modifier
                .fillMaxWidth()
                .padding(top = 12.dp)
        ) {
            Text(text = "Buses cercanos")
        }

        Button(
            onClick = onBusInfo,
            modifier = Modifier
                .fillMaxWidth()
                .padding(top = 12.dp)
        ) {
            Text(text = "Información del bus")
        }

        Button(
            onClick = onEta,
            modifier = Modifier
                .fillMaxWidth()
                .padding(top = 12.dp)
        ) {
            Text(text = "Tiempo estimado de llegada")
        }

        Button(
            onClick = onSos,
            modifier = Modifier
                .fillMaxWidth()
                .padding(top = 12.dp)
        ) {
            Text(text = "SOS")
        }

        Button(
            onClick = onDriverReport,
            modifier = Modifier
                .fillMaxWidth()
                .padding(top = 12.dp)
        ) {
            Text(text = "Reportar conducta del piloto")
        }

        Button(
            onClick = onRouteReport,
            modifier = Modifier
                .fillMaxWidth()
                .padding(top = 12.dp)
        ) {
            Text(text = "Reportar problema de ruta")
        }
    }
}
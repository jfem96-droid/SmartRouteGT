package com.smartroutegt.mobile.ui.screens

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.material3.Button

@Composable
fun DriverDashboardScreen() {
    val busAsignado = "BUS-01"
    val rutaAsignada = "Ruta 101"
    var estadoViaje by remember { mutableStateOf("No iniciado") }
    val sancionesConfirmadas = 2

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(24.dp)
    ) {
        Text(text = "Panel del piloto")

        Spacer(modifier = Modifier.height(24.dp))

        Text(text = "Bus asignado: $busAsignado")

        Spacer(modifier = Modifier.height(12.dp))

        Text(text = "Ruta asignada: $rutaAsignada")

        Spacer(modifier = Modifier.height(12.dp))

        Text(text = "Estado del viaje: $estadoViaje")

        Spacer(modifier = Modifier.height(12.dp))

        Text(text = "Sanciones confirmadas: $sancionesConfirmadas")

        Spacer(modifier = Modifier.height(24.dp))

        Button(
            onClick = {
                estadoViaje = "En curso"
            }
        ) {
            Text(text = "Iniciar viaje")
        }

        Spacer(modifier = Modifier.height(12.dp))

        Button(
            onClick = {
                estadoViaje = "Finalizado"
            }
        ) {
            Text(text = "Finalizar viaje")
        }
    }
}
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

@Composable
fun BusInfoScreen() {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(24.dp)
    ) {

        Text(
            text = "Información del bus"
        )

        Card(
            modifier = Modifier
                .fillMaxWidth()
                .padding(top = 24.dp)
        ) {
            Column(
                modifier = Modifier.padding(16.dp)
            ) {
                Text(text = "Número de bus: pendiente")
                Text(text = "Ruta asignada: pendiente")
                Text(text = "Piloto: pendiente")
                Text(text = "Estado: pendiente")
            }
        }
    }
}
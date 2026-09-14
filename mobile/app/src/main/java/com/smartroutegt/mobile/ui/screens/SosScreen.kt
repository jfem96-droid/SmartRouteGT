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

@Composable
fun SosScreen(
    onBack: () -> Unit
) {
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
            onClick = { },
            modifier = Modifier
                .fillMaxWidth()
                .padding(top = 24.dp)
        ) {
            Text(text = "Enviar alerta SOS")
        }

        Text(
            text = "Función pendiente de conexión con el backend.",
            modifier = Modifier.padding(top = 16.dp)
        )
    }
}
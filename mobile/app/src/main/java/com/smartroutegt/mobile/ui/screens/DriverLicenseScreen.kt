package com.smartroutegt.mobile.ui.screens

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp

@Composable
fun DriverLicenseScreen(
    onLicenseSubmitted: (String) -> Unit
) {
    var licencia by remember {
        mutableStateOf("")
    }

    var error by remember {
        mutableStateOf<String?>(null)
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(24.dp),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(text = "Identificación del piloto")

        OutlinedTextField(
            value = licencia,
            onValueChange = {
                licencia = it
                error = null
            },
            label = {
                Text(text = "Número de licencia")
            },
            modifier = Modifier
                .fillMaxWidth()
                .padding(top = 24.dp),
            singleLine = true
        )

        if (error != null) {
            Text(
                text = error!!,
                modifier = Modifier.padding(top = 8.dp)
            )
        }

        Button(
            onClick = {
                val licenciaIngresada = licencia.trim()

                if (licenciaIngresada.isEmpty()) {
                    error = "Ingresa tu número de licencia"
                } else {
                    onLicenseSubmitted(licenciaIngresada)
                }
            },
            modifier = Modifier
                .fillMaxWidth()
                .padding(top = 24.dp)
        ) {
            Text(text = "Continuar")
        }
    }
}
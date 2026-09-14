package com.smartroutegt.mobile.ui.screens

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
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp

@Composable
fun DriverReportScreen() {

    var description by remember {
        mutableStateOf("")
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(24.dp)
    ) {

        Text(
            text = "Reportar conducta del piloto"
        )

        OutlinedTextField(
            value = description,
            onValueChange = { description = it },
            label = {
                Text(text = "Describe lo ocurrido")
            },
            modifier = Modifier
                .fillMaxWidth()
                .padding(top = 24.dp)
        )

        Button(
            onClick = { },
            modifier = Modifier
                .fillMaxWidth()
                .padding(top = 24.dp)
        ) {
            Text(text = "Enviar reporte")
        }

        Text(
            text = "Envío pendiente de conexión con el backend.",
            modifier = Modifier.padding(top = 16.dp)
        )
    }
}
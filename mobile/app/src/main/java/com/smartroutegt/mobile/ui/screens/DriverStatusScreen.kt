package com.smartroutegt.mobile.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

@Composable
fun DriverStatusScreen(
    status: String,
    onRetry: () -> Unit,
    onLogout: () -> Unit
) {
    val smartBlue = Color(0xFF2563EB)
    val background = Color(0xFFF4F7FB)
    val darkText = Color(0xFF172033)

    val title = when (status) {
        "NO_ASSIGNMENT" -> "Sin asignación activa"
        "INACTIVE" -> "Piloto no disponible"
        "CONNECTION_ERROR" -> "Problema de conexión"
        else -> "Cuenta de piloto no vinculada"
    }

    val description = when (status) {
        "NO_ASSIGNMENT" ->
            "Tu cuenta de piloto es válida, pero todavía no tienes " +
                "una ruta y bus asignados. Comunícate con el administrador."

        "INACTIVE" ->
            "Tu perfil de piloto se encuentra inactivo o suspendido. " +
                "Comunícate con el administrador de SmartRoute GT."

        "CONNECTION_ERROR" ->
            "No fue posible consultar tu información. Verifica la conexión " +
                "con el servidor e intenta nuevamente."

        else ->
            "Tu usuario tiene acceso como piloto, pero todavía no está " +
                "vinculado a un perfil de piloto. El administrador debe " +
                "realizar la vinculación."
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(background)
            .padding(28.dp),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(
            text = "SmartRoute GT",
            fontSize = 32.sp,
            fontWeight = FontWeight.Bold,
            color = smartBlue
        )

        Spacer(modifier = Modifier.height(12.dp))

        Text(
            text = "Portal del Piloto",
            style = MaterialTheme.typography.titleMedium,
            color = darkText
        )

        Spacer(modifier = Modifier.height(36.dp))

        Text(
            text = title,
            style = MaterialTheme.typography.headlineMedium,
            fontWeight = FontWeight.Bold,
            color = darkText,
            textAlign = TextAlign.Center
        )

        Spacer(modifier = Modifier.height(14.dp))

        Text(
            text = description,
            style = MaterialTheme.typography.bodyLarge,
            textAlign = TextAlign.Center
        )

        Spacer(modifier = Modifier.height(32.dp))

        Button(
            onClick = onRetry,
            modifier = Modifier
                .fillMaxWidth()
                .height(52.dp)
        ) {
            Text("VOLVER A CONSULTAR")
        }

        Spacer(modifier = Modifier.height(12.dp))

        OutlinedButton(
            onClick = onLogout,
            modifier = Modifier
                .fillMaxWidth()
                .height(52.dp)
        ) {
            Text("CERRAR SESIÓN")
        }
    }
}

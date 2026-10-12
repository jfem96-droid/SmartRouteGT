package com.smartroutegt.mobile.ui.screens

import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import com.smartroutegt.mobile.data.remote.PassengerV3Api
import com.smartroutegt.mobile.data.remote.ApiClient
import kotlinx.coroutines.delay

@Composable
fun DriverBoardingCodeV3Card(tripId: Long?) {
    val context = LocalContext.current
    val api = remember { ApiClient.retrofit.create(PassengerV3Api::class.java) }
    var code by remember(tripId) { mutableStateOf<String?>(null) }
    var error by remember(tripId) { mutableStateOf<String?>(null) }
    LaunchedEffect(tripId) {
        if (tripId == null) return@LaunchedEffect
        while (true) {
            try {
                code = api.driverBoardingCode(tripId).code
                error = null
                break
            } catch (e: Exception) {
                error = "No se pudo obtener el código. Verifica conexión y viaje activo."
                delay(10_000)
            }
        }
    }
    SmartRouteV3Card {
        Text("Código de abordaje", fontWeight = FontWeight.Bold, fontSize = 17.sp, color = SmartRouteText)
        Spacer(Modifier.height(8.dp))
        if (tripId == null) {
            Text("Inicia un viaje para mostrar el código al pasajero.", color = SmartRouteMuted)
        } else if (code != null) {
            Text(code!!, fontWeight = FontWeight.ExtraBold, fontSize = 32.sp, color = SmartRouteBlue)
            Text("Comparte este código únicamente con pasajeros que estén abordando tu bus.", color = SmartRouteMuted)
            Spacer(Modifier.height(8.dp))
            OutlinedButton(onClick = {
                val clipboard = context.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
                clipboard.setPrimaryClip(ClipData.newPlainText("Código SmartRoute GT", code!!))
            }) { Text("Copiar código") }
        } else {
            Text(error ?: "Obteniendo código del viaje...", color = SmartRouteMuted)
        }
    }
}

package com.smartroutegt.mobile.ui.screens

import android.Manifest
import android.content.pm.PackageManager
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.core.content.ContextCompat
import com.google.android.gms.location.LocationServices
import com.smartroutegt.mobile.data.remote.ApiClient
import com.smartroutegt.mobile.data.remote.PassengerAlertRequest
import kotlinx.coroutines.launch
import kotlinx.coroutines.suspendCancellableCoroutine
import kotlinx.coroutines.withTimeoutOrNull
import kotlin.coroutines.resume

@Composable
fun SosScreen(onBack: () -> Unit) {
    val context = LocalContext.current
    val fused = remember(context) { LocationServices.getFusedLocationProviderClient(context) }
    val scope = rememberCoroutineScope()
    var sending by remember { mutableStateOf(false) }
    var result by remember { mutableStateOf("") }
    var isError by remember { mutableStateOf(false) }
    var reason by remember { mutableStateOf("Accidente de tránsito") }
    var details by remember { mutableStateOf("") }
    val reasons = listOf("Accidente de tránsito", "Robo o asalto", "Acoso o amenaza", "Emergencia médica", "Conducción peligrosa", "Otra emergencia")

    suspend fun locationOrNull(): android.location.Location? {
        val fine = ContextCompat.checkSelfPermission(context, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED
        val coarse = ContextCompat.checkSelfPermission(context, Manifest.permission.ACCESS_COARSE_LOCATION) == PackageManager.PERMISSION_GRANTED
        if (!fine && !coarse) return null
        return withTimeoutOrNull(2500L) {
            suspendCancellableCoroutine { cont ->
                try {
                    fused.lastLocation
                        .addOnSuccessListener { if (cont.isActive) cont.resume(it) }
                        .addOnFailureListener { if (cont.isActive) cont.resume(null) }
                } catch (_: SecurityException) {
                    if (cont.isActive) cont.resume(null)
                }
            }
        }
    }

    fun sendSos(description: String) {
        if (sending) return
        sending = true
        result = ""
        scope.launch {
            try {
                val location = locationOrNull()
                val alert = PassengerAlertRequest(
                    tipo = "SOS",
                    descripcion = description.take(240),
                    latitud = location?.latitude,
                    longitud = location?.longitude
                )
                val response = ApiClient.passengerAlertApiService.createAlert(alert)
                result = "SOS recibido por SmartRoute GT. Alerta #${response.id}." +
                    if (location == null) " Ubicación no disponible; informa tu ubicación al personal de emergencia." else ""
                isError = false
            } catch (e: Exception) {
                result = "No se pudo confirmar el envío del SOS. Comprueba la conexión e inténtalo nuevamente."
                isError = true
            } finally {
                sending = false
            }
        }
    }

    Column(Modifier.fillMaxSize().background(Color(0xFFF4F7FB))) {
        PassengerV3TopBar("Emergencia SOS", "Asistencia y seguridad", onBack)
        Column(
            Modifier.fillMaxSize().verticalScroll(rememberScrollState()).padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            PassengerV3SectionCard {
                Text("¿Necesitas ayuda inmediata?", style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold)
                Spacer(Modifier.height(8.dp))
                Text("Puedes enviar un SOS sin seleccionar un motivo ni escribir detalles.")
                Spacer(Modifier.height(16.dp))
                Button(
                    onClick = { sendSos("SOS rápido - emergencia no especificada") },
                    enabled = !sending,
                    modifier = Modifier.fillMaxWidth().heightIn(min = 56.dp),
                    shape = RoundedCornerShape(16.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFB91C1C))
                ) {
                    Text(if (sending) "Enviando SOS..." else "Enviar SOS rápido", fontWeight = FontWeight.Bold)
                }
                Spacer(Modifier.height(8.dp))
                Text("Un toque envía la alerta. No requiere completar campos.", style = MaterialTheme.typography.bodySmall)
            }
            PassengerV3SectionCard {
                Text("SOS con motivo específico", style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold)
                Spacer(Modifier.height(10.dp))
                reasons.forEach { item ->
                    Row(Modifier.fillMaxWidth(), verticalAlignment = androidx.compose.ui.Alignment.CenterVertically) {
                        RadioButton(selected = reason == item, onClick = { reason = item })
                        Text(item, modifier = Modifier.weight(1f))
                    }
                }
                Spacer(Modifier.height(10.dp))
                OutlinedTextField(
                    value = details,
                    onValueChange = { if (it.length <= 170) details = it },
                    label = { Text("Detalles adicionales (opcional)") },
                    modifier = Modifier.fillMaxWidth(),
                    minLines = 2,
                    maxLines = 4
                )
                Spacer(Modifier.height(12.dp))
                Button(
                    onClick = { sendSos("SOS: $reason" + if (details.isBlank()) "" else " - ${details.trim()}") },
                    enabled = !sending,
                    modifier = Modifier.fillMaxWidth().heightIn(min = 52.dp),
                    shape = RoundedCornerShape(14.dp)
                ) { Text("Enviar SOS con motivo") }
            }
            if (result.isNotBlank()) {
                Surface(
                    color = if (isError) Color(0xFFFFE7E7) else Color(0xFFE3F5E9),
                    shape = RoundedCornerShape(14.dp)
                ) {
                    Text(result, Modifier.fillMaxWidth().padding(16.dp), color = Color(0xFF18243B))
                }
            }
            Text(
                "La ubicación se adjunta si está disponible. Si no hay GPS, el SOS igualmente se envía. En una emergencia real, contacta también a los servicios de emergencia locales.",
                style = MaterialTheme.typography.bodySmall,
                color = Color(0xFF52617A)
            )
        }
    }
}

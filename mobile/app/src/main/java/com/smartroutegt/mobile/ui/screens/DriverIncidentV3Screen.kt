package com.smartroutegt.mobile.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.smartroutegt.mobile.network.*
import kotlinx.coroutines.launch

private fun reportType(v: String) = when(v) { "ACCIDENT" -> "Accidente"; "MECHANICAL" -> "Falla mecánica"; else -> "Incidente" }
private fun reportSeverity(v: String) = when(v) { "HIGH" -> "Alta"; "LOW" -> "Baja"; else -> "Media" }
private fun reportStatus(v: String) = when(v) { "PENDING" -> "Pendiente"; "UNDER_REVIEW" -> "En revisión"; "RESOLVED" -> "Resuelto"; else -> v }

@Composable
fun DriverIncidentV3Screen(
    onBack: () -> Unit, api: DriverApiService, tripId: Long?, latitude: Double?, longitude: Double?,
    onTabSelected: (SmartRouteV3DriverTab) -> Unit = {}
) {
    var type by remember { mutableStateOf("INCIDENT") }
    var severity by remember { mutableStateOf("MEDIUM") }
    var description by remember { mutableStateOf("") }
    var message by remember { mutableStateOf("") }
    var sending by remember { mutableStateOf(false) }
    var history by remember { mutableStateOf<List<DriverReportDto>>(emptyList()) }
    val scope = rememberCoroutineScope()
    LaunchedEffect(Unit) { try { history = api.getMyDriverReports() } catch (_: Exception) { message = "No se pudo cargar el historial" } }
    val blue = Color(0xFF2563EB)
    val ink = Color(0xFF17263C)
    val background = Color(0xFFF4F7FB)
    Scaffold(
        containerColor = background,
        topBar = { SmartRouteV3TopBar(title = "SmartRoute GT", subtitle = "Reportes del piloto", role = SmartRouteV3Role.DRIVER, onBack = onBack) },
        bottomBar = {
            SmartRouteV3DriverBottomBar(
                selected = SmartRouteV3DriverTab.REPORT,
                onHome = { onTabSelected(SmartRouteV3DriverTab.HOME) },
                onRoute = { onTabSelected(SmartRouteV3DriverTab.ROUTE) },
                onGps = { onTabSelected(SmartRouteV3DriverTab.GPS) },
                onAlerts = { onTabSelected(SmartRouteV3DriverTab.ALERTS) },
                onReport = { onTabSelected(SmartRouteV3DriverTab.REPORT) },
                onProfile = { onTabSelected(SmartRouteV3DriverTab.PROFILE) }
            )
        }
    ) { inner ->
        Column(Modifier.fillMaxSize().padding(inner).imePadding().verticalScroll(rememberScrollState()).padding(16.dp), verticalArrangement = Arrangement.spacedBy(14.dp)) {
            Text("Reportar incidente", style = MaterialTheme.typography.headlineSmall, fontWeight = FontWeight.Bold, color = ink)
            Text("Comunica novedades de la ruta al centro de control.", color = Color(0xFF64748B))
            Card(colors = CardDefaults.cardColors(containerColor = Color(0xFFE7F5ED)), shape = RoundedCornerShape(18.dp)) {
                Column(Modifier.fillMaxWidth().padding(16.dp)) {
                    Text("Centro de control conectado", fontWeight = FontWeight.Bold, color = Color(0xFF166534))
                    Text("Los reportes enviados se sincronizan con el administrador.", color = Color(0xFF166534))
                }
            }
            Card(colors = CardDefaults.cardColors(containerColor = Color.White), shape = RoundedCornerShape(18.dp)) {
                Column(Modifier.fillMaxWidth().padding(16.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
                    Text("Nuevo reporte", style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold, color = ink)
                    Text("Tipo de incidente", fontWeight = FontWeight.SemiBold)
                    Row(horizontalArrangement = Arrangement.spacedBy(5.dp)) {
                        listOf("ACCIDENT" to "Accidente", "INCIDENT" to "Incidente", "MECHANICAL" to "Falla").forEach { (key,label) ->
                            FilterChip(selected = type == key, onClick = { type = key }, label = { Text(label) })
                        }
                    }
                    Text("Gravedad", fontWeight = FontWeight.SemiBold)
                    Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                        listOf("LOW" to "Baja", "MEDIUM" to "Media", "HIGH" to "Alta").forEach { (key,label) ->
                            FilterChip(selected = severity == key, onClick = { severity = key }, label = { Text(label) })
                        }
                    }
                    OutlinedTextField(value = description, onValueChange = { if(it.length <= 2000) description = it },
                        label = { Text("Describe lo ocurrido") }, supportingText = { Text("Mínimo 10 caracteres · ${description.length}/2000") },
                        modifier = Modifier.fillMaxWidth(), minLines = 3, shape = RoundedCornerShape(12.dp))
                    Text(if(latitude != null && longitude != null) "● Ubicación GPS disponible" else "Sin ubicación GPS: el reporte se enviará sin coordenadas", color = Color(0xFF64748B))
                    Button(enabled = !sending && description.trim().length >= 10, onClick = {
                        sending = true; message = ""
                        scope.launch {
                            try {
                                api.createDriverReport(DriverReportCreateDto(tripId, type, severity, description.trim(), latitude, longitude))
                                description = ""; message = "Reporte enviado correctamente"
                                history = api.getMyDriverReports()
                            } catch(e: Exception) { message = "Error al enviar: ${e.message ?: "reintenta"}" }
                            finally { sending = false }
                        }
                    }, modifier = Modifier.fillMaxWidth(), shape = RoundedCornerShape(12.dp), colors = ButtonDefaults.buttonColors(containerColor = blue)) {
                        Text(if(sending) "Enviando…" else "Enviar reporte")
                    }
                    if(message.isNotBlank()) Text(message, color = if(message.startsWith("Error")) Color(0xFFB91C1C) else Color(0xFF166534))
                }
            }
            Text("Mis reportes", style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold, color = ink)
            if(history.isEmpty()) Text("Aún no tienes reportes registrados.", color = Color(0xFF64748B))
            history.forEach { item ->
                Card(colors = CardDefaults.cardColors(containerColor = Color.White), shape = RoundedCornerShape(16.dp)) {
                    Column(Modifier.fillMaxWidth().padding(16.dp), verticalArrangement = Arrangement.spacedBy(6.dp)) {
                        Text("Reporte #${item.id} · ${reportType(item.type)}", fontWeight = FontWeight.Bold, color = ink)
                        Text("Gravedad: ${reportSeverity(item.severity)}", color = Color(0xFF64748B))
                        Surface(color = when(item.status) { "RESOLVED" -> Color(0xFFDCFCE7); "UNDER_REVIEW" -> Color(0xFFDBEAFE); else -> Color(0xFFFEF3C7) }, shape = RoundedCornerShape(50.dp)) {
                            Text(reportStatus(item.status), modifier = Modifier.padding(horizontal = 12.dp, vertical = 5.dp), fontWeight = FontWeight.SemiBold)
                        }
                        Text(item.description, color = ink)
                        item.resolution?.takeIf { it.isNotBlank() }?.let { Text("Resolución: $it", color = Color(0xFF166534)) }
                    }
                }
            }
            Text("Para emergencias, comunícate con los servicios de emergencia. Este reporte no solicita asistencia inmediata.", color = Color(0xFF64748B), style = MaterialTheme.typography.bodySmall)
        }
    }
}

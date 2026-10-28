package com.smartroutegt.mobile.ui.screens

import com.smartroutegt.mobile.network.userFacingApiError

import com.smartroutegt.mobile.ui.theme.SmartRoutePalette

import android.Manifest
import android.content.pm.PackageManager
import android.location.LocationManager
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.core.content.ContextCompat
import com.google.android.gms.location.LocationServices
import com.google.android.gms.location.Priority
import com.google.android.gms.tasks.CancellationTokenSource
import com.smartroutegt.mobile.data.remote.*
import com.smartroutegt.mobile.network.TripDto
import kotlinx.coroutines.launch

private val reportBlue = SmartRoutePalette.Mint
private val reportBackground = SmartRoutePalette.Night

@Composable
fun PassengerReportV3Form(onBack: () -> Unit, routeProblem: Boolean) {
    val api = remember { ApiClient.retrofit.create(PassengerV3Api::class.java) }
    val context = LocalContext.current
    val locationClient = remember { LocationServices.getFusedLocationProviderClient(context) }
    val scope = rememberCoroutineScope()
    var boarding by remember { mutableStateOf<BoardingView?>(null) }
    var nearbyBuses by remember { mutableStateOf<List<NearbyBus>>(emptyList()) }
    var searching by remember { mutableStateOf(false) }
    var joining by remember { mutableStateOf(false) }
    var selectedTrip by remember { mutableStateOf<Long?>(null) }
    var description by remember { mutableStateOf("") }
    var type by remember { mutableStateOf(if (routeProblem) "MISSED_STOP" else "BAD_BEHAVIOR") }
    var status by remember { mutableStateOf("") }
    var sending by remember { mutableStateOf(false) }
    var loading by remember { mutableStateOf(true) }
    var permission by remember { mutableStateOf(ContextCompat.checkSelfPermission(context, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED) }
    val requestPermission = rememberLauncherForActivityResult(ActivityResultContracts.RequestPermission()) { permission = it }

    LaunchedEffect(Unit) {
        try {
            boarding = api.myBoarding()
            selectedTrip = boarding?.tripId
        } catch (e: Exception) {
            status = "No se pudieron cargar los viajes: ${userFacingApiError(e)}"
        } finally { loading = false }
    }
    LaunchedEffect(selectedTrip, permission) {
        while (selectedTrip != null && boarding?.active == true && permission) {
            kotlinx.coroutines.delay(15_000)
            if (ContextCompat.checkSelfPermission(context, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED) {
                locationClient.getCurrentLocation(Priority.PRIORITY_HIGH_ACCURACY, CancellationTokenSource().token)
                    .addOnSuccessListener { loc ->
                        if (loc != null && loc.accuracy <= 100f && System.currentTimeMillis() - loc.time <= 30_000L) {
                            scope.launch {
                                try {
                                    val response = api.boardingLocation(BoardingPosition(loc.latitude, loc.longitude))
                                    if (response.isSuccessful) { boarding = response.body(); selectedTrip = boarding?.tripId }
                                    else { status = "Estado de abordaje: HTTP ${response.code()}"; if (response.code() == 403) { boarding = null; selectedTrip = null } }
                                } catch (_: Exception) { /* Reintento en el siguiente intervalo */ }
                            }
                        }
                    }
            }
        }
    }
    val options = if (routeProblem) listOf(
        "MISSED_STOP" to "Parada omitida", "OVERCROWDING" to "Sobrecupo", "OTHER" to "Otro problema"
    ) else listOf(
        "BAD_BEHAVIOR" to "Mala conducta", "RECKLESS_DRIVING" to "Conducción imprudente", "OTHER" to "Otro motivo"
    )
    val gpsEnabled = remember(permission, sending) {
        val lm = context.getSystemService(android.content.Context.LOCATION_SERVICE) as LocationManager
        lm.isProviderEnabled(LocationManager.GPS_PROVIDER) || lm.isProviderEnabled(LocationManager.NETWORK_PROVIDER)
    }

    Column(Modifier.fillMaxSize().background(reportBackground).verticalScroll(rememberScrollState())) {
        PassengerV3TopBar(if (routeProblem) "Problema de ruta" else "Reportar piloto", "Seguridad del pasajero", onBack)
        Column(Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(14.dp)) {
            Card(colors = CardDefaults.cardColors(containerColor = SmartRoutePalette.Navy, contentColor = SmartRoutePalette.Text), shape = RoundedCornerShape(20.dp), border = BorderStroke(1.dp, SmartRoutePalette.Border)) {
                Column(Modifier.fillMaxWidth().padding(20.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Text("Tu seguridad es importante", color = Color.White, style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold)
                    Text("Cuéntanos qué ocurrió. Tu reporte será revisado por el equipo de SmartRoute GT.", color = Color.White.copy(alpha = 0.9f), style = MaterialTheme.typography.bodyMedium)
                }
            }
            ReportSection("01  Viaje relacionado") {
                if (loading) LinearProgressIndicator(Modifier.fillMaxWidth())
                else if (boarding?.tripId != null) {
                    Text("Bus ${boarding?.bus ?: ""}", fontWeight = FontWeight.Bold, color = SmartRoutePalette.Text)
                    Text("${boarding?.route ?: ""} · Viaje #${boarding?.tripId ?: ""}", color = SmartRoutePalette.Muted)
                    Text(if (boarding?.separatedAt.isNullOrBlank()) "Abordaje confirmado · GPS obligatorio" else "Separación detectada · 5 minutos para reportar", color = reportBlue)
                    OutlinedButton(onClick = { scope.launch { try { boarding = api.myBoarding(); selectedTrip = boarding?.tripId } catch (e: Exception) { status = userFacingApiError(e) } } }) { Text("Actualizar estado") }
                } else {
                    Text("Busca buses cercanos con GPS y confirma en cuál estás viajando.", color = SmartRoutePalette.Text)
                    Button(enabled = !searching && permission, onClick = {
                        searching = true; status = "Buscando buses a menos de 40 m..."
                        if (ContextCompat.checkSelfPermission(context, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) { searching=false;status="Autoriza ubicación precisa" }
                        else locationClient.getCurrentLocation(Priority.PRIORITY_HIGH_ACCURACY, CancellationTokenSource().token).addOnSuccessListener { loc ->
                            if (loc == null || loc.accuracy > 30f || System.currentTimeMillis()-loc.time>15000L) { searching=false;status="Se necesita GPS reciente con precisión de 30 m o mejor" }
                            else scope.launch { try {
                                nearbyBuses = api.nearbyBuses(BoardingPosition(loc.latitude,loc.longitude))
                                status = if (nearbyBuses.isEmpty()) "No hay buses activos cercanos con GPS actualizado" else "Selecciona el bus en el que estás"
                            } catch(e:Exception){status=userFacingApiError(e)} finally{searching=false} }
                        }.addOnFailureListener {searching=false;status="Error GPS: ${it.message}"}
                    }) { Text(if(searching) "Buscando..." else "Detectar buses cercanos") }
                    nearbyBuses.forEach { bus ->
                        Card(colors=CardDefaults.cardColors(containerColor=SmartRoutePalette.Navy, contentColor=SmartRoutePalette.Text), border=BorderStroke(1.dp,SmartRoutePalette.Border)) {
                            Column(Modifier.fillMaxWidth().padding(12.dp), verticalArrangement=Arrangement.spacedBy(6.dp)) {
                                Text("Bus ${bus.bus} · ${bus.route}",fontWeight=FontWeight.Bold)
                                Text("Aproximadamente ${bus.distanceMeters} m", color = SmartRoutePalette.Muted)
                                Button(enabled=!joining && permission,onClick={
                                    joining=true;status="Verificando proximidad..."
                                    if(ContextCompat.checkSelfPermission(context,Manifest.permission.ACCESS_FINE_LOCATION)!=PackageManager.PERMISSION_GRANTED){joining=false;status="Autoriza ubicación precisa"}
                                    else locationClient.getCurrentLocation(Priority.PRIORITY_HIGH_ACCURACY,CancellationTokenSource().token).addOnSuccessListener { loc ->
                                        if(loc==null||loc.accuracy>30f||System.currentTimeMillis()-loc.time>15000L){joining=false;status="GPS insuficiente; intenta de nuevo"}
                                        else scope.launch {try{
                                            val response=api.joinNearby(NearbyJoin(bus.tripId,loc.latitude,loc.longitude))
                                            if(response.isSuccessful){boarding=response.body();selectedTrip=boarding?.tripId;nearbyBuses=emptyList();status="Abordaje confirmado"}
                                            else status="Abordaje rechazado HTTP ${response.code()}: ${response.errorBody()?.string()?.take(180)}"
                                        }catch(e:Exception){status=userFacingApiError(e)}finally{joining=false}}
                                    }.addOnFailureListener {joining=false;status="Error GPS: ${it.message}"}
                                }){Text("Estoy a bordo de este bus")}
                            }
                        }
                    }
                }
            }
            ReportSection("02  Motivo del reporte") {
                options.forEach { (code, label) ->
                    val selected = type == code
                    Surface(
                        modifier = Modifier.fillMaxWidth().clickable { type = code },
                        shape = RoundedCornerShape(12.dp),
                        color = if (selected) SmartRoutePalette.Panel else SmartRoutePalette.Navy,
                        border = BorderStroke(1.dp, if (selected) reportBlue else SmartRoutePalette.Border),
                        contentColor = SmartRoutePalette.Text
                    ) {
                        Row(Modifier.padding(horizontal = 12.dp, vertical = 6.dp), verticalAlignment = Alignment.CenterVertically) {
                            RadioButton(selected, onClick = { type = code })
                            Text(label, color = SmartRoutePalette.Text, fontWeight = if (selected) FontWeight.SemiBold else FontWeight.Normal)
                        }
                    }
                }
            }
            ReportSection("03  Cuéntanos qué ocurrió") {
                OutlinedTextField(
                    value = description, onValueChange = { if (it.length <= 2000) description = it },
                    placeholder = { Text("Describe lo sucedido con el mayor detalle posible...") },
                    supportingText = { Text("Mínimo 10 caracteres · ${description.length}/2000") },
                    minLines = 4, modifier = Modifier.fillMaxWidth(), shape = RoundedCornerShape(12.dp)
                )
            }
            ReportSection("04  Ubicación obligatoria") {
                Text(if (permission && gpsEnabled) "Permiso de ubicación concedido" else "Activa el GPS y permite el acceso a tu ubicación.", color = if (permission && gpsEnabled) SmartRoutePalette.Mint else MaterialTheme.colorScheme.error)
                if (!permission) OutlinedButton(onClick = { requestPermission.launch(Manifest.permission.ACCESS_FINE_LOCATION) }) { Text("Permitir ubicación GPS") }
                Text("La ubicación será comprobada al enviar. La validación de abordaje y separación debe realizarla el backend.", style = MaterialTheme.typography.bodySmall, color = SmartRoutePalette.Muted)
            }
            Button(
                enabled = !sending && permission && gpsEnabled && selectedTrip != null && description.trim().length >= 10,
                onClick = {
                    val tripId = selectedTrip ?: return@Button
                    sending = true
                    status = "Obteniendo ubicación GPS..."
                    if (ContextCompat.checkSelfPermission(context, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
                        sending = false; status = "Se requiere permiso de ubicación"
                    } else {
                        locationClient.getCurrentLocation(Priority.PRIORITY_HIGH_ACCURACY, CancellationTokenSource().token).addOnSuccessListener { loc ->
                            if (loc == null || System.currentTimeMillis() - loc.time > 30_000L || loc.accuracy > 100f) {
                                sending = false
                                status = "No se obtuvo una ubicación actual con precisión suficiente (100 m). Comprueba que la ubicación precisa esté habilitada y vuelve a intentarlo."
                            } else scope.launch {
                                try {
                                    if (boarding?.active == true) {
                                        val gpsCheck = api.boardingLocation(BoardingPosition(loc.latitude, loc.longitude))
                                        if (!gpsCheck.isSuccessful) {
                                            status = "Ubicación o plazo rechazado: HTTP ${gpsCheck.code()} ${gpsCheck.errorBody()?.string()?.take(180)}"
                                            return@launch
                                        }
                                        boarding = gpsCheck.body()
                                    }
                                    val result = api.report(PassengerReportPayload(type, description.trim(), tripId, loc.latitude, loc.longitude))
                                    if (result.isSuccessful) { status = "Reporte enviado correctamente"; description = "" }
                                    else status = "El servidor rechazó el reporte (HTTP ${result.code()}): ${result.errorBody()?.string()?.take(250) ?: ""}"
                                } catch (e: Exception) { status = "No se pudo enviar: ${userFacingApiError(e)}" }
                                finally { sending = false }
                            }
                        }.addOnFailureListener { sending = false; status = "No se pudo obtener GPS: ${it.message}" }
                    }
                },
                modifier = Modifier.fillMaxWidth().height(54.dp), shape = RoundedCornerShape(14.dp)
            ) { Text(if (sending) "Enviando..." else "Enviar reporte", fontWeight = FontWeight.Bold) }
            if (status.isNotBlank()) Text(status, color = if (status.startsWith("Reporte enviado")) SmartRoutePalette.Mint else MaterialTheme.colorScheme.onSurface)
        }
    }
}

@Composable
private fun ReportSection(title: String, content: @Composable ColumnScope.() -> Unit) {
    Card(shape = RoundedCornerShape(18.dp), colors = CardDefaults.cardColors(containerColor = SmartRoutePalette.Panel, contentColor = SmartRoutePalette.Text), elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)) {
        Column(Modifier.fillMaxWidth().padding(16.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
            Text(title, fontWeight = FontWeight.Bold, style = MaterialTheme.typography.titleMedium)
            content()
        }
    }
}

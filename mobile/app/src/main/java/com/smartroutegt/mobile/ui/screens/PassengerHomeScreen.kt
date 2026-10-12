package com.smartroutegt.mobile.ui.screens

import androidx.compose.foundation.layout.width
import kotlin.coroutines.resume
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.platform.LocalContext
import android.Manifest
import android.content.pm.PackageManager
import androidx.core.content.ContextCompat
import com.google.android.gms.location.LocationServices
import com.google.android.gms.location.Priority
import com.google.android.gms.tasks.CancellationTokenSource
import com.smartroutegt.mobile.data.remote.*
import com.smartroutegt.mobile.data.remote.ApiClient
import kotlinx.coroutines.launch
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

@Composable
fun PassengerHomeScreen(
    selectedRoute: com.smartroutegt.mobile.data.remote.RouteDto? = null,
    selectedBus: com.smartroutegt.mobile.data.remote.BusMonitoringDto? = null,
    onClearRoute: () -> Unit = {},
    onBoarded: () -> Unit = {},
    onRateDriver: () -> Unit = {},
    onComplimentDriver: () -> Unit = {},
    onSearchRoute: () -> Unit,
    onNearbyBuses: () -> Unit,
    onBusInfo: () -> Unit,
    onEta: () -> Unit,
    onSos: () -> Unit,
    onDriverReport: () -> Unit,
    onRouteReport: () -> Unit,
    onProfile: () -> Unit
) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    val locationClient = remember { LocationServices.getFusedLocationProviderClient(context) }
    val api = remember { ApiClient.retrofit.create(PassengerV3Api::class.java) }
    var boarding by remember { mutableStateOf<BoardingView?>(null) }
    var showLeaveConfirmation by remember { mutableStateOf(false) }
    var busy by remember { mutableStateOf(false) }
    var status by remember { mutableStateOf("") }
    LaunchedEffect(selectedBus?.tripId) {
        try { boarding = api.myBoarding() } catch (_: Exception) { }
    }

    // Passenger GPS tracking V3
    androidx.compose.runtime.LaunchedEffect(boarding?.active, boarding?.tripId) {
        while (boarding?.active == true) {
            if (
                androidx.core.content.ContextCompat.checkSelfPermission(
                    context,
                    android.Manifest.permission.ACCESS_FINE_LOCATION
                ) == android.content.pm.PackageManager.PERMISSION_GRANTED
            ) {
                try {
                    val location = kotlinx.coroutines.suspendCancellableCoroutine<android.location.Location?> { continuation ->
                        locationClient.getCurrentLocation(
                            com.google.android.gms.location.Priority.PRIORITY_HIGH_ACCURACY,
                            com.google.android.gms.tasks.CancellationTokenSource().token
                        ).addOnSuccessListener { loc ->
                            if (continuation.isActive) continuation.resume(loc)
                        }.addOnFailureListener {
                            if (continuation.isActive) continuation.resume(null)
                        }
                    }

                    if (
                        location != null &&
                        location.accuracy <= 30f &&
                        System.currentTimeMillis() - location.time <= 15000L
                    ) {
                        val result = api.boardingLocation(
                            com.smartroutegt.mobile.data.remote.BoardingPosition(
                                location.latitude,
                                location.longitude
                            )
                        )

                        if (result.isSuccessful && result.body() != null) {
                            boarding = result.body()
                            if (boarding?.active == false) {
                                status = "Descenso detectado automáticamente por GPS."
                            }
                        }
                    }
                } catch (_: Exception) {
                    // No interrumpir la pantalla por un error temporal de GPS o red.
                }
            }

            kotlinx.coroutines.delay(10000L)
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFFF4F7FB))
    ) {
        if (showLeaveConfirmation) {
            androidx.compose.material3.AlertDialog(
                onDismissRequest = { showLeaveConfirmation = false },
                title = { Text("Confirmar descenso") },
                text = { Text("¿Confirmas que ya te bajaste del bus? Tendrás 5 minutos para evaluar o reportar.") },
                confirmButton = {
                    androidx.compose.material3.TextButton(
                        onClick = {
                            showLeaveConfirmation = false
                            busy = true
                            scope.launch {
                                try {
                                    val result = api.leaveBoarding()
                                    if (result.isSuccessful) {
                                        boarding = result.body()
                                        status = "Descenso registrado. Tienes 5 minutos para reportar."
                                    } else {
                                        status = "No se pudo registrar el descenso: ${result.code()}"
                                    }
                                } catch (e: Exception) {
                                    status = "Error al registrar descenso: ${e.message}"
                                } finally {
                                    busy = false
                                }
                            }
                        }
                    ) { Text("Confirmar") }
                },
                dismissButton = {
                    androidx.compose.material3.TextButton(
                        onClick = { showLeaveConfirmation = false }
                    ) { Text("Cancelar") }
                }
            )
        }

        PassengerV3Header(onProfile = onProfile)

        Box(
            modifier = Modifier
                .weight(1f)
                .fillMaxWidth()
        ) {
            PassengerLiveMap(modifier = Modifier.fillMaxSize(), selectedRouteId = selectedRoute?.id)

            if (selectedRoute == null) PassengerSearchPanel(
                modifier = Modifier
                    .align(Alignment.TopCenter)
                    .padding(horizontal = 16.dp, vertical = 14.dp),
                onClick = onSearchRoute
            )

            if (selectedRoute != null) {
                Card(modifier = Modifier.align(Alignment.BottomCenter).padding(16.dp).fillMaxWidth(),
                    colors = CardDefaults.cardColors(containerColor = Color(0xFF193E7B))) {
                    Column(Modifier.padding(16.dp)) {
                        Text("${selectedRoute.codigo} · ${selectedRoute.nombre}", color = Color.White, fontWeight = FontWeight.Bold)
                        Text(selectedBus?.let { "${it.codigo} · ${it.placa}" } ?: "Buses de la ruta en el mapa", color = Color.White)
                        if (boarding?.active == true && boarding?.tripId == selectedBus?.tripId) {
                            Text("Abordaje confirmado · ${boarding?.bus}", color = Color.White)
                            Button(onClick = onRateDriver, modifier = Modifier.fillMaxWidth(), colors = androidx.compose.material3.ButtonDefaults.buttonColors(containerColor = Color.White)) {
                                Text("★  Evaluar al piloto", color = Color(0xFF193E7B))
                            }
                            Button(onClick = onComplimentDriver, modifier = Modifier.fillMaxWidth(), colors = androidx.compose.material3.ButtonDefaults.buttonColors(containerColor = Color(0xFFE4F5E9))) {
                                Text("♥  Felicitar al piloto", color = Color(0xFF167447))
                            }
                            androidx.compose.material3.OutlinedButton(onClick = onBoarded, modifier = Modifier.fillMaxWidth()) {
                                Text("Reportar incidente", color = Color.White)
                            }
                            androidx.compose.material3.OutlinedButton(
                                enabled = !busy, modifier = Modifier.fillMaxWidth(),
                                onClick = { showLeaveConfirmation = true }
                            ) { Text("Me bajé del bus", color = Color.White) }
                        } else if (
                            boarding?.active == false &&
                            !boarding?.separatedAt.isNullOrBlank() &&
                            boarding?.tripId == selectedBus?.tripId
                        ) {
                            Text(
                                "Descenso registrado · ${boarding?.bus}",
                                color = Color.White,
                                fontWeight = FontWeight.Bold
                            )
                            Text(
                                "Puedes evaluar o reportar durante los 5 minutos posteriores al descenso.",
                                color = Color.White,
                                fontSize = 12.sp
                            )
                            Button(onClick = onRateDriver, modifier = Modifier.fillMaxWidth(), colors = androidx.compose.material3.ButtonDefaults.buttonColors(containerColor = Color.White)) {
                                Text("★  Evaluar al piloto", color = Color(0xFF193E7B))
                            }
                            Button(onClick = onComplimentDriver, modifier = Modifier.fillMaxWidth(), colors = androidx.compose.material3.ButtonDefaults.buttonColors(containerColor = Color(0xFFE4F5E9))) {
                                Text("♥  Felicitar al piloto", color = Color(0xFF167447))
                            }
                            androidx.compose.material3.OutlinedButton(onClick = onBoarded, modifier = Modifier.fillMaxWidth()) {
                                Text("Reportar incidente", color = Color.White)
                            }
                        } else if (selectedBus?.tripId != null && selectedBus.viajeActivo) {
                            Text("Abordaje disponible únicamente a 30 m del bus", color = Color.White, fontSize = 12.sp)
                            Button(enabled = !busy, onClick = {
                                if (ContextCompat.checkSelfPermission(context, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
                                    status = "Activa el permiso de ubicación precisa"
                                } else {
                                    busy = true
                                    status = "Validando GPS..."
                                    locationClient.getCurrentLocation(Priority.PRIORITY_HIGH_ACCURACY, CancellationTokenSource().token)
                                        .addOnSuccessListener { loc ->
                                            if (loc == null || loc.accuracy > 30f || System.currentTimeMillis() - loc.time > 15000L) {
                                                busy = false; status = "GPS impreciso o desactualizado. Intenta nuevamente"
                                            } else scope.launch {
                                                try {
                                                    val result = api.joinSelectedTrip(NearbyJoin(selectedBus.tripId!!, loc.latitude, loc.longitude))
                                                    if (result.isSuccessful) { boarding = result.body(); status = "Abordaje confirmado" }
                                                    else status = when (result.code()) {
                                                        403 -> "No puedes abordar todavía. Acércate a 30 metros o menos del bus y comprueba que su GPS esté actualizado."
                                                        401 -> "Tu sesión ha caducado. Inicia sesión nuevamente."
                                                        else -> "No se pudo confirmar el abordaje (HTTP ${result.code()}). Intenta nuevamente."
                                                    }
                                                } catch (e: Exception) { status = "Error de conexión: ${e.message}" }
                                                finally { busy = false }
                                            }
                                        }.addOnFailureListener { busy = false; status = "No se obtuvo ubicación GPS" }
                                }
                            }) { Text(if (busy) "Verificando..." else "Estoy a bordo") }
                        } else {
                            Text("Selecciona un bus con viaje activo en Planifica tu viaje", color = Color.White, fontSize = 11.sp)
                        }
                        if (status.isNotBlank()) {
                            Surface(color = if (status.contains("confirmado", true)) Color(0xFFE4F5E9) else Color(0xFFFFF3DC), shape = RoundedCornerShape(12.dp)) {
                                Text(status, modifier = Modifier.fillMaxWidth().padding(12.dp), color = if (status.contains("confirmado", true)) Color(0xFF167447) else Color(0xFF754B00), fontSize = 13.sp)
                            }
                        }
                        androidx.compose.material3.OutlinedButton(
                            onClick = onClearRoute,
                            modifier = Modifier.fillMaxWidth().height(48.dp),
                            shape = RoundedCornerShape(13.dp),
                            border = androidx.compose.foundation.BorderStroke(1.5.dp, Color(0xFFFF8A91)),
                            colors = androidx.compose.material3.ButtonDefaults.outlinedButtonColors(
                                containerColor = Color(0xFF123A73),
                                contentColor = Color(0xFFFFD7DA)
                            )
                        ) {
                            Text("✕", fontWeight = FontWeight.Bold)
                            Spacer(Modifier.width(8.dp))
                            Text("Quitar ruta", fontWeight = FontWeight.SemiBold)
                        }
                    }
                }
            } else LiveBusCard(
                modifier = Modifier.align(Alignment.BottomCenter).padding(16.dp),
                onClick = onBusInfo
            )
        }

        PassengerV3BottomBar(
            onRoutes = onSearchRoute,
            onSos = onSos,
            onReport = onDriverReport,
            onProfile = onProfile
        )
    }
}

@Composable
private fun PassengerV3Header(onProfile: () -> Unit) {
    Surface(
        color = Color.White,
        shadowElevation = 3.dp
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 18.dp, vertical = 13.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .size(42.dp)
                    .clip(RoundedCornerShape(13.dp))
                    .background(MaterialTheme.colorScheme.primary),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = "SR",
                    color = Color.White,
                    fontWeight = FontWeight.ExtraBold
                )
            }

            Column(
                modifier = Modifier
                    .weight(1f)
                    .padding(start = 11.dp)
            ) {
                Text(
                    text = "SmartRoute GT",
                    fontWeight = FontWeight.Bold,
                    fontSize = 19.sp,
                    color = Color(0xFF172033)
                )

                Text(
                    text = "Movilidad inteligente",
                    fontSize = 11.sp,
                    color = Color(0xFF758097)
                )
            }

            Box(
                modifier = Modifier
                    .size(40.dp)
                    .clickable { onProfile() }
                    .clip(CircleShape)
                    .background(Color(0xFFE8F1FF)),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = "P",
                    color = MaterialTheme.colorScheme.primary,
                    fontWeight = FontWeight.Bold
                )
            }
        }
    }
}

@Composable
private fun PassengerSearchPanel(
    modifier: Modifier,
    onClick: () -> Unit
) {
    Card(
        modifier = modifier
            .fillMaxWidth()
            .clickable(onClick = onClick),
        shape = RoundedCornerShape(22.dp),
        colors = CardDefaults.cardColors(
            containerColor = Color.White
        ),
        elevation = CardDefaults.cardElevation(
            defaultElevation = 7.dp
        )
    ) {
        Column(
            modifier = Modifier.padding(16.dp)
        ) {
            Text(
                text = "¿A dónde quieres ir?",
                fontWeight = FontWeight.Bold,
                fontSize = 18.sp,
                color = Color(0xFF172033)
            )

            Spacer(modifier = Modifier.height(13.dp))

            PassengerLocationField(
                color = Color(0xFF2475F0),
                title = "¿Desde dónde viajas?",
                subtitle = "Selecciona tu parada de origen"
            )

            Spacer(modifier = Modifier.height(9.dp))

            PassengerLocationField(
                color = Color(0xFFE63C4F),
                title = "¿A dónde quieres ir?",
                subtitle = "Selecciona tu destino"
            )
        }
    }
}

@Composable
private fun PassengerLocationField(
    color: Color,
    title: String,
    subtitle: String
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(15.dp))
            .background(Color(0xFFF4F6F9))
            .padding(horizontal = 13.dp, vertical = 11.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Box(
            modifier = Modifier
                .size(11.dp)
                .clip(CircleShape)
                .background(color)
        )

        Column(
            modifier = Modifier.padding(start = 12.dp)
        ) {
            Text(
                text = title,
                fontSize = 14.sp,
                fontWeight = FontWeight.SemiBold,
                color = Color(0xFF263247)
            )

            Text(
                text = subtitle,
                fontSize = 11.sp,
                color = Color(0xFF8992A5)
            )
        }
    }
}

@Composable
private fun PassengerMapPlaceholder(
    modifier: Modifier,
    onNearbyBuses: () -> Unit
) {
    Box(
        modifier = modifier
            .background(Color(0xFFE5EBF1))
            .clickable(onClick = onNearbyBuses)
    ) {
        Column(
            modifier = Modifier.align(Alignment.Center),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                text = "MAPA EN TIEMPO REAL",
                color = Color(0xFF77879A),
                fontWeight = FontWeight.Bold,
                fontSize = 13.sp
            )

            Text(
                text = "Buses y rutas SmartRoute GT",
                color = Color(0xFF98A4B4),
                fontSize = 11.sp,
                modifier = Modifier.padding(top = 4.dp)
            )
        }

        PassengerBusMarker(
            modifier = Modifier
                .align(Alignment.CenterStart)
                .padding(start = 55.dp, top = 85.dp)
        )

        PassengerBusMarker(
            modifier = Modifier
                .align(Alignment.CenterEnd)
                .padding(end = 52.dp, bottom = 50.dp)
        )
    }
}

@Composable
private fun PassengerBusMarker(
    modifier: Modifier
) {
    Box(
        modifier = modifier
            .size(45.dp)
            .clip(CircleShape)
            .background(MaterialTheme.colorScheme.primary),
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = "BUS",
            color = Color.White,
            fontSize = 9.sp,
            fontWeight = FontWeight.ExtraBold
        )
    }
}

@Composable
private fun LiveBusCard(
    modifier: Modifier,
    onClick: () -> Unit
) {
    Card(
        modifier = modifier
            .fillMaxWidth()
            .clickable(onClick = onClick),
        shape = RoundedCornerShape(18.dp),
        colors = CardDefaults.cardColors(
            containerColor = Color.White
        ),
        elevation = CardDefaults.cardElevation(
            defaultElevation = 6.dp
        )
    ) {
        Row(
            modifier = Modifier.padding(14.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .size(43.dp)
                    .clip(RoundedCornerShape(13.dp))
                    .background(Color(0xFFE9F2FF)),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = "BUS",
                    color = MaterialTheme.colorScheme.primary,
                    fontWeight = FontWeight.ExtraBold,
                    fontSize = 9.sp
                )
            }

            Column(
                modifier = Modifier
                    .weight(1f)
                    .padding(start = 12.dp)
            ) {
                Text(
                    text = "Buses en tiempo real",
                    fontWeight = FontWeight.Bold,
                    color = Color(0xFF172033)
                )

                Text(
                    text = "Consulta unidades cercanas y su recorrido",
                    fontSize = 11.sp,
                    color = Color(0xFF7E899B)
                )
            }

            Text(
                text = "›",
                fontSize = 27.sp,
                color = MaterialTheme.colorScheme.primary
            )
        }
    }
}

@Composable
private fun PassengerV3BottomBar(
    onRoutes: () -> Unit,
    onSos: () -> Unit,
    onReport: () -> Unit,
    onProfile: () -> Unit
) {
    Surface(
        color = Color.White,
        shadowElevation = 10.dp
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 6.dp, vertical = 8.dp),
            horizontalArrangement = Arrangement.SpaceAround,
            verticalAlignment = Alignment.CenterVertically
        ) {
            PassengerBottomItem(
                symbol = "⌂",
                label = "Inicio",
                selected = true,
                onClick = {}
            )

            PassengerBottomItem(
                symbol = "↝",
                label = "Rutas",
                selected = false,
                onClick = onRoutes
            )

            Box(
                modifier = Modifier
                    .size(52.dp)
                    .clip(CircleShape)
                    .background(Color(0xFFE52E32))
                    .clickable(onClick = onSos),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = "SOS",
                    color = Color.White,
                    fontSize = 11.sp,
                    fontWeight = FontWeight.ExtraBold
                )
            }

            PassengerBottomItem(
                symbol = "!",
                label = "Reportar",
                selected = false,
                onClick = onReport
            )

            PassengerBottomItem(
                symbol = "●",
                label = "Perfil",
                selected = false,
                onClick = onProfile
            )
        }
    }
}

@Composable
private fun PassengerBottomItem(
    symbol: String,
    label: String,
    selected: Boolean,
    onClick: () -> Unit
) {
    Column(
        modifier = Modifier
            .clip(RoundedCornerShape(12.dp))
            .clickable(onClick = onClick)
            .padding(horizontal = 7.dp, vertical = 4.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(
            text = symbol,
            fontSize = 19.sp,
            fontWeight = FontWeight.Bold,
            color = if (selected) {
                MaterialTheme.colorScheme.primary
            } else {
                Color(0xFF8D97A8)
            }
        )

        Text(
            text = label,
            fontSize = 9.sp,
            fontWeight = if (selected) {
                FontWeight.Bold
            } else {
                FontWeight.Medium
            },
            color = if (selected) {
                MaterialTheme.colorScheme.primary
            } else {
                Color(0xFF8D97A8)
            }
        )
    }
}

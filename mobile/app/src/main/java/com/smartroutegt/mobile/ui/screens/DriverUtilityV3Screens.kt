package com.smartroutegt.mobile.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

@Composable
private fun DriverV3Scaffold(
    title: String,
    subtitle: String,
    selected: SmartRouteV3DriverTab,
    onHome: () -> Unit,
    onRoute: () -> Unit,
    onGps: () -> Unit,
    onAlerts: () -> Unit,
    onProfile: () -> Unit,
    onReport: () -> Unit,
    content: @Composable () -> Unit
) {
    Scaffold(
        containerColor = SmartRouteBackground,
        topBar = {
            SmartRouteV3TopBar(
                title = title,
                subtitle = subtitle,
                role = SmartRouteV3Role.DRIVER
            )
        },
        bottomBar = {
            SmartRouteV3DriverBottomBar(
                selected = selected,
                onHome = onHome,
                onRoute = onRoute,
                onGps = onGps,
                onAlerts = onAlerts,
                onProfile = onProfile,
                onReport = onReport
            )
        }
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .verticalScroll(rememberScrollState())
                .padding(16.dp)
        ) {
            content()
            Spacer(modifier = Modifier.height(18.dp))
        }
    }
}

@Composable
fun DriverGpsV3Content(
    tripActive: Boolean,
    gpsActive: Boolean,
    speedKmh: Float,
    speedWarning: Boolean,
    speedViolationCreated: Boolean,
    speedLimitKmh: Double,
    backendSpeedKmh: Double,
    stopSafetyStatus: com.smartroutegt.mobile.network.DriverStopStatusDto?,
    latitude: Double?,
    longitude: Double?,
    nextStopName: String?,
    busCode: String,
    routeName: String,
    onHome: () -> Unit,
    onRoute: () -> Unit,
    onGps: () -> Unit,
    onAlerts: () -> Unit,
    onProfile: () -> Unit,
    onReport: () -> Unit
) {
    DriverV3Scaffold(
        title = "GPS",
        subtitle = "$busCode · $routeName",
        selected = SmartRouteV3DriverTab.GPS,
        onHome = onHome,
        onRoute = onRoute,
        onGps = onGps,
        onAlerts = onAlerts,
        onProfile = onProfile,
        onReport = onReport
    ) {
        DriverSpeedSafetyCard(
            speedKmh = if (tripActive && gpsActive) backendSpeedKmh else 0.0,
            limitKmh = speedLimitKmh,
            warning = tripActive && speedWarning,
            violationCreated = tripActive && speedViolationCreated,
            tripActive = tripActive,
            gpsActive = gpsActive
        )

        DriverStopSafetyCard(
            stopStatus = stopSafetyStatus,
            tripActive = tripActive,
            gpsActive = gpsActive
        )

        Spacer(modifier = Modifier.height(14.dp))

        Spacer(modifier = Modifier.height(14.dp))

        Column(
            modifier = Modifier
                .fillMaxWidth()
                .background(
                    SmartRouteGreenSoft,
                    RoundedCornerShape(22.dp)
                )
                .padding(18.dp)
        ) {
            Text(
                text = "SEGUIMIENTO GPS",
                color = SmartRouteMuted,
                fontSize = 12.sp,
                fontWeight = FontWeight.Bold
            )

            Spacer(modifier = Modifier.height(8.dp))

            Text(
                text = when {
                    !tripActive -> "En espera"
                    gpsActive -> "Transmitiendo"
                    else -> "Sin transmisión"
                },
                color = SmartRouteText,
                fontSize = 24.sp,
                fontWeight = FontWeight.ExtraBold
            )
        }

        /*
        SmartRouteV3StatusCard(
            title = "Seguimiento GPS",
            value = when {
                !tripActive -> "En espera"
                gpsActive -> "Transmitiendo"
                else -> "Sin transmisión"
            }
        )
        */

        Spacer(modifier = Modifier.height(8.dp))

        Text(
            text = when {
                !tripActive ->
                    "Inicia un viaje para comenzar el seguimiento."
                gpsActive ->
                    "La ubicación del bus se está enviando a SmartRoute GT."
                else ->
                    "El viaje está activo, pero el GPS no está transmitiendo."
            },
            color = SmartRouteMuted,
            fontSize = 12.sp
        )

        Spacer(modifier = Modifier.height(14.dp))

        SmartRouteV3Card {
            Text(
                text = "Estado actual",
                color = SmartRouteText,
                fontSize = 18.sp,
                fontWeight = FontWeight.Bold
            )

            Spacer(modifier = Modifier.height(16.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = "VELOCIDAD",
                        color = SmartRouteMuted,
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold
                    )

                    Text(
                        text = if (tripActive && gpsActive) {
                            "${speedKmh.toInt()} km/h"
                        } else {
                            "-- km/h"
                        },
                        color = SmartRouteText,
                        fontSize = 22.sp,
                        fontWeight = FontWeight.Bold
                    )
                }

                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = "VIAJE",
                        color = SmartRouteMuted,
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold
                    )

                    Text(
                        text = if (tripActive) "En curso" else "Sin viaje",
                        color = SmartRouteText,
                        fontSize = 22.sp,
                        fontWeight = FontWeight.Bold
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(14.dp))

        SmartRouteV3Card {
            Text(
                text = "Ubicación",
                color = SmartRouteText,
                fontSize = 18.sp,
                fontWeight = FontWeight.Bold
            )

            Spacer(modifier = Modifier.height(12.dp))

            if (tripActive && gpsActive &&
                latitude != null && longitude != null
            ) {
                Text(
                    text = "Latitud",
                    color = SmartRouteMuted,
                    fontSize = 12.sp
                )
                Text(
                    text = "%.6f".format(latitude),
                    color = SmartRouteText,
                    fontWeight = FontWeight.Bold
                )

                Spacer(modifier = Modifier.height(10.dp))

                Text(
                    text = "Longitud",
                    color = SmartRouteMuted,
                    fontSize = 12.sp
                )
                Text(
                    text = "%.6f".format(longitude),
                    color = SmartRouteText,
                    fontWeight = FontWeight.Bold
                )
            } else {
                Text(
                    text = if (tripActive) {
                        "Esperando una ubicación válida del dispositivo."
                    } else {
                        "La ubicación se mostrará al iniciar el viaje."
                    },
                    color = SmartRouteMuted
                )
            }
        }

        Spacer(modifier = Modifier.height(14.dp))

        SmartRouteV3Card {
            Text(
                text = "Próxima parada",
                color = SmartRouteMuted,
                fontSize = 12.sp,
                fontWeight = FontWeight.Bold
            )

            Spacer(modifier = Modifier.height(5.dp))

            Text(
                text = if (tripActive) {
                    nextStopName ?: "Calculando..."
                } else {
                    "Inicia el viaje para comenzar"
                },
                color = SmartRouteText,
                fontSize = 19.sp,
                fontWeight = FontWeight.Bold
            )
        }
    }
}

@Composable
fun DriverAlertsV3Content(
    sanctions: Int,
    driverActive: Boolean,
    tripActive: Boolean,
    violations: List<com.smartroutegt.mobile.network.ViolationDto>,
    loadingViolations: Boolean,
    violationsError: String?,
    onHome: () -> Unit,
    onRoute: () -> Unit,
    onGps: () -> Unit,
    onAlerts: () -> Unit,
    onProfile: () -> Unit,
    onReport: () -> Unit
) {
    DriverV3Scaffold(
        title = "Alertas",
        subtitle = "Seguridad e historial de operación",
        selected = SmartRouteV3DriverTab.ALERTS,
        onHome = onHome,
        onRoute = onRoute,
        onGps = onGps,
        onAlerts = onAlerts,
        onProfile = onProfile,
        onReport = onReport
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .background(
                    if (sanctions == 0) {
                        SmartRouteGreenSoft
                    } else {
                        SmartRouteRedSoft
                    },
                    RoundedCornerShape(22.dp)
                )
                .padding(18.dp)
        ) {
            Text(
                text = "SANCIONES CONFIRMADAS",
                color = SmartRouteMuted,
                fontSize = 12.sp,
                fontWeight = FontWeight.Bold
            )

            Spacer(modifier = Modifier.height(8.dp))

            Text(
                text = sanctions.toString(),
                color = SmartRouteText,
                fontSize = 30.sp,
                fontWeight = FontWeight.ExtraBold
            )

            Spacer(modifier = Modifier.height(5.dp))

            Text(
                text = if (sanctions == 0) {
                    "No tienes sanciones confirmadas"
                } else {
                    "Tienes sanciones confirmadas por administración"
                },
                color = SmartRouteMuted,
                fontSize = 12.sp
            )
        }

        Spacer(modifier = Modifier.height(14.dp))

        SmartRouteV3Card {
            Text(
                text = "Estado actual",
                color = SmartRouteMuted,
                fontSize = 12.sp,
                fontWeight = FontWeight.Bold
            )

            Spacer(modifier = Modifier.height(8.dp))

            Text(
                text = if (driverActive) {
                    "Piloto activo"
                } else {
                    "Piloto inactivo"
                },
                color = SmartRouteText,
                fontSize = 18.sp,
                fontWeight = FontWeight.Bold
            )

            Spacer(modifier = Modifier.height(4.dp))

            Text(
                text = if (tripActive) {
                    "Viaje en curso"
                } else {
                    "Sin viaje activo"
                },
                color = SmartRouteMuted,
                fontSize = 13.sp
            )
        }

        Spacer(modifier = Modifier.height(20.dp))

        Text(
            text = "Historial de alertas",
            color = SmartRouteText,
            fontSize = 20.sp,
            fontWeight = FontWeight.ExtraBold
        )

        Spacer(modifier = Modifier.height(4.dp))

        Text(
            text = "Eventos de seguridad registrados durante tus viajes.",
            color = SmartRouteMuted,
            fontSize = 12.sp
        )

        Spacer(modifier = Modifier.height(14.dp))

        when {
            loadingViolations -> {
                SmartRouteV3Card {
                    Text(
                        text = "Cargando historial...",
                        color = SmartRouteMuted,
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Medium
                    )
                }
            }

            violationsError != null -> {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(
                            SmartRouteRedSoft,
                            RoundedCornerShape(20.dp)
                        )
                        .padding(16.dp)
                ) {
                    Text(
                        text = "No se pudo cargar el historial",
                        color = SmartRouteText,
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Bold
                    )

                    Spacer(modifier = Modifier.height(5.dp))

                    Text(
                        text = violationsError,
                        color = SmartRouteMuted,
                        fontSize = 12.sp
                    )
                }
            }

            violations.isEmpty() -> {
                SmartRouteV3EmptyState(
                    title = "Sin alertas registradas",
                    description =
                        "Cuando se registre un evento de seguridad durante un viaje aparecerá aquí."
                )
            }

            else -> {
                violations.forEach { violation ->
                    val statusLabel = when (violation.estado.uppercase()) {
                        "PENDING" -> "PENDIENTE"
                        "CONFIRMED" -> "CONFIRMADA"
                        "REJECTED" -> "RECHAZADA"
                        "CANCELLED" -> "CANCELADA"
                        else -> violation.estado.uppercase()
                    }

                    val statusBackground = when (violation.estado.uppercase()) {
                        "CONFIRMED" -> SmartRouteRedSoft
                        "PENDING" -> SmartRouteAmberSoft
                        "REJECTED",
                        "CANCELLED" -> SmartRouteGreenSoft
                        else -> SmartRouteBlueSoft
                    }

                    val typeLabel = when (violation.tipo.uppercase()) {
                        "SPEEDING" -> "Exceso de velocidad"
                        "STOP_SKIPPED" -> "Parada incumplida"
                        else -> violation.tipo
                            .replace("_", " ")
                            .lowercase()
                            .replaceFirstChar { it.uppercase() }
                    }

                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .background(
                                statusBackground,
                                RoundedCornerShape(22.dp)
                            )
                            .padding(16.dp)
                    ) {
                        Text(
                            text = statusLabel,
                            color = SmartRouteMuted,
                            fontSize = 11.sp,
                            fontWeight = FontWeight.ExtraBold
                        )

                        Spacer(modifier = Modifier.height(5.dp))

                        Text(
                            text = typeLabel,
                            color = SmartRouteText,
                            fontSize = 18.sp,
                            fontWeight = FontWeight.ExtraBold
                        )

                        if (violation.descripcion.isNotBlank()) {
                            Spacer(modifier = Modifier.height(5.dp))

                            Text(
                                text = violation.descripcion,
                                color = SmartRouteMuted,
                                fontSize = 12.sp
                            )
                        }

                        if (
                            violation.velocidadRegistrada != null ||
                            violation.limitePermitido != null
                        ) {
                            Spacer(modifier = Modifier.height(14.dp))

                            Row(
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                Column(
                                    modifier = Modifier.weight(1f)
                                ) {
                                    Text(
                                        text = "VELOCIDAD",
                                        color = SmartRouteMuted,
                                        fontSize = 10.sp,
                                        fontWeight = FontWeight.Bold
                                    )

                                    Text(
                                        text = violation.velocidadRegistrada
                                            ?.let { "%.0f km/h".format(it) }
                                            ?: "--",
                                        color = SmartRouteText,
                                        fontSize = 17.sp,
                                        fontWeight = FontWeight.Bold
                                    )
                                }

                                Column(
                                    modifier = Modifier.weight(1f)
                                ) {
                                    Text(
                                        text = "LÍMITE",
                                        color = SmartRouteMuted,
                                        fontSize = 10.sp,
                                        fontWeight = FontWeight.Bold
                                    )

                                    Text(
                                        text = violation.limitePermitido
                                            ?.let { "%.0f km/h".format(it) }
                                            ?: "--",
                                        color = SmartRouteText,
                                        fontSize = 17.sp,
                                        fontWeight = FontWeight.Bold
                                    )
                                }
                            }
                        }

                        Spacer(modifier = Modifier.height(12.dp))

                        violation.bus?.codigo
                            ?.takeIf { it.isNotBlank() }
                            ?.let { busCode ->
                                Text(
                                    text = busCode,
                                    color = SmartRouteMuted,
                                    fontSize = 12.sp,
                                    fontWeight = FontWeight.Bold
                                )

                                Spacer(modifier = Modifier.height(3.dp))
                            }

                        violation.fechaHora
                            ?.takeIf { it.isNotBlank() }
                            ?.let { rawDate ->
                                val formattedDate = try {
                                    val dateTime =
                                        java.time.LocalDateTime.parse(rawDate)

                                    "%02d/%02d/%04d · %02d:%02d".format(
                                        dateTime.dayOfMonth,
                                        dateTime.monthValue,
                                        dateTime.year,
                                        dateTime.hour,
                                        dateTime.minute
                                    )
                                } catch (exception: Exception) {
                                    rawDate
                                        .replace("T", " ")
                                        .take(16)
                                }

                                Text(
                                    text = formattedDate,
                                    color = SmartRouteMuted,
                                    fontSize = 11.sp
                                )
                            }
                    }

                    Spacer(modifier = Modifier.height(12.dp))
                }
            }
        }
    }
}

@Composable
fun DriverProfileV3Content(
    name: String,
    license: String,
    phone: String,
    busCode: String,
    routeName: String,
    driverActive: Boolean,
    onLogout: () -> Unit,
    onHome: () -> Unit,
    onRoute: () -> Unit,
    onGps: () -> Unit,
    onAlerts: () -> Unit,
    onProfile: () -> Unit,
    onReport: () -> Unit
) {
    DriverV3Scaffold(
        title = "Perfil",
        subtitle = "Información del piloto",
        selected = SmartRouteV3DriverTab.PROFILE,
        onHome = onHome,
        onRoute = onRoute,
        onGps = onGps,
        onAlerts = onAlerts,
        onProfile = onProfile,
        onReport = onReport
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .background(
                    SmartRouteBlueSoft,
                    RoundedCornerShape(22.dp)
                )
                .padding(20.dp)
        ) {
            Text(
                text = name,
                color = SmartRouteText,
                fontSize = 25.sp,
                fontWeight = FontWeight.ExtraBold
            )

            Spacer(modifier = Modifier.height(4.dp))

            Text(
                text = "Piloto SmartRoute GT",
                color = SmartRouteMuted,
                fontSize = 13.sp
            )
        }

        Spacer(modifier = Modifier.height(14.dp))

        SmartRouteV3Card {
            ProfileV3Row("Licencia", license)
            Spacer(modifier = Modifier.height(14.dp))

            ProfileV3Row(
                "Teléfono",
                phone.ifBlank { "No registrado" }
            )

            Spacer(modifier = Modifier.height(14.dp))

            ProfileV3Row(
                "Estado",
                if (driverActive) "Activo" else "Inactivo"
            )
        }

        Spacer(modifier = Modifier.height(14.dp))

        SmartRouteV3Card {
            Text(
                text = "Asignación actual",
                color = SmartRouteText,
                fontSize = 18.sp,
                fontWeight = FontWeight.Bold
            )

            Spacer(modifier = Modifier.height(14.dp))

            ProfileV3Row("Bus", busCode)
            Spacer(modifier = Modifier.height(14.dp))
            ProfileV3Row("Ruta", routeName)
        }

        Spacer(modifier = Modifier.height(20.dp))

        OutlinedButton(
            onClick = onLogout,
            modifier = Modifier
                .fillMaxWidth()
                .height(54.dp)
        ) {
            Text(
                text = "CERRAR SESIÓN",
                fontWeight = FontWeight.Bold
            )
        }
    }
}

@Composable
private fun ProfileV3Row(
    label: String,
    value: String
) {
    Text(
        text = label.uppercase(),
        color = SmartRouteMuted,
        fontSize = 11.sp,
        fontWeight = FontWeight.Bold
    )

    Spacer(modifier = Modifier.height(3.dp))

    Text(
        text = value,
        color = SmartRouteText,
        fontSize = 17.sp,
        fontWeight = FontWeight.Bold
    )
}

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
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.smartroutegt.mobile.network.NextStopDto

@Composable
fun DriverDashboardV3Content(
    pilotoNombre: String,
    pilotoLicencia: String,
    busCodigo: String,
    rutaNombre: String,
    estadoViaje: String,
    estadoGps: String,
    velocidadActual: Float,
    speedWarning: Boolean,
    speedViolationCreated: Boolean,
    speedLimitKmh: Double,
    backendSpeedKmh: Double,
    stopSafetyStatus: com.smartroutegt.mobile.network.DriverStopStatusDto?,
    proximaParada: NextStopDto?,
    sancionesConfirmadas: Int,
    viajeActivo: Boolean,
    tripId: Long?,
    iniciandoViaje: Boolean,
    finalizandoViaje: Boolean,
    errorViaje: String?,
    onStartTrip: () -> Unit,
    onFinishTrip: () -> Unit,
    onHome: () -> Unit = {},
    onRoute: () -> Unit = {},
    onGps: () -> Unit = {},
    onAlerts: () -> Unit = {},
    onProfile: () -> Unit = {},
    onReport: () -> Unit = {}
) {
    Scaffold(
        containerColor = SmartRouteBackground,
        topBar = {
            SmartRouteV3TopBar(
                title = "SmartRoute GT",
                subtitle = "Operación del piloto",
                role = SmartRouteV3Role.DRIVER
            )
        },
        bottomBar = {
            SmartRouteV3DriverBottomBar(
                selected = SmartRouteV3DriverTab.HOME,
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
            Text(
                text = "Hola, $pilotoNombre",
                fontSize = 24.sp,
                fontWeight = FontWeight.ExtraBold,
                color = SmartRouteText
            )

            Text(
                text = "Licencia $pilotoLicencia",
                fontSize = 12.sp,
                color = SmartRouteMuted
            )

            Spacer(modifier = Modifier.height(18.dp))

            DriverBoardingCodeV3Card(tripId = tripId)
            Spacer(modifier = Modifier.height(14.dp))

            DriverSpeedSafetyCard(
                speedKmh = if (viajeActivo) backendSpeedKmh else 0.0,
                limitKmh = speedLimitKmh,
                warning = viajeActivo && speedWarning,
                violationCreated = viajeActivo && speedViolationCreated,
                tripActive = viajeActivo,
                gpsActive = viajeActivo && estadoGps == "Transmitiendo"
            )

            DriverStopSafetyCard(
            stopStatus = stopSafetyStatus,
            tripActive = viajeActivo,
            gpsActive = viajeActivo && estadoGps == "Transmitiendo"
        )

        Spacer(modifier = Modifier.height(14.dp))

        Spacer(modifier = Modifier.height(14.dp))

            SmartRouteV3Card {
                Text(
                    text = "Operación asignada",
                    fontWeight = FontWeight.Bold,
                    fontSize = 17.sp,
                    color = SmartRouteText
                )

                Spacer(modifier = Modifier.height(14.dp))

                Text(
                    text = busCodigo,
                    fontSize = 25.sp,
                    fontWeight = FontWeight.ExtraBold,
                    color = SmartRouteBlue
                )

                Text(
                    text = rutaNombre,
                    fontSize = 14.sp,
                    color = SmartRouteMuted
                )

                Spacer(modifier = Modifier.height(14.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    SmartRouteV3StatusCard(
                        title = "VIAJE",
                        value = estadoViaje,
                        positive = viajeActivo,
                        modifier = Modifier.weight(1f)
                    )

                    SmartRouteV3StatusCard(
                        title = "GPS",
                        value = if (
                            estadoGps.contains("disponible", ignoreCase = true) ||
                            estadoGps.contains("concedido", ignoreCase = true)
                        ) {
                            "Activo"
                        } else {
                            estadoGps
                        },
                        detail = "${"%.1f".format(velocidadActual)} km/h",
                        positive = estadoGps.contains(
                            "disponible",
                            ignoreCase = true
                        ),
                        modifier = Modifier.weight(1f)
                    )
                }
            }

            Spacer(modifier = Modifier.height(14.dp))

            SmartRouteV3Card {
                Text(
                    text = "Próxima parada",
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Bold,
                    color = SmartRouteMuted
                )

                Spacer(modifier = Modifier.height(6.dp))

                Text(
                    text = proximaParada?.parada
                        ?: if (viajeActivo) {
                            "Consultando próxima parada..."
                        } else {
                            "Inicia el viaje para comenzar"
                        },
                    fontSize = 20.sp,
                    fontWeight = FontWeight.Bold,
                    color = SmartRouteText
                )

                if (proximaParada?.distanciaMetros != null) {
                    Spacer(modifier = Modifier.height(6.dp))

                    Text(
                        text = if (proximaParada.distanciaMetros >= 1000.0) {
                            "%.2f km de distancia".format(
                                proximaParada.distanciaMetros / 1000.0
                            )
                        } else {
                            "%.0f m de distancia".format(
                                proximaParada.distanciaMetros
                            )
                        },
                        color = SmartRouteMuted,
                        fontSize = 13.sp
                    )
                }

                if (proximaParada?.etaMinutos != null) {
                    Spacer(modifier = Modifier.height(5.dp))

                    Text(
                        text = "ETA aproximado: ${proximaParada.etaMinutos} min",
                        color = SmartRouteBlue,
                        fontWeight = FontWeight.Bold
                    )
                }
            }

            Spacer(modifier = Modifier.height(14.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                SmartRouteV3StatusCard(
                    title = "VELOCIDAD",
                    value = "${"%.0f".format(velocidadActual)} km/h",
                    detail = "GPS actual",
                    positive = true,
                    modifier = Modifier.weight(1f)
                )

                SmartRouteV3StatusCard(
                    title = "SANCIONES",
                    value = sancionesConfirmadas.toString(),
                    detail = "Confirmadas",
                    positive = sancionesConfirmadas == 0,
                    modifier = Modifier.weight(1f)
                )
            }

            if (errorViaje != null) {
                Spacer(modifier = Modifier.height(14.dp))

                SmartRouteV3EmptyState(
                    title = "No se pudo actualizar el viaje",
                    description = errorViaje,
                    error = true
                )
            }

            Spacer(modifier = Modifier.height(18.dp))

            if (!viajeActivo) {
                Button(
                    onClick = onStartTrip,
                    enabled = !iniciandoViaje,
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(56.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = SmartRouteGreen
                    )
                ) {
                    Text(
                        text = if (iniciandoViaje) {
                            "INICIANDO..."
                        } else {
                            "INICIAR VIAJE"
                        },
                        fontWeight = FontWeight.Bold
                    )
                }
            } else {
                Button(
                    onClick = onFinishTrip,
                    enabled = !finalizandoViaje,
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(56.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = SmartRouteRed
                    )
                ) {
                    Text(
                        text = if (finalizandoViaje) {
                            "FINALIZANDO..."
                        } else {
                            "FINALIZAR VIAJE"
                        },
                        fontWeight = FontWeight.Bold
                    )
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            Text(
                text = if (viajeActivo) {
                    "Tu ubicación se utiliza para actualizar el recorrido y la información que reciben los pasajeros."
                } else {
                    "Cuando inicies el viaje se activará el seguimiento GPS de la operación."
                },
                fontSize = 11.sp,
                color = SmartRouteMuted,
                lineHeight = 16.sp
            )

            Spacer(modifier = Modifier.height(16.dp))
        }
    }
}

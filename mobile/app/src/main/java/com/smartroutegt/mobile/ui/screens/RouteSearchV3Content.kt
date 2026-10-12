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
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.smartroutegt.mobile.data.remote.BusMonitoringDto
import com.smartroutegt.mobile.data.remote.RouteDto
import com.smartroutegt.mobile.data.remote.RouteGeometryPointDto
import com.smartroutegt.mobile.data.remote.RouteStopDto
import com.smartroutegt.mobile.data.remote.StopDto
import com.smartroutegt.mobile.network.NextStopDto

@Composable
fun RouteSearchV3Content(
    routes: List<RouteDto>,
    selectedRoute: RouteDto?,
    routeStops: List<RouteStopDto>,
    selectedOrigin: StopDto?,
    selectedDestination: StopDto?,
    routePath: List<Long>?,
    geometryPoints: List<RouteGeometryPointDto>,
    routeBuses: List<BusMonitoringDto>,
    selectedBus: BusMonitoringDto?,
    nextStop: NextStopDto?,
    isLoadingRoutes: Boolean,
    isLoadingStops: Boolean,
    isSearchingRoute: Boolean,
    isLoadingBuses: Boolean,
    isLoadingEta: Boolean,
    routeLoadError: String?,
    stopLoadError: String?,
    routeSearchError: String?,
    busLoadError: String?,
    etaError: String?,
    onBack: () -> Unit,
    onRouteSelected: (RouteDto) -> Unit,
    onOriginSelected: (StopDto) -> Unit,
    onDestinationSelected: (StopDto) -> Unit,
    onSearch: () -> Unit,
    onBusSelected: (BusMonitoringDto) -> Unit,
    onConfirmSelection: () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFFF4F7FB))
    ) {
        PassengerV3TopBar(
            title = "Planifica tu viaje",
            subtitle = "Encuentra la mejor ruta",
            onBack = onBack
        )

        Column(
            modifier = Modifier
                .weight(1f)
                .verticalScroll(rememberScrollState())
                .padding(16.dp)
        ) {
            PassengerV3SectionCard {
                SectionTitle(
                    title = "Selecciona una ruta",
                    subtitle = "Elige la ruta que deseas utilizar"
                )

                when {
                    isLoadingRoutes -> LoadingRow("Cargando rutas...")
                    routeLoadError != null -> ErrorText(routeLoadError)
                    routes.isEmpty() -> EmptyText("No hay rutas disponibles")

                    else -> routes.forEachIndexed { index, route ->
                        PassengerV3Choice(
                            title = "${route.codigo} · ${route.nombre}",
                            subtitle = if (route.id == selectedRoute?.id) {
                                "Ruta seleccionada"
                            } else {
                                "Toca para seleccionar"
                            },
                            selected = route.id == selectedRoute?.id,
                            markerColor = Color(0xFF2E7DFF),
                            onClick = { onRouteSelected(route) }
                        )

                        if (index < routes.lastIndex) {
                            Spacer(modifier = Modifier.height(8.dp))
                        }
                    }
                }
            }

            if (selectedRoute != null) {
                Button(onClick = onConfirmSelection, modifier = Modifier.fillMaxWidth()) {
                    Text("Usar esta ruta en Inicio")
                }

                Spacer(modifier = Modifier.height(14.dp))

                PassengerV3SectionCard {
                    SectionTitle(
                        title = "Tu recorrido",
                        subtitle = "Selecciona dónde subes y a dónde vas"
                    )

                    when {
                        isLoadingStops -> LoadingRow("Cargando paradas...")
                        stopLoadError != null -> ErrorText(stopLoadError)
                        routeStops.isEmpty() -> EmptyText("Esta ruta no tiene paradas")
                        else -> {
                            Text(
                                text = "Origen",
                                fontWeight = FontWeight.SemiBold,
                                color = Color(0xFF344054)
                            )

                            Spacer(modifier = Modifier.height(8.dp))

                            routeStops.forEachIndexed { index, routeStop ->
                                PassengerV3Choice(
                                    title = routeStop.stop.nombre,
                                    subtitle = if (
                                        routeStop.stop.id == selectedOrigin?.id
                                    ) {
                                        "Punto de partida"
                                    } else {
                                        null
                                    },
                                    selected = routeStop.stop.id == selectedOrigin?.id,
                                    markerColor = Color(0xFF2E7DFF),
                                    onClick = {
                                        onOriginSelected(routeStop.stop)
                                    }
                                )

                                if (index < routeStops.lastIndex) {
                                    Spacer(modifier = Modifier.height(6.dp))
                                }
                            }

                            Spacer(modifier = Modifier.height(18.dp))

                            Text(
                                text = "Destino",
                                fontWeight = FontWeight.SemiBold,
                                color = Color(0xFF344054)
                            )

                            Spacer(modifier = Modifier.height(8.dp))

                            routeStops.forEachIndexed { index, routeStop ->
                                PassengerV3Choice(
                                    title = routeStop.stop.nombre,
                                    subtitle = if (
                                        routeStop.stop.id == selectedDestination?.id
                                    ) {
                                        "Destino seleccionado"
                                    } else {
                                        null
                                    },
                                    selected = routeStop.stop.id == selectedDestination?.id,
                                    markerColor = Color(0xFFE5484D),
                                    onClick = {
                                        onDestinationSelected(routeStop.stop)
                                    }
                                )

                                if (index < routeStops.lastIndex) {
                                    Spacer(modifier = Modifier.height(6.dp))
                                }
                            }
                        }
                    }
                }

                if (routeStops.isNotEmpty()) {
                    Spacer(modifier = Modifier.height(14.dp))

                    PassengerV3SectionCard {
                        SectionTitle(
                            title = "Mapa de la ruta",
                            subtitle = selectedRoute.nombre
                        )

                        PassengerMap(
                            routeStops = routeStops,
                            routePath = routePath,
                            geometryPoints = geometryPoints,
                            modifier = Modifier.fillMaxWidth()
                        )
                    }
                }

                Spacer(modifier = Modifier.height(14.dp))

                Button(
                    onClick = onSearch,
                    enabled = selectedOrigin != null &&
                        selectedDestination != null &&
                        !isSearchingRoute,
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(54.dp)
                ) {
                    if (isSearchingRoute) {
                        CircularProgressIndicator(
                            modifier = Modifier.size(22.dp),
                            strokeWidth = 2.dp
                        )

                        Text(
                            text = "Buscando...",
                            modifier = Modifier.padding(start = 10.dp)
                        )
                    } else {
                        Text(
                            text = "Buscar mejor ruta",
                            fontWeight = FontWeight.Bold
                        )
                    }
                }
            }

            routeSearchError?.let {
                Spacer(modifier = Modifier.height(12.dp))
                ErrorText(it)
            }

            when {
                routePath != null && routePath.isEmpty() -> {
                    Spacer(modifier = Modifier.height(14.dp))

                    PassengerV3SectionCard {
                        EmptyText(
                            "No se encontró un recorrido entre las paradas seleccionadas"
                        )
                    }
                }

                routePath != null -> {
                    Spacer(modifier = Modifier.height(14.dp))

                    PassengerV3SectionCard {
                        SectionTitle(
                            title = "Ruta encontrada",
                            subtitle = "Recorrido recomendado por SmartRoute"
                        )

                        val names = routePath.mapNotNull { stopId ->
                            routeStops
                                .firstOrNull { it.stop.id == stopId }
                                ?.stop
                                ?.nombre
                        }

                        Text(
                            text = if (names.isEmpty()) {
                                "Recorrido calculado correctamente"
                            } else {
                                names.joinToString("  →  ")
                            },
                            color = Color(0xFF344054),
                            lineHeight = 21.sp
                        )
                    }

                    Spacer(modifier = Modifier.height(14.dp))

                    PassengerV3SectionCard {
                        SectionTitle(
                            title = "Buses disponibles",
                            subtitle = "Selecciona el bus que deseas seguir"
                        )

                        when {
                            isLoadingBuses -> LoadingRow("Buscando buses...")
                            busLoadError != null -> ErrorText(busLoadError)

                            routeBuses.isEmpty() -> {
                                EmptyText(
                                    "No hay buses activos en esta ruta en este momento"
                                )
                            }

                            else -> routeBuses.forEachIndexed { index, bus ->
                                PassengerV3BusCard(
                                    code = bus.codigo,
                                    plate = bus.placa,
                                    selected = selectedBus?.busId == bus.busId,
                                    onClick = { onBusSelected(bus) }
                                )

                                if (index < routeBuses.lastIndex) {
                                    Spacer(modifier = Modifier.height(8.dp))
                                }
                            }
                        }
                    }
                }
            }

            selectedBus?.let { bus ->
                Spacer(modifier = Modifier.height(14.dp))

                when {
                    isLoadingEta -> {
                        PassengerV3SectionCard {
                            LoadingRow("Calculando llegada del bus...")
                        }
                    }

                    etaError != null -> {
                        PassengerV3SectionCard {
                            ErrorText(etaError)
                        }
                    }

                    nextStop != null -> {
                        PassengerV3EtaCard(
                            busCode = bus.codigo,
                            plate = bus.placa,
                            routeName = bus.ruta ?: selectedRoute?.nombre ?: "Ruta",
                            nextStop = nextStop.parada,
                            etaMinutes = nextStop.etaMinutos?.toInt(),
                            distanceMeters = nextStop.distanciaMetros,
                            speed = nextStop.velocidadActualKmh ?: bus.velocidad,
                            status = nextStop.estadoEta ?: "EN RUTA"
                        )
                    }

                    else -> {
                        PassengerV3SectionCard {
                            EmptyText("Selecciona un bus para consultar su llegada")
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(24.dp))
        }
    }
}

@Composable
private fun SectionTitle(
    title: String,
    subtitle: String
) {
    Text(
        text = title,
        fontWeight = FontWeight.Bold,
        fontSize = 17.sp,
        color = Color(0xFF172033)
    )

    Text(
        text = subtitle,
        fontSize = 12.sp,
        color = Color(0xFF7D899D),
        modifier = Modifier.padding(top = 3.dp, bottom = 12.dp)
    )
}

@Composable
private fun LoadingRow(text: String) {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.Start
    ) {
        CircularProgressIndicator(
            modifier = Modifier.size(22.dp),
            strokeWidth = 2.dp
        )

        Text(
            text = text,
            modifier = Modifier.padding(start = 10.dp),
            color = Color(0xFF667085)
        )
    }
}

@Composable
private fun ErrorText(text: String) {
    Text(
        text = text,
        color = MaterialTheme.colorScheme.error,
        fontSize = 13.sp
    )
}

@Composable
private fun EmptyText(text: String) {
    Text(
        text = text,
        color = Color(0xFF667085),
        fontSize = 13.sp
    )
}

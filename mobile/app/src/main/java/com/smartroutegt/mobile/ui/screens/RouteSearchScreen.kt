package com.smartroutegt.mobile.ui.screens

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.compose.material3.OutlinedButton
import androidx.compose.runtime.LaunchedEffect
import com.smartroutegt.mobile.data.remote.ApiClient
import com.smartroutegt.mobile.data.remote.RouteDto
import com.smartroutegt.mobile.data.remote.StopDto
import com.smartroutegt.mobile.data.remote.BusMonitoringDto
import com.smartroutegt.mobile.data.remote.RouteGeometryPointDto
import com.smartroutegt.mobile.network.DriverApiService
import com.smartroutegt.mobile.network.NextStopDto
import androidx.compose.runtime.rememberCoroutineScope
import kotlinx.coroutines.launch
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll


@Composable
fun RouteSearchScreen(
    onBack: () -> Unit
) {
    var routes by remember {
        mutableStateOf<List<RouteDto>>(emptyList())
    }
    var selectedRoute by remember {
        mutableStateOf<RouteDto?>(null)
    }

    var selectedOrigin by remember {
        mutableStateOf<StopDto?>(null)
    }

    var selectedDestination by remember {
        mutableStateOf<StopDto?>(null)
    }

    var routeStops by remember {
        mutableStateOf<List<com.smartroutegt.mobile.data.remote.RouteStopDto>>(emptyList())
    }

    var isLoadingStops by remember {
        mutableStateOf(false)
    }

    var stopLoadError by remember {
        mutableStateOf<String?>(null)
    }

    var isLoadingRoutes by remember {
        mutableStateOf(true)
    }

    var routeLoadError by remember {
        mutableStateOf<String?>(null)
    }

    var isSearchingRoute by remember {
        mutableStateOf(false)
    }

    var routeSearchError by remember {
        mutableStateOf<String?>(null)
    }

    var routePath by remember {
        mutableStateOf<List<Long>?>(null)
    }

    var routeBuses by remember {
        mutableStateOf<List<BusMonitoringDto>>(emptyList())
    }

    var selectedBus by remember {
        mutableStateOf<BusMonitoringDto?>(null)
    }

    var nextStop by remember {
        mutableStateOf<NextStopDto?>(null)
    }

    var isLoadingEta by remember {
        mutableStateOf(false)
    }

    var etaError by remember {
        mutableStateOf<String?>(null)
    }

    val driverApiService = remember {
        DriverApiService(
            com.smartroutegt.mobile.network.ApiClient.create()
        )
    }

    var geometryPoints by remember {
        mutableStateOf<List<RouteGeometryPointDto>>(emptyList())
    }

    var isLoadingBuses by remember {
        mutableStateOf(false)
    }

    var busLoadError by remember {
        mutableStateOf<String?>(null)
    }

    LaunchedEffect(Unit) {
        try {
            routes = ApiClient.routeApiService.getRoutes()
            routeLoadError = null
        } catch (e: Exception) {
            routeLoadError = "No se pudieron cargar las rutas"
        } finally {
            isLoadingRoutes = false
        }
    }
    LaunchedEffect(selectedRoute) {
        val route = selectedRoute ?: return@LaunchedEffect

        isLoadingStops = true
        stopLoadError = null

        try {
            routeStops = ApiClient.routeStopApiService.getStopsByRoute(route.id)

            geometryPoints =
                ApiClient.routeApiService
                    .getRouteGeometry(route.id)
                    .points

        } catch (e: Exception) {
            routeStops = emptyList()
            geometryPoints = emptyList()
            stopLoadError = "No se pudieron cargar las paradas o geometría de la ruta"
        } finally {
            isLoadingStops = false
        }
    }

    val coroutineScope = rememberCoroutineScope()

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(24.dp)
    ) {

        OutlinedButton(
            onClick = onBack
        ) {
            Text(text = "← Volver al menú")
        }

        Text(
            text = "Buscar ruta",
            modifier = Modifier.padding(top = 16.dp)
        )

        when {
            isLoadingRoutes -> {
                Text(
                    text = "Cargando rutas...",
                    modifier = Modifier.padding(top = 24.dp)
                )
            }

            routeLoadError != null -> {
                Text(
                    text = routeLoadError ?: "",
                    modifier = Modifier.padding(top = 24.dp)
                )
            }

            else -> {
                Text(
                    text = "Rutas disponibles:",
                    modifier = Modifier.padding(top = 24.dp)
                )

                routes.forEach { route ->
                    Button(
                        onClick = {
                            selectedRoute = route
                            selectedOrigin = null
                            selectedDestination = null
                            routePath = null
                            routeBuses = emptyList()
                            selectedBus = null
                            busLoadError = null
                        },
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(top = 8.dp)
                    ) {
                        Text(
                            text = "${route.codigo} - ${route.nombre}"
                        )
                    }
                }
                selectedRoute?.let { route ->
                    Text(
                        text = "Ruta seleccionada: ${route.codigo} - ${route.nombre}",
                        modifier = Modifier.padding(top = 16.dp)
                    )
                }

                if (selectedRoute != null && routeStops.isNotEmpty()) {
                    PassengerMap(
                        routeStops = routeStops,
                        routePath = routePath,
                        geometryPoints = geometryPoints,
                        modifier = Modifier.padding(top = 16.dp)
                    )
                }

                when {
                    selectedRoute == null -> {
                        Text(
                            text = "Selecciona una ruta para ver sus paradas",
                            modifier = Modifier.padding(top = 16.dp)
                        )
                    }

                    isLoadingStops -> {
                        Text(
                            text = "Cargando paradas...",
                            modifier = Modifier.padding(top = 16.dp)
                        )
                    }

                    stopLoadError != null -> {
                        Text(
                            text = stopLoadError ?: "",
                            modifier = Modifier.padding(top = 16.dp)
                        )
                    }

                    routeStops.isEmpty() -> {
                        Text(
                            text = "Esta ruta no tiene paradas disponibles",
                            modifier = Modifier.padding(top = 16.dp)
                        )
                    }

                    else -> {
                        routeStops.forEach { routeStop ->

                            Button(
                                onClick = {
                                    selectedOrigin = routeStop.stop
                                },
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(top = 8.dp)
                            ) {
                                Text(
                                    text = "Origen: ${routeStop.stop.nombre}"
                                )
                            }

                            Button(
                                onClick = {
                                    selectedDestination = routeStop.stop
                                },
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(top = 4.dp)
                            ) {
                                Text(
                                    text = "Destino: ${routeStop.stop.nombre}"
                                )
                            }
                        }
                    }
                }

                selectedOrigin?.let { stop ->
                    Text(
                        text = "Origen seleccionado: ${stop.nombre}",
                        modifier = Modifier.padding(top = 16.dp)
                    )
                }

                selectedDestination?.let { stop ->
                    Text(
                        text = "Destino seleccionado: ${stop.nombre}",
                        modifier = Modifier.padding(top = 8.dp)
                    )
                }
            }
        }

        Button(
            onClick = {
                val route = selectedRoute
                val origin = selectedOrigin
                val destination = selectedDestination

                if (route == null || origin == null || destination == null) {
                    routeSearchError = "Selecciona una ruta, un origen y un destino"
                    routePath = null
                    return@Button
                }

                coroutineScope.launch {
                    isSearchingRoute = true
                    isLoadingBuses = true
                    routeSearchError = null
                    busLoadError = null
                    routePath = null
                    routeBuses = emptyList()
                    selectedBus = null

                    try {
                        routePath = ApiClient.algorithmApiService.getRoutePath(
                            routeId = route.id,
                            origen = origin.id,
                            destino = destination.id
                        )

                        if (!routePath.isNullOrEmpty()) {
                            geometryPoints =
                                ApiClient.routeApiService
                                    .getRouteGeometry(
                                        routeId = route.id,
                                        originStopId = origin.id,
                                        destinationStopId = destination.id
                                    )
                                    .points
                        }

                        val buses = ApiClient.busMonitoringApiService.getBuses()

                        routeBuses = buses.filter { bus ->
                            bus.routeId == route.id &&
                            bus.viajeActivo &&
                            bus.tripId != null
                        }

                    } catch (e: Exception) {
                        routeSearchError = "No se pudo completar la búsqueda: ${e.message}"
                    } finally {
                        isSearchingRoute = false
                        isLoadingBuses = false
                    }
                }
            },
            modifier = Modifier
                .fillMaxWidth()
                .padding(top = 24.dp)
        ) {
            Text(text = "Buscar")
        }

        when {
            isSearchingRoute -> {
                Text(
                    text = "Calculando ruta...",
                    modifier = Modifier.padding(top = 16.dp)
                )
            }

            routeSearchError != null -> {
                Text(
                    text = routeSearchError ?: "",
                    modifier = Modifier.padding(top = 16.dp)
                )
            }

            routePath != null && routePath!!.isEmpty() -> {
                Text(
                    text = "No se encontró una ruta entre las paradas seleccionadas",
                    modifier = Modifier.padding(top = 16.dp)
                )
            }

            routePath != null -> {
                Text(
                    text = "Ruta encontrada:",
                    modifier = Modifier.padding(top = 16.dp)
                )

                routePath!!.forEach { stopId ->
                    val stopName = routeStops
                        .find { it.stop.id == stopId }
                        ?.stop
                        ?.nombre

                    Text(
                        text = stopName ?: "Parada ID: $stopId",
                        modifier = Modifier.padding(top = 8.dp)
                    )
                }

                Text(
                    text = "Buses disponibles en esta ruta:",
                    modifier = Modifier.padding(top = 24.dp)
                )

                when {
                    isLoadingBuses -> {
                        Text(
                            text = "Buscando buses activos...",
                            modifier = Modifier.padding(top = 8.dp)
                        )
                    }

                    busLoadError != null -> {
                        Text(
                            text = busLoadError ?: "",
                            modifier = Modifier.padding(top = 8.dp)
                        )
                    }

                    routeBuses.isEmpty() -> {
                        Text(
                            text = "No hay buses realizando esta ruta en este momento.",
                            modifier = Modifier.padding(top = 8.dp)
                        )
                    }

                    else -> {
                        routeBuses.forEach { bus ->
                            Button(
                                onClick = {
                                    selectedBus = bus
                                    nextStop = null
                                    etaError = null

                                    val tripId = bus.tripId

                                    if (tripId != null) {
                                        coroutineScope.launch {
                                            isLoadingEta = true

                                            try {
                                                nextStop =
                                                    driverApiService
                                                        .getNextStop(tripId)

                                                if (nextStop == null) {
                                                    etaError =
                                                        "El viaje no tiene paradas pendientes"
                                                }
                                            } catch (e: Exception) {
                                                nextStop = null
                                                etaError =
                                                    "No se pudo consultar el ETA: ${e.message}"
                                            } finally {
                                                isLoadingEta = false
                                            }
                                        }
                                    } else {
                                        etaError =
                                            "El bus no tiene un viaje activo"
                                    }
                                },
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(top = 8.dp)
                            ) {
                                Text(
                                    text = "${bus.codigo} · ${bus.placa}"
                                )
                            }
                        }
                    }
                }

                selectedBus?.let { bus ->
                    Card(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(top = 16.dp)
                    ) {
                        Column(
                            modifier = Modifier.padding(16.dp)
                        ) {
                            Text(text = "Bus seleccionado: ${bus.codigo}")
                            Text(text = "Placa: ${bus.placa}")
                            Text(text = "Ruta: ${bus.ruta ?: "Sin información"}")
                            Text(text = "Viaje activo: #${bus.tripId}")
                            Text(
                                text = "Velocidad: ${
                                    bus.velocidad?.let { "%.1f km/h".format(it) }
                                        ?: "Sin información"
                                }"
                            )

                            when {
                                isLoadingEta -> {
                                    Text(
                                        text = "Calculando ETA...",
                                        modifier = Modifier.padding(top = 12.dp)
                                    )
                                }

                                etaError != null -> {
                                    Text(
                                        text = etaError ?: "",
                                        modifier = Modifier.padding(top = 12.dp)
                                    )
                                }

                                nextStop != null -> {
                                    val stop = nextStop!!

                                    Text(
                                        text = "Próxima parada: ${stop.parada}",
                                        modifier = Modifier.padding(top = 12.dp)
                                    )

                                    Text(
                                        text = "ETA a próxima parada: ${
                                            stop.etaMinutos?.let { "$it min" }
                                                ?: "No disponible"
                                        }"
                                    )

                                    Text(
                                        text = "Distancia: ${
                                            stop.distanciaMetros?.let {
                                                if (it >= 1000.0) {
                                                    "%.2f km".format(it / 1000.0)
                                                } else {
                                                    "%.0f m".format(it)
                                                }
                                            } ?: "No disponible"
                                        }"
                                    )

                                    Text(
                                        text = "Velocidad GPS: ${
                                            stop.velocidadActualKmh?.let {
                                                "%.1f km/h".format(it)
                                            } ?: "No disponible"
                                        }"
                                    )

                                    Text(
                                        text = "Estado ETA: ${
                                            stop.estadoEta ?: "Sin información"
                                        }"
                                    )
                                }
                            }
                        }
                    }
                }
            }
        }
        }
    }
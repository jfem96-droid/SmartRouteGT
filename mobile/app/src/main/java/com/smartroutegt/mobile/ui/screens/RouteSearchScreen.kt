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
import com.smartroutegt.mobile.data.remote.JourneyOptionDto
import com.smartroutegt.mobile.network.DriverApiService
import com.smartroutegt.mobile.network.NextStopDto
import androidx.compose.runtime.rememberCoroutineScope
import kotlinx.coroutines.launch
import kotlinx.coroutines.delay
import com.smartroutegt.mobile.network.userFacingApiError
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll


@Composable
fun RouteSearchScreen(
    onConfirmSelection: (RouteDto, BusMonitoringDto?) -> Unit,
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

    var selectedDirection by remember { mutableStateOf<String?>(null) }
    var orderedNames by remember { mutableStateOf<List<String>>(emptyList()) }
    var availableItineraries by remember { mutableStateOf<List<JourneyOptionDto>>(emptyList()) }

    var routePath by remember {
        mutableStateOf<List<Long>?>(null)
    }

    var mapBuses by remember { mutableStateOf<List<BusMonitoringDto>>(emptyList()) }
    LaunchedEffect(selectedRoute?.id) {
        val id = selectedRoute?.id ?: return@LaunchedEffect
        while (true) {
            try { mapBuses = ApiClient.busMonitoringApiService.getBuses().filter {
                it.routeId == id && it.viajeActivo && it.tripId != null && it.latitud != null && it.longitud != null
            } } catch (_: Exception) { /* Conservar última información hasta recuperar la conexión */ }
            delay(10000)
        }
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
            routeLoadError = userFacingApiError(e)
        } finally {
            isLoadingRoutes = false
        }
    }
    LaunchedEffect(selectedRoute) {
        val route = selectedRoute ?: return@LaunchedEffect

        isLoadingStops = true
        stopLoadError = null

        try {
            val idaStops = ApiClient.routeStopApiService.getStopsByRoute(route.id)
            availableItineraries = ApiClient.routeApiService.getItineraries(route.id)
            val extraStops = availableItineraries.flatMap { it.stops }
                .filter { stop -> idaStops.none { it.stop.id == stop.id } }
                .distinctBy { it.id }
                .mapIndexed { i, stop ->
                    com.smartroutegt.mobile.data.remote.RouteStopDto(-i.toLong()-1, route, stop, i+1)
                }
            routeStops = idaStops + extraStops

            try {
                geometryPoints = ApiClient.routeApiService.getRouteGeometry(route.id).points
            } catch (_: Exception) {
                geometryPoints = emptyList() // La selección de paradas sigue disponible.
            }

        } catch (e: Exception) {
            routeStops = emptyList()
            geometryPoints = emptyList()
            stopLoadError = userFacingApiError(e)
        } finally {
            isLoadingStops = false
        }
    }

    val coroutineScope = rememberCoroutineScope()


    fun buscarRuta() {
        val route = selectedRoute
        val origin = selectedOrigin
        val destination = selectedDestination

        if (route == null || origin == null || destination == null) {
            routeSearchError = "Selecciona una ruta, un origen y un destino"
            routePath = null
            geometryPoints = emptyList()
            return
        }

        coroutineScope.launch {
            isSearchingRoute = true
            isLoadingBuses = true

            routeSearchError = null
            busLoadError = null
            etaError = null

            routePath = null
            geometryPoints = emptyList()
            routeBuses = emptyList()
            selectedBus = null
            nextStop = null

            try {
                val options = ApiClient.routeApiService.getJourneyOptions(route.id, origin.id, destination.id)
                val monitoring = try { ApiClient.busMonitoringApiService.getBuses() } catch (_: Exception) { emptyList() }
                val option = options.firstOrNull { candidate -> monitoring.any { bus ->
                    bus.routeId == route.id && bus.viajeActivo && bus.tripId != null && bus.direction == candidate.direction
                } } ?: options.firstOrNull()
                selectedDirection = option?.direction
                routePath = option?.stopIds ?: emptyList()
                orderedNames = option?.stops?.map { it.nombre } ?: emptyList()
            } catch (e: Exception) {
                routeSearchError = userFacingApiError(e)
                isSearchingRoute = false
                isLoadingBuses = false
                return@launch
            }

            if (routePath.isNullOrEmpty()) {
                routeSearchError = "No se encontró un recorrido entre las paradas seleccionadas"
                isSearchingRoute = false
                isLoadingBuses = false
                return@launch
            }

            try {
                geometryPoints = ApiClient.routeApiService
                    .getRouteGeometry(
                        routeId = route.id,
                        originStopId = origin.id,
                        destinationStopId = destination.id,
                        direction = selectedDirection
                    )
                    .points
            } catch (e: Exception) {
                routeSearchError = userFacingApiError(e)
                geometryPoints = emptyList()
                isSearchingRoute = false
                isLoadingBuses = false
                return@launch
            }

            try {
                val buses = ApiClient.busMonitoringApiService.getBuses()

                routeBuses = buses.filter { bus ->
                    bus.routeId == route.id &&
                        bus.viajeActivo &&
                        bus.tripId != null && bus.direction == selectedDirection
                }

                if (routeBuses.isEmpty()) {
                    busLoadError = "No hay buses activos actualmente en esta ruta"
                }
            } catch (e: Exception) {
                routeBuses = emptyList()
                busLoadError = userFacingApiError(e)
            } finally {
                isSearchingRoute = false
                isLoadingBuses = false
            }
        }
    }

    fun seleccionarBus(bus: BusMonitoringDto) {
        selectedBus = bus
        nextStop = null
        etaError = null

        val tripId = bus.tripId

        if (tripId == null) {
            etaError = "El bus no tiene un viaje activo"
            return
        }

        coroutineScope.launch {
            isLoadingEta = true

            try {
                nextStop = driverApiService.getNextStop(tripId)

                if (nextStop == null) {
                    etaError = "El viaje no tiene paradas pendientes"
                }
            } catch (e: Exception) {
                nextStop = null
                etaError = userFacingApiError(e)
            } finally {
                isLoadingEta = false
            }
        }
    }

    RouteSearchV3Content(
        routes = routes,
        selectedRoute = selectedRoute,
        routeStops = routeStops,
        selectedOrigin = selectedOrigin,
        selectedDestination = selectedDestination,
        routePath = routePath,
        selectedDirection = selectedDirection,
        orderedNames = orderedNames,
        geometryPoints = geometryPoints,
        routeBuses = routeBuses,
        mapBuses = mapBuses,
        selectedBus = selectedBus,
        nextStop = nextStop,
        isLoadingRoutes = isLoadingRoutes,
        isLoadingStops = isLoadingStops,
        isSearchingRoute = isSearchingRoute,
        isLoadingBuses = isLoadingBuses,
        isLoadingEta = isLoadingEta,
        routeLoadError = routeLoadError,
        stopLoadError = stopLoadError,
        routeSearchError = routeSearchError,
        busLoadError = busLoadError,
        etaError = etaError,
        onBack = onBack,

        onRouteSelected = { route ->
            selectedRoute = route
            selectedOrigin = null
            selectedDestination = null
            routePath = null
            selectedDirection = null
            orderedNames = emptyList()
            routeBuses = emptyList()
            selectedBus = null
            nextStop = null
            etaError = null
            busLoadError = null
        },

        onOriginSelected = { stop ->
            selectedOrigin = stop
            routePath = null
            routeBuses = emptyList()
            selectedBus = null
            nextStop = null
            etaError = null
        },

        onDestinationSelected = { stop ->
            selectedDestination = stop
            routePath = null
            routeBuses = emptyList()
            selectedBus = null
            nextStop = null
            etaError = null
        },

        onSearch = {
            buscarRuta()
        },

        onBusSelected = { bus ->
            seleccionarBus(bus)
        },
        onConfirmSelection = { selectedRoute?.let { onConfirmSelection(it, selectedBus) } }
    )
}

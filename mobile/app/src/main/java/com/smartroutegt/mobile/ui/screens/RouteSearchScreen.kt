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
                routePath = ApiClient.algorithmApiService.getRoutePath(
                    routeId = route.id,
                    origen = origin.id,
                    destino = destination.id
                )
            } catch (e: Exception) {
                routeSearchError = "Error calculando la ruta: ${e.message}"
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
                        destinationStopId = destination.id
                    )
                    .points
            } catch (e: Exception) {
                routeSearchError = "Error cargando la geometría de la ruta: ${e.message}"
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
                        bus.tripId != null
                }

                if (routeBuses.isEmpty()) {
                    busLoadError = "No hay buses activos actualmente en esta ruta"
                }
            } catch (e: Exception) {
                routeBuses = emptyList()
                busLoadError = "Error cargando los buses: ${e.message}"
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
                etaError = "No se pudo consultar el ETA: ${e.message}"
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
        geometryPoints = geometryPoints,
        routeBuses = routeBuses,
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

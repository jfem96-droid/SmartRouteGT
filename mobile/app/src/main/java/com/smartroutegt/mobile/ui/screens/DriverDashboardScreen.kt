package com.smartroutegt.mobile.ui.screens

import com.smartroutegt.mobile.network.userFacingApiError

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Text
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Card
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.DisposableEffect
import androidx.compose.material3.Button
import android.Manifest
import android.content.pm.PackageManager
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.ui.platform.LocalContext
import androidx.core.content.ContextCompat
import android.content.Context
import android.location.LocationManager
import android.location.Location
import android.location.LocationListener
import com.smartroutegt.mobile.network.DriverRouteAssignmentDto
import com.smartroutegt.mobile.network.TripDto
import androidx.compose.runtime.rememberCoroutineScope
import com.smartroutegt.mobile.network.ApiClient
import com.smartroutegt.mobile.network.DriverApiService
import kotlinx.coroutines.launch
import kotlinx.coroutines.delay
import retrofit2.HttpException
import java.io.IOException
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import com.smartroutegt.mobile.network.RouteStopDto
import androidx.compose.ui.viewinterop.AndroidView
import org.osmdroid.tileprovider.tilesource.TileSourceFactory
import org.osmdroid.util.GeoPoint
import org.osmdroid.views.MapView
import org.osmdroid.views.overlay.Marker
import org.osmdroid.views.overlay.Polyline
import android.graphics.Color
import org.osmdroid.config.Configuration
import com.smartroutegt.mobile.network.NextStopDto

@Composable
fun DriverDashboardScreen(
    assignment: DriverRouteAssignmentDto,
    initialTrip: TripDto?,
    onLogout: () -> Unit
) {
    var selectedV3Tab by remember {
        mutableStateOf(SmartRouteV3DriverTab.HOME)
    }
    val busAsignado = assignment.bus.codigo
    val rutaAsignada = assignment.route.nombre
    val pilotoNombre = assignment.driver.nombre
    val pilotoLicencia = assignment.driver.licencia

    var dispatchStatus by remember { mutableStateOf<com.smartroutegt.mobile.network.DispatchStatusDto?>(null) }
    var tripStage by remember { mutableStateOf<com.smartroutegt.mobile.network.DispatchTripStateDto?>(null) }

    var viajeActual by remember(initialTrip) {
        mutableStateOf(initialTrip)
    }

    var estadoViaje by remember(initialTrip) {
        mutableStateOf(
            if (initialTrip != null) {
                "En curso"
            } else {
                "No iniciado"
            }
        )
    }

    val sancionesConfirmadas = assignment.driver.sancionesConfirmadas
    var velocidadActual by remember { mutableStateOf(0f) }

    var speedWarning by remember { mutableStateOf(false) }
    var speedViolationCreated by remember { mutableStateOf(false) }
    var speedLimitKmh by remember { mutableStateOf(70.0) }
    var backendSpeedKmh by remember { mutableStateOf(0.0) }

    var stopSafetyStatus by remember {
        mutableStateOf<com.smartroutegt.mobile.network.DriverStopStatusDto?>(null)
    }

    var violations by remember {
        mutableStateOf(emptyList<com.smartroutegt.mobile.network.ViolationDto>())
    }

    var loadingViolations by remember {
        mutableStateOf(false)
    }

    var violationsError by remember {
        mutableStateOf<String?>(null)
    }

    var violationsRefreshKey by remember {
        mutableStateOf(0)
    }

    var proximaParada by remember {
        mutableStateOf<NextStopDto?>(null)
    }

    var estadoProximaParada by remember {
        mutableStateOf("No disponible")
    }
    var paradas by remember {
        mutableStateOf(emptyList<RouteStopDto>())
    }

    var cargandoParadas by remember {
        mutableStateOf(true)
    }

    var errorParadas by remember {
        mutableStateOf<String?>(null)
    }

    var geometryPoints by remember {
        mutableStateOf(
            emptyList<com.smartroutegt.mobile.data.remote.RouteGeometryPointDto>()
        )
    }

    var loadingGeometry by remember {
        mutableStateOf(true)
    }

    var geometryError by remember {
        mutableStateOf<String?>(null)
    }
    var estadoGps by remember { mutableStateOf("Permiso pendiente") }
    val alertaActual = "Sin alertas"

    val context = LocalContext.current

    val coroutineScope = rememberCoroutineScope()

    val driverApiService = remember {
        DriverApiService(ApiClient.create())
    }

    LaunchedEffect(assignment.driver.id, viajeActual?.id) {
        while (true) {
            try {
                dispatchStatus = driverApiService.getDispatchStatus(assignment.driver.id)
                tripStage = viajeActual?.id?.let { driverApiService.getDispatchTripState(it) }
            } catch (_: Exception) { /* Se volverá a consultar; backend impide cambios no autorizados. */ }
            delay(30_000)
        }
    }

    LaunchedEffect(assignment.route.id, tripStage?.direction) {
        errorParadas = null

        try {
            paradas = driverApiService.getRouteStops(assignment.route.id, tripStage?.direction)
                .sortedBy { routeStop -> routeStop.orden }
        } catch (exception: HttpException) {
            errorParadas = "No se pudieron cargar las paradas"
        } catch (exception: IOException) {
            errorParadas = "No se pudo conectar con el servidor"
        }
        cargandoParadas = false
    }

    LaunchedEffect(assignment.route.id, tripStage?.direction) {
        loadingGeometry = true
        geometryError = null

        try {
            val geometry =
                com.smartroutegt.mobile.data.remote.ApiClient
                    .routeApiService
                    .getRouteGeometry(assignment.route.id, direction = tripStage?.direction)

            geometryPoints = geometry.points
        } catch (exception: Exception) {
            geometryPoints = emptyList()
            geometryError = "No se pudo cargar la geometría de la ruta"
        } finally {
            loadingGeometry = false
        }
    }

    LaunchedEffect(viajeActual?.id) {
        val tripId = viajeActual?.id

        if (tripId == null) {
            proximaParada = null
            estadoProximaParada = "No disponible"
            return@LaunchedEffect
        }

        try {
            estadoProximaParada = "Consultando próxima parada..."
            
            val siguienteParada = driverApiService.getNextStop(tripId)

            proximaParada = siguienteParada
            estadoProximaParada = siguienteParada?.parada
                ?: "No hay paradas pendientes"
        } catch (exception: IOException) {
            proximaParada = null
            estadoProximaParada = "Sin conexión con el servidor"
        } catch (exception: IllegalStateException) {
            proximaParada = null
            estadoProximaParada = "No se pudo consultar la próxima parada"
        }
    }

    var iniciandoViaje by remember {
        mutableStateOf(false)
    }

    var finalizandoViaje by remember {
        mutableStateOf(false)
    }   

    var errorViaje by remember {
        mutableStateOf<String?>(null)
    }

    val locationManager = remember {
        context.getSystemService(Context.LOCATION_SERVICE) as LocationManager
    }

    var latitud by remember { mutableStateOf<Double?>(null) }
    var longitud by remember { mutableStateOf<Double?>(null) }

    var gpsActivo by remember(viajeActual?.id) {
        mutableStateOf(viajeActual != null)
    }

    LaunchedEffect(viajeActual?.id) {
        if (viajeActual != null && !gpsActivo) {
            gpsActivo = true
        }
    }

    DisposableEffect(gpsActivo, viajeActual?.id) {
        if (!gpsActivo) {
            return@DisposableEffect onDispose { }
        }

        val permisoPreciso = ContextCompat.checkSelfPermission(
            context,
            Manifest.permission.ACCESS_FINE_LOCATION
        )

        val permisoAproximado = ContextCompat.checkSelfPermission(
            context,
            Manifest.permission.ACCESS_COARSE_LOCATION
        )

        if (
            permisoPreciso != PackageManager.PERMISSION_GRANTED &&
            permisoAproximado != PackageManager.PERMISSION_GRANTED
        ) {
            estadoGps = "Permiso requerido"

            return@DisposableEffect onDispose { }
        }

        val proveedor = when {
            permisoPreciso == PackageManager.PERMISSION_GRANTED &&
                locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER) -> {
                LocationManager.GPS_PROVIDER
            }

            locationManager.isProviderEnabled(LocationManager.NETWORK_PROVIDER) -> {
                LocationManager.NETWORK_PROVIDER
            }

            else -> {
                estadoGps = "Ubicación desactivada"

                return@DisposableEffect onDispose { }
            }
        }

        estadoGps = "Buscando ubicación..."

        val locationListener = object : LocationListener {
            override fun onLocationChanged(location: Location) {
                android.util.Log.d(
                    "SmartRouteGPS",
                    "GPS RECIBIDO lat=${location.latitude} lon=${location.longitude}"
                )
                latitud = location.latitude
                longitud = location.longitude

                velocidadActual = if (location.hasSpeed()) {
                    location.speed * 3.6f
                } else {
                    0f
                }

                estadoGps = "Ubicación disponible"

                val tripId = viajeActual?.id

                if (tripId != null) {
                    coroutineScope.launch {
                        try {

                            android.util.Log.d(
                                "SmartRouteGPS",
                                "ENVIANDO GPS tripId=$tripId"
                            )

                            val safetyResponse = driverApiService.registerLocation(
                                tripId = tripId,
                                latitud = location.latitude,
                                longitud = location.longitude,
                                velocidad = velocidadActual.toDouble()
                            )

                            android.util.Log.d(
                                "SmartRouteGPS",
                                "GPS GUARDADO tripId=$tripId"
                            )

                            safetyResponse.speed?.let { speed ->
                                speedWarning = speed.warning
                                speedLimitKmh = speed.limitKmh
                                backendSpeedKmh = speed.currentKmh

                                if (speed.violationCreated) {
                                    speedViolationCreated = true

                                    coroutineScope.launch {
                                        delay(5000L)
                                        speedViolationCreated = false
                                    }
                                }
                            }

                            try {
                                val siguienteParada =
                                    driverApiService.getNextStop(tripId)

                                proximaParada = siguienteParada
                                estadoProximaParada = siguienteParada?.parada
                                    ?: "No hay paradas pendientes"
                            } catch (exception: IOException) {
                                estadoProximaParada =
                                    "Sin conexión con el servidor"
                            } catch (exception: IllegalStateException) {
                                estadoProximaParada =
                                    "No se pudo consultar la próxima parada"
                            }

                            stopSafetyStatus = safetyResponse.stop
                        } catch (exception: HttpException) {
                            android.util.Log.e(
                                "SmartRouteGPS",
                                "HTTP ERROR ${exception.code()}: ${exception.message()}",
                                exception
                            )
                            estadoGps = userFacingApiError(exception)
                        } catch (exception: IOException) {
                            android.util.Log.e(
                                "SmartRouteGPS",
                                "IO ERROR: ${exception.message}",
                                exception
                            )
                            estadoGps = userFacingApiError(exception)
                        } catch (exception: Exception) {
                            android.util.Log.e(
                                "SmartRouteGPS",
                                "ERROR INESPERADO: ${exception.message}",
                                exception
                            )
                            estadoGps = "Error al procesar ubicación"
                        }
                    }
                }
            }
        }

        locationManager.requestLocationUpdates(
            proveedor,
            2000L,
            0f,
            locationListener
        )

        onDispose {
            locationManager.removeUpdates(locationListener)
        }
    }

val permisoUbicacionLauncher = rememberLauncherForActivityResult(
    contract = ActivityResultContracts.RequestMultiplePermissions()
) { permisos ->
    val ubicacionPrecisaConcedida =
        permisos[Manifest.permission.ACCESS_FINE_LOCATION] == true

    val ubicacionAproximadaConcedida =
        permisos[Manifest.permission.ACCESS_COARSE_LOCATION] == true

    if (ubicacionPrecisaConcedida || ubicacionAproximadaConcedida) {
        estadoGps = "Permiso concedido"
        gpsActivo = true
    } else {
        estadoGps = "Permiso denegado"
    }
}

    val navigateHome = {
        selectedV3Tab = SmartRouteV3DriverTab.HOME
    }

    val navigateRoute = {
        selectedV3Tab = SmartRouteV3DriverTab.ROUTE
    }

    val navigateGps = {
        selectedV3Tab = SmartRouteV3DriverTab.GPS
    }

    val navigateAlerts = {
        violationsRefreshKey++
        selectedV3Tab = SmartRouteV3DriverTab.ALERTS
    }

    LaunchedEffect(
        selectedV3Tab,
        violationsRefreshKey,
        assignment.driver.id
    ) {
        if (selectedV3Tab != SmartRouteV3DriverTab.ALERTS) {
            return@LaunchedEffect
        }

        loadingViolations = true
        violationsError = null

        try {
            violations = driverApiService.getViolationsByDriver(
                assignment.driver.id
            )
        } catch (exception: HttpException) {
            violationsError =
                "No se pudieron cargar las alertas (HTTP ${exception.code()})"
        } catch (exception: IOException) {
            violationsError =
                "No se pudo conectar con el servidor"
        } catch (exception: Exception) {
            violationsError =
                "No se pudieron cargar las alertas"
        } finally {
            loadingViolations = false
        }
    }

    val navigateProfile = {
        selectedV3Tab = SmartRouteV3DriverTab.PROFILE
    }

    if (selectedV3Tab == SmartRouteV3DriverTab.GPS) {
        DriverGpsV3Content(
            tripActive = viajeActual != null,
            gpsActive = viajeActual != null && gpsActivo,
            speedKmh = velocidadActual,
            speedWarning = speedWarning,
            speedViolationCreated = speedViolationCreated,
            speedLimitKmh = speedLimitKmh,
            backendSpeedKmh = backendSpeedKmh,
            stopSafetyStatus = stopSafetyStatus,
            latitude = latitud,
            longitude = longitud,
            nextStopName = proximaParada?.parada,
            busCode = busAsignado,
            routeName = rutaAsignada,
            onHome = navigateHome,
            onRoute = navigateRoute,
            onGps = navigateGps,
            onAlerts = navigateAlerts,
            onProfile = navigateProfile,
            onReport = { selectedV3Tab = SmartRouteV3DriverTab.REPORT }
        )

        return
    }

    if (selectedV3Tab == SmartRouteV3DriverTab.ALERTS) {
        DriverAlertsV3Content(
            sanctions = sancionesConfirmadas,
            driverActive = assignment.driver.activo,
            tripActive = viajeActual != null,
            violations = violations,
            loadingViolations = loadingViolations,
            violationsError = violationsError,
            onHome = navigateHome,
            onRoute = navigateRoute,
            onGps = navigateGps,
            onAlerts = navigateAlerts,
            onProfile = navigateProfile,
            onReport = { selectedV3Tab = SmartRouteV3DriverTab.REPORT }
        )

        return
    }

    if (selectedV3Tab == SmartRouteV3DriverTab.REPORT) {
        DriverIncidentV3Screen(
            onBack = { selectedV3Tab = SmartRouteV3DriverTab.HOME },
            api = driverApiService,
            tripId = viajeActual?.id,
            latitude = latitud,
            longitude = longitud,
            onTabSelected = { selectedV3Tab = it }
        )
        return
    }
    if (selectedV3Tab == SmartRouteV3DriverTab.PROFILE) {
        DriverProfileV3Content(
            name = pilotoNombre,
            license = pilotoLicencia,
            phone = assignment.driver.telefono,
            busCode = busAsignado,
            routeName = rutaAsignada,
            driverActive = assignment.driver.activo,
            onLogout = onLogout,
            onHome = navigateHome,
            onRoute = navigateRoute,
            onGps = navigateGps,
            onAlerts = navigateAlerts,
            onProfile = navigateProfile,
            onReport = { selectedV3Tab = SmartRouteV3DriverTab.REPORT }
        )

        return
    }

    if (selectedV3Tab == SmartRouteV3DriverTab.ROUTE) {
        DriverRouteV3Content(
            routeName = rutaAsignada,
            busCode = busAsignado,
            routeStops = paradas,
            geometryPoints = geometryPoints,
            nextStop = proximaParada,
            latitude = latitud,
            longitude = longitud,
            tripActive = viajeActual != null,
            loadingGeometry = loadingGeometry,
            geometryError = geometryError,

            onHome = {
                selectedV3Tab = SmartRouteV3DriverTab.HOME
            },
            onRoute = {
                selectedV3Tab = SmartRouteV3DriverTab.ROUTE
            },
            onGps = {
                selectedV3Tab = SmartRouteV3DriverTab.GPS
            },
            onAlerts = {
                selectedV3Tab = SmartRouteV3DriverTab.ALERTS
            },
            onProfile = {
                selectedV3Tab = SmartRouteV3DriverTab.PROFILE
            },
            onReport = { selectedV3Tab = SmartRouteV3DriverTab.REPORT }
        )

        return
    }

    DriverDashboardV3Content(
        pilotoNombre = pilotoNombre,
        pilotoLicencia = pilotoLicencia,
        busCodigo = busAsignado,
        rutaNombre = rutaAsignada,
        estadoViaje = estadoViaje,
        estadoGps = if (viajeActual != null && gpsActivo) {
            "Transmitiendo"
        } else {
            "En espera"
        },
        velocidadActual = if (viajeActual != null && gpsActivo) {
            velocidadActual
        } else {
            0f
        },
        speedWarning = speedWarning,
        speedViolationCreated = speedViolationCreated,
        speedLimitKmh = speedLimitKmh,
        backendSpeedKmh = backendSpeedKmh,
        stopSafetyStatus = stopSafetyStatus,
        proximaParada = proximaParada,
        sancionesConfirmadas = sancionesConfirmadas,
        viajeActivo = viajeActual != null,
        tripId = viajeActual?.id,
        dispatchNotice = when (dispatchStatus?.tipoRecorrido) {
            "SOLO_IDA" -> "Centro de control: SOLO IDA"
            "SOLO_VUELTA" -> "Centro de control: SOLO VUELTA"
            "IDA_Y_VUELTA" -> "Centro de control: IDA Y VUELTA"
            else -> "Consultando asignación del centro de control..."
        },
        nextLegRequired = tripStage?.tipoRecorrido == "IDA_Y_VUELTA" && tripStage?.direction == "IDA",
        assignmentOutdated = dispatchStatus?.assignmentId != null && dispatchStatus?.assignmentId != assignment.id,
        iniciandoViaje = iniciandoViaje,
        finalizandoViaje = finalizandoViaje,
        errorViaje = errorViaje,

        onStartTrip = {
            coroutineScope.launch {
                iniciandoViaje = true
                errorViaje = null

                try {
                    val nuevoViaje = driverApiService.startTrip(
                        assignment.id
                    )

                    viajeActual = nuevoViaje
                    tripStage = driverApiService.getDispatchTripState(nuevoViaje.id)
                    estadoViaje = "En curso"

                    val permisoPreciso =
                        ContextCompat.checkSelfPermission(
                            context,
                            Manifest.permission.ACCESS_FINE_LOCATION
                        )

                    val permisoAproximado =
                        ContextCompat.checkSelfPermission(
                            context,
                            Manifest.permission.ACCESS_COARSE_LOCATION
                        )

                    if (
                        permisoPreciso == PackageManager.PERMISSION_GRANTED ||
                        permisoAproximado == PackageManager.PERMISSION_GRANTED
                    ) {
                        gpsActivo = true
                    } else {
                        permisoUbicacionLauncher.launch(
                            arrayOf(
                                Manifest.permission.ACCESS_FINE_LOCATION,
                                Manifest.permission.ACCESS_COARSE_LOCATION
                            )
                        )
                    }
                } catch (exception: HttpException) {
                    errorViaje = "No se pudo iniciar el viaje"
                } catch (exception: IOException) {
                    errorViaje = "No se pudo conectar con el servidor"
                } finally {
                    iniciandoViaje = false
                }
            }
        },

        onFinishTrip = {
            val tripId = viajeActual?.id

            if (tripId != null) {
                coroutineScope.launch {
                    finalizandoViaje = true
                    errorViaje = null

                    try {
                        if (tripStage?.tipoRecorrido == "IDA_Y_VUELTA" && tripStage?.direction == "IDA") {
                            val vuelta = driverApiService.nextLeg(tripId)
                            viajeActual = vuelta
                            tripStage = driverApiService.getDispatchTripState(vuelta.id)
                            estadoViaje = "Vuelta autorizada en curso"
                            proximaParada = null
                        } else {
                            driverApiService.finishTrip(tripId)
                            viajeActual = null
                            tripStage = null
                            estadoViaje = "Finalizado"
                            gpsActivo = false
                        }

                        speedWarning = false
                        speedViolationCreated = false
                        backendSpeedKmh = 0.0
                        stopSafetyStatus = null
                        velocidadActual = 0f
                    } catch (exception: HttpException) {
                        errorViaje = "No se pudo finalizar el viaje"
                    } catch (exception: IOException) {
                        errorViaje =
                            "No se pudo conectar con el servidor"
                    } finally {
                        finalizandoViaje = false
                    }
                }
            }
        },

        onHome = {
            selectedV3Tab = SmartRouteV3DriverTab.HOME
        },
        onRoute = {
            selectedV3Tab = SmartRouteV3DriverTab.ROUTE
        },
        onGps = {
            selectedV3Tab = SmartRouteV3DriverTab.GPS
        },
        onAlerts = {
            selectedV3Tab = SmartRouteV3DriverTab.ALERTS
        },
        onProfile = {
            selectedV3Tab = SmartRouteV3DriverTab.PROFILE
        },
        onReport = { selectedV3Tab = SmartRouteV3DriverTab.REPORT }
    )
}

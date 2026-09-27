package com.smartroutegt.mobile.ui.screens

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
import org.osmdroid.config.Configuration
import com.smartroutegt.mobile.network.NextStopDto

@Composable
fun DriverDashboardScreen(
    assignment: DriverRouteAssignmentDto,
    initialTrip: TripDto?
) {
    val busAsignado = assignment.bus.codigo
    val rutaAsignada = assignment.route.nombre
    val pilotoNombre = assignment.driver.nombre
    val pilotoLicencia = assignment.driver.licencia

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
    var estadoGps by remember { mutableStateOf("Permiso pendiente") }
    val alertaActual = "Sin alertas"

    val context = LocalContext.current

    val coroutineScope = rememberCoroutineScope()

    val driverApiService = remember {
        DriverApiService(ApiClient.create())
    }

    LaunchedEffect(assignment.route.id) {
        errorParadas = null

        try {
            paradas = driverApiService.getRouteStops(assignment.route.id)
                .sortedBy { routeStop -> routeStop.orden }
        } catch (exception: HttpException) {
            errorParadas = "No se pudieron cargar las paradas"
        } catch (exception: IOException) {
            errorParadas = "No se pudo conectar con el servidor"
        }
        cargandoParadas = false
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

    var gpsActivo by remember {
        mutableStateOf(false)
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
                            driverApiService.registerLocation(
                                tripId = tripId,
                                latitud = location.latitude,
                                longitud = location.longitude,
                                velocidad = velocidadActual.toDouble()
                            )

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
                        } catch (exception: HttpException) {
                            estadoGps = "Error al registrar ubicación"
                        } catch (exception: IOException) {
                            estadoGps = "Sin conexión con el servidor"
                        }
                    }
                }
            }
        }

        locationManager.requestLocationUpdates(
            proveedor,
            2000L,
            1f,
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

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(24.dp)
    ) {
        Text(
            text = "Panel del piloto",
            style = MaterialTheme.typography.headlineMedium
        )

        Spacer(modifier = Modifier.height(24.dp))

        Text(text = "Piloto: $pilotoNombre")

        Spacer(modifier = Modifier.height(8.dp))

        Text(text = "Licencia: $pilotoLicencia")

        Spacer(modifier = Modifier.height(16.dp))

        Card(
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(
                modifier = Modifier.padding(16.dp)
        ) {
            Text(
                text = "Operación asignada",
                style = MaterialTheme.typography.titleMedium
            )

            Spacer(modifier = Modifier.height(12.dp))

            Text(text = "Bus: $busAsignado")

            Spacer(modifier = Modifier.height(8.dp))

            Text(text = "Ruta: $rutaAsignada")

            Spacer(modifier = Modifier.height(8.dp))

            Text(
                text = "Estado del viaje: $estadoViaje",
                style = MaterialTheme.typography.bodyLarge
            )
        }
    }

    Spacer(modifier = Modifier.height(16.dp))

        Text(text = "Sanciones confirmadas: $sancionesConfirmadas")

        Spacer(modifier = Modifier.height(12.dp))

        Card(
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(
                modifier = Modifier.padding(16.dp)
            ) {
                Text(
                    text = "Ruta y paradas",
                    style = MaterialTheme.typography.titleMedium
                )

                Spacer(modifier = Modifier.height(12.dp))

                Text(
                    text = "Próxima parada",
                    style = MaterialTheme.typography.labelLarge
                ) 

                Text(
                    text = estadoProximaParada,
                    style = MaterialTheme.typography.bodyLarge
                )

                Spacer(modifier = Modifier.height(16.dp))

                Text(
                    text = "Paradas de la ruta",
                    style = MaterialTheme.typography.labelLarge
                )

                Spacer(modifier = Modifier.height(8.dp))

                when {
                    cargandoParadas -> {
                        Text(text = "Cargando paradas...")
                    }

                    errorParadas != null -> {
                        Text(text = errorParadas!!)
                    }

                    paradas.isEmpty() -> {
                        Text(text = "No hay paradas disponibles")
                    }

                    else -> {
                        paradas.forEach { routeStop ->
                            Text(text = "• ${routeStop.stop.nombre}")
                        }
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(12.dp))

        Card(
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(
                modifier = Modifier.padding(16.dp)
            ) {
                Text(
                    text = "Mapa de la ruta",
                    style = MaterialTheme.typography.titleMedium
                )

                Spacer(modifier = Modifier.height(12.dp))

                AndroidView(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(300.dp),
                        factory = { mapContext ->
                        Configuration.getInstance().userAgentValue =
                            mapContext.packageName

                        MapView(mapContext).apply {
                            setTileSource(TileSourceFactory.MAPNIK)
                            setMultiTouchControls(true)
                            controller.setZoom(14.0)
                        }
                    },
                    update = { mapView ->
                        mapView.overlays.removeAll { overlay ->
                            overlay is Marker
                        }

                        paradas.forEach { routeStop ->
                            val marcadorParada = Marker(mapView).apply {
                                position = GeoPoint(
                                    routeStop.stop.latitud,
                                    routeStop.stop.longitud
                                )
                                title = routeStop.stop.nombre
                                snippet = "Parada ${routeStop.orden}"
                                setAnchor(
                                    Marker.ANCHOR_CENTER,
                                    Marker.ANCHOR_BOTTOM
                                )
                            }

                            mapView.overlays.add(marcadorParada)
                        }

                        val latitudActual = latitud
                        val longitudActual = longitud

                        if (latitudActual != null && longitudActual != null) {
                            val marcadorBus = Marker(mapView).apply {
                                position = GeoPoint(
                                    latitudActual,
                                    longitudActual
                                )
                                title = "Ubicación actual del bus"
                                snippet = "Piloto: $pilotoNombre"
                                setAnchor(
                                    Marker.ANCHOR_CENTER,
                                    Marker.ANCHOR_BOTTOM
                                )
                            }

                            mapView.overlays.add(marcadorBus)
                        }

                        proximaParada?.let { siguienteParada ->
                            val marcadorProximaParada = Marker(mapView).apply {
                                position = GeoPoint(
                                    siguienteParada.latitud,
                                    siguienteParada.longitud
                                )
                                title = "Próxima parada: ${siguienteParada.parada}"
                                snippet = "Parada ${siguienteParada.orden}"
                                setAnchor(
                                    Marker.ANCHOR_CENTER,
                                    Marker.ANCHOR_BOTTOM
                                )
                            }

                            mapView.overlays.add(marcadorProximaParada)
                        }

                        paradas.firstOrNull()?.let { primeraParada ->
                            mapView.controller.setCenter(
                                GeoPoint(
                                    primeraParada.stop.latitud,
                                    primeraParada.stop.longitud
                                )
                            )
                        }

                        mapView.invalidate()
                    }
                )
            }
        }

        Spacer(modifier = Modifier.height(12.dp))

        Card(
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(
                modifier = Modifier.padding(16.dp)
            ) {
                Text(
                    text = "GPS y ubicación",
                    style = MaterialTheme.typography.titleMedium
                )

                Spacer(modifier = Modifier.height(12.dp))

                Text(
                    text = "Velocidad actual",
                    style = MaterialTheme.typography.labelLarge
                )

                Text(
                    text = "%.1f km/h".format(velocidadActual),
                    style = MaterialTheme.typography.headlineSmall
                )

                Spacer(modifier = Modifier.height(12.dp))

                Text(text = "Estado GPS: $estadoGps")

                Spacer(modifier = Modifier.height(8.dp))

            Text(
    text = if (latitud != null && longitud != null) {
        val paradaCercana = paradas.minByOrNull { routeStop ->
            val resultado = FloatArray(1)

            android.location.Location.distanceBetween(
                latitud!!,
                longitud!!,
                routeStop.stop.latitud,
                routeStop.stop.longitud,
                resultado
            )

            resultado[0]
        }

        if (paradaCercana != null) {
            val resultado = FloatArray(1)

            android.location.Location.distanceBetween(
                latitud!!,
                longitud!!,
                paradaCercana.stop.latitud,
                paradaCercana.stop.longitud,
                resultado
            )

            if (resultado[0] <= 300) {
                "Ubicación: Cerca de ${paradaCercana.stop.nombre}"
            } else {
                "Ubicación: En ruta"
            }
        } else {
            "Ubicación: En ruta"
        }
    } else {
        "Ubicación: no disponible"
    }
)

                Spacer(modifier = Modifier.height(16.dp))

                Button(
                    onClick = {
                        val permisoPreciso = ContextCompat.checkSelfPermission(
                            context,
                            Manifest.permission.ACCESS_FINE_LOCATION
                        )

                        val permisoAproximado = ContextCompat.checkSelfPermission(
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
                    }
                ) {
                    Text(
                        text = if (gpsActivo) {
                            "GPS activo"
                        } else {
                            "Activar GPS"
                        }
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(12.dp))

        Text(text = "Alertas: $alertaActual")

        Spacer(modifier = Modifier.height(24.dp))

        Card(
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(
                modifier = Modifier.padding(16.dp)
            ) {
                Text(
                    text = "Control del viaje",
                    style = MaterialTheme.typography.titleMedium
                )

                Spacer(modifier = Modifier.height(12.dp))

                Text(text = "Estado: $estadoViaje")

                Spacer(modifier = Modifier.height(16.dp))

                Button(
                    onClick = {
                        coroutineScope.launch {
                            iniciandoViaje = true
                            errorViaje = null

                            try {
                                val nuevoViaje = driverApiService.startTrip(
                                    assignment.id
                                )

                                viajeActual = nuevoViaje
                                estadoViaje = "En curso"
                            } catch (exception: HttpException) {
                                errorViaje = "No se pudo iniciar el viaje"
                            } catch (exception: IOException) {
                                errorViaje = "No se pudo conectar con el servidor"
                            } finally {
                                iniciandoViaje = false
                            }
                        }
                    },
                    enabled = viajeActual == null && !iniciandoViaje,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text(
                        text = if (iniciandoViaje) {
                            "Iniciando..."
                        } else {
                            "Iniciar viaje"
                        }
                    )
                }

                if (errorViaje != null) {
                    Text(
                        text = errorViaje!!,
                        modifier = Modifier.padding(top = 8.dp)
                    )
                }

                Spacer(modifier = Modifier.height(12.dp))

                Button(
                    onClick = {
                        val tripId = viajeActual?.id

                        if (tripId != null) {
                            coroutineScope.launch {
                                finalizandoViaje = true
                                errorViaje = null

                                try {
                                    driverApiService.finishTrip(tripId)

                                    viajeActual = null
                                    estadoViaje = "Finalizado"
                                    gpsActivo = false
                                } catch (exception: HttpException) {
                                    errorViaje = "No se pudo finalizar el viaje"
                                } catch (exception: IOException) {
                                    errorViaje = "No se pudo conectar con el servidor"
                                } finally {
                                    finalizandoViaje = false
                                }
                            }
                        }
                    },
                    enabled = viajeActual != null && !finalizandoViaje,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text(
                        text = if (finalizandoViaje) {
                            "Finalizando..."
                        } else {
                            "Finalizar viaje"
                        }
                    )
                }
            }
        }
    }
}
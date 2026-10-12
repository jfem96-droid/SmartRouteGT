package com.smartroutegt.mobile.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.viewinterop.AndroidView
import com.smartroutegt.mobile.data.remote.RouteGeometryPointDto
import com.smartroutegt.mobile.network.NextStopDto
import com.smartroutegt.mobile.network.RouteStopDto
import org.osmdroid.config.Configuration
import org.osmdroid.tileprovider.tilesource.TileSourceFactory
import org.osmdroid.util.GeoPoint
import org.osmdroid.views.MapView
import org.osmdroid.views.overlay.Marker
import org.osmdroid.views.overlay.Polyline

@Composable
fun DriverRouteV3Content(
    routeName: String,
    busCode: String,
    routeStops: List<RouteStopDto>,
    geometryPoints: List<RouteGeometryPointDto>,
    nextStop: NextStopDto?,
    latitude: Double?,
    longitude: Double?,
    tripActive: Boolean,
    loadingGeometry: Boolean,
    geometryError: String?,
    onHome: () -> Unit,
    onRoute: () -> Unit,
    onGps: () -> Unit,
    onAlerts: () -> Unit,
    onProfile: () -> Unit,
    onReport: () -> Unit
) {
    Scaffold(
        containerColor = SmartRouteBackground,
        topBar = {
            SmartRouteV3TopBar(
                title = "Mi ruta",
                subtitle = "$busCode · $routeName",
                role = SmartRouteV3Role.DRIVER
            )
        },
        bottomBar = {
            SmartRouteV3DriverBottomBar(
                selected = SmartRouteV3DriverTab.ROUTE,
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
            SmartRouteV3Card {
                Text(
                    text = "Recorrido asignado",
                    color = SmartRouteMuted,
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Bold
                )

                Spacer(modifier = Modifier.height(4.dp))

                Text(
                    text = routeName,
                    color = SmartRouteText,
                    fontSize = 21.sp,
                    fontWeight = FontWeight.ExtraBold
                )

                Spacer(modifier = Modifier.height(4.dp))

                Text(
                    text = "${routeStops.size} paradas · Bus $busCode",
                    color = SmartRouteMuted,
                    fontSize = 12.sp
                )
            }

            Spacer(modifier = Modifier.height(14.dp))

            SmartRouteV3Card {
                Text(
                    text = "Mapa de la ruta",
                    color = SmartRouteText,
                    fontWeight = FontWeight.Bold,
                    fontSize = 17.sp
                )

                Spacer(modifier = Modifier.height(12.dp))

                if (loadingGeometry) {
                    SmartRouteV3EmptyState(
                        title = "Cargando recorrido",
                        description = "Obteniendo la geometría de la ruta..."
                    )
                } else if (geometryError != null) {
                    SmartRouteV3EmptyState(
                        title = "No se pudo cargar el recorrido",
                        description = geometryError,
                        error = true
                    )
                } else {
                    AndroidView(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(390.dp),
                        factory = { context ->
                            Configuration.getInstance().userAgentValue =
                                context.packageName

                            MapView(context).apply {
                                setTileSource(TileSourceFactory.MAPNIK)
                                setMultiTouchControls(true)
                                controller.setZoom(14.0)
                            }
                        },
                        update = { mapView ->
                            mapView.overlays.clear()

                            if (geometryPoints.size >= 2) {
                                val routeLine = Polyline().apply {
                                    setPoints(
                                        geometryPoints.map { point ->
                                            GeoPoint(
                                                point.latitud,
                                                point.longitud
                                            )
                                        }
                                    )

                                    outlinePaint.color =
                                        android.graphics.Color.parseColor(
                                            "#2563EB"
                                        )

                                    outlinePaint.strokeWidth = 10f
                                }

                                mapView.overlays.add(routeLine)
                            }

                            routeStops
                                .sortedBy { it.orden }
                                .forEach { routeStop ->

                                    val marker = Marker(mapView).apply {
                                        position = GeoPoint(
                                            routeStop.stop.latitud,
                                            routeStop.stop.longitud
                                        )

                                        title =
                                            "${routeStop.orden}. ${routeStop.stop.nombre}"

                                        snippet = "Parada de la ruta"

                                        setAnchor(
                                            Marker.ANCHOR_CENTER,
                                            Marker.ANCHOR_BOTTOM
                                        )
                                    }

                                    mapView.overlays.add(marker)
                                }

                            nextStop?.let { stop ->
                                val marker = Marker(mapView).apply {
                                    position = GeoPoint(
                                        stop.latitud,
                                        stop.longitud
                                    )

                                    title = "Próxima parada"
                                    snippet = stop.parada

                                    setAnchor(
                                        Marker.ANCHOR_CENTER,
                                        Marker.ANCHOR_BOTTOM
                                    )
                                }

                                mapView.overlays.add(marker)
                            }

                            if (latitude != null && longitude != null) {
                                val busMarker = Marker(mapView).apply {
                                    position = GeoPoint(
                                        latitude,
                                        longitude
                                    )

                                    title = busCode

                                    snippet = if (tripActive) {
                                        "Bus en recorrido"
                                    } else {
                                        "Última ubicación conocida"
                                    }

                                    setAnchor(
                                        Marker.ANCHOR_CENTER,
                                        Marker.ANCHOR_BOTTOM
                                    )
                                }

                                mapView.overlays.add(busMarker)
                            }

                            val center = when {
                                latitude != null && longitude != null ->
                                    GeoPoint(latitude, longitude)

                                geometryPoints.isNotEmpty() ->
                                    GeoPoint(
                                        geometryPoints.first().latitud,
                                        geometryPoints.first().longitud
                                    )

                                routeStops.isNotEmpty() ->
                                    GeoPoint(
                                        routeStops.first().stop.latitud,
                                        routeStops.first().stop.longitud
                                    )

                                else -> null
                            }

                            if (center != null) {
                                mapView.controller.setCenter(center)
                            }

                            mapView.invalidate()
                        }
                    )
                }
            }

            Spacer(modifier = Modifier.height(14.dp))

            if (tripActive) {
                SmartRouteV3Card {
                    Text(
                        text = "Siguiente parada",
                        color = SmartRouteMuted,
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold
                    )

                    Spacer(modifier = Modifier.height(5.dp))

                    Text(
                        text = nextStop?.parada
                            ?: "Consultando próxima parada...",
                        color = SmartRouteText,
                        fontSize = 20.sp,
                        fontWeight = FontWeight.Bold
                    )

                    if (nextStop?.distanciaMetros != null) {
                        Spacer(modifier = Modifier.height(6.dp))

                        Text(
                            text = if (
                                nextStop.distanciaMetros >= 1000.0
                            ) {
                                "%.2f km".format(
                                    nextStop.distanciaMetros / 1000.0
                                )
                            } else {
                                "%.0f m".format(
                                    nextStop.distanciaMetros
                                )
                            },
                            color = SmartRouteBlue,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }
            } else {
                SmartRouteV3EmptyState(
                    title = "Viaje no iniciado",
                    description =
                        "Puedes revisar el recorrido. La próxima parada y el seguimiento se activarán al iniciar el viaje."
                )
            }

            Spacer(modifier = Modifier.height(14.dp))

            SmartRouteV3Card {
                Text(
                    text = "Paradas",
                    color = SmartRouteText,
                    fontWeight = FontWeight.Bold,
                    fontSize = 17.sp
                )

                Spacer(modifier = Modifier.height(10.dp))

                if (routeStops.isEmpty()) {
                    Text(
                        text = "No hay paradas disponibles.",
                        color = SmartRouteMuted,
                        fontSize = 12.sp
                    )
                } else {
                    routeStops
                        .sortedBy { it.orden }
                        .forEach { routeStop ->

                            val isNext =
                                nextStop?.stopId == routeStop.stop.id

                            Text(
                                text =
                                    "${routeStop.orden}. ${routeStop.stop.nombre}" +
                                        if (isNext) "  ·  SIGUIENTE" else "",
                                color = if (isNext) {
                                    SmartRouteBlue
                                } else {
                                    SmartRouteText
                                },
                                fontSize = 13.sp,
                                fontWeight = if (isNext) {
                                    FontWeight.Bold
                                } else {
                                    FontWeight.Medium
                                },
                                modifier = Modifier.padding(
                                    vertical = 7.dp
                                )
                            )
                        }
                }
            }

            Spacer(modifier = Modifier.height(16.dp))
        }
    }
}

package com.smartroutegt.mobile.ui.screens

import android.graphics.Color
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidView
import com.smartroutegt.mobile.data.remote.RouteGeometryPointDto
import com.smartroutegt.mobile.data.remote.RouteStopDto
import com.smartroutegt.mobile.data.remote.BusMonitoringDto
import org.maplibre.android.MapLibre
import org.maplibre.android.annotations.MarkerOptions
import org.maplibre.android.annotations.PolylineOptions
import org.maplibre.android.camera.CameraPosition
import org.maplibre.android.geometry.LatLng
import org.maplibre.android.maps.MapView

private const val MAP_STYLE_URL =
    "https://tiles.openfreemap.org/styles/liberty"

private const val PASSENGER_ROUTE_COLOR = "#2563EB"

@Composable
fun PassengerMap(
    routeStops: List<RouteStopDto>,
    routePath: List<Long>?,
    geometryPoints: List<RouteGeometryPointDto> = emptyList(),
    activeBuses: List<BusMonitoringDto> = emptyList(),
    modifier: Modifier = Modifier
) {
    AndroidView(
        modifier = modifier
            .fillMaxWidth()
            .height(300.dp),

        factory = { context ->
            MapLibre.getInstance(context)

            MapView(context).apply {
                onCreate(null)
                onStart()
                onResume()

                getMapAsync { map ->
                    map.setStyle(MAP_STYLE_URL)
                }
            }
        },

        update = { mapView ->
            mapView.getMapAsync { map ->

                map.markers.toList().forEach {
                    map.removeMarker(it)
                }

                map.polylines.toList().forEach {
                    map.removePolyline(it)
                }

                val ordered = routeStops.sortedBy { it.orden }

                val visibleStopIds =
                    routePath
                        ?.takeIf { it.isNotEmpty() }
                        ?.toSet()

                ordered
                    .filter { routeStop ->
                        visibleStopIds == null ||
                            routeStop.stop.id in visibleStopIds
                    }
                    .forEach { routeStop ->

                        val stop = routeStop.stop

                        map.addMarker(
                            MarkerOptions()
                                .position(
                                    LatLng(
                                        stop.latitud,
                                        stop.longitud
                                    )
                                )
                                .title(
                                    "${routeStop.orden}. ${stop.nombre}"
                                )
                                .snippet("Parada SmartRoute")
                        )
                    }

                // Marcadores GPS reales: solo buses con viaje activo y coordenadas válidas.
                activeBuses.filter { it.viajeActivo && it.tripId != null && it.latitud != null && it.longitud != null }.forEach { bus ->
                    val lat = bus.latitud!!
                    val lon = bus.longitud!!
                    if (lat in -90.0..90.0 && lon in -180.0..180.0) {
                        map.addMarker(MarkerOptions()
                            .position(LatLng(lat, lon))
                            .title("Bus activo · ${bus.codigo}")
                            .snippet("${bus.placa} · ${bus.direction ?: "Recorrido en curso"}"))
                    }
                }

                val roadPoints =
                    geometryPoints.map { point ->
                        LatLng(
                            point.latitud,
                            point.longitud
                        )
                    }

                if (roadPoints.size >= 2) {
                    map.addPolyline(
                        PolylineOptions()
                            .addAll(roadPoints)
                            .color(
                                Color.parseColor(
                                    PASSENGER_ROUTE_COLOR
                                )
                            )
                            .width(7.0f)
                    )
                }

                val cameraPoint =
                    roadPoints.firstOrNull()
                        ?: ordered.firstOrNull()
                            ?.stop
                            ?.let {
                                LatLng(
                                    it.latitud,
                                    it.longitud
                                )
                            }

                cameraPoint?.let {
                    map.cameraPosition =
                        CameraPosition.Builder()
                            .target(it)
                            .zoom(13.5)
                            .tilt(0.0)
                            .bearing(0.0)
                            .build()
                }
            }
        }
    )
}

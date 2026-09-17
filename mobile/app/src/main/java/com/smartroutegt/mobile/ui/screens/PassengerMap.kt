package com.smartroutegt.mobile.ui.screens

import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidView
import com.smartroutegt.mobile.data.remote.RouteStopDto
import org.maplibre.android.MapLibre
import org.maplibre.android.annotations.MarkerOptions
import org.maplibre.android.camera.CameraPosition
import org.maplibre.android.geometry.LatLng
import org.maplibre.android.maps.MapView

private const val MAP_STYLE_URL = "https://tiles.openfreemap.org/styles/liberty"

@Composable
fun PassengerMap(
    routeStops: List<RouteStopDto>,
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
                    map.setStyle(MAP_STYLE_URL) {

                        routeStops.forEach { routeStop ->
                            val stop = routeStop.stop

                            map.addMarker(
                                MarkerOptions()
                                    .position(
                                        LatLng(
                                            stop.latitud,
                                            stop.longitud
                                        )
                                    )
                                    .title(stop.nombre)
                                    .snippet("Parada ${routeStop.orden}")
                            )
                        }

                        val firstStop = routeStops.firstOrNull()?.stop

                        if (firstStop != null) {
                            map.cameraPosition = CameraPosition.Builder()
                                .target(
                                    LatLng(
                                        firstStop.latitud,
                                        firstStop.longitud
                                    )
                                )
                                .zoom(13.0)
                                .tilt(0.0)
                                .bearing(0.0)
                                .build()
                        }
                    }
                }
            }
        }
    )
}
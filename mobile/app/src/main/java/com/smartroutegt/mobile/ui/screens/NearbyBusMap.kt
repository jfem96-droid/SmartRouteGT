package com.smartroutegt.mobile.ui.screens

import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidView
import com.smartroutegt.mobile.data.remote.BusLocationDto
import com.smartroutegt.mobile.data.remote.BusMonitoringDto
import org.maplibre.android.MapLibre
import org.maplibre.android.annotations.MarkerOptions
import org.maplibre.android.camera.CameraPosition
import org.maplibre.android.geometry.LatLng
import org.maplibre.android.maps.MapView

private const val NEARBY_BUS_MAP_STYLE_URL =
    "https://tiles.openfreemap.org/styles/liberty"

@Composable
fun NearbyBusMap(
    passengerLatitude: Double,
    passengerLongitude: Double,
    nearestBus: BusLocationDto?,
    busesWithLocation: List<BusMonitoringDto>,
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
                    map.setStyle(NEARBY_BUS_MAP_STYLE_URL) {

                        map.cameraPosition = CameraPosition.Builder()
                            .target(
                                LatLng(
                                    passengerLatitude,
                                    passengerLongitude
                                )
                            )
                            .zoom(13.0)
                            .tilt(0.0)
                            .bearing(0.0)
                            .build()
                    }
                }
            }
        },

        update = { mapView ->
            mapView.getMapAsync { map ->

                map.markers.toList().forEach { marker ->
                    map.removeMarker(marker)
                }

                map.addMarker(
                    MarkerOptions()
                        .position(
                            LatLng(
                                passengerLatitude,
                                passengerLongitude
                            )
                        )
                        .title("Mi ubicación")
                        .snippet("Pasajero")
                )

                busesWithLocation.forEach { bus ->
                    val latitude = bus.latitud
                    val longitude = bus.longitud

                    if (latitude != null && longitude != null) {
                        val isNearestBus = nearestBus?.bus?.id == bus.busId

                        map.addMarker(
                            MarkerOptions()
                                .position(
                                    LatLng(
                                        latitude,
                                        longitude
                                    )
                                )
                                .title("Bus ${bus.codigo}")
                                .snippet(
                                    if (isNearestBus) {
                                        "Bus cercano"
                                    } else {
                                        "Bus con ubicación"
                                    }
                                )
                        )
                    }
                }

                nearestBus?.let { busLocation ->
                    val alreadyShown = busesWithLocation.any { bus ->
                        bus.busId == busLocation.bus.id
                    }

                    if (!alreadyShown) {
                        map.addMarker(
                            MarkerOptions()
                                .position(
                                    LatLng(
                                        busLocation.latitud,
                                        busLocation.longitud
                                    )
                                )
                                .title("Bus ${busLocation.bus.codigo}")
                                .snippet("Bus cercano")
                        )
                    }
                }

                map.cameraPosition = CameraPosition.Builder()
                    .target(
                        LatLng(
                            passengerLatitude,
                            passengerLongitude
                        )
                    )
                    .zoom(13.0)
                    .tilt(0.0)
                    .bearing(0.0)
                    .build()
            }
        }
    )
}
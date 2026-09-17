package com.smartroutegt.mobile.ui.screens

import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidView
import com.smartroutegt.mobile.data.remote.BusLocationDto
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

                nearestBus?.let { busLocation ->
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
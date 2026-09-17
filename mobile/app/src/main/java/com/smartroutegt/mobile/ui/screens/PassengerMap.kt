package com.smartroutegt.mobile.ui.screens

import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidView
import org.maplibre.android.camera.CameraPosition
import org.maplibre.android.geometry.LatLng
import org.maplibre.android.maps.MapView
import org.maplibre.android.MapLibre

private const val MAP_STYLE_URL = "https://tiles.openfreemap.org/styles/liberty"
@Composable
fun PassengerMap(
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
                        map.cameraPosition = CameraPosition.Builder()
                            .target(LatLng(14.6349, -90.5069))
                            .zoom(13.0)
                            .tilt(0.0)
                            .bearing(0.0)
                            .build()
                    }
                }
            }
        }
    )
}
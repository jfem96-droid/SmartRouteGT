package com.smartroutegt.mobile.ui.screens

import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.viewinterop.AndroidView
import com.smartroutegt.mobile.data.remote.ApiClient
import kotlinx.coroutines.delay
import org.maplibre.android.MapLibre
import org.maplibre.android.camera.CameraPosition
import org.maplibre.android.geometry.LatLng
import org.maplibre.android.maps.MapView
import org.maplibre.android.annotations.MarkerOptions
import org.maplibre.android.annotations.PolylineOptions

/** Mapa de telemetría real: sin ruta, todos los viajes activos; con ruta, solo esa ruta. */
@Composable
fun PassengerLiveMap(modifier: Modifier = Modifier, selectedRouteId: Long? = null) {
    val context = LocalContext.current
    val mapView = remember(context) {
        MapLibre.getInstance(context)
        MapView(context).apply { onCreate(null) }
    }
    var buses by remember { mutableStateOf(emptyList<com.smartroutegt.mobile.data.remote.BusMonitoringDto>()) }
    var path by remember { mutableStateOf(emptyList<com.smartroutegt.mobile.data.remote.RouteGeometryPointDto>()) }
    LaunchedEffect(selectedRouteId) {
        path = emptyList()
        if (selectedRouteId != null) {
            try { path = ApiClient.routeApiService.getRouteGeometry(selectedRouteId).points }
            catch (_: Exception) { }
        }
    }
    LaunchedEffect(selectedRouteId) {
        while (true) {
            try {
                buses = ApiClient.busMonitoringApiService.getBuses().filter {
                    it.viajeActivo && it.tripId != null && it.latitud != null && it.longitud != null &&
                        (selectedRouteId == null || it.routeId == selectedRouteId)
                }
            } catch (_: Exception) { buses = emptyList() }
            delay(10000)
        }
    }
    LaunchedEffect(buses, path) {
        mapView.getMapAsync { map ->
            map.setStyle("https://tiles.openfreemap.org/styles/liberty") { _ ->
                map.clear()
                if (path.size > 1) {
                    map.addPolyline(PolylineOptions().addAll(path.map { LatLng(it.latitud, it.longitud) }).width(5f).color(android.graphics.Color.BLUE))
                }
                buses.forEach { bus ->
                    map.addMarker(MarkerOptions().position(LatLng(bus.latitud!!, bus.longitud!!))
                        .title("${bus.codigo} · ${bus.ruta ?: "Ruta sin nombre"}")
                        .snippet("${bus.placa} · Viaje activo"))
                }
                val focus = buses.firstOrNull()?.let { LatLng(it.latitud!!, it.longitud!!) }
                    ?: path.firstOrNull()?.let { LatLng(it.latitud, it.longitud) }
                    ?: LatLng(14.6349, -90.5069)
                map.cameraPosition = CameraPosition.Builder().target(focus).zoom(if (selectedRouteId == null) 11.5 else 13.0).build()
            }
        }
    }
    DisposableEffect(mapView) {
        mapView.onStart(); mapView.onResume()
        onDispose { mapView.onPause(); mapView.onStop(); mapView.onDestroy() }
    }
    AndroidView(factory = { mapView }, modifier = modifier)
}

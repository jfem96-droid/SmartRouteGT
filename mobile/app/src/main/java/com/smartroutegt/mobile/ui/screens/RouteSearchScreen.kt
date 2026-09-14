package com.smartroutegt.mobile.ui.screens

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.OutlinedTextField
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


@Composable
fun RouteSearchScreen(
    onBack: () -> Unit
) {

    var origin by remember {
        mutableStateOf("")
    }
    var routes by remember {
        mutableStateOf<List<RouteDto>>(emptyList())
    }
    var selectedRoute by remember {
        mutableStateOf<RouteDto?>(null)
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
        } catch (e: Exception) {
            routeStops = emptyList()
            stopLoadError = "No se pudieron cargar las paradas de la ruta"
        } finally {
            isLoadingStops = false
        }
    }

    var destination by remember {
        mutableStateOf("")
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(24.dp)
    ) {

        OutlinedButton(
            onClick = onBack
        ) {
            Text(text = "← Volver al menú")
        }

        Text(
            text = "Buscar ruta",
            modifier = Modifier.padding(top = 16.dp)
        )

        when {
            isLoadingRoutes -> {
                Text(
                    text = "Cargando rutas...",
                    modifier = Modifier.padding(top = 24.dp)
                )
            }

            routeLoadError != null -> {
                Text(
                    text = routeLoadError ?: "",
                    modifier = Modifier.padding(top = 24.dp)
                )
            }

            else -> {
                Text(
                    text = "Rutas disponibles:",
                    modifier = Modifier.padding(top = 24.dp)
                )

                routes.forEach { route ->
                    Button(
                        onClick = {
                            selectedRoute = route
                        },
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(top = 8.dp)
                    ) {
                        Text(
                            text = "${route.codigo} - ${route.nombre}"
                        )
                    }
                }
                selectedRoute?.let { route ->
                    Text(
                        text = "Ruta seleccionada: ${route.codigo} - ${route.nombre}",
                        modifier = Modifier.padding(top = 16.dp)
                    )
                }
                when {
                    selectedRoute == null -> {
                        Text(
                            text = "Selecciona una ruta para ver sus paradas",
                            modifier = Modifier.padding(top = 16.dp)
                        )
                    }

                    isLoadingStops -> {
                        Text(
                            text = "Cargando paradas...",
                            modifier = Modifier.padding(top = 16.dp)
                        )
                    }

                    stopLoadError != null -> {
                        Text(
                            text = stopLoadError ?: "",
                            modifier = Modifier.padding(top = 16.dp)
                        )
                    }

                    routeStops.isEmpty() -> {
                        Text(
                            text = "Esta ruta no tiene paradas disponibles",
                            modifier = Modifier.padding(top = 16.dp)
                        )
                    }

                    else -> {
                        routeStops.forEach { routeStop ->
                            Text(
                                text = "${routeStop.orden}. ${routeStop.stop.nombre}",
                                modifier = Modifier.padding(top = 8.dp)
                            )
                        }
                    }
                }
            }
        }

        OutlinedTextField(
            value = origin,
            onValueChange = { origin = it },
            label = {
                Text(text = "Origen")
            },
            modifier = Modifier
                .fillMaxWidth()
                .padding(top = 24.dp)
        )

        OutlinedTextField(
            value = destination,
            onValueChange = { destination = it },
            label = {
                Text(text = "Destino")
            },
            modifier = Modifier
                .fillMaxWidth()
                .padding(top = 16.dp)
        )

        Button(
            onClick = { },
            modifier = Modifier
                .fillMaxWidth()
                .padding(top = 24.dp)
        ) {
            Text(text = "Buscar")
        }
    }
}
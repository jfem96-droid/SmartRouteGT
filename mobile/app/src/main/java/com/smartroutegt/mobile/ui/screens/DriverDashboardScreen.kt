package com.smartroutegt.mobile.ui.screens

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.material3.Button
import android.Manifest
import android.content.pm.PackageManager
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.ui.platform.LocalContext
import androidx.core.content.ContextCompat
import android.content.Context
import android.location.LocationManager
import android.os.Build
import android.location.Location

@Composable
fun DriverDashboardScreen() {
    val busAsignado = "BUS-01"
    val rutaAsignada = "Ruta 101"
    var estadoViaje by remember { mutableStateOf("No iniciado") }
    val sancionesConfirmadas = 2
    var velocidadActual by remember { mutableStateOf(0f) }
    val proximaParada = "Parada Central"
    val paradas = listOf(
    "Parada Central",
    "Parada Norte",
    "Parada Sur"
)
    var estadoGps by remember { mutableStateOf("Permiso pendiente") }
    val alertaActual = "Sin alertas"

    val context = LocalContext.current

    val locationManager = remember {
        context.getSystemService(Context.LOCATION_SERVICE) as LocationManager
    }

    var latitud by remember { mutableStateOf<Double?>(null) }
    var longitud by remember { mutableStateOf<Double?>(null) }

    val permisoUbicacionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestMultiplePermissions()
    ) { permisos ->
        val ubicacionPrecisaConcedida =
            permisos[Manifest.permission.ACCESS_FINE_LOCATION] == true

    val ubicacionAproximadaConcedida =
        permisos[Manifest.permission.ACCESS_COARSE_LOCATION] == true

    estadoGps = if (ubicacionPrecisaConcedida || ubicacionAproximadaConcedida) {
        "Permiso concedido"
    } else {
        "Permiso denegado"
    }
    }

    fun obtenerUbicacion() {
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
        return
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
            return
        }
    }

    estadoGps = "Buscando ubicación..."

    fun actualizarUbicacion(location: Location?) {
    if (location != null) {
        latitud = location.latitude
        longitud = location.longitude

        velocidadActual = if (location.hasSpeed()) {
            location.speed * 3.6f
        } else {
            0f
        }

        estadoGps = "Ubicación disponible"
    } else {
        estadoGps = "Ubicación no disponible"
    }
}

if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
    locationManager.getCurrentLocation(
        proveedor,
        null,
        ContextCompat.getMainExecutor(context)
    ) { location ->
        actualizarUbicacion(location)
    }
} else {
    actualizarUbicacion(
        locationManager.getLastKnownLocation(proveedor)
    )
}
}

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(24.dp)
    ) {
        Text(text = "Panel del piloto")

        Spacer(modifier = Modifier.height(24.dp))

        Text(text = "Bus asignado: $busAsignado")

        Spacer(modifier = Modifier.height(12.dp))

        Text(text = "Ruta asignada: $rutaAsignada")

        Spacer(modifier = Modifier.height(12.dp))

        Text(text = "Estado del viaje: $estadoViaje")

        Spacer(modifier = Modifier.height(12.dp))

        Text(text = "Sanciones confirmadas: $sancionesConfirmadas")

        Spacer(modifier = Modifier.height(12.dp))

        Text(text = "Velocidad actual: $velocidadActual km/h")

        Spacer(modifier = Modifier.height(12.dp))

        Text(text = "Próxima parada: $proximaParada")

        Spacer(modifier = Modifier.height(12.dp))

        Text(text = "Paradas de la ruta:")

        paradas.forEach { parada ->
            Text(text = "• $parada")
        }

        Spacer(modifier = Modifier.height(12.dp))

        Text(text = "Estado GPS: $estadoGps")

        Text(
            text = if (latitud != null && longitud != null) {
                 "Ubicación: $latitud, $longitud"
            } else {
                "Ubicación: no disponible"
            }
        )

        Spacer(modifier = Modifier.height(12.dp))

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
                    obtenerUbicacion()
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
        Text(text = "Obtener ubicación")
    }

        Spacer(modifier = Modifier.height(12.dp))

        Text(text = "Alertas: $alertaActual")

        Spacer(modifier = Modifier.height(24.dp))

        Button(
            onClick = {
                estadoViaje = "En curso"
            }
        ) {
            Text(text = "Iniciar viaje")
        }

        Spacer(modifier = Modifier.height(12.dp))

        Button(
            onClick = {
                estadoViaje = "Finalizado"
            }
        ) {
            Text(text = "Finalizar viaje")
        }
    }
}
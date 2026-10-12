package com.smartroutegt.mobile.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.smartroutegt.mobile.network.DriverStopStatusDto

@Composable
fun DriverStopSafetyCard(
    stopStatus: DriverStopStatusDto?,
    tripActive: Boolean,
    gpsActive: Boolean
) {
    val status = stopStatus?.status?.uppercase()

    val backgroundColor = when {
        !tripActive -> SmartRouteBlueSoft
        !gpsActive -> SmartRouteBlueSoft
        status == "SKIPPED" -> SmartRouteRedSoft
        status == "COMPLETED" -> SmartRouteGreenSoft
        status == "INSIDE" -> SmartRouteAmberSoft
        status == "APPROACHING" -> SmartRouteBlueSoft
        else -> SmartRouteBlueSoft
    }

    val title = when {
        !tripActive -> "CONTROL DE PARADAS"
        !gpsActive -> "CONTROL DE PARADAS"
        status == "SKIPPED" -> "PARADA INCUMPLIDA"
        status == "COMPLETED" -> "PARADA CUMPLIDA"
        status == "INSIDE" -> "EN PARADA"
        status == "APPROACHING" -> "PRÓXIMA PARADA"
        else -> "CONTROL DE PARADAS"
    }

    val description = when {
        !tripActive ->
            "Disponible al iniciar el viaje"

        !gpsActive ->
            "Esperando señal GPS"

        stopStatus == null ->
            "Esperando información de la próxima parada"

        status == "SKIPPED" ->
            "Saliste de la parada antes de completar el tiempo mínimo"

        status == "COMPLETED" ->
            "Tiempo mínimo completado correctamente"

        status == "INSIDE" ->
            "Permanece dentro del radio de la parada"

        status == "APPROACHING" ->
            "Próxima parada pendiente de la ruta"

        else ->
            "Seguimiento de parada activo"
    }

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .background(
                backgroundColor,
                RoundedCornerShape(22.dp)
            )
            .padding(18.dp)
    ) {
        Text(
            text = title,
            color = SmartRouteMuted,
            fontSize = 12.sp,
            fontWeight = FontWeight.ExtraBold
        )

        Spacer(modifier = Modifier.height(7.dp))

        Text(
            text = stopStatus?.stopName ?: "Sin información",
            color = SmartRouteText,
            fontSize = 20.sp,
            fontWeight = FontWeight.ExtraBold
        )

        Spacer(modifier = Modifier.height(5.dp))

        Text(
            text = description,
            color = SmartRouteMuted,
            fontSize = 12.sp
        )

        if (
            tripActive &&
            gpsActive &&
            stopStatus != null
        ) {
            Spacer(modifier = Modifier.height(12.dp))

            stopStatus.distanceMeters?.let { distance ->
                val distanceText =
                    if (distance >= 1000.0) {
                        "%.2f km".format(distance / 1000.0)
                    } else {
                        "%.0f m".format(distance)
                    }

                Text(
                    text = "Distancia: $distanceText",
                    color = SmartRouteText,
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Bold
                )
            }

            if (
                status == "INSIDE" ||
                status == "COMPLETED" ||
                status == "SKIPPED"
            ) {
                Spacer(modifier = Modifier.height(4.dp))

                Text(
                    text = "Permanencia mínima: ${stopStatus.requiredSeconds ?: 0} s",
                    color = SmartRouteText,
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Bold
                )
            }
        }
    }
}

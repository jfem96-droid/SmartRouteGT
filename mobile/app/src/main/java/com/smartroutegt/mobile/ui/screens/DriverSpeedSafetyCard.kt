package com.smartroutegt.mobile.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

@Composable
fun DriverSpeedSafetyCard(
    speedKmh: Double,
    limitKmh: Double,
    warning: Boolean,
    violationCreated: Boolean,
    tripActive: Boolean,
    gpsActive: Boolean
) {
    val backgroundColor = when {
        violationCreated -> SmartRouteRedSoft
        warning -> SmartRouteAmberSoft
        tripActive && gpsActive -> SmartRouteGreenSoft
        else -> SmartRouteBlueSoft
    }

    val title = when {
        violationCreated -> "INFRACCIÓN REGISTRADA"
        warning -> "REDUCE LA VELOCIDAD"
        tripActive && gpsActive -> "VELOCIDAD SEGURA"
        else -> "CONTROL DE VELOCIDAD"
    }

    val status = when {
        violationCreated -> "Exceso de velocidad registrado"
        warning -> "Superaste el límite permitido"
        tripActive && gpsActive -> "Dentro del límite permitido"
        tripActive -> "Esperando señal GPS"
        else -> "Disponible al iniciar el viaje"
    }

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .background(
                color = backgroundColor,
                shape = RoundedCornerShape(22.dp)
            )
            .padding(18.dp),
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        Text(
            text = title,
            fontSize = 13.sp,
            fontWeight = FontWeight.Bold,
            color = SmartRouteText
        )

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Column {
                Text(
                    text = "${speedKmh.toInt()} km/h",
                    fontSize = 30.sp,
                    fontWeight = FontWeight.Bold,
                    color = SmartRouteText
                )

                Text(
                    text = status,
                    fontSize = 13.sp,
                    color = SmartRouteMuted
                )
            }

            Column {
                Text(
                    text = "LÍMITE",
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Bold,
                    color = SmartRouteMuted
                )

                Text(
                    text = "${limitKmh.toInt()} km/h",
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Bold,
                    color = SmartRouteText
                )
            }
        }

        if (violationCreated) {
            Text(
                text = "La infracción fue registrada por SmartRoute GT.",
                fontSize = 12.sp,
                fontWeight = FontWeight.SemiBold,
                color = SmartRouteRed
            )
        }
    }
}

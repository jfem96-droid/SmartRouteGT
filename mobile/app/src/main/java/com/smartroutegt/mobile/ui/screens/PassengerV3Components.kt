package com.smartroutegt.mobile.ui.screens

import com.smartroutegt.mobile.ui.theme.SmartRoutePalette

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

@Composable
fun PassengerV3TopBar(
    title: String,
    subtitle: String,
    onBack: (() -> Unit)? = null
) {
    Surface(color = SmartRoutePalette.Panel, shadowElevation = 3.dp) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 13.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            if (onBack != null) {
                Box(
                    modifier = Modifier
                        .size(42.dp)
                        .clip(CircleShape)
                        .background(SmartRoutePalette.Navy)
                        .clickable(onClick = onBack),
                    contentAlignment = Alignment.Center
                ) {
                    Text("‹", fontSize = 30.sp, color = SmartRoutePalette.Text)
                }

                Spacer(modifier = Modifier.size(12.dp))
            }

            Box(
                modifier = Modifier
                    .size(42.dp)
                    .clip(RoundedCornerShape(13.dp))
                    .background(MaterialTheme.colorScheme.primary),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    "SR",
                    color = SmartRoutePalette.Night,
                    fontWeight = FontWeight.ExtraBold
                )
            }

            Column(
                modifier = Modifier
                    .weight(1f)
                    .padding(start = 11.dp)
            ) {
                Text(
                    title,
                    fontWeight = FontWeight.Bold,
                    fontSize = 18.sp,
                    color = SmartRoutePalette.Text
                )

                Text(
                    subtitle,
                    fontSize = 11.sp,
                    color = SmartRoutePalette.Muted
                )
            }
        }
    }
}

@Composable
fun PassengerV3SectionCard(
    modifier: Modifier = Modifier,
    content: @Composable () -> Unit
) {
    Card(
        modifier = modifier.fillMaxWidth(),
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(containerColor = SmartRoutePalette.Panel, contentColor = SmartRoutePalette.Text),
        elevation = CardDefaults.cardElevation(defaultElevation = 3.dp)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            content()
        }
    }
}

@Composable
fun PassengerV3Choice(
    title: String,
    subtitle: String? = null,
    selected: Boolean,
    markerColor: Color = MaterialTheme.colorScheme.primary,
    onClick: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(15.dp))
            .background(
                if (selected) SmartRoutePalette.Panel
                else SmartRoutePalette.Navy
            )
            .clickable(onClick = onClick)
            .padding(horizontal = 13.dp, vertical = 12.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Box(
            modifier = Modifier
                .size(11.dp)
                .clip(CircleShape)
                .background(markerColor)
        )

        Column(
            modifier = Modifier
                .weight(1f)
                .padding(start = 12.dp)
        ) {
            Text(
                title,
                fontWeight = FontWeight.SemiBold,
                fontSize = 14.sp,
                color = SmartRoutePalette.Text
            )

            if (!subtitle.isNullOrBlank()) {
                Text(
                    subtitle,
                    fontSize = 11.sp,
                    color = SmartRoutePalette.Muted
                )
            }
        }

        if (selected) {
            Text(
                "✓",
                color = MaterialTheme.colorScheme.primary,
                fontWeight = FontWeight.Bold
            )
        }
    }
}

@Composable
fun PassengerV3BusCard(
    code: String,
    plate: String,
    selected: Boolean,
    onClick: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick),
        shape = RoundedCornerShape(17.dp),
        colors = CardDefaults.cardColors(
            containerColor =
                if (selected) SmartRoutePalette.Panel
                else SmartRoutePalette.Panel
        ),
        elevation = CardDefaults.cardElevation(
            defaultElevation = if (selected) 4.dp else 1.dp
        )
    ) {
        Row(
            modifier = Modifier.padding(14.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .size(44.dp)
                    .clip(RoundedCornerShape(13.dp))
                    .background(MaterialTheme.colorScheme.primary),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    "BUS",
                    color = SmartRoutePalette.Night,
                    fontSize = 9.sp,
                    fontWeight = FontWeight.ExtraBold
                )
            }

            Column(
                modifier = Modifier
                    .weight(1f)
                    .padding(start = 12.dp)
            ) {
                Text(
                    code,
                    fontWeight = FontWeight.Bold,
                    color = SmartRoutePalette.Text
                )

                Text(
                    "Placa $plate",
                    fontSize = 12.sp,
                    color = SmartRoutePalette.Muted
                )
            }

            Text(
                if (selected) "✓" else "›",
                color = MaterialTheme.colorScheme.primary,
                fontSize = if (selected) 18.sp else 26.sp,
                fontWeight = FontWeight.Bold
            )
        }
    }
}

@Composable
fun PassengerV3EtaCard(
    busCode: String,
    plate: String,
    routeName: String,
    nextStop: String?,
    etaMinutes: Int?,
    distanceMeters: Double?,
    speed: Double?,
    status: String?
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(22.dp),
        colors = CardDefaults.cardColors(
            containerColor = SmartRoutePalette.Panel
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 5.dp)
    ) {
        Column(modifier = Modifier.padding(18.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Box(
                    modifier = Modifier
                        .size(48.dp)
                        .clip(RoundedCornerShape(14.dp))
                        .background(Color.White.copy(alpha = 0.14f)),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        "BUS",
                        color = Color.White,
                        fontSize = 10.sp,
                        fontWeight = FontWeight.ExtraBold
                    )
                }

                Column(
                    modifier = Modifier
                        .weight(1f)
                        .padding(start = 12.dp)
                ) {
                    Text(
                        "$busCode · $plate",
                        color = Color.White,
                        fontWeight = FontWeight.Bold,
                        fontSize = 17.sp
                    )

                    Text(
                        routeName,
                        color = Color.White.copy(alpha = 0.75f),
                        fontSize = 12.sp
                    )
                }

                if (etaMinutes != null) {
                    Text(
                        "$etaMinutes min",
                        color = Color.White,
                        fontWeight = FontWeight.ExtraBold,
                        fontSize = 22.sp
                    )
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            Text(
                nextStop?.let { "Próxima parada · $it" }
                    ?: "Próxima parada no disponible",
                color = Color.White,
                fontWeight = FontWeight.SemiBold
            )

            Spacer(modifier = Modifier.height(10.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text(
                    distanceMeters?.let {
                        if (it >= 1000.0) "%.2f km".format(it / 1000.0)
                        else "%.0f m".format(it)
                    } ?: "Distancia --",
                    color = Color.White.copy(alpha = 0.78f),
                    fontSize = 12.sp
                )

                Text(
                    speed?.let { "%.1f km/h".format(it) }
                        ?: "Velocidad --",
                    color = Color.White.copy(alpha = 0.78f),
                    fontSize = 12.sp
                )

                Text(
                    status ?: "Sin ETA",
                    color = Color.White.copy(alpha = 0.78f),
                    fontSize = 12.sp
                )
            }
        }
    }
}

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
import androidx.compose.foundation.layout.statusBarsPadding
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

val SmartRouteBlue = SmartRoutePalette.Mint
val SmartRouteDarkBlue = SmartRoutePalette.Navy
val SmartRouteGreen = Color(0xFF63E4C4)
val SmartRouteRed = Color(0xFFDC2626)
val SmartRouteBackground = SmartRoutePalette.Night
val SmartRouteText = SmartRoutePalette.Text
val SmartRouteMuted = SmartRoutePalette.Muted

val SmartRouteBlueSoft = SmartRoutePalette.Panel
val SmartRouteGreenSoft = SmartRoutePalette.Panel
val SmartRouteRedSoft = SmartRoutePalette.Panel
val SmartRouteAmberSoft = SmartRoutePalette.Panel
val SmartRoutePurpleSoft = SmartRoutePalette.Panel

enum class SmartRouteV3Role {
    PASSENGER,
    DRIVER
}

enum class SmartRouteV3PassengerTab {
    HOME,
    ROUTES,
    SOS,
    REPORT,
    PROFILE
}

enum class SmartRouteV3DriverTab {
    HOME,
    ROUTE,
    GPS,
    ALERTS,
    REPORT,
    PROFILE
}

@Composable
fun SmartRouteV3TopBar(
    title: String,
    subtitle: String,
    role: SmartRouteV3Role,
    onBack: (() -> Unit)? = null
) {
    Surface(
        color = SmartRoutePalette.Panel,
        shadowElevation = 2.dp
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            if (onBack != null) {
                Box(
                    modifier = Modifier
                        .size(40.dp)
                        .clip(CircleShape)
                        .background(SmartRoutePalette.Navy)
                        .clickable(onClick = onBack),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = "‹",
                        fontSize = 29.sp,
                        color = SmartRouteText
                    )
                }

                Spacer(modifier = Modifier.size(10.dp))
            }

            Box(
                modifier = Modifier
                    .size(42.dp)
                    .clip(RoundedCornerShape(13.dp))
                    .background(SmartRouteBlue),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = "SR",
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
                    text = title,
                    fontWeight = FontWeight.Bold,
                    fontSize = 18.sp,
                    color = SmartRouteText
                )

                Text(
                    text = subtitle,
                    fontSize = 11.sp,
                    color = SmartRouteMuted
                )
            }

            Surface(
                shape = RoundedCornerShape(50),
                color = if (role == SmartRouteV3Role.DRIVER) {
                    SmartRoutePalette.Panel
                } else {
                    SmartRoutePalette.Panel
                }
            ) {
                Text(
                    text = if (role == SmartRouteV3Role.DRIVER) {
                        "PILOTO"
                    } else {
                        "PASAJERO"
                    },
                    modifier = Modifier.padding(
                        horizontal = 10.dp,
                        vertical = 6.dp
                    ),
                    color = if (role == SmartRouteV3Role.DRIVER) {
                        SmartRouteGreen
                    } else {
                        SmartRouteBlue
                    },
                    fontSize = 10.sp,
                    fontWeight = FontWeight.Bold
                )
            }
        }
    }
}

@Composable
fun SmartRouteV3Card(
    modifier: Modifier = Modifier,
    content: @Composable () -> Unit
) {
    Card(
        modifier = modifier.fillMaxWidth(),
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(
            containerColor = SmartRoutePalette.Panel
        ),
        elevation = CardDefaults.cardElevation(
            defaultElevation = 2.dp
        )
    ) {
        Column(
            modifier = Modifier.padding(16.dp)
        ) {
            content()
        }
    }
}

@Composable
fun SmartRouteV3StatusCard(
    title: String,
    value: String,
    detail: String? = null,
    positive: Boolean = true,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier,
        shape = RoundedCornerShape(18.dp),
        colors = CardDefaults.cardColors(
            containerColor = if (positive) {
                SmartRoutePalette.Panel
            } else {
                SmartRoutePalette.Panel
            }
        )
    ) {
        Column(
            modifier = Modifier.padding(15.dp)
        ) {
            Text(
                text = title,
                color = SmartRouteMuted,
                fontSize = 11.sp,
                fontWeight = FontWeight.SemiBold
            )

            Spacer(modifier = Modifier.height(4.dp))

            Text(
                text = value,
                color = SmartRouteText,
                fontSize = 17.sp,
                fontWeight = FontWeight.Bold
            )

            if (!detail.isNullOrBlank()) {
                Spacer(modifier = Modifier.height(3.dp))

                Text(
                    text = detail,
                    color = SmartRouteMuted,
                    fontSize = 11.sp
                )
            }
        }
    }
}

@Composable
fun SmartRouteV3EmptyState(
    title: String,
    description: String,
    error: Boolean = false
) {
    Surface(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(18.dp),
        color = if (error) {
            SmartRoutePalette.Panel
        } else {
            SmartRoutePalette.Navy
        }
    ) {
        Column(
            modifier = Modifier.padding(16.dp)
        ) {
            Text(
                text = title,
                fontWeight = FontWeight.Bold,
                color = if (error) SmartRouteRed else SmartRouteText
            )

            Spacer(modifier = Modifier.height(4.dp))

            Text(
                text = description,
                fontSize = 12.sp,
                color = SmartRouteMuted
            )
        }
    }
}

@Composable
fun SmartRouteV3NavItem(
    label: String,
    selected: Boolean,
    emphasized: Boolean = false,
    onClick: () -> Unit
) {
    Column(
        modifier = Modifier
            .clip(RoundedCornerShape(14.dp))
            .clickable(onClick = onClick)
            .padding(horizontal = 10.dp, vertical = 7.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Box(
            modifier = Modifier
                .size(if (emphasized) 34.dp else 8.dp)
                .clip(CircleShape)
                .background(
                    when {
                        emphasized -> SmartRouteRed
                        selected -> SmartRouteBlue
                        else -> SmartRoutePalette.Muted
                    }
                ),
            contentAlignment = Alignment.Center
        ) {
            if (emphasized) {
                Text(
                    text = "SOS",
                    color = Color.White,
                    fontSize = 8.sp,
                    fontWeight = FontWeight.ExtraBold
                )
            }
        }

        if (!emphasized) {
            Spacer(modifier = Modifier.height(5.dp))
        }

        Text(
            text = label,
            fontSize = 10.sp,
            color = if (selected || emphasized) {
                if (emphasized) SmartRouteRed else SmartRouteBlue
            } else {
                SmartRouteMuted
            },
            fontWeight = if (selected || emphasized) {
                FontWeight.Bold
            } else {
                FontWeight.Medium
            }
        )
    }
}

@Composable
fun SmartRouteV3PassengerBottomBar(
    selected: SmartRouteV3PassengerTab,
    onHome: () -> Unit,
    onRoutes: () -> Unit,
    onSos: () -> Unit,
    onReport: () -> Unit,
    onProfile: () -> Unit
) {
    Surface(
        color = SmartRoutePalette.Panel,
        shadowElevation = 8.dp
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 6.dp, vertical = 5.dp),
            horizontalArrangement = Arrangement.SpaceEvenly,
            verticalAlignment = Alignment.CenterVertically
        ) {
            SmartRouteV3NavItem(
                "Inicio",
                selected == SmartRouteV3PassengerTab.HOME,
                onClick = onHome
            )

            SmartRouteV3NavItem(
                "Rutas",
                selected == SmartRouteV3PassengerTab.ROUTES,
                onClick = onRoutes
            )

            SmartRouteV3NavItem(
                "SOS",
                selected == SmartRouteV3PassengerTab.SOS,
                emphasized = true,
                onClick = onSos
            )

            SmartRouteV3NavItem(
                "Reportar",
                selected == SmartRouteV3PassengerTab.REPORT,
                onClick = onReport
            )

            SmartRouteV3NavItem(
                "Perfil",
                selected == SmartRouteV3PassengerTab.PROFILE,
                onClick = onProfile
            )
        }
    }
}

@Composable
fun SmartRouteV3DriverBottomBar(
    selected: SmartRouteV3DriverTab,
    onHome: () -> Unit,
    onRoute: () -> Unit,
    onGps: () -> Unit,
    onAlerts: () -> Unit,
    onProfile: () -> Unit,
    onReport: () -> Unit = {}
) {
    Surface(
        color = SmartRoutePalette.Panel,
        shadowElevation = 8.dp
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 6.dp, vertical = 5.dp),
            horizontalArrangement = Arrangement.SpaceEvenly,
            verticalAlignment = Alignment.CenterVertically
        ) {
            SmartRouteV3NavItem(
                "Inicio",
                selected == SmartRouteV3DriverTab.HOME,
                onClick = onHome
            )

            SmartRouteV3NavItem(
                "Ruta",
                selected == SmartRouteV3DriverTab.ROUTE,
                onClick = onRoute
            )

            SmartRouteV3NavItem(
                "GPS",
                selected == SmartRouteV3DriverTab.GPS,
                onClick = onGps
            )

            SmartRouteV3NavItem(
                "Alertas",
                selected == SmartRouteV3DriverTab.ALERTS,
                onClick = onAlerts
            )

            SmartRouteV3NavItem(
                "Reportar",
                selected == SmartRouteV3DriverTab.REPORT,
                onClick = onReport
            )
            SmartRouteV3NavItem(
                "Perfil",
                selected == SmartRouteV3DriverTab.PROFILE,
                onClick = onProfile
            )
        }
    }
}

package com.smartroutegt.mobile.ui.theme

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

private val SmartRouteColorScheme = lightColorScheme(
    primary = SmartBlue,
    onPrimary = Color.White,

    primaryContainer = SmartBlueSoft,
    onPrimaryContainer = SmartBlueDeep,

    secondary = SmartCyan,
    onSecondary = Color.White,

    secondaryContainer = Color(0xFFE0F2FE),
    onSecondaryContainer = Color(0xFF075985),

    tertiary = SmartGreen,
    onTertiary = Color.White,

    error = SmartRed,
    onError = Color.White,
    errorContainer = SmartRedSoft,
    onErrorContainer = SmartRed,

    background = SmartBackground,
    onBackground = SmartText,

    surface = SmartSurface,
    onSurface = SmartText,

    surfaceVariant = SmartSurfaceVariant,
    onSurfaceVariant = SmartTextSecondary,

    outline = SmartBorder
)

@Composable
fun SmartRouteGTTheme(
    darkTheme: Boolean = false,
    dynamicColor: Boolean = false,
    content: @Composable () -> Unit
) {
    MaterialTheme(
        colorScheme = SmartRouteColorScheme,
        typography = Typography,
        content = content
    )
}

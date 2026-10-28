package com.smartroutegt.mobile.ui.theme

import android.app.Activity
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.safeDrawing
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.SideEffect
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalView
import androidx.core.view.WindowCompat

/** Paleta del administrador /css/admin-dark-v2.css.
 *  Sólo personalización visual: no toca pantallas, navegación, GPS o servicios API.
 */
object SmartRoutePalette {
    val Night = Color(0xFF071727)
    val Navy = Color(0xFF10283F)
    val Panel = Color(0xFF153850)
    val Mint = Color(0xFF63E4C4)
    val Text = Color(0xFFF5FBFF)
    val Muted = Color(0xFFB6C8D7)
    val Border = Color(0xFF31566A)
    val Error = Color(0xFFF5A1A8)
}

private val SmartRouteDarkColors = darkColorScheme(
    primary = SmartRoutePalette.Mint,
    onPrimary = SmartRoutePalette.Night,
    primaryContainer = SmartRoutePalette.Panel,
    onPrimaryContainer = SmartRoutePalette.Text,
    secondary = Color(0xFF79C8E8),
    onSecondary = SmartRoutePalette.Night,
    background = SmartRoutePalette.Night,
    onBackground = SmartRoutePalette.Text,
    surface = SmartRoutePalette.Navy,
    onSurface = SmartRoutePalette.Text,
    surfaceVariant = SmartRoutePalette.Panel,
    onSurfaceVariant = SmartRoutePalette.Muted,
    outline = SmartRoutePalette.Border,
    outlineVariant = SmartRoutePalette.Border,
    secondaryContainer = SmartRoutePalette.Panel,
    onSecondaryContainer = SmartRoutePalette.Text,
    tertiary = SmartRoutePalette.Mint,
    onTertiary = SmartRoutePalette.Night,
    tertiaryContainer = SmartRoutePalette.Panel,
    onTertiaryContainer = SmartRoutePalette.Text,
    inverseSurface = SmartRoutePalette.Text,
    inverseOnSurface = SmartRoutePalette.Night,
    inversePrimary = Color(0xFF126B59),
    error = SmartRoutePalette.Error,
    onError = SmartRoutePalette.Night
)

/** Envuelve el tema que ya usa la aplicación, conserva tipografía y formas,
 *  consume safeDrawing UNA vez y evita superposiciones con status/navigation bar.
 *  Los insets que Compose consume en este Box no se vuelven a aplicar aguas abajo.
 */
@Composable
fun SmartRouteVisualTheme(content: @Composable () -> Unit) {
    SmartRouteGTTheme {
        val view = LocalView.current
        SideEffect {
            val activity = view.context as? Activity
            if (activity != null) {
                val controller = WindowCompat.getInsetsController(activity.window, view)
                controller.isAppearanceLightStatusBars = false
                controller.isAppearanceLightNavigationBars = false
            }
        }
        MaterialTheme(
            colorScheme = SmartRouteDarkColors,
            typography = MaterialTheme.typography,
            shapes = MaterialTheme.shapes
        ) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(SmartRoutePalette.Night)
                    .windowInsetsPadding(WindowInsets.safeDrawing)
            ) {
                content()
            }
        }
    }
}

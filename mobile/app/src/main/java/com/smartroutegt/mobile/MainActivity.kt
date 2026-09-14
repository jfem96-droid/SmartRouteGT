package com.smartroutegt.mobile

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import com.smartroutegt.mobile.ui.screens.DriverDashboardScreen
import com.smartroutegt.mobile.ui.screens.WelcomeScreen
import com.smartroutegt.mobile.ui.theme.SmartRouteGTTheme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        setContent {
            SmartRouteGTTheme {
                var mostrarDashboard by remember {
                    mutableStateOf(false)
                }

                if (mostrarDashboard) {
                    DriverDashboardScreen()
                } else {
                    WelcomeScreen(
                        onContinue = {
                            mostrarDashboard = true
                        }
                    )
                }
            }
        }
    }
}
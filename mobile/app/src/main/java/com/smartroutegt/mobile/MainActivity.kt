package com.smartroutegt.mobile

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import com.smartroutegt.mobile.ui.screens.WelcomeScreen
import com.smartroutegt.mobile.ui.theme.SmartRouteGTTheme
import com.smartroutegt.mobile.ui.screens.LoginScreen
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import com.smartroutegt.mobile.ui.screens.RoleSelectionScreen
import com.smartroutegt.mobile.ui.screens.PassengerHomeScreen
import com.smartroutegt.mobile.ui.screens.RouteSearchScreen
import com.smartroutegt.mobile.ui.screens.NearbyBusesScreen
class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            SmartRouteGTTheme {
                var currentScreen by remember {
                    mutableStateOf("welcome")
                }

                when (currentScreen) {
                    "welcome" -> {
                        WelcomeScreen(
                            onContinue = {
                                currentScreen = "login"
                            }
                        )
                    }

                    "login" -> {
                        LoginScreen(
                            onLoginClick = {
                                currentScreen = "role"
                            }
                        )
                    }

                    "role" -> {
                        RoleSelectionScreen(
                            onPassengerSelected = {
                                currentScreen = "passenger"
                            },
                            onDriverSelected = { }
                        )
                    }

                    "passenger" -> {
                        PassengerHomeScreen(
                            onSearchRoute = {
                                currentScreen = "routeSearch"
                            },
                            onNearbyBuses = {
                                currentScreen = "nearbyBuses"
                            }
                        )
                    }

                    "routeSearch" -> {
                        RouteSearchScreen()
                    }

                    "nearbyBuses" -> {
                        NearbyBusesScreen()
                    }
                }
            }
        }
    }
}
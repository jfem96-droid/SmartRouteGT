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
import com.smartroutegt.mobile.ui.screens.LoginScreen
import com.smartroutegt.mobile.ui.screens.RoleSelectionScreen
import com.smartroutegt.mobile.ui.screens.PassengerHomeScreen
import com.smartroutegt.mobile.ui.screens.RouteSearchScreen
import com.smartroutegt.mobile.ui.screens.NearbyBusesScreen
import com.smartroutegt.mobile.ui.screens.BusInfoScreen
import com.smartroutegt.mobile.ui.screens.EtaScreen
import com.smartroutegt.mobile.ui.screens.SosScreen
import com.smartroutegt.mobile.ui.screens.DriverReportScreen
import com.smartroutegt.mobile.ui.screens.RouteReportScreen
import com.smartroutegt.mobile.ui.screens.DriverLicenseScreen
import com.smartroutegt.mobile.network.DriverDto
import com.smartroutegt.mobile.network.DriverRouteAssignmentDto

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        setContent {
            SmartRouteGTTheme {

                var currentScreen by remember {
                    mutableStateOf("welcome")
                }

                var driverActual by remember {
                    mutableStateOf<DriverDto?>(null)
                }

                var asignacionActual by remember {
                    mutableStateOf<DriverRouteAssignmentDto?>(null)
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
                            onDriverSelected = {
                                currentScreen = "driverLicense"
                            }
                        )
                    }

                    "driverLicense" -> {
                        DriverLicenseScreen(
                            onDriverValidated = { driver, assignment ->
                                driverActual = driver
                                asignacionActual = assignment
                                currentScreen = "driver"
                            }
                        )
                    }
                    "driver" -> {
                        DriverDashboardScreen()
                    }

                    "passenger" -> {
                        PassengerHomeScreen(
                            onSearchRoute = {
                                currentScreen = "routeSearch"
                            },
                            onNearbyBuses = {
                                currentScreen = "nearbyBuses"
                            },
                            onBusInfo = {
                                currentScreen = "busInfo"
                            },
                            onEta = {
                                currentScreen = "eta"
                            },
                            onSos = {
                                currentScreen = "sos"
                            },
                            onDriverReport = {
                                currentScreen = "driverReport"
                            },
                            onRouteReport = {
                                currentScreen = "routeReport"
                            }
                        )
                    }

                    "routeSearch" -> {
                        RouteSearchScreen(
                            onBack = {
                                currentScreen = "passenger"
                            }
                        )
                    }

                    "nearbyBuses" -> {
                        NearbyBusesScreen(
                            onBack = {
                                currentScreen = "passenger"
                            }
                        )
                    }

                    "busInfo" -> {
                        BusInfoScreen(
                            onBack = {
                                currentScreen = "passenger"
                            }
                        )
                    }

                    "eta" -> {
                        EtaScreen(
                            onBack = {
                                currentScreen = "passenger"
                            }
                        )
                    }

                    "sos" -> {
                        SosScreen(
                            onBack = {
                                currentScreen = "passenger"
                            }
                        )
                    }
                    "driverReport" -> {
                        DriverReportScreen(
                            onBack = {
                                currentScreen = "passenger"
                            }
                        )
                    }
                    "routeReport" -> {
                        RouteReportScreen(
                            onBack = {
                                currentScreen = "passenger"
                            }
                        )
                    }

                }
            }
        }
    }
}
package com.smartroutegt.mobile

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.foundation.layout.fillMaxSize
import com.smartroutegt.mobile.ui.screens.DriverDashboardScreen
import com.smartroutegt.mobile.ui.screens.AccessTypeScreen
import com.smartroutegt.mobile.ui.screens.RegisterScreen
import com.smartroutegt.mobile.ui.screens.VerifyEmailScreen
import com.smartroutegt.mobile.ui.screens.ForgotPasswordScreen
import com.smartroutegt.mobile.ui.theme.SmartRouteVisualTheme
import com.smartroutegt.mobile.ui.screens.LoginScreen
import com.smartroutegt.mobile.ui.screens.RoleSelectionScreen
import com.smartroutegt.mobile.ui.screens.PassengerProfileScreen
import com.smartroutegt.mobile.ui.screens.PassengerHomeScreen
import com.smartroutegt.mobile.ui.screens.RouteSearchScreen
import com.smartroutegt.mobile.ui.screens.NearbyBusesScreen
import com.smartroutegt.mobile.ui.screens.BusInfoScreen
import com.smartroutegt.mobile.ui.screens.EtaScreen
import com.smartroutegt.mobile.ui.screens.PassengerFeedbackScreen
import com.smartroutegt.mobile.ui.screens.PassengerReportV3Form
import com.smartroutegt.mobile.ui.screens.SosScreen
import com.smartroutegt.mobile.ui.screens.DriverReportScreen
import com.smartroutegt.mobile.ui.screens.RouteReportScreen
import com.smartroutegt.mobile.ui.screens.DriverLicenseScreen
import com.smartroutegt.mobile.ui.screens.DriverStatusScreen
import com.smartroutegt.mobile.network.DriverDto
import com.smartroutegt.mobile.network.DriverRouteAssignmentDto
import com.smartroutegt.mobile.network.TripDto
import com.smartroutegt.mobile.auth.SessionManager
import com.smartroutegt.mobile.network.ApiClient
import com.smartroutegt.mobile.network.DriverApiService
import androidx.compose.runtime.LaunchedEffect

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        SessionManager.init(applicationContext)
        enableEdgeToEdge()

        setContent {
            SmartRouteVisualTheme {

                // SessionManager ya conserva token y rol en SharedPreferences.
                // Cuando Android recrea la actividad, reabrimos la zona autenticada.
                // Para piloto, driverLoading consulta de nuevo la asignación y viaje
                // activos; no se finaliza ningún viaje por minimizar la aplicación.
                var currentScreen by remember {
                    mutableStateOf(
                        if (SessionManager.isLoggedIn()) {
                            when (SessionManager.getRole()?.uppercase()) {
                                "DRIVER" -> "driverLoading"
                                "PASSENGER" -> "passenger"
                                else -> "role"
                            }
                        } else {
                            "accessType"
                        }
                    )
                }

                var passengerRoute by remember { mutableStateOf<com.smartroutegt.mobile.data.remote.RouteDto?>(null) }
                var passengerBus by remember { mutableStateOf<com.smartroutegt.mobile.data.remote.BusMonitoringDto?>(null) }

                var pendingVerificationEmail by remember {
                    mutableStateOf("")
                }

                var forgotPasswordAccessType by remember {
                    mutableStateOf("PASSENGER")
                }

                var driverStatus by remember {
                    mutableStateOf("NOT_LINKED")
                }

                var driverActual by remember {
                    mutableStateOf<DriverDto?>(null)
                }

                var asignacionActual by remember {
                    mutableStateOf<DriverRouteAssignmentDto?>(null)
                }

                var viajeActual by remember {
                    mutableStateOf<TripDto?>(null)
                }

                when (currentScreen) {
                    "accessType" -> {
                        AccessTypeScreen(
                            onPassengerSelected = {
                                currentScreen = "loginPassenger"
                            },
                            onDriverSelected = {
                                currentScreen = "loginDriver"
                            }
                        )
                    }

                    "loginPassenger", "loginDriver" -> {
                        LoginScreen(
                            accessType = if (currentScreen == "loginDriver") {
                                "DRIVER"
                            } else {
                                "PASSENGER"
                            },
                            onBack = {
                                currentScreen = "accessType"
                            },
                            onCreateAccount = {
                                currentScreen = "registerPassenger"
                            },
                            onForgotPassword = {
                                forgotPasswordAccessType =
                                    if (currentScreen == "loginDriver") {
                                        "DRIVER"
                                    } else {
                                        "PASSENGER"
                                    }

                                currentScreen = "forgotPassword"
                            },
                            onLoginSuccess = { login ->
                                currentScreen = when (login.role.uppercase()) {
                                    "DRIVER" -> "driverLoading"
                                    "PASSENGER" -> "passenger"
                                    else -> "role"
                                }
                            }
                        )
                    }

                    "registerPassenger" -> {
                        RegisterScreen(
                            onBack = {
                                currentScreen = "loginPassenger"
                            },
                            onRegistrationSuccess = { email ->
                                pendingVerificationEmail = email
                                currentScreen = "verifyEmail"
                            }
                        )
                    }

                    "verifyEmail" -> {
                        VerifyEmailScreen(
                            email = pendingVerificationEmail,
                            onBack = {
                                currentScreen = "registerPassenger"
                            },
                            onVerified = {
                                currentScreen = "loginPassenger"
                            }
                        )
                    }

                    "forgotPassword" -> {
                        ForgotPasswordScreen(
                            accessType = forgotPasswordAccessType,

                            onBack = {
                                currentScreen =
                                    if (
                                        forgotPasswordAccessType ==
                                        "DRIVER"
                                    ) {
                                        "loginDriver"
                                    } else {
                                        "loginPassenger"
                                    }
                            },

                            onPasswordChanged = {
                                currentScreen =
                                    if (
                                        forgotPasswordAccessType ==
                                        "DRIVER"
                                    ) {
                                        "loginDriver"
                                    } else {
                                        "loginPassenger"
                                    }
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

                    "driverLoading" -> {
                        val driverId = SessionManager.getDriverId()
                        val licencia = SessionManager.getLicense()

                        LaunchedEffect(driverId, licencia) {
                            if (
                                driverId == null ||
                                licencia.isNullOrBlank()
                            ) {
                                driverStatus = "NOT_LINKED"
                                currentScreen = "driverStatus"
                            } else {
                                try {
                                    val service = DriverApiService(
                                        ApiClient.create()
                                    )

                                    val driver =
                                        service.getDriverByLicense(
                                            licencia
                                        )

                                    if (
                                        driver.id != driverId
                                    ) {
                                        driverStatus = "NOT_LINKED"
                                        currentScreen = "driverStatus"
                                    } else if (!driver.activo) {
                                        driverStatus = "INACTIVE"
                                        currentScreen = "driverStatus"
                                    } else {
                                        val assignment =
                                            service.getActiveAssignment(
                                                driverId
                                            )

                                        if (assignment == null) {
                                            driverActual = driver
                                            asignacionActual = null
                                            viajeActual = null

                                            driverStatus =
                                                "NO_ASSIGNMENT"

                                            currentScreen =
                                                "driverStatus"
                                        } else {
                                            val activeTrip =
                                                service.getActiveTrip(
                                                    driverId
                                                )

                                            driverActual = driver
                                            asignacionActual = assignment
                                            viajeActual = activeTrip

                                            currentScreen = "driver"
                                        }
                                    }
                                } catch (
                                    exception: retrofit2.HttpException
                                ) {
                                    android.util.Log.e(
                                        "SmartRouteDriver",
                                        "HTTP ${exception.code()} en driverLoading",
                                        exception
                                    )

                                    driverStatus =
                                        if (exception.code() == 404) {
                                            "NOT_LINKED"
                                        } else {
                                            "CONNECTION_ERROR"
                                        }

                                    currentScreen = "driverStatus"
                                } catch (
                                    exception: java.io.IOException
                                ) {
                                    android.util.Log.e(
                                        "SmartRouteDriver",
                                        "IOException en driverLoading: ${exception.message}",
                                        exception
                                    )

                                    driverStatus = "CONNECTION_ERROR"
                                    currentScreen = "driverStatus"
                                } catch (
                                    exception: Exception
                                ) {
                                    android.util.Log.e(
                                        "SmartRouteDriver",
                                        "Error en driverLoading: ${exception.message}",
                                        exception
                                    )

                                    driverStatus = "CONNECTION_ERROR"
                                    currentScreen = "driverStatus"
                                }
                            }
                        }

                        androidx.compose.foundation.layout.Box(
                            modifier =
                                androidx.compose.ui.Modifier
                                    .fillMaxSize(),
                            contentAlignment =
                                androidx.compose.ui.Alignment.Center
                        ) {
                            androidx.compose.material3
                                .CircularProgressIndicator()
                        }
                    }

                    "driverStatus" -> {
                        DriverStatusScreen(
                            status = driverStatus,

                            onRetry = {
                                currentScreen = "driverLoading"
                            },

                            onLogout = {
                                SessionManager.clear()

                                driverActual = null
                                asignacionActual = null
                                viajeActual = null
                                driverStatus = "NOT_LINKED"

                                currentScreen = "accessType"
                            }
                        )
                    }

                    "driverLicense" -> {
                        DriverLicenseScreen(
                            onDriverValidated = { driver, assignment, activeTrip ->
                                driverActual = driver
                                asignacionActual = assignment
                                viajeActual = activeTrip
                                currentScreen = "driver"
                            }
                        )
                    }

                    "driver" -> {
                        val assignment = asignacionActual

                        if (assignment != null) {
                            DriverDashboardScreen(
                                assignment = assignment,
                                initialTrip = viajeActual,
                                onLogout = {
                                    SessionManager.clear()
                                    asignacionActual = null
                                    viajeActual = null
                                    currentScreen = "accessType"
                                }
                            )
                        }
                    }

                    "passenger" -> {
                        PassengerHomeScreen(
                            selectedRoute = passengerRoute,
                            selectedBus = passengerBus,
                            onClearRoute = { passengerRoute = null; passengerBus = null },
                            onBoarded = { currentScreen = "driverReport" },
                            onRateDriver = { currentScreen = "rateDriver" },
                            onComplimentDriver = { currentScreen = "complimentDriver" },
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
                            },
                            onProfile = {
                                currentScreen = "passengerProfile"
                            }
                        )
                    }

                    "passengerProfile" -> {
                        PassengerProfileScreen(
                            onBack = { currentScreen = "passenger" },
                            onLogout = {
                                SessionManager.clear()
                                currentScreen = "accessType"
                            }
                        )
                    }

                    "routeSearch" -> {
                        RouteSearchScreen(
                            onConfirmSelection = { route, bus ->
                                passengerRoute = route
                                passengerBus = bus
                                currentScreen = "passenger"
                            },
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
                    "rateDriver" -> PassengerFeedbackScreen("RATING", onBack = { currentScreen = "passenger" })
                    "complimentDriver" -> PassengerFeedbackScreen("COMPLIMENT", onBack = { currentScreen = "passenger" })
                    "driverReport" -> {
                        PassengerReportV3Form(routeProblem = false,
                            onBack = {
                                currentScreen = "passenger"
                            }
                        )
                    }
                    "routeReport" -> {
                        PassengerReportV3Form(routeProblem = true,
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
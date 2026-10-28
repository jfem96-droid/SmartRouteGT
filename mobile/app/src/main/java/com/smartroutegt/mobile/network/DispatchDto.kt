package com.smartroutegt.mobile.network

data class DispatchStatusDto(
    val assignmentId: Long,
    val driverId: Long,
    val bus: String,
    val route: String,
    val routeId: Long,
    val tipoRecorrido: String
)
data class DispatchTripStateDto(
    val tripId: Long,
    val direction: String,
    val tipoRecorrido: String,
    val active: Boolean
)

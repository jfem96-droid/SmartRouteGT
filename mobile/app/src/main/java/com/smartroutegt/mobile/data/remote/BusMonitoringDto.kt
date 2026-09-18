package com.smartroutegt.mobile.data.remote

data class BusMonitoringDto(
    val busId: Long,
    val codigo: String,
    val placa: String,
    val capacidad: Int,
    val activo: Boolean,
    val driverId: Long?,
    val piloto: String?,
    val routeId: Long?,
    val ruta: String?,
    val tripId: Long?,
    val viajeActivo: Boolean,
    val latitud: Double?,
    val longitud: Double?,
    val velocidad: Double?,
    val ultimaActualizacion: String?
)
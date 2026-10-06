package com.smartroutegt.mobile.network

data class DriverDto(
    val id: Long,
    val nombre: String,
    val licencia: String,
    val telefono: String,
    val sancionesConfirmadas: Int,
    val activo: Boolean
)

data class BusDto(
    val id: Long,
    val codigo: String,
    val placa: String,
    val capacidad: Int,
    val activa: Boolean
)

data class RouteDto(
    val id: Long,
    val codigo: String,
    val nombre: String,
    val activa: Boolean
)

data class DriverRouteAssignmentDto(
    val id: Long,
    val driver: DriverDto,
    val bus: BusDto,
    val route: RouteDto,
    val activa: Boolean
)

data class TripDto(
    val id: Long,
    val driver: DriverDto,
    val bus: BusDto,
    val route: RouteDto,
    val fechaInicio: String?,
    val fechaFin: String?,
    val activo: Boolean
)

data class BusLocationDto(
    val id: Long,
    val trip: TripDto,
    val bus: BusDto,
    val latitud: Double,
    val longitud: Double,
    val velocidad: Double,
    val fechaHora: String?
)

data class StopDto(
    val id: Long,
    val nombre: String,
    val latitud: Double,
    val longitud: Double,
    val radioPermitido: Double,
    val activa: Boolean
)

data class RouteStopDto(
    val id: Long,
    val route: RouteDto,
    val stop: StopDto,
    val orden: Int
)

data class NextStopDto(
    val tripId: Long,
    val stopId: Long,
    val parada: String,
    val orden: Int,
    val latitud: Double,
    val longitud: Double,
    val radioPermitido: Double,
    val distanciaMetros: Double?,
    val velocidadActualKmh: Double? = null,
    val velocidadCalculoKmh: Double? = null,
    val etaSegundos: Long? = null,
    val etaMinutos: Long? = null,
    val estadoEta: String? = null,
    val ultimaActualizacion: String? = null
)

package com.smartroutegt.mobile.network

import com.google.gson.Gson

class DriverApiService(
    private val api: SmartRouteApi
) {

    suspend fun getDriverByLicense(
        licencia: String
    ): DriverDto {
        return api.getDriverByLicense(licencia)
    }

    suspend fun getActiveAssignment(
        driverId: Long
    ): DriverRouteAssignmentDto? {
        return api.getAssignmentsByDriver(driverId)
            .firstOrNull { assignment -> assignment.activa }
    }

    suspend fun getActiveTrip(
        driverId: Long
    ): TripDto? {
        val response = api.getActiveTripByDriver(driverId)

        return when {
            response.isSuccessful -> response.body()
            response.code() == 404 -> null
            else -> throw IllegalStateException(
                "Error al consultar viaje activo: HTTP ${response.code()}"
            )
        }
    }

    suspend fun startTrip(
        assignmentId: Long
    ): TripDto {
        return api.startTrip(assignmentId)
    }

    suspend fun finishTrip(
        tripId: Long
    ): TripDto {
        return api.finishTrip(tripId)
    }

    suspend fun registerLocation(
        tripId: Long,
        latitud: Double,
        longitud: Double,
        velocidad: Double
    ): BusLocationDto {
        return api.registerLocation(
            tripId = tripId,
            latitud = latitud,
            longitud = longitud,
            velocidad = velocidad
        )
    }

    suspend fun getRouteStops(
        routeId: Long
    ): List<RouteStopDto> {
        return api.getRouteStops(routeId)
    }

    suspend fun getNextStop(
        tripId: Long
    ): NextStopDto? {
        val response = api.getNextStop(tripId)

        if (!response.isSuccessful) {
            throw IllegalStateException(
                "Error al consultar próxima parada: HTTP ${response.code()}"
            )
        }

        val responseText = response.body()?.string()

        if (responseText.isNullOrBlank() ||
            responseText == "No hay paradas pendientes"
        ) {
            return null
        }

        return Gson().fromJson(
            responseText,
            NextStopDto::class.java
        )
    }
}

package com.smartroutegt.mobile.network

import com.google.gson.Gson

class DriverApiService(
    private val api: SmartRouteApi
) {

    suspend fun getDriverByLicense(
        licencia: String
    ): DriverDto {
        android.util.Log.d(
            "SmartRouteDriver",
            "Consultando piloto por licencia"
        )

        return try {
            api.getDriverByLicense(licencia)
        } catch (exception: retrofit2.HttpException) {
            android.util.Log.e(
                "SmartRouteDriver",
                "getDriverByLicense -> HTTP ${exception.code()}",
                exception
            )
            throw exception
        }
    }

    suspend fun getActiveAssignment(
        driverId: Long
    ): DriverRouteAssignmentDto? {
        android.util.Log.d(
            "SmartRouteDriver",
            "Consultando asignaciones del driverId=$driverId"
        )

        return try {
            api.getAssignmentsByDriver(driverId)
                .firstOrNull { assignment -> assignment.activa }
        } catch (exception: retrofit2.HttpException) {
            android.util.Log.e(
                "SmartRouteDriver",
                "getActiveAssignment -> HTTP ${exception.code()} driverId=$driverId",
                exception
            )
            throw exception
        }
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

    suspend fun getDispatchStatus(driverId: Long): DispatchStatusDto = api.getDispatchStatus(driverId)
    suspend fun getDispatchTripState(tripId: Long): DispatchTripStateDto = api.getDispatchTripState(tripId)
    suspend fun nextLeg(tripId: Long): TripDto = api.nextLeg(tripId)

    suspend fun registerLocation(
        tripId: Long,
        latitud: Double,
        longitud: Double,
        velocidad: Double
    ): DriverSafetyResponseDto {
        return api.registerLocation(
            tripId = tripId,
            latitud = latitud,
            longitud = longitud,
            velocidad = velocidad
        )
    }

    suspend fun createDriverReport(request: DriverReportCreateDto): DriverReportDto = api.createDriverReport(request)
    suspend fun getMyDriverReports(): List<DriverReportDto> = api.getMyDriverReports()

    suspend fun getViolationsByDriver(
        driverId: Long
    ): List<ViolationDto> {
        return api.getViolationsByDriver(driverId)
    }

    suspend fun getRouteStops(
        routeId: Long,
        direction: String? = null
    ): List<RouteStopDto> {
        return api.getRouteStops(routeId, direction)
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

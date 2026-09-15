package com.smartroutegt.mobile.network

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
}
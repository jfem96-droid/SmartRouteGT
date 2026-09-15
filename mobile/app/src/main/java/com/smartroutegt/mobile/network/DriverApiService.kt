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
}
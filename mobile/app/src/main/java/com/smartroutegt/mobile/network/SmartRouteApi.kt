package com.smartroutegt.mobile.network

import retrofit2.http.GET
import retrofit2.http.POST
import retrofit2.http.PUT
import retrofit2.http.Path
import retrofit2.http.Query
import retrofit2.Response
import okhttp3.ResponseBody

interface SmartRouteApi {

    @GET("api/drivers/license/{licencia}")
    suspend fun getDriverByLicense(
        @Path("licencia") licencia: String
    ): DriverDto

    @GET("api/assignments/driver/{driverId}")
    suspend fun getAssignmentsByDriver(
        @Path("driverId") driverId: Long
    ): List<DriverRouteAssignmentDto>

    @GET("api/trips/driver/{driverId}/active")
    suspend fun getActiveTripByDriver(
        @Path("driverId") driverId: Long
    ): Response<TripDto>

    @POST("api/trips/start")
    suspend fun startTrip(
        @Query("assignmentId") assignmentId: Long
    ): TripDto

    @PUT("api/trips/{id}/finish")
    suspend fun finishTrip(
        @Path("id") tripId: Long
    ): TripDto

    @POST("api/locations")
    suspend fun registerLocation(
        @Query("tripId") tripId: Long,
        @Query("latitud") latitud: Double,
        @Query("longitud") longitud: Double,
        @Query("velocidad") velocidad: Double
    ): BusLocationDto

    @GET("api/route-stops/route")
    suspend fun getRouteStops(
        @Query("routeId") routeId: Long
    ): List<RouteStopDto>

    @GET("api/trips/{tripId}/next-stop")
    suspend fun getNextStop(
        @Path("tripId") tripId: Long
    ): Response<ResponseBody>
}

package com.smartroutegt.mobile.network

import retrofit2.http.GET
import retrofit2.http.POST
import retrofit2.http.PUT
import retrofit2.http.Path
import retrofit2.http.Query

interface SmartRouteApi {

    @GET("api/assignments/driver/{driverId}")
    suspend fun getAssignmentsByDriver(
        @Path("driverId") driverId: Long
    ): List<DriverRouteAssignmentDto>

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
}
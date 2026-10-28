package com.smartroutegt.mobile.data.remote

import retrofit2.http.GET
import retrofit2.http.Path
import retrofit2.http.Query

data class RouteGeometryPointDto(
    val latitud: Double,
    val longitud: Double
)

data class RouteGeometryResponseDto(
    val routeId: Long,
    val distanceMeters: Double,
    val durationSeconds: Double,
    val points: List<RouteGeometryPointDto>
)

interface RouteApiService {

    @GET("api/routes/{routeId}/itineraries")
    suspend fun getItineraries(@Path("routeId") routeId: Long): List<JourneyOptionDto>

    @GET("api/routes/{routeId}/journey-options")
    suspend fun getJourneyOptions(@Path("routeId") routeId: Long,
        @Query("originStopId") originStopId: Long,
        @Query("destinationStopId") destinationStopId: Long): List<JourneyOptionDto>

    @GET("api/routes")
    suspend fun getRoutes(): List<RouteDto>

    @GET("api/routes/{routeId}/geometry")
    suspend fun getRouteGeometry(
        @Path("routeId") routeId: Long,
        @Query("originStopId") originStopId: Long? = null,
        @Query("destinationStopId") destinationStopId: Long? = null,
        @Query("direction") direction: String? = null
    ): RouteGeometryResponseDto
}

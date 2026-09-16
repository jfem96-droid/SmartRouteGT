package com.smartroutegt.mobile.data.remote

import retrofit2.http.GET
import retrofit2.http.Query

interface RouteStopApiService {

    @GET("api/route-stops/route")
    suspend fun getStopsByRoute(
        @Query("routeId") routeId: Long
    ): List<RouteStopDto>
}
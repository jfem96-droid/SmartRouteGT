package com.smartroutegt.mobile.data.remote

import retrofit2.http.GET
import retrofit2.http.Query

interface NearestBusApiService {

    @GET("api/algorithms/nearest-bus")
    suspend fun getNearestBus(
        @Query("latitud") latitud: Double,
        @Query("longitud") longitud: Double
    ): BusLocationDto?
}
package com.smartroutegt.mobile.data.remote

import retrofit2.http.GET
import retrofit2.http.Query

interface AlgorithmApiService {

    @GET("api/algorithms/route/dijkstra")
    suspend fun getRoutePath(
        @Query("routeId") routeId: Long,
        @Query("origen") origen: Long,
        @Query("destino") destino: Long
    ): List<Long>
}
package com.smartroutegt.mobile.data.remote

import retrofit2.http.GET
import retrofit2.http.Query
import retrofit2.Response
import okhttp3.ResponseBody

interface NearestBusApiService {

    @GET("api/algorithms/nearest-bus")
    suspend fun getNearestBus(
        @Query("latitud") latitud: Double,
        @Query("longitud") longitud: Double
    ): Response<ResponseBody>
}
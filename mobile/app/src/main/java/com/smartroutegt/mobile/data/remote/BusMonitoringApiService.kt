package com.smartroutegt.mobile.data.remote

import retrofit2.http.GET

interface BusMonitoringApiService {

    @GET("api/passenger/buses")
    suspend fun getBuses(): List<BusMonitoringDto>
}
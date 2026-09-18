package com.smartroutegt.mobile.data.remote

import retrofit2.http.GET

interface BusMonitoringApiService {

    @GET("api/monitoring/buses")
    suspend fun getBuses(): List<BusMonitoringDto>
}
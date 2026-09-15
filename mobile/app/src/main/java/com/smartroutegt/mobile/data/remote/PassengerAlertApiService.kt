package com.smartroutegt.mobile.data.remote

import retrofit2.http.Body
import retrofit2.http.POST

interface PassengerAlertApiService {

    @POST("api/passenger-alerts")
    suspend fun createAlert(
        @Body alert: PassengerAlertRequest
    ): PassengerAlertDto
}
package com.smartroutegt.mobile.data.remote

import com.smartroutegt.mobile.auth.SessionManager
import okhttp3.OkHttpClient
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory

object ApiClient {

    private const val BASE_URL = "http://10.0.2.2:8080/"

    private val authenticatedClient: OkHttpClient by lazy {
        OkHttpClient.Builder()
            .addInterceptor { chain ->
                val originalRequest = chain.request()

                val requestBuilder = originalRequest
                    .newBuilder()

                SessionManager
                    .getAuthorizationHeader()
                    ?.let { authorization ->
                        requestBuilder.header(
                            "Authorization",
                            authorization
                        )
                    }

                chain.proceed(
                    requestBuilder.build()
                )
            }
            .build()
    }

    val retrofit: Retrofit by lazy {
        Retrofit.Builder()
            .baseUrl(BASE_URL)
            .client(authenticatedClient)
            .addConverterFactory(
                GsonConverterFactory.create()
            )
            .build()
    }

    val routeApiService: RouteApiService by lazy {
        retrofit.create(
            RouteApiService::class.java
        )
    }

    val routeStopApiService: RouteStopApiService by lazy {
        retrofit.create(
            RouteStopApiService::class.java
        )
    }

    val algorithmApiService: AlgorithmApiService by lazy {
        retrofit.create(
            AlgorithmApiService::class.java
        )
    }

    val nearestBusApiService: NearestBusApiService by lazy {
        retrofit.create(
            NearestBusApiService::class.java
        )
    }

    val passengerAlertApiService: PassengerAlertApiService by lazy {
        retrofit.create(
            PassengerAlertApiService::class.java
        )
    }

    val busMonitoringApiService: BusMonitoringApiService by lazy {
        retrofit.create(
            BusMonitoringApiService::class.java
        )
    }
}

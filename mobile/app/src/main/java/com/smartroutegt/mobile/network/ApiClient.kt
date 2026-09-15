package com.smartroutegt.mobile.network

import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory

object ApiClient {

    private const val DEVELOPMENT_BASE_URL = "http://10.0.2.2:8080/"

    fun create(
        baseUrl: String = DEVELOPMENT_BASE_URL
    ): SmartRouteApi {
        return Retrofit.Builder()
            .baseUrl(baseUrl)
            .addConverterFactory(GsonConverterFactory.create())
            .build()
            .create(SmartRouteApi::class.java)
    }
}
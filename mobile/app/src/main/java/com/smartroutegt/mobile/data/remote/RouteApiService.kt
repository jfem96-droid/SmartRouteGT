package com.smartroutegt.mobile.data.remote

import retrofit2.http.GET

interface RouteApiService {

    @GET("api/routes")
    suspend fun getRoutes(): List<RouteDto>
}
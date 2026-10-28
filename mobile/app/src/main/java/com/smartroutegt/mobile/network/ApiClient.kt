package com.smartroutegt.mobile.network

import com.smartroutegt.mobile.auth.AuthApiService
import com.smartroutegt.mobile.auth.SessionManager
import okhttp3.OkHttpClient
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory

object ApiClient {

    private const val DEVELOPMENT_BASE_URL = "https://smartroutegt.duckdns.org/"

    private val authenticatedClient: OkHttpClient by lazy {
        OkHttpClient.Builder()
            .addInterceptor { chain ->

                val originalRequest = chain.request()

                val requestBuilder = originalRequest
                    .newBuilder()

                SessionManager.getAuthorizationHeader()?.let { authorization ->
                    requestBuilder.header(
                        "Authorization",
                        authorization
                    )
                }

                chain.proceed(requestBuilder.build())
            }
            .build()
    }

    private fun retrofit(
        baseUrl: String = DEVELOPMENT_BASE_URL
    ): Retrofit {
        return Retrofit.Builder()
            .baseUrl(baseUrl)
            .client(authenticatedClient)
            .addConverterFactory(GsonConverterFactory.create())
            .build()
    }

    fun create(
        baseUrl: String = DEVELOPMENT_BASE_URL
    ): SmartRouteApi {
        return retrofit(baseUrl)
            .create(SmartRouteApi::class.java)
    }

    fun createAuthApi(
        baseUrl: String = DEVELOPMENT_BASE_URL
    ): AuthApiService {
        return retrofit(baseUrl)
            .create(AuthApiService::class.java)
    }
}

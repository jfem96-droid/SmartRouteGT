package com.smartroutegt.mobile.data.remote
import retrofit2.http.*
import retrofit2.Response

data class PassengerProfileDto(val nombre:String,val email:String,val telefono:String?)
data class PassengerProfileUpdate(val nombre:String,val email:String,val telefono:String,val passwordActual:String)
data class PassengerPasswordUpdate(val actual:String,val nueva:String)
interface PassengerProfileApi {
 @GET("api/passenger/profile") suspend fun get():PassengerProfileDto
 @PUT("api/passenger/profile") suspend fun update(@Body data:PassengerProfileUpdate):Response<PassengerProfileDto>
 @PUT("api/passenger/profile/password") suspend fun password(@Body data:PassengerPasswordUpdate):Response<okhttp3.ResponseBody>
}

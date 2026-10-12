package com.smartroutegt.mobile.data.remote

import com.smartroutegt.mobile.network.TripDto
import com.smartroutegt.mobile.network.StopDto
import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.POST
import retrofit2.http.Path
import retrofit2.http.Query

data class PassengerReportPayload(val tipo:String,val descripcion:String,val tripId:Long,val latitud:Double,val longitud:Double)
data class PassengerFeedbackPayload(val tripId:Long,val kind:String,val stars:Int?,val reason:String,val comment:String,val latitud:Double,val longitud:Double)
data class DriverBoardingCode(val code:String)
data class NearbyBus(val tripId:Long,val bus:String,val route:String,val distanceMeters:Int)
data class NearbyJoin(val tripId:Long,val latitud:Double,val longitud:Double)
data class BoardingJoin(val code:String,val latitud:Double,val longitud:Double)
data class BoardingPosition(val latitud:Double,val longitud:Double)
data class BoardingView(val active:Boolean=false,val tripId:Long?=null,val bus:String?=null,val route:String?=null,val separatedAt:String?=null)
interface PassengerV3Api {
 @GET("api/boarding/trip/{tripId}/code") suspend fun driverBoardingCode(@Path("tripId") tripId:Long):DriverBoardingCode
 @POST("api/boarding/nearby") suspend fun nearbyBuses(@Body body:BoardingPosition):List<NearbyBus>
 @POST("api/boarding/join-trip") suspend fun joinSelectedTrip(@Body body:NearbyJoin):retrofit2.Response<BoardingView>
 @POST("api/boarding/join-nearby") suspend fun joinNearby(@Body body:NearbyJoin):retrofit2.Response<BoardingView>
 @POST("api/boarding/leave") suspend fun leaveBoarding():retrofit2.Response<BoardingView>
 @GET("api/boarding/me") suspend fun myBoarding():BoardingView
 @POST("api/boarding/join") suspend fun joinBoarding(@Body body:BoardingJoin):retrofit2.Response<BoardingView>
 @POST("api/boarding/location") suspend fun boardingLocation(@Body body:BoardingPosition):retrofit2.Response<BoardingView>

 @GET("api/trips/active") suspend fun trips():List<TripDto>
 @GET("api/stops") suspend fun stops():List<StopDto>
 @POST("api/passenger-feedback") suspend fun feedback(@Body body:PassengerFeedbackPayload):retrofit2.Response<okhttp3.ResponseBody>
 @POST("api/passenger-reports") suspend fun report(@Body body:PassengerReportPayload):retrofit2.Response<okhttp3.ResponseBody>
 @GET("api/trips/{tripId}/eta-to-stop") suspend fun eta(@Path("tripId") tripId:Long,@Query("stopId") stopId:Long):retrofit2.Response<com.google.gson.JsonObject>
}

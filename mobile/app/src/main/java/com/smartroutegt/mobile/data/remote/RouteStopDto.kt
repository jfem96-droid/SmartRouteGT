package com.smartroutegt.mobile.data.remote

data class RouteStopDto(
    val id: Long,
    val route: RouteDto,
    val stop: StopDto,
    val orden: Int
)
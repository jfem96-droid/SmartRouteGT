package com.smartroutegt.mobile.network

data class DriverSafetyResponseDto(
    val location: BusLocationDto,
    val speed: DriverSpeedStatusDto?,
    val stop: DriverStopStatusDto?
)

data class DriverSpeedStatusDto(
    val currentKmh: Double,
    val limitKmh: Double,
    val warning: Boolean,
    val violationCreated: Boolean
)

data class DriverStopStatusDto(
    val stopId: Long?,
    val stopName: String?,
    val status: String?,
    val distanceMeters: Double?,
    val requiredSeconds: Int?
)

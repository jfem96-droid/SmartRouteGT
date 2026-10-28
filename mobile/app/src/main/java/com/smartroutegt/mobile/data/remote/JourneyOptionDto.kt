package com.smartroutegt.mobile.data.remote

data class JourneyOptionDto(
    val direction: String,
    val stopIds: List<Long>,
    val stops: List<StopDto>
)

package com.smartroutegt.mobile.data.remote

data class BusLocationDto(
    val id: Long,
    val bus: BusDto,
    val latitud: Double,
    val longitud: Double,
    val velocidad: Double,
    val fechaHora: String?
)
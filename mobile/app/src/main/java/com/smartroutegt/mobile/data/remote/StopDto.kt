package com.smartroutegt.mobile.data.remote

data class StopDto(
    val id: Long,
    val nombre: String,
    val latitud: Double,
    val longitud: Double,
    val radioPermitido: Double,
    val activa: Boolean
)
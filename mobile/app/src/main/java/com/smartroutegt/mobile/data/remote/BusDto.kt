package com.smartroutegt.mobile.data.remote

data class BusDto(
    val id: Long,
    val codigo: String,
    val placa: String,
    val capacidad: Int,
    val activa: Boolean
)
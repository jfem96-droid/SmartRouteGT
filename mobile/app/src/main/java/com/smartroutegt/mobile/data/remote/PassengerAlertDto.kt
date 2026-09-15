package com.smartroutegt.mobile.data.remote

data class PassengerAlertDto(
    val id: Long,
    val tipo: String,
    val descripcion: String?,
    val latitud: Double?,
    val longitud: Double?,
    val fechaHora: String?,
    val estado: String
)
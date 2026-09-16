package com.smartroutegt.mobile.data.remote

data class PassengerAlertRequest(
    val tipo: String,
    val descripcion: String,
    val latitud: Double?,
    val longitud: Double?
)
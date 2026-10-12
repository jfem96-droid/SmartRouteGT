package com.smartroutegt.mobile.network

data class ViolationDto(
    val id: Long,
    val tipo: String,
    val descripcion: String,
    val velocidadRegistrada: Double?,
    val limitePermitido: Double?,
    val fechaHora: String?,
    val estado: String,
    val bus: ViolationBusDto?
)

data class ViolationBusDto(
    val id: Long?,
    val codigo: String?,
    val placa: String?
)

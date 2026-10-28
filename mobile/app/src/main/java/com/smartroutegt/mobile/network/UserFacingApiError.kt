package com.smartroutegt.mobile.network

import java.net.ConnectException
import java.net.SocketTimeoutException
import java.net.UnknownHostException
import java.io.IOException
import kotlinx.coroutines.CancellationException
import retrofit2.HttpException

/** Mensajes claros sin atribuir errores 5xx al Internet del usuario. */
fun userFacingApiError(error: Throwable): String {
    if (error is CancellationException) throw error
    return when(error) {
        is UnknownHostException, is ConnectException -> "Sin conexión a Internet o al servidor. Comprueba tu red e inténtalo de nuevo."
        is SocketTimeoutException -> "La conexión tardó demasiado. Comprueba tu señal e inténtalo nuevamente."
        is HttpException -> when(error.code()) {
            400, 422 -> "El recorrido solicitado no es válido o no está disponible. Revisa origen, destino y sentido."
            401 -> "Tu sesión ha caducado. Vuelve a iniciar sesión."
            403 -> "No tienes permiso para realizar esta operación."
            404 -> "El servicio o registro solicitado ya no está disponible."
            409 -> "La operación no puede completarse en el estado actual. Actualiza e inténtalo de nuevo."
            in 500..599 -> "El servicio de SmartRoute GT presenta un problema temporal. Intenta nuevamente más tarde."
            else -> "La solicitud no pudo completarse (código ${error.code()})."
        }
        is IOException -> "Problema de conexión. Comprueba tus datos móviles o Wi-Fi e inténtalo de nuevo."
        else -> "No fue posible completar la operación. Intenta nuevamente."
    }
}

package com.smartroutegt.mobile.auth

import com.smartroutegt.mobile.network.ApiClient

class AuthRepository {

    private val api = ApiClient.createAuthApi()

    suspend fun login(
        email: String,
        password: String
    ): LoginResponse {

        val response = api.login(
            LoginRequest(
                email = email.trim(),
                password = password
            )
        )

        SessionManager.saveSession(response)

        return response
    }

    suspend fun register(
        nombre: String,
        email: String,
        telefono: String,
        password: String
    ): RegisterResponse {

        return api.register(
            RegisterRequest(
                nombre = nombre.trim(),
                email = email.trim(),
                telefono = telefono.trim(),
                password = password,
                role = "PASSENGER"
            )
        )
    }

    suspend fun verifyEmail(
        email: String,
        code: String
    ): MessageResponse {

        return api.verifyEmail(
            VerifyEmailRequest(
                email = email.trim(),
                code = code.trim()
            )
        )
    }

    suspend fun resendCode(
        email: String
    ): MessageResponse {

        return api.resendCode(
            EmailRequest(email.trim())
        )
    }

    suspend fun forgotPassword(
        email: String
    ): MessageResponse {

        return api.forgotPassword(
            EmailRequest(email.trim())
        )
    }

    suspend fun resetPassword(
        email: String,
        code: String,
        newPassword: String
    ): MessageResponse {

        return api.resetPassword(
            ResetPasswordRequest(
                email = email.trim(),
                code = code.trim(),
                newPassword = newPassword
            )
        )
    }

    fun logout() {
        SessionManager.clear()
    }
}

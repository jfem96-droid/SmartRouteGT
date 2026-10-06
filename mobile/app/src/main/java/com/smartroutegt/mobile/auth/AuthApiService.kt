package com.smartroutegt.mobile.auth

import retrofit2.http.Body
import retrofit2.http.POST

interface AuthApiService {

    @POST("api/auth/login")
    suspend fun login(
        @Body request: LoginRequest
    ): LoginResponse

    @POST("api/auth/register")
    suspend fun register(
        @Body request: RegisterRequest
    ): RegisterResponse

    @POST("api/auth/verify-email")
    suspend fun verifyEmail(
        @Body request: VerifyEmailRequest
    ): MessageResponse

    @POST("api/auth/resend-code")
    suspend fun resendCode(
        @Body request: EmailRequest
    ): MessageResponse

    @POST("api/auth/forgot-password")
    suspend fun forgotPassword(
        @Body request: EmailRequest
    ): MessageResponse

    @POST("api/auth/reset-password")
    suspend fun resetPassword(
        @Body request: ResetPasswordRequest
    ): MessageResponse
}

data class RegisterRequest(
    val nombre: String,
    val email: String,
    val telefono: String,
    val password: String,
    val role: String = "PASSENGER"
)

data class RegisterResponse(
    val id: Long? = null,
    val nombre: String? = null,
    val email: String? = null,
    val role: String? = null,
    val emailVerified: Boolean? = null,
    val verificationRequired: Boolean? = null,
    val message: String? = null
)

data class VerifyEmailRequest(
    val email: String,
    val code: String
)

data class EmailRequest(
    val email: String
)

data class ResetPasswordRequest(
    val email: String,
    val code: String,
    val newPassword: String
)

data class MessageResponse(
    val message: String? = null,
    val sent: Boolean? = null,
    val verified: Boolean? = null,
    val changed: Boolean? = null
)

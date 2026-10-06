package com.smartroutegt.mobile.auth

import android.content.Context

object SessionManager {

    private const val PREFS_NAME = "smartroute_session"
    private const val KEY_TOKEN = "jwt_token"
    private const val KEY_TOKEN_TYPE = "token_type"
    private const val KEY_USER_ID = "user_id"
    private const val KEY_NAME = "name"
    private const val KEY_EMAIL = "email"
    private const val KEY_ROLE = "role"
    private const val KEY_DRIVER_ID = "driver_id"
    private const val KEY_LICENSE = "license"

    private var appContext: Context? = null

    fun init(context: Context) {
        appContext = context.applicationContext
    }

    private fun prefs() =
        requireNotNull(appContext) {
            "SessionManager debe inicializarse antes de utilizarse"
        }.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)

    fun saveSession(response: LoginResponse) {
        prefs().edit()
            .putString(KEY_TOKEN, response.token)
            .putString(KEY_TOKEN_TYPE, response.tokenType)
            .putLong(KEY_USER_ID, response.id)
            .putString(KEY_NAME, response.nombre)
            .putString(KEY_EMAIL, response.email)
            .putString(KEY_ROLE, response.role)
            .apply {
                if (response.driverId != null) {
                    putLong(KEY_DRIVER_ID, response.driverId)
                } else {
                    remove(KEY_DRIVER_ID)
                }

                if (response.licencia != null) {
                    putString(KEY_LICENSE, response.licencia)
                } else {
                    remove(KEY_LICENSE)
                }
            }
            .apply()
    }

    fun getToken(): String? =
        prefs().getString(KEY_TOKEN, null)

    fun getAuthorizationHeader(): String? {
        val token = getToken() ?: return null
        val type = prefs().getString(KEY_TOKEN_TYPE, "Bearer") ?: "Bearer"
        return "$type $token"
    }

    fun getRole(): String? =
        prefs().getString(KEY_ROLE, null)

    fun getDriverId(): Long? {
        val preferences = prefs()

        return if (preferences.contains(KEY_DRIVER_ID)) {
            preferences.getLong(KEY_DRIVER_ID, -1L)
                .takeIf { it > 0 }
        } else {
            null
        }
    }

    fun getLicense(): String? =
        prefs().getString(KEY_LICENSE, null)

    fun getUserName(): String? =
        prefs().getString(KEY_NAME, null)

    fun isLoggedIn(): Boolean =
        !getToken().isNullOrBlank()

    fun clear() {
        prefs().edit().clear().apply()
    }
}

data class LoginRequest(
    val email: String,
    val password: String
)

data class LoginResponse(
    val token: String,
    val tokenType: String,
    val expiresIn: Long,
    val id: Long,
    val nombre: String,
    val email: String,
    val role: String,
    val driverId: Long? = null,
    val licencia: String? = null
)

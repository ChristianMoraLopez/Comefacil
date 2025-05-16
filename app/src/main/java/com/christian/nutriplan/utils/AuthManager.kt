package com.christian.nutriplan.utils

import android.content.Context
import android.content.pm.ApplicationInfo
import androidx.core.content.edit
import androidx.security.crypto.EncryptedSharedPreferences
import androidx.security.crypto.MasterKey
import com.auth0.android.jwt.JWT
import com.auth0.jwt.JWTVerifier
import com.auth0.jwt.algorithms.Algorithm
import timber.log.Timber

object AuthManager {
    // Configuración JWT (debe coincidir con tu backend)
    private const val JWT_SECRET = "UnSecretoDePruebaSuficientementeLargo12345" // <-- SECRET del backend
    private const val JWT_ISSUER = "nutriplan-api"
    private const val JWT_AUDIENCE = "nutriplan-users"
    private const val JWT_REALM = "NutriPlan App"

    private val jwtAlgorithm = Algorithm.HMAC256(JWT_SECRET)
    private val jwtVerifier: JWTVerifier = com.auth0.jwt.JWT
        .require(jwtAlgorithm)
        .withIssuer(JWT_ISSUER)
        .withAudience(JWT_AUDIENCE)
        .build()

    // Preferencias seguras
    private const val PREFS_NAME = "NutriPlanSecurePrefs"
    private const val ACCESS_TOKEN_KEY = "secure_access_token"
    private const val REFRESH_TOKEN_KEY = "secure_refresh_token"
    private const val USER_ID_KEY = "secure_user_id"
    private const val TOKEN_EXPIRATION_KEY = "token_expiration"

    private fun getSecurePreferences(context: Context) = try {
        if (context.applicationInfo != null &&
            (context.applicationInfo.flags and ApplicationInfo.FLAG_DEBUGGABLE) != 0) {
            // Modo debug
            context.getSharedPreferences("${PREFS_NAME}_debug", Context.MODE_PRIVATE)
        } else {
            val masterKey = MasterKey.Builder(context)
                .setKeyScheme(MasterKey.KeyScheme.AES256_GCM)
                .build()

            EncryptedSharedPreferences.create(
                context,
                PREFS_NAME,
                masterKey,
                EncryptedSharedPreferences.PrefKeyEncryptionScheme.AES256_SIV,
                EncryptedSharedPreferences.PrefValueEncryptionScheme.AES256_GCM
            )
        }
    } catch (e: Exception) {
        Timber.e(e, "Error creating secure preferences")
        context.getSharedPreferences("${PREFS_NAME}_fallback", Context.MODE_PRIVATE)
    }

    // Métodos de compatibilidad (para UserRepository)
    fun saveTokens(context: Context, accessToken: String, refreshToken: String) {
        saveAuthData(context, accessToken, refreshToken)
    }

    fun clearTokens(context: Context) {
        clearAuthData(context)
    }

    // Implementación principal
    fun saveAuthData(
        context: Context,
        accessToken: String,
        refreshToken: String,
        userId: String? = null
    ) {
        val jwt = try {
            JWT(accessToken)
        } catch (e: Exception) {
            Timber.e(e, "Invalid JWT format")
            return
        }

        val expiresAt = jwt.expiresAt?.time ?: System.currentTimeMillis() + 3600_000 // 1 hora por defecto

        getSecurePreferences(context).edit {
            putString(ACCESS_TOKEN_KEY, accessToken)
            putString(REFRESH_TOKEN_KEY, refreshToken)
            putLong(TOKEN_EXPIRATION_KEY, expiresAt)
            userId?.let { putString(USER_ID_KEY, it) }
        }
    }

    fun getValidatedAccessToken(context: Context): String? {
        val token = getAccessToken(context) ?: return null
        return if (verifyJWT(token)) token else null
    }

    fun getAccessToken(context: Context): String? {
        val prefs = getSecurePreferences(context)
        val token = prefs.getString(ACCESS_TOKEN_KEY, null)
        val expiration = prefs.getLong(TOKEN_EXPIRATION_KEY, 0L)

        return when {
            token == null -> null
            expiration <= System.currentTimeMillis() -> {
                Timber.w("Token expired")
                null
            }
            else -> token
        }
    }

    fun getRefreshToken(context: Context): String? {
        return getSecurePreferences(context).getString(REFRESH_TOKEN_KEY, null)
    }

    fun getUserId(context: Context): String? {
        return getSecurePreferences(context).getString(USER_ID_KEY, null)
    }

    fun clearAuthData(context: Context) {
        getSecurePreferences(context).edit {
            clear()
        }
    }

    fun isLoggedIn(context: Context): Boolean {
        return getValidatedAccessToken(context) != null && getRefreshToken(context) != null
    }

    fun saveAccessToken(context: Context, accessToken: String) {
        val jwt = try {
            JWT(accessToken)
        } catch (e: Exception) {
            Timber.e(e, "Invalid JWT format")
            return
        }

        val expiresAt = jwt.expiresAt?.time ?: System.currentTimeMillis() + 3600_000
        getSecurePreferences(context).edit {
            putString(ACCESS_TOKEN_KEY, accessToken)
            putLong(TOKEN_EXPIRATION_KEY, expiresAt)
        }
    }

    fun verifyJWT(token: String): Boolean {
        return try {
            // Verificación con el secret real
            jwtVerifier.verify(token)
            true
        } catch (e: Exception) {
            Timber.e(e, "JWT verification failed")
            false
        }
    }
}
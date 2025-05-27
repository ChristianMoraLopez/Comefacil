package com.christian.nutriplan.models.responses

import com.christian.nutriplan.models.Usuario
import kotlinx.serialization.Serializable

@Serializable
sealed class ApiResponse<out T> {
    @Serializable
    data class Success<out T>(
        val data: T,
        val message: String? = null,
        val status: String = "success"
    ) : ApiResponse<T>()

    @Serializable
    data class Error(
        val message: String,
        val error: String? = null,
        val status: String = "error"
    ) : ApiResponse<Nothing>()

    // Modelo que coincide con la respuesta real del servidor
    @Serializable
    data class LoginServerResponse(
        val token: String,  // String directo en lugar de objeto Tokens
        val usuario: Usuario
    )

    // Modelo para cuando necesites el formato con accessToken y refreshToken
    @Serializable
    data class LoginClientResponse(
        val usuario: Usuario,
        val tokens: Tokens
    )

    @Serializable
    data class RecetaGuardadaResponse(
        val guardadoId: Int? = null,
        val usuarioId: Int,
        val recetaId: Int,
        val fechaGuardado: String? = null,
        val comentarioPersonal: String? = null,
        val nombreReceta: String
    )

    @Serializable
    data class Tokens(
        val accessToken: String,
        val refreshToken: String
    )

    @Serializable
    data class RefreshTokenResponse(
        val accessToken: String,
        val refreshToken: String
    )
}
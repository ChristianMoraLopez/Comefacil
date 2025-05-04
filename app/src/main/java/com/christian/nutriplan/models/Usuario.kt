package com.christian.nutriplan.models

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import java.time.LocalDateTime

@Serializable
data class Usuario(
    val usuarioId: Int? = null,
    val nombre: String,
    val email: String,
    val contrasena: String,
    val aceptaTerminos: Boolean = false,
    val rol: String = "usuario",
    val fechaRegistro: String = LocalDateTime.now().toString()
)
@Serializable
data class UsuarioResponse(
    val usuarioId: Int,
    val nombre: String,
    val email: String,
    val rol: String,
    val fechaRegistro: String,
    val aceptaTerminos: Boolean
)

@Serializable
data class Credentials(
    val email: String,
    @SerialName("contrasena") val password: String
)
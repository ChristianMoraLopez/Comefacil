package com.christian.nutriplan.models

import java.time.LocalDateTime

data class Usuario(
    val usuarioId: Int? = null,
    val nombre: String,
    val email: String,
    val contrasena: String,
    val aceptaTerminos: Boolean = false,
    val rol: String = "usuario",
    val fechaRegistro: String = LocalDateTime.now().toString()
)

data class UsuarioResponse(
    val usuarioId: Int,
    val nombre: String,
    val email: String,
    val rol: String,
    val fechaRegistro: String,
    val aceptaTerminos: Boolean
)

data class Credentials(
    val email: String,
    val password: String
)

package com.christian.nutriplan.models

import kotlinx.serialization.Serializable

@Serializable
data class RecetaGuardada(
    val guardadoId: Int? = null,
    val usuarioId: Int,
    val recetaId: Int,
    val fechaGuardado: String? = null,
    val comentarioPersonal: String? = null,
    val nombreReceta: String
)
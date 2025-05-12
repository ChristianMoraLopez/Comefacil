package com.christian.nutriplan.models

import kotlinx.serialization.Serializable

@Serializable
data class RecetaGuardada(
    val recetaGuardadaId: Int?,
    val usuarioId: Int,
    val recetaId: Int
)
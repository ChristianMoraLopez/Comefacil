package com.christian.nutriplan.models

import kotlinx.serialization.Serializable

@Serializable
data class Receta(
    val recetaId: Int? = null,
    val nombre: String,
    val tipoComidaId: Int,
    val fit: Boolean = false,
    val instrucciones: String,
    val tiempoPreparacion: Int? = null,
    val disponibleBogota: Boolean = true,
    val metodoId: Int? = null
)

@Serializable
data class TipoComida(
    val tipoComidaId: Int? = null,
    val nombre: String
)

enum class MealType {
    BREAKFAST, LUNCH, DINNER, SNACK
}
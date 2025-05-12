package com.christian.nutriplan.models

import kotlinx.serialization.Serializable
import kotlinx.serialization.SerialName

@Serializable
data class RecetaIngrediente(
    @SerialName("recetaId") val recetaId: Int?,
    @SerialName("ingredienteId") val ingredienteId: Int?,
    @SerialName("cantidad") val cantidad: Double?,
    @SerialName("nombreIngrediente") val nombreIngrediente: String,
    @SerialName("unidad") val unidad: String?
)
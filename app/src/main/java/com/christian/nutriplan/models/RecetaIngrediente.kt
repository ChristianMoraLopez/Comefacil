package com.christian.nutriplan.models


data class RecetaIngrediente(
    val recetaId: Int?,
    val ingredienteId: Int?,
    val cantidad: Double?,
    val nombreIngrediente: String,
    val unidad: String?
)
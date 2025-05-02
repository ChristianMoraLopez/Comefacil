package com.christian.nutriplan.models

data class Ingrediente(
    val ingredienteId: Int? = null,
    val nombre: String,
    val categoriaId: Int,
    val calorias: Double? = null
)

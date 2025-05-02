package com.christian.nutriplan.models

data class SeleccionIngrediente(
    val seleccionId: Int? = null,
    val menuId: Int,
    val ingredienteId: Int,
    val cantidad: Double? = null
)

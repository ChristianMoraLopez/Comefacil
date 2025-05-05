package com.christian.nutriplan.models

import kotlinx.serialization.Serializable

@Serializable
data class Ciudad(
    val ciudadId: Int,
    val nombre: String
)

@Serializable
data class Localidad(
    val localidadId: Int,
    val nombre: String,
    val ciudadId: Int
)

@Serializable
data class LocationUpdate(
    val localidadId: Int
)
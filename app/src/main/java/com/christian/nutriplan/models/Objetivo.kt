package com.christian.nutriplan.models

import kotlinx.serialization.Contextual
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter


@Serializable
data class Objetivo(
    val objetivoId: Int? = null,
    val nombre: String,
    val tieneTiempo: Boolean,
    @Contextual
    val fechaCreacion: String? = LocalDateTime.now().format(DateTimeFormatter.ISO_LOCAL_DATE_TIME),
    val usuarioId: Int? = null
)

@Serializable
data class ErrorResponse(
    val message: String,
    val error: String
)
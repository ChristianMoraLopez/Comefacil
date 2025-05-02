package com.christian.nutriplan.models

import java.time.LocalDateTime

data class Menu(
    val menuId: Int? = null,
    val usuarioId: Int,
    val objetivoId: Int,
    val comidaId: Int,
    val fechaCreacion: String? = LocalDateTime.now().toString(),
    val metodoId: Int? = null
)

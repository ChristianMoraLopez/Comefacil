package com.christian.nutriplan.models.responses

import kotlinx.serialization.Serializable

@Serializable
sealed class ApiResponse<out T> {
    @Serializable
    data class Success<out T>(
        val data: T,
        val message: String? = null,
        val status: String = "success"
    ) : ApiResponse<T>()

    @Serializable
    data class Error(
        val message: String,
        val error: String? = null,
        val status: String = "error"
    ) : ApiResponse<Nothing>()
}

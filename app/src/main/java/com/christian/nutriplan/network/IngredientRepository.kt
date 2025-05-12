package com.christian.nutriplan.network

import android.util.Log
import com.christian.nutriplan.models.Ingrediente
import io.ktor.client.*
import io.ktor.client.call.*
import io.ktor.client.plugins.ClientRequestException
import io.ktor.client.plugins.HttpRequestTimeoutException
import io.ktor.client.plugins.ServerResponseException
import io.ktor.client.request.*
import io.ktor.client.network.sockets.ConnectTimeoutException
import io.ktor.http.*
import java.net.UnknownHostException

class IngredientRepository {
    private val TAG = "IngredientRepository"

    companion object {
        private const val INGREDIENTES_ENDPOINT = "/public/ingredientes"
    }

    suspend fun getIngredientes(): Result<List<Ingrediente>> {
        return try {
            val response = ApiClient.client.get("${ApiClient.BASE_URL}$INGREDIENTES_ENDPOINT")
            when (response.status) {
                HttpStatusCode.OK -> {
                    val ingredientes = response.body<List<Ingrediente>>()
                    Result.success(ingredientes)
                }
                else -> {
                    Result.failure(Exception("Error al obtener ingredientes: ${response.status}"))
                }
            }
        } catch (e: Exception) {
            Log.e(TAG, "Error en getIngredientes: ${e.message}", e)
            Result.failure(Exception(handleNetworkError(e)))
        }
    }

    private fun handleNetworkError(e: Exception): String {
        return when (e) {
            is ConnectTimeoutException ->
                "Tiempo de conexión agotado. Verifica tu conexión a internet."
            is UnknownHostException ->
                "No se pudo encontrar el servidor. Verifica la URL del servidor."
            is HttpRequestTimeoutException ->
                "La solicitud tardó demasiado. Intenta de nuevo."
            is ClientRequestException ->
                "Error del cliente: ${e.response.status}"
            is ServerResponseException ->
                "Error interno del servidor: ${e.response.status}"
            else -> "Error de red: ${e.message ?: "Desconocido"}"
        }
    }
}
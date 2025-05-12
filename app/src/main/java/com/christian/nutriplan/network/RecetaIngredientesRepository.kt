package com.christian.nutriplan.network

import android.util.Log
import com.christian.nutriplan.models.RecetaIngrediente
import io.ktor.client.*
import io.ktor.client.call.*
import io.ktor.client.network.sockets.*
import io.ktor.client.plugins.*
import io.ktor.client.request.*
import io.ktor.http.*
import io.ktor.serialization.JsonConvertException
import javax.inject.Inject
import java.net.UnknownHostException

class RecetaIngredientesRepository @Inject constructor(
    private val client: HttpClient
) {
    private val TAG = "RecetaIngredientesRepository"

    companion object {
        private const val RECETA_INGREDIENTES_ENDPOINT = "/receta_ingredientes/receta"
    }

    suspend fun getIngredientesForReceta(recetaId: Int): Result<List<RecetaIngrediente>> {
        return try {
            val response = client.get {
                url("${ApiClient.BASE_URL}$RECETA_INGREDIENTES_ENDPOINT/$recetaId")
                contentType(ContentType.Application.Json)
            }

            when (response.status) {
                HttpStatusCode.OK -> {
                    val ingredientes = response.body<List<RecetaIngrediente>>()
                    Log.d(TAG, "Ingredientes obtenidos: ${ingredientes.size}")
                    Result.success(ingredientes)
                }
                HttpStatusCode.Unauthorized -> {
                    Result.failure(Exception("No autorizado - Token inválido o expirado"))
                }
                else -> {
                    val errorMessage = try {
                        response.body<String>()
                    } catch (e: Exception) {
                        "Error al obtener ingredientes: ${response.status}"
                    }
                    Result.failure(Exception(errorMessage))
                }
            }
        } catch (e: Exception) {
            val errorMessage = handleNetworkError(e)
            Log.e(TAG, "Error en getIngredientesForReceta: $errorMessage", e)
            Result.failure(Exception(errorMessage))
        }
    }

    private fun handleNetworkError(e: Exception): String {
        return when (e) {
            is ConnectTimeoutException -> "Tiempo de conexión agotado. Intente nuevamente"
            is UnknownHostException -> "No se pudo conectar al servidor. Verifique su conexión"
            is HttpRequestTimeoutException -> "La solicitud tardó demasiado. Intente nuevamente"
            is ClientRequestException -> "Error en la solicitud: ${e.response.status.description}"
            is ServerResponseException -> "Error del servidor: ${e.response.status.description}"
            is JsonConvertException -> "Error al procesar los datos recibidos"
            else -> "Error de red: ${e.message ?: "Desconocido"}"
        }
    }
}
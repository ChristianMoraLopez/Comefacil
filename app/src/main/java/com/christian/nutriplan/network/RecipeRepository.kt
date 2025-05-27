package com.christian.nutriplan.network

import android.util.Log
import com.christian.nutriplan.models.Receta
import com.christian.nutriplan.models.TipoComida
import com.christian.nutriplan.models.RecetaGuardada
import io.ktor.client.*
import io.ktor.client.call.*
import io.ktor.client.plugins.ClientRequestException
import io.ktor.client.plugins.HttpRequestTimeoutException
import io.ktor.client.plugins.ServerResponseException
import io.ktor.client.request.*
import io.ktor.client.network.sockets.ConnectTimeoutException
import io.ktor.http.*
import java.net.UnknownHostException
import kotlin.String

class RecipeRepository {
    private val TAG = "RecipeRepository"

    companion object {
        private const val RECETAS_ENDPOINT = "/public/recetas"
        private const val TIPOS_COMIDA_ENDPOINT = "/public/tipos_comida"
        private const val UPDATE_TIPO_COMIDA_ENDPOINT = "/recetas"
        private const val RECETAS_GUARDADAS_ENDPOINT = "/recetas_guardadas"
    }

    suspend fun getRecetas(): Result<List<Receta>> {
        return try {
            val response = ApiClient.client.get("${ApiClient.BASE_URL}$RECETAS_ENDPOINT")
            when (response.status) {
                HttpStatusCode.OK -> {
                    val recetas = response.body<List<Receta>>()
                    Result.success(recetas)
                }
                else -> {
                    Result.failure(Exception("Error al obtener recetas: ${response.status}"))
                }
            }
        } catch (e: Exception) {
            Log.e(TAG, "Error en getRecetas: ${e.message}", e)
            Result.failure(Exception(handleNetworkError(e)))
        }
    }





    suspend fun getTiposComida(): Result<List<TipoComida>> {
        return try {
            val response = ApiClient.client.get("${ApiClient.BASE_URL}$TIPOS_COMIDA_ENDPOINT")
            when (response.status) {
                HttpStatusCode.OK -> {
                    val tiposComida = response.body<List<TipoComida>>()
                    Result.success(tiposComida)
                }
                else -> {
                    Result.failure(Exception("Error al obtener tipos de comida: ${response.status}"))
                }
            }
        } catch (e: Exception) {
            Log.e(TAG, "Error en getTiposComida: ${e.message}", e)
            Result.failure(Exception(handleNetworkError(e)))
        }
    }

    suspend fun updateTipoComida(recetaId: Int, tipoComidaId: Int, token: String): Result<Unit> {
        return try {
            val response = ApiClient.client.put("${ApiClient.BASE_URL}$UPDATE_TIPO_COMIDA_ENDPOINT/$recetaId/tipo_comida") {
                header("Authorization", "Bearer $token")
                contentType(ContentType.Application.Json)
                setBody(mapOf("tipoComidaId" to tipoComidaId))
            }
            when (response.status) {
                HttpStatusCode.OK -> Result.success(Unit)
                HttpStatusCode.Unauthorized -> Result.failure(Exception("No autorizado"))
                else -> Result.failure(Exception("Error al actualizar tipo de comida: ${response.status}"))
            }
        } catch (e: Exception) {
            Log.e(TAG, "Error en updateTipoComida: ${e.message}", e)
            Result.failure(Exception(handleNetworkError(e)))
        }
    }

    suspend fun saveFavoriteRecipe(recetaId: Int, userId: Int, token: String): Result<Unit> {
        return try {
            val recetaGuardada = RecetaGuardada(
                guardadoId = null, // El backend asignará el ID
                usuarioId = userId,
                recetaId = recetaId,
                fechaGuardado = null, // Set by backend
                comentarioPersonal = null,
                nombreReceta = String.toString()
            )
            val response = ApiClient.client.post("${ApiClient.BASE_URL}$RECETAS_GUARDADAS_ENDPOINT") {
                header("Authorization", "Bearer $token")
                contentType(ContentType.Application.Json)
                setBody(recetaGuardada)
            }
            when (response.status) {
                HttpStatusCode.Created -> Result.success(Unit)
                HttpStatusCode.Unauthorized -> Result.failure(Exception("No autorizado"))
                HttpStatusCode.Conflict -> Result.failure(Exception("La receta ya está guardada"))
                else -> Result.failure(Exception("Error al guardar receta: ${response.status}"))
            }
        } catch (e: Exception) {
            Log.e(TAG, "Error en saveFavoriteRecipe: ${e.message}", e)
            Result.failure(Exception(handleNetworkError(e)))
        }
    }

    suspend fun isRecipeSaved(recetaId: Int, userId: String, token: String): Boolean {
        return try {
            val response = ApiClient.client.get("${ApiClient.BASE_URL}$RECETAS_GUARDADAS_ENDPOINT") {
                header("Authorization", "Bearer $token")
                contentType(ContentType.Application.Json)
            }
            when (response.status) {
                HttpStatusCode.OK -> {
                    val savedRecipes = response.body<List<RecetaGuardada>>()
                    savedRecipes.any { it.recetaId == recetaId && it.usuarioId == userId.toIntOrNull() }
                }
                else -> {
                    Log.e(TAG, "Error checking saved recipe: ${response.status}")
                    false
                }
            }
        } catch (e: Exception) {
            Log.e(TAG, "Error in isRecipeSaved: ${e.message}", e)
            false
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
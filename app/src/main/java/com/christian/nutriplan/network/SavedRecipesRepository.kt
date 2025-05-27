package com.christian.nutriplan.network

import android.util.Log
import com.christian.nutriplan.models.RecetaGuardada
import com.christian.nutriplan.network.ApiClient.client
import io.ktor.client.*
import io.ktor.client.call.*
import io.ktor.client.network.sockets.*
import io.ktor.client.plugins.*
import io.ktor.client.request.*
import io.ktor.http.*
import io.ktor.serialization.JsonConvertException
import javax.inject.Inject
import java.net.UnknownHostException

class SavedRecipesRepository @Inject constructor(
    private val userRepository: UserRepository
) {
    private val TAG = "SavedRecipesRepository"

    companion object {
        private const val RECETAS_GUARDADAS_ENDPOINT = "/recetas_guardadas"
    }

    suspend fun saveRecipe(recetaGuardada: RecetaGuardada): Result<RecetaGuardada> {
        return userRepository.tryWithTokenRefresh {
            try {
                val response = ApiClient.client.post {
                    url("${ApiClient.BASE_URL}$RECETAS_GUARDADAS_ENDPOINT")
                    contentType(ContentType.Application.Json)
                    setBody(recetaGuardada)
                }
                when (response.status) {
                    HttpStatusCode.Created -> {
                        val savedRecipe = response.body<RecetaGuardada>()
                        Log.d(TAG, "Receta guardada creada: ${savedRecipe.guardadoId}")
                        Result.success(savedRecipe)
                    }
                    HttpStatusCode.Unauthorized -> {
                        Result.failure(Exception("No autorizado - Token inválido o expirado"))
                    }
                    HttpStatusCode.Forbidden -> {
                        Result.failure(Exception("No tienes permiso para guardar esta receta"))
                    }
                    else -> {
                        val errorMessage = try {
                            response.body<String>()
                        } catch (e: Exception) {
                            "Error al guardar receta: ${response.status}"
                        }
                        Result.failure(Exception(errorMessage))
                    }
                }
            } catch (e: Exception) {
                val errorMessage = handleNetworkError(e)
                Log.e(TAG, "Error en saveRecipe: $errorMessage", e)
                Result.failure(Exception(errorMessage))
            }
        }
    }
    suspend fun getSavedRecipes(userId: String, token: String): Result<List<RecetaGuardada>> {
        return try {
            val response = client.get {
                url("${ApiClient.BASE_URL}$RECETAS_GUARDADAS_ENDPOINT")
                header(HttpHeaders.Authorization, "Bearer $token")
                contentType(ContentType.Application.Json)
            }

            when (response.status) {
                HttpStatusCode.OK -> {
                    val savedRecipes = response.body<List<RecetaGuardada>>()
                    Log.d(TAG, "Recetas guardadas obtenidas: ${savedRecipes.size}")
                    Result.success(savedRecipes)
                }
                HttpStatusCode.Unauthorized -> {
                    Result.failure(Exception("No autorizado - Token inválido o expirado"))
                }
                else -> {
                    val errorMessage = try {
                        response.body<String>()
                    } catch (e: Exception) {
                        "Error al obtener recetas guardadas: ${response.status}"
                    }
                    Result.failure(Exception(errorMessage))
                }
            }
        } catch (e: Exception) {
            val errorMessage = handleNetworkError(e)
            Log.e(TAG, "Error en getSavedRecipes: $errorMessage", e)
            Result.failure(Exception(errorMessage))
        }
    }

    suspend fun deleteSavedRecipe(guardadoId: Int, token: String): Result<Unit> {
        return try {
            val response = client.delete {
                url("${ApiClient.BASE_URL}$RECETAS_GUARDADAS_ENDPOINT/$guardadoId")
                header(HttpHeaders.Authorization, "Bearer $token")
                contentType(ContentType.Application.Json)
            }

            when (response.status) {
                HttpStatusCode.OK -> {
                    Log.d(TAG, "Receta guardada eliminada: $guardadoId")
                    Result.success(Unit)
                }
                HttpStatusCode.Unauthorized -> {
                    Result.failure(Exception("No autorizado - Token inválido o expirado"))
                }
                else -> {
                    val errorMessage = try {
                        response.body<String>()
                    } catch (e: Exception) {
                        "Error al eliminar receta guardada: ${response.status}"
                    }
                    Result.failure(Exception(errorMessage))
                }
            }
        } catch (e: Exception) {
            val errorMessage = handleNetworkError(e)
            Log.e(TAG, "Error en deleteSavedRecipe: $errorMessage", e)
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
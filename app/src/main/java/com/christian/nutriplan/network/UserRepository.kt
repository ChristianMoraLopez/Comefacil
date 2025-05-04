package com.christian.nutriplan.network

import android.content.Context
import com.christian.nutriplan.models.Credentials
import com.christian.nutriplan.models.Usuario
import com.christian.nutriplan.utils.AuthManager
import io.ktor.client.call.body
import io.ktor.client.request.*
import io.ktor.http.*
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import android.util.Log
import com.christian.nutriplan.models.responses.ApiResponse
import io.ktor.client.plugins.defaultRequest
import io.ktor.client.plugins.ClientRequestException
import io.ktor.client.plugins.ServerResponseException
import io.ktor.client.plugins.HttpRequestTimeoutException
import io.ktor.client.network.sockets.ConnectTimeoutException
import java.net.UnknownHostException

class UserRepository(private val context: Context) : BaseRepository() {
    private val TAG = "UserRepository"
    // Referencia al objeto singleton AuthManager
    private val authManager = AuthManager

    companion object {
        private const val REGISTER_ENDPOINT = "/registro"
        private const val LOGIN_ENDPOINT = "/login"
        private const val CURRENT_USER_ENDPOINT = "/usuarios/me"
        private const val USER_ENDPOINT = "/usuarios"
        private const val TOKEN_REFRESH_ENDPOINT = "/refresh-token"
        private const val LOGOUT_ENDPOINT = "/logout"
    }

    init {
        configureHttpClient()
    }

    private fun configureHttpClient() {
        ApiClient.client.config {
            defaultRequest {
                contentType(ContentType.Application.Json)
                authManager.getAccessToken(context)?.let { token ->
                    header("Authorization", "Bearer $token")
                }
            }
        }
    }

    suspend fun registerUser(
        nombre: String,
        email: String,
        contrasena: String,
        aceptaTerminos: Boolean
    ): Result<Usuario> = withContext(Dispatchers.IO) {
        try {
            val usuario = Usuario(
                nombre = nombre,
                email = email,
                contrasena = contrasena,
                aceptaTerminos = aceptaTerminos
            )

            val response = ApiClient.client.post("${ApiClient.BASE_URL}$REGISTER_ENDPOINT") {
                setBody(usuario)
            }

            when (response.status) {
                HttpStatusCode.Created -> {
                    val apiResponse = response.body<ApiResponse.Success<Usuario>>()
                    Result.success(apiResponse.data)
                }
                HttpStatusCode.Conflict -> {
                    val apiResponse = response.body<ApiResponse.Error>()
                    Result.failure(Exception(apiResponse.message))
                }
                else -> {
                    Result.failure(Exception("Error al registrar: ${response.status}"))
                }
            }
        } catch (e: Exception) {
            Log.e(TAG, "Error en registerUser: ${e.message}", e)
            Result.failure(Exception(handleNetworkError(e)))
        }
    }

    suspend fun loginUser(email: String, password: String): Result<Pair<Usuario, String>> {
        return try {
            val response = ApiClient.client.post("${ApiClient.BASE_URL}$LOGIN_ENDPOINT") {
                setBody(Credentials(email, password))
            }

            when (response.status) {
                HttpStatusCode.OK -> {
                    // Usa el modelo que coincide con la respuesta del servidor
                    val apiResponse = response.body<ApiResponse.Success<ApiResponse.LoginServerResponse>>()

                    // Guardar el token (aquí puedes dividirlo si es JWT)
                    authManager.saveAuthData(
                        context = context,
                        accessToken = apiResponse.data.token,
                        refreshToken = "" // O maneja refresh token si está disponible
                    )

                    Result.success(apiResponse.data.usuario to apiResponse.data.token)
                }
                HttpStatusCode.Unauthorized -> {
                    Result.failure(Exception("Credenciales incorrectas"))
                }
                else -> {
                    Result.failure(Exception("Error al iniciar sesión: ${response.status}"))
                }
            }
        } catch (e: Exception) {
            Log.e(TAG, "Error en loginUser: ${e.message}", e)
            Result.failure(Exception(handleNetworkError(e)))
        }
    }
    // In UserRepository class
    suspend fun getCurrentUser(token: String): Result<Usuario> {
        return try {
            val response = ApiClient.client.get("${ApiClient.BASE_URL}$CURRENT_USER_ENDPOINT") {
                header("Authorization", "Bearer $token")
                header("X-Debug", "true") // Optional debug header
            }

            when (response.status) {
                HttpStatusCode.OK -> {
                    val apiResponse = response.body<ApiResponse.Success<Usuario>>()
                    Result.success(apiResponse.data)
                }
                HttpStatusCode.Unauthorized -> {
                    Result.failure(Exception("Session expired - please login again"))
                }
                HttpStatusCode.NotFound -> {
                    Result.failure(Exception("User not found - please contact support"))
                }
                else -> {
                    val error = try {
                        response.body<ApiResponse.Error>().message
                    } catch (e: Exception) {
                        "Unknown error (status ${response.status})"
                    }
                    Result.failure(Exception(error))
                }
            }
        } catch (e: Exception) {
            Result.failure(Exception("Network error: ${e.message}"))
        }
    }

    suspend fun updateUserProfile(updatedUser: Usuario): Result<Usuario> {
        return tryWithTokenRefresh {
            val response = ApiClient.client.put("${ApiClient.BASE_URL}$USER_ENDPOINT/${updatedUser.usuarioId}") {
                setBody(updatedUser)
            }

            when (response.status) {
                HttpStatusCode.OK -> {
                    val apiResponse = response.body<ApiResponse.Success<Usuario>>()
                    Result.success(apiResponse.data)
                }
                HttpStatusCode.Unauthorized -> {
                    Result.failure(Exception("No autorizado"))
                }
                HttpStatusCode.BadRequest -> {
                    Result.failure(Exception("Datos inválidos"))
                }
                else -> {
                    Result.failure(Exception("Error al actualizar perfil: ${response.status}"))
                }
            }
        }
    }

    suspend fun deleteUserAccount(userId: Int): Result<Unit> {
        return tryWithTokenRefresh {
            val response = ApiClient.client.delete("${ApiClient.BASE_URL}$USER_ENDPOINT/$userId")

            when (response.status) {
                HttpStatusCode.NoContent -> {
                    authManager.clearTokens(context)
                    Result.success(Unit)
                }
                HttpStatusCode.Unauthorized -> {
                    Result.failure(Exception("No autorizado"))
                }
                else -> {
                    Result.failure(Exception("Error al eliminar cuenta: ${response.status}"))
                }
            }
        }
    }

    private suspend fun refreshToken(): Result<String> {
        return try {
            val refreshToken = authManager.getRefreshToken(context)
                ?: return Result.failure(Exception("No hay refresh token disponible"))

            val response = ApiClient.client.post("${ApiClient.BASE_URL}$TOKEN_REFRESH_ENDPOINT") {
                setBody(mapOf("refreshToken" to refreshToken))
            }

            when (response.status) {
                HttpStatusCode.OK -> {
                    val apiResponse = response.body<ApiResponse.Success<ApiResponse.RefreshTokenResponse>>()
                    val newAccessToken = apiResponse.data.accessToken
                    val newRefreshToken = apiResponse.data.refreshToken
                    authManager.saveTokens(context, newAccessToken, newRefreshToken)
                    Result.success(newAccessToken)
                }
                else -> {
                    Result.failure(Exception("Error al refrescar token: ${response.status}"))
                }
            }
        } catch (e: Exception) {
            Log.e(TAG, "Error al refrescar token", e)
            Result.failure(Exception(handleNetworkError(e)))
        }
    }

    private suspend fun <T> tryWithTokenRefresh(block: suspend () -> Result<T>): Result<T> {
        val initialResult = block()

        // Si el resultado es exitoso o no es un error de autorización, retornarlo directamente
        if (initialResult.isSuccess) return initialResult

        val exception = initialResult.exceptionOrNull()
        val isAuthError = exception?.message?.let {
            it.contains("Sesión expirada", ignoreCase = true) ||
                    it.contains("No autorizado", ignoreCase = true) ||
                    it.contains("token inválido", ignoreCase = true)
        } ?: false

        if (!isAuthError) return initialResult

        // Intentar refrescar el token
        val refreshResult = refreshToken()
        return if (refreshResult.isSuccess) {
            // Reconfiguramos el cliente con el nuevo token
            configureHttpClient()
            // Reintentamos la operación original
            block()
        } else {
            // Si falla el refresh, limpiamos tokens y notificamos
            authManager.clearTokens(context)
            Result.failure(Exception("Sesión expirada. Por favor, inicia sesión de nuevo."))
        }
    }

    suspend fun logout() {
        try {
            ApiClient.client.post("${ApiClient.BASE_URL}$LOGOUT_ENDPOINT")
        } catch (e: Exception) {
            Log.w(TAG, "Error al hacer logout en el servidor", e)
        } finally {
            authManager.clearTokens(context)
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

data class LoginData(
    val usuario: Usuario,
    val tokens: ApiResponse.Tokens
)
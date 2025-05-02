package com.christian.nutriplan.network

import com.christian.nutriplan.models.Credentials
import com.christian.nutriplan.models.Usuario
import com.christian.nutriplan.models.responses.ApiResponse
import io.ktor.client.call.*
import io.ktor.client.request.*
import io.ktor.http.*
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

class UserRepository : BaseRepository() {
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

            val response = ApiClient.client.post("${ApiClient.BASE_URL}/registro") {
                contentType(ContentType.Application.Json)
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
            Result.failure(e)
        }
    }

    suspend fun loginUser(email: String, password: String): Result<Pair<String, Usuario>> = 
        safeApiCall {
            val credentials = Credentials(email, password)
            val response = ApiClient.client.post("${ApiClient.BASE_URL}/login") {
                contentType(ContentType.Application.Json)
                setBody(credentials)
            }
            val apiResponse = handleResponse<Map<String, Any>>(response)
            val token = apiResponse["token"] as String
            val usuario = apiResponse["usuario"] as Usuario
            token to usuario
        }

    suspend fun getUserProfile(userId: Int, token: String): Result<Usuario> = 
        getRequest("/usuarios/$userId", token)

    suspend fun updateUserProfile(
        userId: Int,
        token: String,
        updatedUser: Usuario
    ): Result<Usuario> = putRequest("/usuarios/$userId", updatedUser, token)

    suspend fun deleteUserAccount(userId: Int, token: String): Result<Unit> = 
        deleteRequest("/usuarios/$userId", token)
}

package com.christian.nutriplan.network

import com.christian.nutriplan.models.responses.ApiResponse
import io.ktor.client.call.*
import io.ktor.client.request.*
import io.ktor.http.*
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

open class BaseRepository {
    suspend inline fun <reified T> safeApiCall(
        token: String? = null,
        crossinline request: suspend () -> T
    ): Result<T> = withContext(Dispatchers.IO) {
        try {
            Result.success(request())
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend inline fun <reified T> getRequest(
        endpoint: String,
        token: String? = null
    ): Result<T> = safeApiCall(token) {
        val response = ApiClient.client.get("${ApiClient.BASE_URL}$endpoint") {
            contentType(ContentType.Application.Json)
            token?.let { header("Authorization", "Bearer $it") }
        }
        handleResponse(response)
    }

    suspend inline fun <reified T, reified R> postRequest(
        endpoint: String,
        body: R,
        token: String? = null
    ): Result<T> = safeApiCall(token) {
        val response = ApiClient.client.post("${ApiClient.BASE_URL}$endpoint") {
            contentType(ContentType.Application.Json)
            token?.let { header("Authorization", "Bearer $it") }
            setBody(body)
        }
        handleResponse(response)
    }

    suspend inline fun <reified T, reified R> putRequest(
        endpoint: String,
        body: R,
        token: String? = null
    ): Result<T> = safeApiCall(token) {
        val response = ApiClient.client.put("${ApiClient.BASE_URL}$endpoint") {
            contentType(ContentType.Application.Json)
            token?.let { header("Authorization", "Bearer $it") }
            setBody(body)
        }
        handleResponse(response)
    }

    suspend inline fun <reified T> deleteRequest(
        endpoint: String,
        token: String? = null
    ): Result<T> = safeApiCall(token) {
        val response = ApiClient.client.delete("${ApiClient.BASE_URL}$endpoint") {
            contentType(ContentType.Application.Json)
            token?.let { header("Authorization", "Bearer $it") }
        }
        handleResponse(response)
    }

    suspend inline fun <reified T> handleResponse(response: io.ktor.client.statement.HttpResponse): T {
        return when (response.status) {
            HttpStatusCode.OK, HttpStatusCode.Created -> {
                response.body<ApiResponse.Success<T>>().data
            }
            else -> {
                val error = response.body<ApiResponse.Error>()
                throw Exception(error.message ?: "Unknown error")
            }
        }
    }
}

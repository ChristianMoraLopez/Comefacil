package com.christian.nutriplan.network

import com.christian.nutriplan.models.ErrorResponse
import com.christian.nutriplan.models.Objetivo
import io.ktor.client.*
import io.ktor.client.call.*
import io.ktor.client.request.*
import io.ktor.http.*

class ObjetivoApiService(private val client: HttpClient) {
    private val baseUrl = ApiClient.BASE_URL

    suspend fun getAll(): List<Objetivo> {
        return client.get("$baseUrl/objetivos") {
            contentType(ContentType.Application.Json)
        }.body()
    }

    suspend fun getById(id: Int): Objetivo {
        return client.get("$baseUrl/objetivos/$id") {
            contentType(ContentType.Application.Json)
        }.body()
    }

    suspend fun create(objetivo: Objetivo, token: String): Result<Objetivo> {
        return try {
            val response = client.post("$baseUrl/objetivos") {
                contentType(ContentType.Application.Json)
                header(HttpHeaders.Authorization, "Bearer $token")
                setBody(objetivo)
            }
            when (response.status) {
                HttpStatusCode.OK, HttpStatusCode.Created -> {
                    val createdObjetivo: Objetivo = response.body()
                    Result.success(createdObjetivo)
                }
                HttpStatusCode.BadRequest -> {
                    val errorResponse: ErrorResponse = response.body()
                    Result.failure(Exception("Bad Request: ${errorResponse.message}"))
                }
                HttpStatusCode.Unauthorized -> {
                    val errorResponse: ErrorResponse = response.body()
                    Result.failure(Exception("Unauthorized: ${errorResponse.message}"))
                }
                else -> {
                    Result.failure(Exception("Unexpected response: ${response.status}"))
                }
            }
        } catch (e: Exception) {
            println("Error in create objective: ${e.message}")
            Result.failure(e)
        }
    }

    suspend fun update(id: Int, objetivo: Objetivo): Objetivo {
        return client.put("$baseUrl/objetivos/$id") {
            contentType(ContentType.Application.Json)
            setBody(objetivo)
        }.body()
    }

    suspend fun delete(id: Int): Boolean {
        return client.delete("$baseUrl/objetivos/$id") {
            contentType(ContentType.Application.Json)
        }.let { true }
    }
}
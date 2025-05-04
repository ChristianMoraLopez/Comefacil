package com.christian.nutriplan.network

import android.content.Context
import io.ktor.client.*
import io.ktor.client.engine.cio.*
import io.ktor.client.plugins.*
import io.ktor.client.plugins.auth.providers.*  // Add this import
import io.ktor.client.plugins.contentnegotiation.*
import io.ktor.client.plugins.logging.*
import io.ktor.http.*
import io.ktor.serialization.kotlinx.json.*
import kotlinx.serialization.json.Json
import com.christian.nutriplan.utils.AuthManager
import io.ktor.client.call.body
import io.ktor.client.request.header
import io.ktor.client.request.post
import io.ktor.client.request.setBody



object ApiClient {
    private lateinit var _client: HttpClient

    fun initialize(context: Context) {
        _client = HttpClient(CIO) {
            install(ContentNegotiation) {
                json(Json {
                    prettyPrint = true
                    isLenient = true
                    ignoreUnknownKeys = true
                    explicitNulls = false
                })
            }

            install(Logging) {
                logger = Logger.DEFAULT
                level = LogLevel.ALL
            }

            install(io.ktor.client.plugins.auth.Auth) {
                bearer {
                    loadTokens {
                        val accessToken = AuthManager.getAccessToken(context)
                        val refreshToken = AuthManager.getRefreshToken(context)
                        if (accessToken != null && refreshToken != null) {
                            BearerTokens(accessToken, refreshToken)
                        } else {
                            null
                        }
                    }

                    refreshTokens {
                        try {
                            val refreshToken = AuthManager.getRefreshToken(context)
                            if (refreshToken != null) {
                                val response = _client.post("$BASE_URL/auth/refresh") {
                                    contentType(ContentType.Application.Json)
                                    setBody(mapOf("refreshToken" to refreshToken))
                                }
                                val newTokens = response.body<RefreshTokenResponse>()
                                AuthManager.saveTokens(context, newTokens.accessToken, newTokens.refreshToken)
                                BearerTokens(newTokens.accessToken, newTokens.refreshToken)
                            } else {
                                null
                            }
                        } catch (e: Exception) {
                            AuthManager.clearAuthData(context)
                            null // Handle refresh failure (e.g., logout user)
                        }
                    }

                    sendWithoutRequest { request ->
                        request.url.host == BASE_URL
                    }
                }
            }

            defaultRequest {
                header(HttpHeaders.ContentType, ContentType.Application.Json)
                header(HttpHeaders.Accept, ContentType.Application.Json)
                url(BASE_URL)
            }
        }
    }

    val client: HttpClient
        get() {
            if (!::_client.isInitialized) {
                throw IllegalStateException("ApiClient not initialized. Call initialize() first.")
            }
            return _client
        }

    const val BASE_URL = "https://nutriplanbackend-production.up.railway.app"
}

@kotlinx.serialization.Serializable
data class RefreshTokenResponse(
    val accessToken: String,
    val refreshToken: String
)
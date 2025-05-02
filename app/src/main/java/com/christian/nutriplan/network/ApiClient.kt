// app/src/main/java/com/christian/nutriplan/network/ApiClient.kt
package com.christian.nutriplan.network

import io.ktor.client.*
import io.ktor.client.engine.cio.*
import io.ktor.client.plugins.contentnegotiation.*
import io.ktor.serialization.kotlinx.json.*
import kotlinx.serialization.json.Json

object ApiClient {
    val client = HttpClient(CIO) {
        install(ContentNegotiation) {
            json(Json {
                prettyPrint = true
                isLenient = true
                ignoreUnknownKeys = true
            })
        }
    }

    const val BASE_URL = "http://10.0.2.2:8080"  // Reemplaza con la IP de tu servidor
}
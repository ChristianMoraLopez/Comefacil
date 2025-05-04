package com.christian.nutriplan.utils

import android.util.Base64
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.jsonObject
import kotlinx.serialization.json.jsonPrimitive
import kotlinx.serialization.json.intOrNull

object JwtUtils {
    fun getUserIdFromToken(token: String): Int? {
        return try {
            val payload = token.split(".")[1]
            val decodedPayload = String(Base64.decode(payload, Base64.URL_SAFE), Charsets.UTF_8)
            val jsonPayload = Json.parseToJsonElement(decodedPayload).jsonObject
            jsonPayload["userId"]?.jsonPrimitive?.intOrNull
        } catch (e: Exception) {
            null
        }
    }
}
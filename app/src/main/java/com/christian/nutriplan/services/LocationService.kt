package com.christian.nutriplan.services

import android.util.Log
import io.ktor.client.*
import io.ktor.client.call.*
import io.ktor.client.request.*
import kotlinx.serialization.Serializable

@Serializable
data class IpApiResponse(
    val city: String?,
    val region: String?,
    val country: String?
)

@Serializable
data class IpGeolocationResponse(
    val city: String?,
    val postalCode: String?,
    val countryName: String?
)

class GeolocationService(private val client: HttpClient) {
    private val TAG = "GeolocationService"
    private val IP_GEOLOCATION_API_KEY = "eb59f334-c1ca-4aa5-8e21-5daf440e51b6"

    suspend fun getUserLocation(): Pair<String, String> {
        // Try ipapi.co first
        try {
            val response: IpApiResponse = client.get("https://ipapi.co/json/").body()
            Log.d(TAG, "ipapi.co response: $response")
            val city = response.city?.takeIf { it.isNotEmpty() } ?: return tryFallback()
            val locality = response.region?.takeIf { it.isNotEmpty() } ?: response.country ?: "Unknown"
            Log.d(TAG, "Location fetched from ipapi.co: city=$city, locality=$locality")
            return Pair(city, locality)
        } catch (e: Exception) {
            Log.e(TAG, "Error fetching location from ipapi.co: ${e.message}", e)
            return tryFallback()
        }
    }

    private suspend fun tryFallback(): Pair<String, String> {
        try {
            val response: IpGeolocationResponse = client.get("https://apiip.net/api/check?accessKey=$IP_GEOLOCATION_API_KEY").body()
            Log.d(TAG, "ipgeolocation.io response: $response")
            val city = response.city?.takeIf { it.isNotEmpty() } ?: return Pair("Unknown", "Unknown")
            val locality = response.postalCode?.takeIf { it.isNotEmpty() } ?: response.countryName ?: "Unknown"
            Log.d(TAG, "Location fetched from ipgeolocation.io: city=$city, locality=$locality")
            return Pair(city, locality)
        } catch (e: Exception) {
            Log.e(TAG, "Error fetching location from ipgeolocation.io: ${e.message}", e)
            return Pair("Unknown", "Unknown")
        }
    }
}
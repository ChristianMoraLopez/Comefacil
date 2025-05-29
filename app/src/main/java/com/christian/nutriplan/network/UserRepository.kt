package com.christian.nutriplan.network

import android.content.Context
import android.util.Log
import com.christian.nutriplan.models.Credentials
import com.christian.nutriplan.models.GoogleLoginRequest // Asegúrate que esta importación sea correcta
import com.christian.nutriplan.models.Usuario
import com.christian.nutriplan.models.responses.ApiResponse
import com.christian.nutriplan.utils.AuthManager
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.android.gms.auth.api.signin.GoogleSignInClient
import com.google.android.gms.auth.api.signin.GoogleSignInOptions
import com.google.firebase.auth.FirebaseAuth // Aún necesario para signOut si se usa en otros lados
import com.google.firebase.auth.GoogleAuthProvider
// import com.google.firebase.auth.GoogleAuthProvider // Ya no es estrictamente necesario aquí si el backend verifica
import io.ktor.client.call.*
import io.ktor.client.network.sockets.ConnectTimeoutException
import io.ktor.client.plugins.ClientRequestException
import io.ktor.client.plugins.HttpRequestTimeoutException
import io.ktor.client.plugins.ServerResponseException
import io.ktor.client.plugins.defaultRequest
import io.ktor.client.request.*
import io.ktor.client.statement.bodyAsText
import io.ktor.http.*
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.tasks.await
// import kotlinx.coroutines.tasks.await // Ya no es necesario para signInWithCredential aquí
import kotlinx.coroutines.withContext
import java.net.UnknownHostException

class UserRepository(private val context: Context) : BaseRepository() {
    private val TAG = "UserRepository"
    private val authManager = AuthManager
    private val firebaseAuth by lazy { FirebaseAuth.getInstance() }

    companion object {
        private const val REGISTER_ENDPOINT = "/registro"
        private const val LOGIN_ENDPOINT = "/login"
        private const val GOOGLE_LOGIN_BACKEND_ENDPOINT = "/google-login" // Endpoint del backend para Google Sign-In
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

    fun getGoogleSignInClient(): GoogleSignInClient {
        val gso = GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
            // ESTE ID DEBE SER EL WEB CLIENT ID DE TU PROYECTO EN GOOGLE CLOUD CONSOLE
            .requestIdToken("91827763305-0rvv01f2p7tikp53lnduoik9c41g12m6.apps.googleusercontent.com")
            .requestEmail()
            .build()
        return GoogleSignIn.getClient(context, gso)
    }

    /**
     * Inicia sesión con Google enviando el idToken de Google al backend.
     * El backend verifica el token, crea/actualiza el usuario y devuelve un token JWT de la aplicación.
     */

    suspend fun signInWithGoogle(googleIdToken: String): Result<Pair<Usuario, String>> = withContext(Dispatchers.IO) {
        try {
            // Step 1: Authenticate with Firebase to get a Firebase ID token
            val credential = GoogleAuthProvider.getCredential(googleIdToken, null)
            val authResult = firebaseAuth.signInWithCredential(credential).await()
            val firebaseUser = authResult.user ?: return@withContext Result.failure(Exception("Firebase authentication failed: No user returned"))

            val firebaseIdToken = firebaseUser.getIdToken(false).await().token
                ?: return@withContext Result.failure(Exception("Failed to retrieve Firebase ID token"))

            Log.d(TAG, "Sending POST to ${ApiClient.BASE_URL}$GOOGLE_LOGIN_BACKEND_ENDPOINT with Firebase ID token: ${firebaseIdToken.take(15)}...")

            // Step 2: Try to sign in with the backend
            val signInResponse = ApiClient.client.post("${ApiClient.BASE_URL}$GOOGLE_LOGIN_BACKEND_ENDPOINT") {
                contentType(ContentType.Application.Json)
                setBody(GoogleLoginRequest(firebaseIdToken))
            }

            Log.d(TAG, "Sign-in response status: ${signInResponse.status}, headers: ${signInResponse.headers.entries()}, body: ${signInResponse.bodyAsText()}")

            when (signInResponse.status) {
                HttpStatusCode.OK -> {
                    val apiResponse = signInResponse.body<ApiResponse.Success<ApiResponse.LoginServerResponse>>()
                    val user = apiResponse.data.usuario
                    val appToken = apiResponse.data.token
                    Log.d(TAG, "Success: User=${user.email}, Token=${appToken.take(10)}...")
                    authManager.saveAuthData(context, appToken, "", user.usuarioId.toString())
                    Result.success(user to appToken)
                }
                HttpStatusCode.NotFound -> {
                    // User not found, attempt automatic registration
                    Log.d(TAG, "User not found, attempting registration...")
                    val usuario = Usuario(
                        usuarioId = 0, // Backend will assign ID
                        nombre = firebaseUser.displayName ?: "",
                        email = firebaseUser.email ?: "",
                        contrasena = "", // Not needed for Google users
                        aceptaTerminos = true, // Assume terms accepted
                    )
                    val registerResponse = ApiClient.client.post("${ApiClient.BASE_URL}$REGISTER_ENDPOINT") {
                        contentType(ContentType.Application.Json)
                        setBody(usuario)
                    }

                    when (registerResponse.status) {
                        HttpStatusCode.Created -> {
                            // Registration successful, attempt login again
                            Log.d(TAG, "Registration successful, retrying sign-in...")
                            val retryResponse = ApiClient.client.post("${ApiClient.BASE_URL}$GOOGLE_LOGIN_BACKEND_ENDPOINT") {
                                contentType(ContentType.Application.Json)
                                setBody(GoogleLoginRequest(firebaseIdToken))
                            }

                            when (retryResponse.status) {
                                HttpStatusCode.OK -> {
                                    val apiResponse = retryResponse.body<ApiResponse.Success<ApiResponse.LoginServerResponse>>()
                                    val user = apiResponse.data.usuario
                                    val appToken = apiResponse.data.token
                                    Log.d(TAG, "Success after registration: User=${user.email}, Token=${appToken.take(10)}...")
                                    authManager.saveAuthData(context, appToken, "", user.usuarioId.toString())
                                    Result.success(user to appToken)
                                }
                                else -> {
                                    val errorBody = retryResponse.bodyAsText()
                                    Log.e(TAG, "Failed to sign in after registration: ${retryResponse.status}, $errorBody")
                                    Result.failure(Exception("Failed to sign in after registration: $errorBody"))
                                }
                            }
                        }
                        else -> {
                            val errorBody = registerResponse.bodyAsText()
                            Log.e(TAG, "Registration failed: ${registerResponse.status}, $errorBody")
                            Result.failure(Exception("Registration failed: $errorBody"))
                        }
                    }
                }
                HttpStatusCode.Unauthorized -> {
                    val errorMsg = try { signInResponse.body<ApiResponse.Error>().message } catch (e: Exception) { "Google Sign-In failed: Unauthorized" }
                    Log.e(TAG, "Failed: $errorMsg")
                    Result.failure(Exception(errorMsg))
                }
                else -> {
                    val errorBody = signInResponse.bodyAsText()
                    Log.e(TAG, "Failed with status ${signInResponse.status}: $errorBody")
                    Result.failure(Exception("Google Sign-In failed: $errorBody"))
                }
            }
        } catch (e: Exception) {
            Log.e(TAG, "Exception in signInWithGoogle: ${e.javaClass.simpleName}: ${e.message}", e)
            Result.failure(Exception(handleNetworkError(e)))
        }
    }

    // La función registerUser se mantiene para el registro tradicional con email/contraseña
    suspend fun registerUser(usuario: Usuario): Result<Pair<Usuario, String>> = withContext(Dispatchers.IO) {
        try {
            val response = ApiClient.client.post("${ApiClient.BASE_URL}$REGISTER_ENDPOINT") {
                setBody(usuario)
            }

            when (response.status) {
                HttpStatusCode.Created -> {
                    val apiResponse = response.body<ApiResponse.Success<Usuario>>()
                    val user = apiResponse.data

                    // Después de registrar, intentar hacer login para obtener el token de la app
                    // (Esto asume que el registro no devuelve directamente un token de sesión)
                    val loginResult = loginUser(usuario.email, usuario.contrasena)

                    loginResult.fold(
                        onSuccess = { (loggedInUser, token) ->
                            // authManager.saveAuthData ya se llama dentro de loginUser
                            Log.d(TAG, "Registro exitoso, y login post-registro también. Usuario: ${user.email}")
                            Result.success(user to token) // Devolver el usuario del registro y el token del login
                        },
                        onFailure = { throwable ->
                            Log.e(TAG, "Registro exitoso pero fallo al iniciar sesión automáticamente: ${throwable.message}")
                            Result.failure(Exception("Registro exitoso pero falló el inicio de sesión automático: ${throwable.message}"))
                        }
                    )
                }
                HttpStatusCode.Conflict -> {
                    val apiResponse = response.body<ApiResponse.Error>()
                    Log.w(TAG, "Error en registerUser (Conflict): ${apiResponse.message}")
                    Result.failure(Exception(apiResponse.message))
                }
                else -> {
                    val errorBody = try { response.bodyAsText() } catch (e: Exception) { "Error desconocido" }
                    Log.e(TAG, "Error en registerUser (status ${response.status}): $errorBody")
                    Result.failure(Exception("Error al registrar: ${response.status} - $errorBody"))
                }
            }
        } catch (e: Exception) {
            Log.e(TAG, "Excepción en registerUser: ${e.message}", e)
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
                    val apiResponse = response.body<ApiResponse.Success<ApiResponse.LoginServerResponse>>()
                    authManager.saveAuthData(
                        context = context,
                        accessToken = apiResponse.data.token,
                        refreshToken = "",
                        userId = apiResponse.data.usuario.usuarioId.toString()
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
    suspend fun getCurrentUser(token: String): Result<Usuario> {
        return try {
            Log.d(TAG, "Sending GET to ${ApiClient.BASE_URL}$CURRENT_USER_ENDPOINT")
            val response = ApiClient.client.get("${ApiClient.BASE_URL}$CURRENT_USER_ENDPOINT") {
                header("X-Debug", "true")
            }

            Log.d(TAG, "Response status: ${response.status}, body: ${response.bodyAsText()}")
            when (response.status) {
                HttpStatusCode.OK -> {
                    val apiResponse = response.body<ApiResponse.Success<Usuario>>()
                    Result.success(apiResponse.data)
                }
                HttpStatusCode.Unauthorized -> {
                    Result.failure(Exception("Sesión expirada o token inválido. Por favor, inicia sesión de nuevo."))
                }
                HttpStatusCode.NotFound -> {
                    Result.failure(Exception("Usuario no encontrado. Contacta a soporte."))
                }
                else -> {
                    val error = try {
                        response.body<ApiResponse.Error>().message
                    } catch (e: Exception) {
                        "Error desconocido (status ${response.status})"
                    }
                    Result.failure(Exception(error))
                }
            }
        } catch (e: Exception) {
            Log.e(TAG, "Error en getCurrentUser: ${e.message}", e)
            Result.failure(Exception("Error de red al obtener usuario: ${e.message}"))
        }
    }

    suspend fun updateUserProfile(updatedUser: Usuario): Result<Usuario> {
        return tryWithTokenRefresh {
            // El token se añade por defecto desde configureHttpClient
            // No es necesario obtenerlo manualmente aquí si la configuración de Ktor es global.
            // AuthManager.getAccessToken(context) ?: return@tryWithTokenRefresh Result.failure(Exception("No authentication token available"))

            Log.d(TAG, "Actualizando usuario ${updatedUser.usuarioId}")
            val response = ApiClient.client.put("${ApiClient.BASE_URL}$USER_ENDPOINT/${updatedUser.usuarioId}") {
                setBody(updatedUser)
            }

            Log.d(TAG, "Respuesta de actualización status: ${response.status}")
            when (response.status) {
                HttpStatusCode.OK -> {
                    val apiResponse = response.body<ApiResponse.Success<Usuario>>()
                    Log.d(TAG, "Usuario actualizado: ${apiResponse.data}")
                    Result.success(apiResponse.data)
                }
                HttpStatusCode.Unauthorized -> Result.failure(Exception("No autorizado"))
                HttpStatusCode.NotFound -> Result.failure(Exception("Usuario no encontrado"))
                HttpStatusCode.BadRequest -> Result.failure(Exception("Datos inválidos"))
                else -> Result.failure(Exception("Error al actualizar perfil: ${response.status}"))
            }
        }
    }

    suspend fun deleteUserAccount(userId: Int): Result<Unit> {
        return tryWithTokenRefresh {
            val response = ApiClient.client.delete("${ApiClient.BASE_URL}$USER_ENDPOINT/$userId")
            // El token de autorización se añade por defecto

            when (response.status) {
                HttpStatusCode.NoContent -> {
                    authManager.clearTokens(context)
                    firebaseAuth.signOut() // Si el usuario también estaba logueado en Firebase en el cliente
                    GoogleSignIn.getClient(context, GoogleSignInOptions.DEFAULT_SIGN_IN).signOut() // Cerrar sesión de Google SDK
                    Result.success(Unit)
                }
                HttpStatusCode.Unauthorized -> Result.failure(Exception("No autorizado"))
                else -> Result.failure(Exception("Error al eliminar cuenta: ${response.status}"))
            }
        }
    }

    private suspend fun refreshToken(): Result<String> {
        return try {
            val currentRefreshToken = authManager.getRefreshToken(context)
                ?: return Result.failure(Exception("No hay refresh token disponible"))

            // Para el refresh token, Ktor no debe usar el access token expirado.
            // Se debe hacer una instancia de cliente separada o configurar esta llamada para no usar el defaultRequest.
            // Por ahora, asumimos que el defaultRequest no interfiere o que el token no se añade si es la ruta de refresh.
            val response = ApiClient.client.post("${ApiClient.BASE_URL}$TOKEN_REFRESH_ENDPOINT") {
                // No queremos el header "Authorization" con el access token aquí
                // Considera crear una instancia de Ktor client sin el interceptor de Auth para esta llamada
                // o modificar el interceptor para no añadir el token en esta ruta específica.
                // Temporalmente, para probar, podrías remover el header si se añade por defecto:
                // headers.remove(HttpHeaders.Authorization)
                setBody(mapOf("refreshToken" to currentRefreshToken))
            }

            when (response.status) {
                HttpStatusCode.OK -> {
                    val apiResponse = response.body<ApiResponse.Success<ApiResponse.RefreshTokenResponse>>()
                    val newAccessToken = apiResponse.data.accessToken
                    val newRefreshToken = apiResponse.data.refreshToken // Backend debe enviar nuevo refresh token
                    authManager.saveTokens(context, newAccessToken, newRefreshToken)
                    // Reconfigurar el cliente Ktor con el nuevo token podría ser necesario si no se hace automáticamente
                    configureHttpClient() // Para que las siguientes llamadas usen el nuevo token
                    Result.success(newAccessToken)
                }
                else -> {
                    Log.e(TAG, "Error al refrescar token: ${response.status}. Limpiando tokens.")
                    authManager.clearTokens(context) // Si falla el refresh, limpiar tokens y forzar login
                    firebaseAuth.signOut()
                    GoogleSignIn.getClient(context, GoogleSignInOptions.DEFAULT_SIGN_IN).signOut()
                    Result.failure(Exception("Error al refrescar token: ${response.status}"))
                }
            }
        } catch (e: Exception) {
            Log.e(TAG, "Excepción al refrescar token", e)
            authManager.clearTokens(context) // Limpiar tokens en caso de error de red también
            firebaseAuth.signOut()
            GoogleSignIn.getClient(context, GoogleSignInOptions.DEFAULT_SIGN_IN).signOut()
            Result.failure(Exception(handleNetworkError(e)))
        }
    }

    internal suspend fun <T> tryWithTokenRefresh(block: suspend () -> Result<T>): Result<T> {
        var result = block()
        val exception = result.exceptionOrNull()

        if (exception != null) {
            val isAuthError = exception.message?.let {
                it.contains("Sesión expirada", ignoreCase = true) ||
                        it.contains("No autorizado", ignoreCase = true) ||
                        it.contains("token inválido", ignoreCase = true) ||
                        (exception is ClientRequestException && exception.response.status == HttpStatusCode.Unauthorized)
            } ?: ((exception is ClientRequestException && exception.response.status == HttpStatusCode.Unauthorized))


            if (isAuthError) {
                Log.d(TAG, "Error de autenticación detectado, intentando refrescar token.")
                val refreshResult = refreshToken()
                if (refreshResult.isSuccess) {
                    Log.d(TAG, "Token refrescado exitosamente, reintentando la operación original.")
                    // configureHttpClient() // Ya se llama dentro de refreshToken si es exitoso
                    result = block() // Reintentar la operación original con el nuevo token
                } else {
                    Log.e(TAG, "Fallo al refrescar token. ${refreshResult.exceptionOrNull()?.message}")
                    // No cambiar el 'result' aquí, ya falló y el refresh también.
                    // El usuario necesitará volver a iniciar sesión.
                    // El ViewModel/UI debe manejar este fallo final.
                    return Result.failure(Exception("Sesión expirada. Por favor, inicia sesión de nuevo."))
                }
            }
        }
        return result
    }


    suspend fun logout() {
        try {
            // El token de autorización se añade por defecto
            ApiClient.client.post("${ApiClient.BASE_URL}$LOGOUT_ENDPOINT")
            Log.d(TAG, "Logout en servidor solicitado.")
        } catch (e: Exception) {
            Log.w(TAG, "Error al hacer logout en el servidor (puede ser normal si el token ya era inválido)", e)
        } finally {
            Log.d(TAG, "Limpiando tokens locales y cerrando sesión de Firebase/Google.")
            authManager.clearTokens(context)
            firebaseAuth.signOut() // Cierra sesión de Firebase si estaba activa
            // También cierra sesión del SDK de Google Sign-In para permitir seleccionar otra cuenta la próxima vez.
            GoogleSignIn.getClient(context, GoogleSignInOptions.DEFAULT_SIGN_IN).signOut().addOnCompleteListener {
                Log.d(TAG, "GoogleSignInClient signOut complete.")
            }
        }
    }

    private fun handleNetworkError(e: Exception): String {
        Log.e(TAG, "Handling network error: ${e::class.java.simpleName}", e)
        return when (e) {
            is ConnectTimeoutException -> "Tiempo de conexión agotado. Verifica tu conexión a internet."
            is UnknownHostException -> "No se pudo encontrar el servidor. Verifica tu conexión y la URL del servidor."
            is HttpRequestTimeoutException -> "La solicitud tardó demasiado. Intenta de nuevo."
            is ClientRequestException -> "Error del cliente: ${e.response.status}. ${e.message}"
            is ServerResponseException -> "Error interno del servidor: ${e.response.status}. ${e.message}"
            else -> "Error de red: ${e.message ?: "Desconocido"}"
        }
    }
}
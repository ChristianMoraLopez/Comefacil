import android.content.Context
import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.christian.nutriplan.models.Usuario
import com.christian.nutriplan.network.UserRepository
import com.christian.nutriplan.services.GeolocationService
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import org.koin.androidx.viewmodel.dsl.viewModel
import org.koin.dsl.module

val userModule = module {
    viewModel { UserViewModel(get(), get()) }
}

class UserViewModel(
    private val userRepository: UserRepository,
    private val geolocationService: GeolocationService
) : ViewModel() {
    private val _currentUser = MutableStateFlow<Usuario?>(null)
    val currentUser: StateFlow<Usuario?> = _currentUser.asStateFlow()

    private val _locationData = MutableStateFlow<Pair<String, String>?>(null)
    val locationData: StateFlow<Pair<String, String>?> = _locationData.asStateFlow()

    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading.asStateFlow()

    private val _errorMessage = MutableStateFlow<String?>(null)
    val errorMessage: StateFlow<String?> = _errorMessage.asStateFlow()

    init {
        fetchUserLocation()
    }

    private fun fetchUserLocation() {
        viewModelScope.launch {
            _isLoading.value = true
            val location = geolocationService.getUserLocation()
            _locationData.value = location ?: Pair("Unknown", "Unknown")
            _isLoading.value = false
        }
    }

    fun setErrorMessage(message: String?) {
        _errorMessage.value = message
    }

    // New function to handle Google login
    fun signInWithGoogle(context: Context, googleIdToken: String, onSuccess: () -> Unit) {
        viewModelScope.launch {
            _isLoading.value = true
            _errorMessage.value = null

            userRepository.signInWithGoogle(googleIdToken).fold(
                onSuccess = { (user, token) ->
                    _currentUser.value = user // Store the user directly
                    _isLoading.value = false
                    onSuccess()
                    Log.d("UserViewModel", "Google login successful, user: ${user.email}")
                },
                onFailure = { throwable ->
                    _errorMessage.value = throwable.message
                    _isLoading.value = false
                    Log.e("UserViewModel", "Google login failed: ${throwable.message}")
                }
            )
        }
    }

    fun register(
        nombre: String,
        email: String,
        contrasena: String,
        aceptaTerminos: Boolean,
        onSuccess: () -> Unit
    ) {
        viewModelScope.launch {
            _isLoading.value = true
            _errorMessage.value = null

            val (ciudad, localidad) = _locationData.value ?: Pair("Unknown", "Unknown")

            val usuario = Usuario(
                nombre = nombre,
                email = email,
                contrasena = contrasena,
                aceptaTerminos = aceptaTerminos,
                ciudad = ciudad,
                localidad = localidad
            )

            userRepository.registerUser(usuario).fold(
                onSuccess = { (user, token) ->
                    _currentUser.value = user
                    onSuccess()
                },
                onFailure = { e ->
                    _errorMessage.value = e.message ?: "Error al registrarse"
                }
            )

            _isLoading.value = false
        }
    }

    fun fetchUserProfile(token: String) {
        viewModelScope.launch {
            _isLoading.value = true
            _errorMessage.value = null

            userRepository.getCurrentUser(token).fold(
                onSuccess = { user ->
                    _currentUser.value = user
                    if (!user.ciudad.isNullOrEmpty() && !user.localidad.isNullOrEmpty()) {
                        _locationData.value = Pair(user.ciudad, user.localidad)
                    }
                },
                onFailure = { e ->
                    _errorMessage.value = e.message ?: "Error desconocido"
                }
            )

            _isLoading.value = false
        }
    }

    fun updateUserProfile(updatedUser: Usuario, onSuccess: () -> Unit) {
        viewModelScope.launch {
            _isLoading.value = true
            userRepository.updateUserProfile(updatedUser).fold(
                onSuccess = { user ->
                    _currentUser.value = user
                    _isLoading.value = false
                    onSuccess()
                },
                onFailure = { throwable ->
                    _errorMessage.value = throwable.message
                    _isLoading.value = false
                }
            )
        }
    }

    fun logout(context: Context) {
        viewModelScope.launch {
            userRepository.logout()
            _currentUser.value = null
            _locationData.value = null
        }
    }

    fun clearErrorMessage() {
        _errorMessage.value = null
    }
}
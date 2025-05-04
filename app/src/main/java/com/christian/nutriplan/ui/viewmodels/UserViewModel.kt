// UserViewModel.kt
import android.content.Context
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.christian.nutriplan.models.Usuario
import com.christian.nutriplan.network.UserRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import org.koin.androidx.viewmodel.dsl.viewModel
import org.koin.dsl.module

val userModule = module {
    viewModel { UserViewModel(get()) }
}

class UserViewModel(
    private val userRepository: UserRepository
) : ViewModel() {
    private val _currentUser = MutableStateFlow<Usuario?>(null)
    val currentUser: StateFlow<Usuario?> = _currentUser.asStateFlow()

    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading.asStateFlow()

    private val _errorMessage = MutableStateFlow<String?>(null)
    val errorMessage: StateFlow<String?> = _errorMessage.asStateFlow()

    fun fetchUserProfile(token: String) {
        viewModelScope.launch {
            _isLoading.value = true
            _errorMessage.value = null

            userRepository.getCurrentUser(token).fold(
                onSuccess = { user ->
                    _currentUser.value = user
                },
                onFailure = { e ->
                    _errorMessage.value = e.message ?: "Error desconocido"
                }
            )

            _isLoading.value = false
        }
    }

    fun logout(context: Context) {
        viewModelScope.launch {
            userRepository.logout()
            _currentUser.value = null
        }
    }

    fun clearErrorMessage() {
        _errorMessage.value = null
    }
}
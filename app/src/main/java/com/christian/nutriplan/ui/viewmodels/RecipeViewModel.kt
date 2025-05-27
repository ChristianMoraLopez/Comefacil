package com.christian.nutriplan.viewmodels

import android.content.Context
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.christian.nutriplan.AppState
import com.christian.nutriplan.models.Ingrediente
import com.christian.nutriplan.models.MealType
import com.christian.nutriplan.models.Receta
import com.christian.nutriplan.models.TipoComida
import com.christian.nutriplan.network.IngredientRepository
import com.christian.nutriplan.network.RecetaIngredientesRepository
import com.christian.nutriplan.network.RecipeRepository
import com.christian.nutriplan.utils.AuthManager
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

class RecipeViewModel(
    private val recipeRepository: RecipeRepository,
    private val ingredientRepository: IngredientRepository,
    private val recetaIngredientesRepository: RecetaIngredientesRepository,
    private val context: Context
) : ViewModel() {
    private val _recetas = MutableStateFlow<List<Receta>>(emptyList())
    val recetas: StateFlow<List<Receta>> = _recetas

    private val _tiposComida = MutableStateFlow<List<TipoComida>>(emptyList())
    val tiposComida: StateFlow<List<TipoComida>> = _tiposComida

    private val _ingredientes = MutableStateFlow<Map<Int, List<IngredienteConCantidad>>>(emptyMap())
    val ingredientes: StateFlow<Map<Int, List<IngredienteConCantidad>>> = _ingredientes

    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading

    private val _isLoadingIngredients = MutableStateFlow(false)
    val isLoadingIngredients: StateFlow<Boolean> = _isLoadingIngredients

    private val _errorMessage = MutableStateFlow<String?>(null)
    val errorMessage: StateFlow<String?> = _errorMessage

    private val _ingredientesReceta = MutableStateFlow<List<IngredienteConCantidad>>(emptyList())
    val ingredientesReceta: StateFlow<List<IngredienteConCantidad>> = _ingredientesReceta

    private val _isRecipeSaved = MutableStateFlow(false)
    val isRecipeSaved: StateFlow<Boolean> = _isRecipeSaved.asStateFlow()

    data class IngredienteConCantidad(
        val nombre: String,
        val cantidad: Double?,
        val unidad: String?
    )

    fun fetchRecetas(mealType: MealType) {
        viewModelScope.launch {
            _isLoading.value = true
            try {
                val result = recipeRepository.getRecetas()
                result.fold(
                    onSuccess = { recetas ->
                        val tipoComidaId = when (mealType) {
                            MealType.BREAKFAST -> 1
                            MealType.LUNCH -> 2
                            MealType.DINNER -> 3
                            MealType.SNACK -> 4
                        }

                        val filteredRecetas = recetas.filter { it.tipoComidaId == tipoComidaId }
                            .filter { receta ->
                                if (AppState.objetivo == "Bajar de Peso") receta.fit else true
                            }

                        _recetas.value = filteredRecetas
                    },
                    onFailure = { throwable ->
                        _errorMessage.value = throwable.message
                    }
                )
            } finally {
                _isLoading.value = false
            }
        }
    }

    fun fetchTiposComida() {
        viewModelScope.launch {
            try {
                val result = recipeRepository.getTiposComida()
                result.fold(
                    onSuccess = { tiposComida ->
                        _tiposComida.value = tiposComida
                    },
                    onFailure = { throwable ->
                        _errorMessage.value = throwable.message
                    }
                )
            } catch (e: Exception) {
                _errorMessage.value = "Error inesperado: ${e.message}"
            }
        }
    }

    fun updateTipoComida(recetaId: Int, tipoComidaId: Int, token: String) {
        viewModelScope.launch {
            try {
                val result = recipeRepository.updateTipoComida(recetaId, tipoComidaId, token)
                result.fold(
                    onSuccess = {
                        _recetas.value = _recetas.value.map { receta ->
                            if (receta.recetaId == recetaId) receta.copy(tipoComidaId = tipoComidaId) else receta
                        }
                    },
                    onFailure = { throwable ->
                        _errorMessage.value = throwable.message
                    }
                )
            } catch (e: Exception) {
                _errorMessage.value = "Error inesperado: ${e.message}"
            }
        }
    }

    fun saveFavoriteRecipe(recetaId: Int, userId: String) {
        viewModelScope.launch {
            try {
                val userIdInt = userId.toIntOrNull()
                if (userIdInt == null) {
                    _errorMessage.value = "ID de usuario inválido"
                    return@launch
                }

                val token = getToken()
                if (token == null) {
                    _errorMessage.value = "No autenticado. Por favor inicia sesión."
                    return@launch
                }

                val result = recipeRepository.saveFavoriteRecipe(recetaId, userIdInt, token)
                result.fold(
                    onSuccess = {
                        _errorMessage.value = "Receta guardada con éxito"
                        _isRecipeSaved.value = true
                    },
                    onFailure = { throwable ->
                        _errorMessage.value = throwable.message
                        _isRecipeSaved.value = false
                    }
                )
            } catch (e: Exception) {
                _errorMessage.value = "Error inesperado: ${e.message}"
                _isRecipeSaved.value = false
            }
        }
    }

    fun checkIfRecipeSaved(recetaId: Int, userId: String) {
        viewModelScope.launch {
            val token = getToken()
            if (token == null) {
                _errorMessage.value = "No autenticado. Por favor inicia sesión."
                return@launch
            }

            try {
                val isSaved = recipeRepository.isRecipeSaved(recetaId, userId, token)
                _isRecipeSaved.value = isSaved
            } catch (e: Exception) {
                _errorMessage.value = "Error al verificar receta guardada: ${e.message}"
            }
        }
    }

    fun fetchIngredientesForReceta(recetaId: Int) {
        viewModelScope.launch {
            _isLoadingIngredients.value = true
            try {
                val result = recetaIngredientesRepository.getIngredientesForReceta(recetaId)
                result.fold(
                    onSuccess = { ingredientes ->
                        val ingredientesConNombre = ingredientes.map {
                            IngredienteConCantidad(
                                nombre = it.nombreIngrediente ?: "Ingrediente desconocido",
                                cantidad = it.cantidad,
                                unidad = it.unidad
                            )
                        }
                        _ingredientesReceta.value = ingredientesConNombre
                        _ingredientes.value = _ingredientes.value + (recetaId to ingredientesConNombre)
                    },
                    onFailure = { throwable ->
                        _errorMessage.value = "Error al obtener ingredientes: ${throwable.message}"
                    }
                )
            } catch (e: Exception) {
                _errorMessage.value = "Error inesperado: ${e.message}"
            } finally {
                _isLoadingIngredients.value = false
            }
        }
    }

    fun setErrorMessage(message: String) {
        _errorMessage.value = message
    }

    fun clearErrorMessage() {
        _errorMessage.value = null
    }

    private fun getToken(): String? {
        return AuthManager.getAccessToken(context)
    }
}
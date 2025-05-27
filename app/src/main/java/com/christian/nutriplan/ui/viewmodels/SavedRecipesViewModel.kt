package com.christian.nutriplan.viewmodels

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.christian.nutriplan.models.RecetaGuardada
import com.christian.nutriplan.network.SavedRecipesRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

class SavedRecipesViewModel @Inject constructor(
    private val repository: SavedRecipesRepository
) : ViewModel() {

    private val _savedRecipes = MutableStateFlow<List<RecetaGuardada>>(emptyList())
    val savedRecipes: StateFlow<List<RecetaGuardada>> = _savedRecipes.asStateFlow()

    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading.asStateFlow()

    private val _errorMessage = MutableStateFlow<String?>(null)
    val errorMessage: StateFlow<String?> = _errorMessage.asStateFlow()

    fun fetchSavedRecipes(userId: String, token: String) {
        viewModelScope.launch {
            _isLoading.value = true
            val result = repository.getSavedRecipes(userId, token)
            _isLoading.value = false
            result.fold(
                onSuccess = { recipes ->
                    _savedRecipes.value = recipes
                },
                onFailure = { error ->
                    _errorMessage.value = error.message
                }
            )
        }
    }
    fun setErrorMessage(message: String?) {
        _errorMessage.value = message
    }

    fun deleteSavedRecipe(guardadoId: Int, userId: String, token: String) {
        viewModelScope.launch {
            _isLoading.value = true
            val result = repository.deleteSavedRecipe(guardadoId, token)
            _isLoading.value = false
            result.fold(
                onSuccess = {
                    _savedRecipes.value = _savedRecipes.value.filter { it.guardadoId != guardadoId }
                },
                onFailure = { error ->
                    _errorMessage.value = error.message
                }
            )
        }
    }

    fun clearErrorMessage() {
        _errorMessage.value = null
    }
}
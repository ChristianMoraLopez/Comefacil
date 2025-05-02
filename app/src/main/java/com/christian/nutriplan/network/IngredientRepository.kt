package com.christian.nutriplan.network

import com.christian.nutriplan.models.CategoriaIngrediente
import com.christian.nutriplan.models.Ingrediente

class IngredientRepository : BaseRepository() {
    suspend fun getAllIngredients(token: String? = null): Result<List<Ingrediente>> = 
        getRequest("/ingredientes", token)

    suspend fun getIngredient(id: Int, token: String? = null): Result<Ingrediente> = 
        getRequest("/ingredientes/$id", token)

    suspend fun createIngredient(ingredient: Ingrediente, token: String): Result<Ingrediente> = 
        postRequest("/ingredientes", ingredient, token)

    suspend fun updateIngredient(id: Int, ingredient: Ingrediente, token: String): Result<Ingrediente> = 
        putRequest("/ingredientes/$id", ingredient, token)

    suspend fun deleteIngredient(id: Int, token: String): Result<Unit> = 
        deleteRequest("/ingredientes/$id", token)
}

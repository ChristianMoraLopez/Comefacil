package com.christian.nutriplan.network

import com.christian.nutriplan.models.CategoriaIngrediente

class CategoryRepository : BaseRepository() {
    suspend fun getAllCategories(token: String? = null): Result<List<CategoriaIngrediente>> = 
        getRequest("/categorias", token)

    suspend fun getCategory(id: Int, token: String? = null): Result<CategoriaIngrediente> = 
        getRequest("/categorias/$id", token)

    suspend fun createCategory(category: CategoriaIngrediente, token: String): Result<CategoriaIngrediente> = 
        postRequest("/categorias", category, token)

    suspend fun updateCategory(id: Int, category: CategoriaIngrediente, token: String): Result<CategoriaIngrediente> = 
        putRequest("/categorias/$id", category, token)

    suspend fun deleteCategory(id: Int, token: String): Result<Unit> = 
        deleteRequest("/categorias/$id", token)
}

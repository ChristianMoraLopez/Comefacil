package com.christian.nutriplan.network

import com.christian.nutriplan.models.Comida

class MealRepository : BaseRepository() {
    suspend fun getAllMeals(token: String? = null): Result<List<Comida>> = 
        getRequest("/comidas", token)

    suspend fun getMeal(id: Int, token: String? = null): Result<Comida> = 
        getRequest("/comidas/$id", token)

    suspend fun createMeal(meal: Comida, token: String): Result<Comida> = 
        postRequest("/comidas", meal, token)

    suspend fun updateMeal(id: Int, meal: Comida, token: String): Result<Comida> = 
        putRequest("/comidas/$id", meal, token)

    suspend fun deleteMeal(id: Int, token: String): Result<Unit> = 
        deleteRequest("/comidas/$id", token)
}

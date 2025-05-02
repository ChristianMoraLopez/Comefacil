package com.christian.nutriplan.network

import com.christian.nutriplan.models.Objetivo

class GoalRepository : BaseRepository() {
    suspend fun getAllGoals(token: String? = null): Result<List<Objetivo>> = 
        getRequest("/objetivos", token)

    suspend fun getGoal(id: Int, token: String? = null): Result<Objetivo> = 
        getRequest("/objetivos/$id", token)

    suspend fun createGoal(goal: Objetivo, token: String): Result<Objetivo> = 
        postRequest("/objetivos", goal, token)

    suspend fun updateGoal(id: Int, goal: Objetivo, token: String): Result<Objetivo> = 
        putRequest("/objetivos/$id", goal, token)

    suspend fun deleteGoal(id: Int, token: String): Result<Unit> = 
        deleteRequest("/objetivos/$id", token)
}

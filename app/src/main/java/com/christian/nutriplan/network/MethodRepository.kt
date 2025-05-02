package com.christian.nutriplan.network

import com.christian.nutriplan.models.MetodoPreparacion

class MethodRepository : BaseRepository() {
    suspend fun getAllMethods(token: String? = null): Result<List<MetodoPreparacion>> = 
        getRequest("/metodos", token)

    suspend fun getMethod(id: Int, token: String? = null): Result<MetodoPreparacion> = 
        getRequest("/metodos/$id", token)

    suspend fun createMethod(method: MetodoPreparacion, token: String): Result<MetodoPreparacion> = 
        postRequest("/metodos", method, token)

    suspend fun updateMethod(id: Int, method: MetodoPreparacion, token: String): Result<MetodoPreparacion> = 
        putRequest("/metodos/$id", method, token)

    suspend fun deleteMethod(id: Int, token: String): Result<Unit> = 
        deleteRequest("/metodos/$id", token)
}

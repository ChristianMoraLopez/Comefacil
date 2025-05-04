package com.christian.nutriplan.network

import com.christian.nutriplan.models.Objetivo

class ObjetivoRepository(private val apiService: ObjetivoApiService) {


    suspend fun getObjetivoById(id: Int): Objetivo {
        return apiService.getById(id)
    }

    suspend fun updateObjetivo(id: Int, objetivo: Objetivo): Objetivo {
        return apiService.update(id, objetivo)
    }

    suspend fun deleteObjetivo(id: Int) {
        apiService.delete(id)
    }
}
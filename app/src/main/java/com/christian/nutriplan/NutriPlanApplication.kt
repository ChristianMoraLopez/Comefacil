package com.christian.nutriplan

import UserViewModel
import android.app.Application
import com.christian.nutriplan.network.ApiClient
import com.christian.nutriplan.network.ObjetivoApiService
import com.christian.nutriplan.network.ObjetivoRepository
import com.christian.nutriplan.network.UserRepository
import com.christian.nutriplan.utils.AuthManager
import org.koin.android.ext.koin.androidContext
import org.koin.android.ext.koin.androidLogger
import org.koin.core.context.startKoin
import org.koin.core.logger.Level
import org.koin.dsl.module

class NutriPlanApplication : Application() {
    override fun onCreate() {
        super.onCreate()
        startKoin {
            // Inicializa el cliente API
            ApiClient.initialize(applicationContext)
            // Configura el logger para depuración
            androidLogger(Level.DEBUG)
            // Proporciona el contexto de la aplicación
            androidContext(this@NutriPlanApplication)
            // Registra los módulos
            modules(appModule)
        }
    }
}

val appModule = module {
    // Network
    single { ApiClient.client }
    single { ObjetivoApiService(get()) }
    single { UserRepository(get()) }
    single { AuthManager }
    // No necesitamos registrar AuthManager ya que ahora es un singleton (object)

    // Repositories
    single { ObjetivoRepository(get()) }

    // ViewModels
    single { UserViewModel(get()) }
}
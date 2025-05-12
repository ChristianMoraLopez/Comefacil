package com.christian.nutriplan

import UserViewModel
import android.app.Application
import com.christian.nutriplan.network.ApiClient
import com.christian.nutriplan.network.IngredientRepository
import com.christian.nutriplan.network.RecetaIngredientesRepository
import com.christian.nutriplan.network.RecipeRepository
import com.christian.nutriplan.network.UserRepository
import com.christian.nutriplan.services.GeolocationService
import com.christian.nutriplan.utils.AuthManager
import com.christian.nutriplan.viewmodels.RecipeViewModel
import org.koin.android.ext.koin.androidContext
import org.koin.android.ext.koin.androidLogger
import org.koin.core.context.startKoin
import org.koin.core.logger.Level
import org.koin.dsl.module

class NutriPlanApplication : Application() {
    override fun onCreate() {
        super.onCreate()
        startKoin {
            ApiClient.initialize(applicationContext)
            androidLogger(Level.DEBUG)
            androidContext(this@NutriPlanApplication)
            modules(appModule)
        }
    }
}

val appModule = module {
    single { ApiClient.client }
    single { UserRepository(get()) }
    single { RecipeRepository() }
    single { IngredientRepository() }
    single { RecetaIngredientesRepository() }
    single { AuthManager }
    single { GeolocationService(get()) }
    single { UserViewModel(get(), get()) }
    single { RecipeViewModel(get(), get(), get()) }
}
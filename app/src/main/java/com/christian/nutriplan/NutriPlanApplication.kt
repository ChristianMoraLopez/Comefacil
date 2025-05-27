package com.christian.nutriplan

import UserViewModel
import android.app.Application
import android.util.Log
import com.christian.nutriplan.network.ApiClient
import com.christian.nutriplan.network.IngredientRepository
import com.christian.nutriplan.network.RecetaIngredientesRepository
import com.christian.nutriplan.network.RecipeRepository
import com.christian.nutriplan.network.SavedRecipesRepository
import com.christian.nutriplan.network.UserRepository
import com.christian.nutriplan.services.GeolocationService
import com.christian.nutriplan.utils.AuthManager
import com.christian.nutriplan.viewmodels.RecipeViewModel
import com.christian.nutriplan.viewmodels.SavedRecipesViewModel
import com.google.android.gms.common.GoogleApiAvailability
import com.google.firebase.FirebaseApp
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import org.koin.android.ext.koin.androidContext
import org.koin.android.ext.koin.androidLogger
import org.koin.androidx.viewmodel.dsl.viewModel
import org.koin.core.context.startKoin
import org.koin.core.logger.Level
import org.koin.dsl.module

class NutriPlanApplication : Application() {
    override fun onCreate() {
        super.onCreate()
        // Initialize Firebase
        try {
            FirebaseApp.initializeApp(this)


            Log.d("NutriPlanApplication", "Firebase initialized successfully")
        } catch (e: Exception) {
            Log.e("NutriPlanApplication", "Firebase initialization failed", e)
        }

        // Initialize ApiClient asynchronously
        CoroutineScope(Dispatchers.IO).launch {
            ApiClient.initialize(applicationContext)
        }

        // Initialize Koin
        startKoin {
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
    single { RecetaIngredientesRepository(get()) }
    single { AuthManager }
    single { GeolocationService(get()) }
    single { UserViewModel(get(), get()) }
    viewModel { SavedRecipesViewModel(get()) }
    single { SavedRecipesRepository(get()) }
    single { RecipeViewModel(get(), get(), get(),get(), get()) }
}
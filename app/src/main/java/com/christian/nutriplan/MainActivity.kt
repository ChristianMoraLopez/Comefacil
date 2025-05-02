package com.christian.nutriplan

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.ui.Modifier
import com.christian.nutriplan.network.ApiClient
import com.christian.nutriplan.network.UserRepository
import com.christian.nutriplan.ui.navigation.AppNavigation
import com.christian.nutriplan.ui.theme.NutriPlanTheme
import kotlinx.coroutines.DelicateCoroutinesApi

class MainActivity : ComponentActivity() {
    @OptIn(DelicateCoroutinesApi::class)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)



        setContent {
            NutriPlanTheme {
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background
                ) {
                    // Create repository instances
                    val userRepository = UserRepository()

                    // Start app navigation
                    AppNavigation(
                        userRepository = userRepository
                    )
                }
            }
        }
    }
}
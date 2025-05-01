package com.christian.nutriplan

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.Surface
import androidx.compose.ui.Modifier
import DatabaseHelper
import com.christian.nutriplan.ui.navigation.AppNavigation
import com.christian.nutriplan.ui.theme.NutriPlanTheme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // Inicializar credenciales de la base de datos (solo una vez)
        // TODO: En producción, usar un usuario limitado (ej. app_user) y obtener credenciales de forma segura
        DatabaseHelper.initDbCredentials(
            context = this,
            user = "christian", // Cambiar a app_user en producción
            password = "4682Oscuridad" // Cambiar a contraseña de app_user
        )

        setContent {
            NutriPlanTheme {
                Surface(
                    modifier = Modifier.fillMaxSize()
                ) {
                    AppNavigation()
                }
            }
        }
    }
}
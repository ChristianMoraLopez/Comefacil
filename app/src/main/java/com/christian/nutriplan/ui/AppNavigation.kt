package com.christian.nutriplan.ui.navigation

import androidx.compose.runtime.Composable
import androidx.compose.ui.platform.LocalContext
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import DatabaseHelper
import com.christian.nutriplan.ui.screens.HomeScreen
import com.christian.nutriplan.ui.screens.LoginScreen
import com.christian.nutriplan.ui.screens.RegisterScreen

object NavRoutes {
    const val HOME = "home"
    const val LOGIN = "login"
    const val REGISTER = "register"
    const val DASHBOARD = "dashboard" // Nueva ruta para después del registro/login
}

@Composable
fun AppNavigation() {
    val navController = rememberNavController()
    val context = LocalContext.current

    NavHost(
        navController = navController,
        startDestination = NavRoutes.HOME
    ) {
        composable(NavRoutes.HOME) {
            HomeScreen(
                onStartClick = { navController.navigate(NavRoutes.LOGIN) }
            )
        }

        composable(NavRoutes.LOGIN) {
            LoginScreen(
                onLoginSuccess = { navController.navigate(NavRoutes.DASHBOARD) },
                onRegisterClick = { navController.navigate(NavRoutes.REGISTER) }
            )
        }

        composable(NavRoutes.REGISTER) {
            RegisterScreen(
                databaseHelper = DatabaseHelper.getInstance(context),
                onRegisterSuccess = {
                    navController.navigate(NavRoutes.DASHBOARD) {
                        popUpTo(NavRoutes.HOME) { inclusive = true }
                    }
                },
                onLoginClick = {
                    navController.popBackStack()
                    navController.navigate(NavRoutes.LOGIN)
                }
            )
        }

        composable(NavRoutes.DASHBOARD) {
            // Aquí iría tu pantalla principal después del login/registro
            DashboardScreen(
                onLogout = { navController.navigate(NavRoutes.HOME) }
            )
        }
    }
}

// Screen de ejemplo para el dashboard
@Composable
fun DashboardScreen(onLogout: () -> Unit) {
    // Implementa tu pantalla principal aquí
}
package com.christian.nutriplan.ui.navigation

import androidx.compose.runtime.Composable
import androidx.compose.ui.platform.LocalContext
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.christian.nutriplan.network.UserRepository
import com.christian.nutriplan.ui.screens.HomeScreen
import com.christian.nutriplan.ui.screens.LoginScreen
import com.christian.nutriplan.ui.screens.RegisterScreen

object NavRoutes {
    const val HOME = "home"
    const val LOGIN = "login"
    const val REGISTER = "register"
    const val DASHBOARD = "dashboard"
}

@Composable
fun AppNavigation(
    userRepository: UserRepository = UserRepository() // Default parameter for preview/testing
) {
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
                userRepository = userRepository,
                onLoginSuccess = { navController.navigate(NavRoutes.DASHBOARD) {
                    popUpTo(NavRoutes.HOME) { inclusive = true }
                }},
                onRegisterClick = { navController.navigate(NavRoutes.REGISTER) }
            )
        }

        composable(NavRoutes.REGISTER) {
            RegisterScreen(
                userRepository = userRepository,
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
            DashboardScreen(
                onLogout = {
                    navController.navigate(NavRoutes.HOME) {
                        popUpTo(NavRoutes.DASHBOARD) { inclusive = true }
                    }
                }
            )
        }
    }
}

// Screen de ejemplo para el dashboard
@Composable
fun DashboardScreen(onLogout: () -> Unit) {
    // Implementa tu pantalla principal aquí
}
package com.christian.nutriplan.ui.navigation

import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.platform.LocalContext
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.christian.nutriplan.network.UserRepository
import com.christian.nutriplan.ui.screens.DashboardScreen
import com.christian.nutriplan.ui.screens.GoalSelectionScreen
import com.christian.nutriplan.ui.screens.HomeScreen
import com.christian.nutriplan.ui.screens.LoginScreen
import com.christian.nutriplan.ui.screens.RegisterScreen
import com.christian.nutriplan.utils.AuthManager
import org.koin.compose.koinInject

object NavRoutes {
    const val HOME = "home"
    const val LOGIN = "login"
    const val REGISTER = "register"
    const val DASHBOARD = "dashboard"
    const val GOAL_SELECTION = "goal_selection"
}

@Composable
fun AppNavigation() {
    val navController = rememberNavController()
    val userRepository: UserRepository = koinInject()
    val context = LocalContext.current
    val authManager: AuthManager = koinInject()

    // Verificar estado de autenticación al iniciar
    LaunchedEffect(Unit) {
        if (authManager.isLoggedIn(context)) {
            navController.navigate(NavRoutes.DASHBOARD) {
                popUpTo(NavRoutes.HOME) { inclusive = true }
            }
        }
    }

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
                onLoginSuccess = {
                    navController.navigate(NavRoutes.GOAL_SELECTION) {
                        popUpTo(NavRoutes.HOME) { inclusive = true }
                    }
                },
                onRegisterClick = { navController.navigate(NavRoutes.REGISTER) }
            )
        }

        composable(NavRoutes.REGISTER) {
            RegisterScreen(
                onRegisterSuccess = {
                    navController.navigate(NavRoutes.GOAL_SELECTION) {
                        popUpTo(NavRoutes.HOME) { inclusive = true }
                    }
                },
                onLoginClick = {
                    navController.popBackStack()
                    navController.navigate(NavRoutes.LOGIN)
                }
            )
        }

        composable(NavRoutes.GOAL_SELECTION) {
            GoalSelectionScreen(
                onGoalSelected = {
                    // Guardar la selección localmente si es necesario
                    navController.navigate(NavRoutes.DASHBOARD) {
                        popUpTo(NavRoutes.HOME) { inclusive = true }
                    }
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
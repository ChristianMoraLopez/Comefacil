package com.christian.nutriplan.ui.navigation

import RegisterScreen
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.platform.LocalContext
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.christian.nutriplan.models.MealType
import com.christian.nutriplan.network.UserRepository
import com.christian.nutriplan.ui.screens.AboutMeScreen
import com.christian.nutriplan.ui.screens.DashboardScreen
import com.christian.nutriplan.ui.screens.EditProfileScreen
import com.christian.nutriplan.ui.screens.GoalSelectionScreen
import com.christian.nutriplan.ui.screens.HomeScreen
import com.christian.nutriplan.ui.screens.LoginScreen
import com.christian.nutriplan.ui.screens.RecipeDetailScreen
import com.christian.nutriplan.ui.screens.RecipeListScreen
import com.christian.nutriplan.utils.AuthManager
import org.koin.compose.koinInject
import android.Manifest
import android.widget.Toast

object NavRoutes {
    const val HOME = "home"
    const val LOGIN = "login"
    const val REGISTER = "register"
    const val DASHBOARD = "dashboard"
    const val GOAL_SELECTION = "goal_selection"
    const val ABOUT_ME = "about_me"
    const val RECIPE_LIST = "recipe_list/{mealType}"
    const val RECIPE_DETAIL = "recipe_detail/{recetaId}"
    const val EDIT_PROFILE = "edit_profile/{userId}"
}

@Composable
fun AppNavigation() {
    val navController = rememberNavController()
    val userRepository: UserRepository = koinInject()
    val context = LocalContext.current
    val authManager: AuthManager = koinInject()

    // State to track if permissions have been requested
    var permissionsRequested by remember { mutableStateOf(false) }

    // Permission launcher for location permissions
    val permissionLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.RequestMultiplePermissions()
    ) { permissions ->
        val fineLocationGranted = permissions[Manifest.permission.ACCESS_FINE_LOCATION] ?: false
        val coarseLocationGranted = permissions[Manifest.permission.ACCESS_COARSE_LOCATION] ?: false

        if (fineLocationGranted || coarseLocationGranted) {
            // Permissions granted, proceed with navigation
            if (authManager.isLoggedIn(context)) {
                navController.navigate(NavRoutes.DASHBOARD) {
                    popUpTo(NavRoutes.HOME) { inclusive = true }
                }
            }
        } else {
            // Permissions denied, show a toast or handle accordingly
            Toast.makeText(context, "Location permission is required for full functionality", Toast.LENGTH_LONG).show()
            // Optionally proceed to dashboard even if permissions are denied
            if (authManager.isLoggedIn(context)) {
                navController.navigate(NavRoutes.DASHBOARD) {
                    popUpTo(NavRoutes.HOME) { inclusive = true }
                }
            }
        }
        permissionsRequested = true
    }

    // Request permissions when the composable is first launched
    LaunchedEffect(Unit) {
        if (!permissionsRequested) {
            permissionLauncher.launch(
                arrayOf(
                    Manifest.permission.ACCESS_FINE_LOCATION,
                    Manifest.permission.ACCESS_COARSE_LOCATION
                )
            )
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
                },
                navController = navController
            )
        }

        composable(NavRoutes.RECIPE_LIST) { backStackEntry ->
            val mealType = backStackEntry.arguments?.getString("mealType")?.let { MealType.valueOf(it) } ?: MealType.BREAKFAST
            RecipeListScreen(
                mealType = mealType,
                navController = navController
            )
        }

        composable(NavRoutes.RECIPE_DETAIL) { backStackEntry ->
            val recetaId = backStackEntry.arguments?.getString("recetaId")?.toIntOrNull() ?: 0
            RecipeDetailScreen(
                recetaId = recetaId,
                navController = navController
            )
        }

        composable(
            route = NavRoutes.EDIT_PROFILE,
            arguments = listOf(navArgument("userId") { type = NavType.IntType; defaultValue = 0 })
        ) { backStackEntry ->
            val userId = backStackEntry.arguments?.getInt("userId") ?: 0
            EditProfileScreen(
                navController = navController,
                userId = userId
            )
        }
        composable(NavRoutes.ABOUT_ME) {
            AboutMeScreen(navController = navController)
        }
    }
}
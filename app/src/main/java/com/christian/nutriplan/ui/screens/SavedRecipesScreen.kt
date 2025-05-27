package com.christian.nutriplan.ui.screens

import android.content.Context
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import com.christian.nutriplan.R
import com.christian.nutriplan.models.RecetaGuardada
import com.christian.nutriplan.ui.theme.*
import com.christian.nutriplan.utils.AuthManager
import com.christian.nutriplan.viewmodels.SavedRecipesViewModel
import org.koin.compose.koinInject

@Composable
fun SavedRecipesScreen(
    navController: NavController,
    viewModel: SavedRecipesViewModel = koinInject(),
    authManager: AuthManager = koinInject(),
    context: Context = LocalContext.current
) {
    val savedRecipes by viewModel.savedRecipes.collectAsState()
    val isLoading by viewModel.isLoading.collectAsState()
    val errorMessage by viewModel.errorMessage.collectAsState()
    val snackbarHostState = remember { SnackbarHostState() }
    val userId = authManager.getUserId(context)

    LaunchedEffect(Unit) {
        if (userId != null) {
            viewModel.fetchSavedRecipes(userId, authManager.getAccessToken(context) ?: "")
        } else {
            viewModel.setErrorMessage("Usuario no autenticado")
        }
    }

    LaunchedEffect(errorMessage) {
        errorMessage?.let {
            snackbarHostState.showSnackbar(it)
            viewModel.clearErrorMessage()
        }
    }

    SavedRecipesContent(
        savedRecipes = savedRecipes,
        isLoading = isLoading,
        snackbarHostState = snackbarHostState,
        onBackClick = { navController.popBackStack() },
        onRecipeClick = { recetaId ->
            navController.navigate("recipe_detail/$recetaId")
        },
        onDeleteRecipe = { guardadoId ->
            if (userId != null) {
                viewModel.deleteSavedRecipe(
                    guardadoId = guardadoId,
                    userId = userId,
                    token = authManager.getAccessToken(context) ?: ""
                )
            }
        }
    )
}
@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun SavedRecipesContent(
    savedRecipes: List<RecetaGuardada>,
    isLoading: Boolean,
    snackbarHostState: SnackbarHostState,
    onBackClick: () -> Unit,
    onRecipeClick: (Int) -> Unit,
    onDeleteRecipe: (Int) -> Unit
) {
    val infiniteTransition = rememberInfiniteTransition(label = "loading")
    val rotation by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 360f,
        animationSpec = infiniteRepeatable(
            animation = tween(1500, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "rotation"
    )

    Scaffold(
        snackbarHost = { SnackbarHost(hostState = snackbarHostState) },
        containerColor = Cream400,
        topBar = {
            TopAppBar(
                title = { Text(stringResource(R.string.saved_recipes_title), color = Green800) },
                navigationIcon = {
                    IconButton(onClick = onBackClick) {
                        Icon(Icons.Default.ArrowBack, contentDescription = stringResource(R.string.back_button))
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = Cream300)
            )
        }
    ) { padding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding),
            contentAlignment = Alignment.Center // Center content in the Box
        ) {
            if (isLoading) {
                LoadingIndicator(
                    modifier = Modifier, // No need for align here
                    rotation = rotation
                )
            } else if (savedRecipes.isEmpty()) {
                EmptySavedRecipesMessage(padding)
            } else {
                SavedRecipesList(
                    savedRecipes = savedRecipes,
                    onRecipeClick = onRecipeClick,
                    onDeleteRecipe = onDeleteRecipe,
                    padding = padding
                )
            }
        }
    }
}

@Composable
private fun SavedRecipesList(
    savedRecipes: List<RecetaGuardada>,
    onRecipeClick: (Int) -> Unit,
    onDeleteRecipe: (Int) -> Unit,
    padding: PaddingValues
) {
    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .padding(padding)
            .padding(horizontal = 16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        items(savedRecipes) { recetaGuardada ->
            SavedRecipeCard(
                recetaGuardada = recetaGuardada,
                onRecipeClick = onRecipeClick,
                onDeleteRecipe = onDeleteRecipe
            )
        }
    }
}

@Composable
private fun SavedRecipeCard(
    recetaGuardada: RecetaGuardada,
    onRecipeClick: (Int) -> Unit,
    onDeleteRecipe: (Int) -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onRecipeClick(recetaGuardada.recetaId) },
        colors = CardDefaults.cardColors(containerColor = Cream100),
        shape = RoundedCornerShape(12.dp),
        elevation = CardDefaults.cardElevation(2.dp),
        border = BorderStroke(1.dp, Green400)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .size(40.dp)
                    .clip(CircleShape)
                    .background(Green200),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = Icons.Default.Favorite,
                    contentDescription = null,
                    tint = Green700,
                    modifier = Modifier.size(24.dp)
                )
            }

            Spacer(modifier = Modifier.width(16.dp))

            Column(
                modifier = Modifier.weight(1f)
            ) {
                Text(
                    text = "Receta ID: ${recetaGuardada.recetaId}", // Ideally, fetch recipe name from Receta
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.SemiBold,
                    color = Green800
                )
                Text(
                    text = "Guardado: ${recetaGuardada.fechaGuardado}",
                    style = MaterialTheme.typography.bodySmall,
                    color = Black500
                )
                recetaGuardada.comentarioPersonal?.let {
                    Text(
                        text = "Comentario: $it",
                        style = MaterialTheme.typography.bodySmall,
                        color = Black400
                    )
                }
            }

            IconButton(onClick = { recetaGuardada.guardadoId?.let { onDeleteRecipe(it.toInt()) } }) {
                Icon(
                    imageVector = Icons.Default.Delete,
                    contentDescription = "Eliminar receta guardada",
                    tint = Red500
                )
            }
        }
    }
}

@Composable
private fun EmptySavedRecipesMessage(padding: PaddingValues) {
    Box(
        modifier = Modifier
            .fillMaxSize()
            .padding(padding)
            .padding(16.dp),
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = stringResource(R.string.no_saved_recipes),
            style = MaterialTheme.typography.bodyLarge,
            color = Black500,
            textAlign = TextAlign.Center
        )
    }
}
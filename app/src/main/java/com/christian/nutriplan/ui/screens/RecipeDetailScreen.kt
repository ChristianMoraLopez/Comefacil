package com.christian.nutriplan.ui.screens

import android.content.Context
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.spring
import androidx.compose.animation.expandVertically
import androidx.compose.animation.fadeIn
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Circle
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import com.christian.nutriplan.R
import com.christian.nutriplan.models.Receta
import com.christian.nutriplan.ui.theme.*
import com.christian.nutriplan.utils.AuthManager
import com.christian.nutriplan.viewmodels.RecipeViewModel
import org.koin.compose.koinInject
// RecipeDetailScreen.kt
@Composable
fun RecipeDetailScreen(
    recetaId: Int,
    navController: NavController,
    viewModel: RecipeViewModel = koinInject(),
    authManager: AuthManager = koinInject(),
    context: Context = LocalContext.current
) {
    val recetas by viewModel.recetas.collectAsState()
    val ingredientes by viewModel.ingredientesReceta.collectAsState()
    val errorMessage by viewModel.errorMessage.collectAsState()
    val isLoading by viewModel.isLoading.collectAsState()
    val snackbarHostState = remember { SnackbarHostState() }
    val receta = recetas.find { it.recetaId == recetaId }
    val userId = authManager.getUserId(context)

    LaunchedEffect(recetaId) {
        viewModel.fetchIngredientesForReceta(recetaId)
    }

    LaunchedEffect(errorMessage) {
        errorMessage?.let {
            snackbarHostState.showSnackbar(it)
            viewModel.clearErrorMessage()
        }
    }

    RecipeDetailContent(
        receta = receta,
        ingredientes = ingredientes,
        isLoading = isLoading,
        snackbarHostState = snackbarHostState,
        onBackClick = { navController.popBackStack() },
        onSaveRecipe = {
            if (userId == null) {
                viewModel.setErrorMessage("Usuario no autenticado")
            } else {
                viewModel.saveFavoriteRecipe(
                    recetaId = recetaId,
                    userId = userId,
                    token = authManager.getAccessToken(context) ?: ""
                )
            }
        }
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun RecipeDetailContent(
    receta: Receta?,
    ingredientes: List<RecipeViewModel.IngredienteConCantidad>,
    isLoading: Boolean,
    snackbarHostState: SnackbarHostState,
    onBackClick: () -> Unit,
    onSaveRecipe: () -> Unit
) {
    Scaffold(
        snackbarHost = { SnackbarHost(hostState = snackbarHostState) },
        containerColor = Cream400,
        topBar = {
            RecipeDetailTopBar(
                recetaNombre = receta?.nombre,
                onBackClick = onBackClick
            )
        }
    ) { padding ->
        if (isLoading) {
            LoadingIndicator()
        } else if (receta == null) {
            RecipeNotFound()
        } else {
            RecipeContent(
                receta = receta,
                ingredientes = ingredientes,
                onSaveRecipe = onSaveRecipe,
                padding = padding
            )
        }
    }
}

@Composable
private fun RecipeContent(
    receta: Receta,
    ingredientes: List<RecipeViewModel.IngredienteConCantidad>,
    onSaveRecipe: () -> Unit,
    padding: PaddingValues
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(padding)
            .padding(horizontal = 16.dp)
            .verticalScroll(rememberScrollState()),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        RecipeCard(
            receta = receta,
            ingredientes = ingredientes,
            onSaveRecipe = onSaveRecipe
        )
    }
}

@Composable
private fun RecipeCard(
    receta: Receta,
    ingredientes: List<RecipeViewModel.IngredienteConCantidad>,
    onSaveRecipe: () -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = Cream100),
        shape = RoundedCornerShape(12.dp),
        elevation = CardDefaults.cardElevation(2.dp),
        border = BorderStroke(1.dp, Green400)
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            RecipeHeader(receta)
            RecipeMetadata(receta)
            IngredientsSection(ingredientes)
            RecipeInstructions(receta)
            SaveRecipeButton(onSaveRecipe)
        }
    }
}

@Composable
private fun RecipeHeader(receta: Receta) {
    RecipeImage(receta)
    RecipeTitle(receta)
}

@Composable
private fun RecipeImage(receta: Receta) {
    Box(
        modifier = Modifier
            .size(80.dp)
            .clip(CircleShape)
            .background(Green200),
        contentAlignment = Alignment.Center
    ) {
        Image(
            painter = painterResource(
                id = if (receta.recetaId!! % 2 == 0) R.drawable.receta else R.drawable.receta2
            ),
            contentDescription = receta.nombre,
            modifier = Modifier.size(50.dp)
        )
    }
    Spacer(modifier = Modifier.height(16.dp))
}

@Composable
private fun RecipeTitle(receta: Receta) {
    Text(
        text = receta.nombre,
        style = MaterialTheme.typography.headlineSmall,
        fontWeight = FontWeight.Bold,
        color = Green800,
        textAlign = TextAlign.Center
    )
    Spacer(modifier = Modifier.height(8.dp))
}

@Composable
private fun RecipeMetadata(receta: Receta) {
    Column {
        Text(
            text = "Tiempo: ${receta.tiempoPreparacion ?: "N/A"} min",
            style = MaterialTheme.typography.bodyLarge,
            color = Black500
        )
        Text(
            text = if (receta.fit) "Fit" else "Normal",
            style = MaterialTheme.typography.bodyLarge,
            color = if (receta.fit) Green600 else Black500
        )
    }
    Spacer(modifier = Modifier.height(16.dp))
}

@Composable
private fun IngredientsSection(ingredientes: List<RecipeViewModel.IngredienteConCantidad>) {
    Column {
        Text(
            text = stringResource(R.string.ingredients_title),
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.SemiBold,
            color = Green700
        )
        Spacer(modifier = Modifier.height(8.dp))

        if (ingredientes.isEmpty()) {
            EmptyIngredientsMessage()
        } else {
            IngredientsList(ingredientes)
        }
    }
    Spacer(modifier = Modifier.height(16.dp))
}

@Composable
private fun IngredientsList(ingredientes: List<RecipeViewModel.IngredienteConCantidad>) {
    Column(
        modifier = Modifier.padding(start = 8.dp)
    ) {
        ingredientes.forEach { ingrediente ->
            IngredientItem(ingrediente)
            Spacer(modifier = Modifier.height(4.dp))
        }
    }
}

@Composable
private fun IngredientItem(ingrediente: RecipeViewModel.IngredienteConCantidad) {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        modifier = Modifier.padding(vertical = 4.dp)
    ) {
        Icon(
            imageVector = Icons.Default.Circle,
            contentDescription = null,
            modifier = Modifier.size(8.dp),
            tint = Green600
        )
        Spacer(modifier = Modifier.width(8.dp))
        Text(
            text = buildFormattedIngredientText(ingrediente),
            style = MaterialTheme.typography.bodyMedium,
            color = Black500
        )
    }
}

private fun buildFormattedIngredientText(ingrediente: RecipeViewModel.IngredienteConCantidad): String {
    return buildString {
        ingrediente.cantidad?.let {
            append(if (it % 1.0 == 0.0) "${it.toInt()} " else "%.2f ".format(it).replace(",", "."))
        }
        ingrediente.unidad?.let { append("$it ") }
        append(ingrediente.nombre)
    }.trim().replace(" .", ".")
}

@Composable
private fun RecipeInstructions(receta: Receta) {
    Column {
        Text(
            text = stringResource(R.string.instructions_title),
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.SemiBold,
            color = Green700
        )
        Spacer(modifier = Modifier.height(8.dp))
        Text(
            text = receta.instrucciones ?: "No disponibles",
            style = MaterialTheme.typography.bodyMedium,
            color = Black500
        )
    }
    Spacer(modifier = Modifier.height(24.dp))
}

@Composable
private fun SaveRecipeButton(onSaveRecipe: () -> Unit) {
    Button(
        onClick = onSaveRecipe,
        colors = ButtonDefaults.buttonColors(
            containerColor = Green600,
            contentColor = Cream100
        ),
        shape = RoundedCornerShape(8.dp),
        modifier = Modifier.fillMaxWidth()
    ) {
        Icon(Icons.Default.Favorite, contentDescription = null)
        Spacer(modifier = Modifier.width(8.dp))
        Text(stringResource(R.string.save_recipe_button))
    }
}

// Componentes reutilizables
@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun RecipeDetailTopBar(recetaNombre: String?, onBackClick: () -> Unit) {
    TopAppBar(
        title = { Text(recetaNombre ?: stringResource(R.string.recipe_detail_title)) },
        navigationIcon = {
            IconButton(onClick = onBackClick) {
                Icon(Icons.Default.ArrowBack, contentDescription = stringResource(R.string.back_button))
            }
        },
        colors = TopAppBarDefaults.topAppBarColors(containerColor = Cream300)
    )
}

@Composable
private fun LoadingIndicator() {
    Box(
        modifier = Modifier.fillMaxSize(),
        contentAlignment = Alignment.Center
    ) {
        CircularProgressIndicator()
    }
}

@Composable
private fun RecipeNotFound() {
    Box(
        modifier = Modifier.fillMaxSize(),
        contentAlignment = Alignment.Center
    ) {
        Text(stringResource(R.string.recipe_not_found))
    }
}

@Composable
private fun EmptyIngredientsMessage() {
    Text(
        text = "No se encontraron ingredientes",
        style = MaterialTheme.typography.bodyMedium,
        color = Black500
    )
}
package com.christian.nutriplan.ui.screens

import android.content.Context
import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import com.christian.nutriplan.R
import com.christian.nutriplan.models.Receta
import com.christian.nutriplan.utils.AuthManager
import com.christian.nutriplan.viewmodels.RecipeViewModel
import org.koin.compose.koinInject
import kotlinx.coroutines.delay

@Composable
fun RecipeDetailScreen(
    recetaId: Int,
    navController: NavController,
    viewModel: RecipeViewModel = koinInject<RecipeViewModel>(),
    authManager: AuthManager = koinInject<AuthManager>(),
    context: Context = LocalContext.current
) {
    val recetas by viewModel.recetas.collectAsState()
    val ingredientes by viewModel.ingredientesReceta.collectAsState()
    val errorMessage by viewModel.errorMessage.collectAsState()
    val isLoading by viewModel.isLoading.collectAsState()
    val isRecipeSaved by viewModel.isRecipeSaved.collectAsState()
    val snackbarHostState = remember { SnackbarHostState() }
    val receta = recetas.find { it.recetaId == recetaId }
    val userId = authManager.getUserId(context)

    LaunchedEffect(recetaId, userId) {
        println("Fetching ingredients for recetaId: $recetaId")
        viewModel.fetchIngredientesForReceta(recetaId)
        if (userId != null) {
            viewModel.checkIfRecipeSaved(recetaId, userId) // Check if recipe is saved
        }
    }

    LaunchedEffect(ingredientes) {
        println("Ingredients loaded: $ingredientes")
    }

    LaunchedEffect(errorMessage) {
        errorMessage?.let {
            snackbarHostState.showSnackbar(it)
            if (it.contains("No autorizado") || it.contains("Usuario no autenticado")) {
                navController.navigate("login") {
                    popUpTo(navController.graph.startDestinationId) { inclusive = true }
                }
            }
            viewModel.clearErrorMessage()
        }
    }

    RecipeDetailContent(
        receta = receta,
        ingredientes = ingredientes,
        isLoading = isLoading,
        snackbarHostState = snackbarHostState,
        viewModel = viewModel,
        onBackClick = { navController.popBackStack() },
        onSaveRecipe = {
            if (userId == null) {
                viewModel.setErrorMessage("Usuario no autenticado")
            } else {
                viewModel.saveFavoriteRecipe(recetaId = recetaId, userId = userId)
            }
        },
        isRecipeSaved = isRecipeSaved
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun RecipeDetailContent(
    receta: Receta?,
    ingredientes: List<RecipeViewModel.IngredienteConCantidad>,
    isLoading: Boolean,
    snackbarHostState: SnackbarHostState,
    viewModel: RecipeViewModel,
    onBackClick: () -> Unit,
    onSaveRecipe: () -> Unit,
    isRecipeSaved: Boolean
) {
    var showTutorialDialog by remember { mutableStateOf(false) }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(
                brush = Brush.verticalGradient(
                    colors = listOf(
                        Color(0xFFF8F5F0),
                        Color(0xFFE8F5E8),
                        Color(0xFFD0F0C0)
                    )
                )
            )
    ) {
        Scaffold(
            snackbarHost = {
                SnackbarHost(hostState = snackbarHostState) { data ->
                    ModernSnackbar(data)
                }
            },
            containerColor = Color.Transparent,
            topBar = {
                ModernTopBar(
                    recetaNombre = receta?.nombre,
                    onBackClick = onBackClick
                )
            }
        ) { padding ->
            if (isLoading) {
                ModernLoadingIndicator()
            } else if (receta == null) {
                RecipeNotFound()
            } else {
                RecipeContent(
                    receta = receta,
                    ingredientes = ingredientes,
                    viewModel = viewModel,
                    onSaveRecipe = onSaveRecipe,
                    onTutorialClick = { showTutorialDialog = true },
                    padding = padding,
                    isRecipeSaved = isRecipeSaved
                )
            }
        }
    }

    if (showTutorialDialog) {
        TutorialDialog(
            onDismiss = { showTutorialDialog = false }
        )
    }
}

@Composable
private fun ModernSnackbar(data: SnackbarData) {
    Snackbar(
        modifier = Modifier
            .padding(16.dp)
            .shadow(
                elevation = 8.dp,
                shape = RoundedCornerShape(16.dp)
            ),
        containerColor = if (data.visuals.message == "Receta guardada con éxito")
            Color(0xFF4CAF50) else Color(0xFFF44336),
        contentColor = Color.White,
        shape = RoundedCornerShape(16.dp),
        action = {
            data.visuals.actionLabel?.let { label ->
                TextButton(
                    onClick = { data.performAction() },
                    colors = ButtonDefaults.textButtonColors(
                        contentColor = Color.White
                    )
                ) {
                    Text(
                        text = label,
                        fontWeight = FontWeight.Bold
                    )
                }
            }
        }
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                imageVector = if (data.visuals.message == "Receta guardada con éxito")
                    Icons.Default.CheckCircle else Icons.Default.Error,
                contentDescription = null,
                modifier = Modifier.size(20.dp)
            )
            Spacer(modifier = Modifier.width(8.dp))
            Text(
                text = data.visuals.message,
                fontWeight = FontWeight.Medium
            )
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun ModernTopBar(recetaNombre: String?, onBackClick: () -> Unit) {
    TopAppBar(
        title = {
            Text(
                text = recetaNombre ?: stringResource(R.string.recipe_detail_title),
                fontWeight = FontWeight.Bold,
                color = Color(0xFF2E7D32)
            )
        },
        navigationIcon = {
            IconButton(
                onClick = onBackClick,
                modifier = Modifier
                    .padding(8.dp)
                    .background(
                        color = Color.White.copy(alpha = 0.9f),
                        shape = CircleShape
                    )
                    .shadow(4.dp, CircleShape)
            ) {
                Icon(
                    Icons.Default.ArrowBack,
                    contentDescription = stringResource(R.string.back_button),
                    tint = Color(0xFF2E7D32)
                )
            }
        },
        colors = TopAppBarDefaults.topAppBarColors(
            containerColor = Color.Transparent
        )
    )
}

@Composable
private fun RecipeContent(
    receta: Receta,
    ingredientes: List<RecipeViewModel.IngredienteConCantidad>,
    viewModel: RecipeViewModel,
    onSaveRecipe: () -> Unit,
    onTutorialClick: () -> Unit,
    padding: PaddingValues,
    isRecipeSaved: Boolean // Add parameter
) {
    var isVisible by remember { mutableStateOf(false) }

    LaunchedEffect(Unit) {
        delay(100)
        isVisible = true
    }

    AnimatedVisibility(
        visible = isVisible,
        enter = slideInVertically(
            initialOffsetY = { it },
            animationSpec = spring(
                dampingRatio = Spring.DampingRatioMediumBouncy,
                stiffness = Spring.StiffnessLow
            )
        ) + fadeIn()
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(horizontal = 16.dp)
                .verticalScroll(rememberScrollState()),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            ModernRecipeCard(
                receta = receta,
                ingredientes = ingredientes,
                viewModel = viewModel,
                onSaveRecipe = onSaveRecipe,
                onTutorialClick = onTutorialClick,
                isRecipeSaved = isRecipeSaved // Pass the saved state
            )
        }
    }
}

@Composable
private fun ModernRecipeCard(
    receta: Receta,
    ingredientes: List<RecipeViewModel.IngredienteConCantidad>,
    viewModel: RecipeViewModel,
    onSaveRecipe: () -> Unit,
    onTutorialClick: () -> Unit,
    isRecipeSaved: Boolean // Add parameter
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .shadow(
                elevation = 12.dp,
                shape = RoundedCornerShape(24.dp),
                spotColor = Color(0xFF4CAF50).copy(alpha = 0.3f)
            ),
        colors = CardDefaults.cardColors(
            containerColor = Color.White.copy(alpha = 0.95f)
        ),
        shape = RoundedCornerShape(24.dp),
        border = BorderStroke(
            width = 2.dp,
            brush = Brush.horizontalGradient(
                colors = listOf(
                    Color(0xFF81C784),
                    Color(0xFF66BB6A),
                    Color(0xFF4CAF50)
                )
            )
        )
    ) {
        Column(
            modifier = Modifier.padding(24.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            ModernRecipeHeader(receta)
            Spacer(modifier = Modifier.height(24.dp))
            ModernRecipeMetadata(receta)
            Spacer(modifier = Modifier.height(32.dp))
            ModernIngredientsSection(ingredientes, viewModel, receta.recetaId!!)
            Spacer(modifier = Modifier.height(32.dp))
            ModernRecipeInstructions(receta)
            Spacer(modifier = Modifier.height(32.dp))
            ModernActionButtons(
                onSaveRecipe = onSaveRecipe,
                onTutorialClick = onTutorialClick,
                isRecipeSaved = isRecipeSaved // Pass the saved state
            )
        }
    }
}

@Composable
private fun ModernRecipeHeader(receta: Receta) {
    var isImageVisible by remember { mutableStateOf(false) }

    LaunchedEffect(Unit) {
        delay(300)
        isImageVisible = true
    }

    AnimatedVisibility(
        visible = isImageVisible,
        enter = scaleIn(
            animationSpec = spring(
                dampingRatio = Spring.DampingRatioMediumBouncy
            )
        ) + fadeIn()
    ) {
        Box(
            modifier = Modifier
                .size(120.dp)
                .shadow(
                    elevation = 16.dp,
                    shape = CircleShape,
                    spotColor = Color(0xFF4CAF50).copy(alpha = 0.4f)
                )
                .background(
                    brush = Brush.radialGradient(
                        colors = listOf(
                            Color(0xFFC8E6C9),
                            Color(0xFFA5D6A7),
                            Color(0xFF81C784)
                        )
                    ),
                    shape = CircleShape
                )
                .clip(CircleShape),
            contentAlignment = Alignment.Center
        ) {
            Image(
                painter = painterResource(
                    id = if (receta.recetaId!! % 2 == 0) R.drawable.receta else R.drawable.receta2
                ),
                contentDescription = receta.nombre,
                modifier = Modifier.size(70.dp)
            )
        }
    }

    Spacer(modifier = Modifier.height(20.dp))

    Text(
        text = receta.nombre,
        style = MaterialTheme.typography.headlineMedium,
        fontWeight = FontWeight.ExtraBold,
        color = Color(0xFF1B5E20),
        textAlign = TextAlign.Center,
        modifier = Modifier.fillMaxWidth()
    )
}

@Composable
private fun ModernRecipeMetadata(receta: Receta) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceEvenly
    ) {
        MetadataChip(
            icon = Icons.Default.Schedule,
            text = "${receta.tiempoPreparacion ?: "N/A"} min",
            color = Color(0xFF2196F3)
        )

        MetadataChip(
            icon = if (receta.fit) Icons.Default.FitnessCenter else Icons.Default.Restaurant,
            text = if (receta.fit) "Fit" else "Normal",
            color = if (receta.fit) Color(0xFF4CAF50) else Color(0xFF9E9E9E)
        )
    }
}

@Composable
private fun MetadataChip(
    icon: ImageVector,
    text: String,
    color: Color
) {
    Card(
        colors = CardDefaults.cardColors(
            containerColor = color.copy(alpha = 0.1f)
        ),
        shape = RoundedCornerShape(20.dp),
        border = BorderStroke(1.dp, color.copy(alpha = 0.3f))
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                tint = color,
                modifier = Modifier.size(18.dp)
            )
            Spacer(modifier = Modifier.width(6.dp))
            Text(
                text = text,
                style = MaterialTheme.typography.bodyMedium,
                fontWeight = FontWeight.SemiBold,
                color = color
            )
        }
    }
}

@Composable
private fun ModernIngredientsSection(
    ingredientes: List<RecipeViewModel.IngredienteConCantidad>,
    viewModel: RecipeViewModel,
    recetaId: Int
) {
    val isLoadingIngredients by viewModel.isLoadingIngredients.collectAsState()

    Column {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier.fillMaxWidth()
        ) {
            Icon(
                imageVector = Icons.Default.List,
                contentDescription = "Lista de ingredientes",
                tint = Color(0xFF2E7D32),
                modifier = Modifier.size(24.dp)
            )
            Spacer(modifier = Modifier.width(8.dp))
            Text(
                text = stringResource(R.string.ingredients_title),
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Bold,
                color = Color(0xFF1B5E20)
            )
        }
        Spacer(modifier = Modifier.height(16.dp))
        if (isLoadingIngredients) {
            CircularProgressIndicator(
                color = Color(0xFF4CAF50),
                modifier = Modifier.size(24.dp)
            )
        } else if (ingredientes.isEmpty()) {
            EmptyIngredientsMessage(onRetry = { viewModel.fetchIngredientesForReceta(recetaId) })
        } else {
            ModernIngredientsList(ingredientes)
        }
    }
}

@Composable
private fun ModernIngredientsList(ingredientes: List<RecipeViewModel.IngredienteConCantidad>) {
    Card(
        colors = CardDefaults.cardColors(
            containerColor = Color(0xFFF1F8E9)
        ),
        shape = RoundedCornerShape(16.dp),
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(
            modifier = Modifier.padding(16.dp)
        ) {
            ingredientes.forEachIndexed { index, ingrediente ->
                ModernIngredientItem(ingrediente, index)
                if (index < ingredientes.size - 1) {
                    Spacer(modifier = Modifier.height(12.dp))
                }
            }
        }
    }
}

@Composable
private fun ModernIngredientItem(
    ingrediente: RecipeViewModel.IngredienteConCantidad,
    index: Int
) {
    var isVisible by remember { mutableStateOf(false) }

    LaunchedEffect(Unit) {
        delay(100L * index)
        isVisible = true
    }

    AnimatedVisibility(
        visible = isVisible,
        enter = slideInHorizontally(
            initialOffsetX = { -it },
            animationSpec = spring(dampingRatio = Spring.DampingRatioMediumBouncy)
        ) + fadeIn()
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier
                .fillMaxWidth()
                .background(
                    color = Color.White.copy(alpha = 0.7f),
                    shape = RoundedCornerShape(12.dp)
                )
                .padding(12.dp)
        ) {
            Box(
                modifier = Modifier
                    .size(32.dp)
                    .background(
                        color = Color(0xFF4CAF50),
                        shape = CircleShape
                    ),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = (index + 1).toString(),
                    color = Color.White,
                    fontWeight = FontWeight.Bold,
                    style = MaterialTheme.typography.bodySmall
                )
            }

            Spacer(modifier = Modifier.width(12.dp))

            Text(
                text = buildFormattedIngredientText(ingrediente),
                style = MaterialTheme.typography.bodyLarge,
                color = Color(0xFF2E2E2E),
                fontWeight = FontWeight.Medium,
                modifier = Modifier.weight(1f)
            )
        }
    }
}

@Composable
private fun ModernRecipeInstructions(receta: Receta) {
    Column {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier.fillMaxWidth()
        ) {
            Icon(
                imageVector = Icons.Default.Description,
                contentDescription = "Instrucciones de la receta",
                tint = Color(0xFF2E7D32),
                modifier = Modifier.size(24.dp)
            )
            Spacer(modifier = Modifier.width(8.dp))
            Text(
                text = stringResource(R.string.instructions_title),
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Bold,
                color = Color(0xFF1B5E20)
            )
        }

        Spacer(modifier = Modifier.height(16.dp))

        Card(
            colors = CardDefaults.cardColors(
                containerColor = Color(0xFFF3E5F5).copy(alpha = 0.6f)
            ),
            shape = RoundedCornerShape(16.dp),
            modifier = Modifier.fillMaxWidth()
        ) {
            Text(
                text = receta.instrucciones ?: "No disponibles",
                style = MaterialTheme.typography.bodyLarge,
                color = Color(0xFF424242),
                modifier = Modifier.padding(20.dp),
                lineHeight = MaterialTheme.typography.bodyLarge.lineHeight * 1.3
            )
        }
    }
}

@Composable
private fun ModernActionButtons(
    onSaveRecipe: () -> Unit,
    onTutorialClick: () -> Unit,
    isRecipeSaved: Boolean // Add parameter
) {
    Column(
        modifier = Modifier.fillMaxWidth(),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Button(
            onClick = onSaveRecipe,
            enabled = !isRecipeSaved, // Disable button if recipe is saved
            colors = ButtonDefaults.buttonColors(
                containerColor = Color.Transparent,
                disabledContainerColor = Color.Gray.copy(alpha = 0.5f) // Optional: Gray out when disabled
            ),
            shape = RoundedCornerShape(16.dp),
            modifier = Modifier
                .fillMaxWidth()
                .height(56.dp)
                .background(
                    brush = Brush.horizontalGradient(
                        colors = listOf(
                            Color(0xFF66BB6A),
                            Color(0xFF4CAF50),
                            Color(0xFF388E3C)
                        )
                    ),
                    shape = RoundedCornerShape(16.dp)
                )
                .shadow(8.dp, RoundedCornerShape(16.dp)),
            contentPadding = PaddingValues(0.dp)
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.Center
            ) {
                Icon(
                    Icons.Default.Favorite,
                    contentDescription = "Guardar receta",
                    tint = if (isRecipeSaved) Color.White.copy(alpha = 0.7f) else Color.White,
                    modifier = Modifier.size(24.dp)
                )
                Spacer(modifier = Modifier.width(12.dp))
                Text(
                    text = stringResource(R.string.save_recipe_button),
                    color = if (isRecipeSaved) Color.White.copy(alpha = 0.7f) else Color.White,
                    fontWeight = FontWeight.Bold,
                    style = MaterialTheme.typography.titleMedium
                )
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        OutlinedButton(
            onClick = onTutorialClick,
            colors = ButtonDefaults.outlinedButtonColors(
                contentColor = Color(0xFF7B1FA2)
            ),
            border = BorderStroke(
                width = 2.dp,
                brush = Brush.horizontalGradient(
                    colors = listOf(
                        Color(0xFF9C27B0),
                        Color(0xFF7B1FA2),
                        Color(0xFF6A1B9A)
                    )
                )
            ),
            shape = RoundedCornerShape(16.dp),
            modifier = Modifier
                .fillMaxWidth()
                .height(56.dp)
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.Center
            ) {
                Icon(
                    Icons.Default.QuestionMark,
                    contentDescription = "Ver tutorial",
                    tint = Color(0xFF7B1FA2),
                    modifier = Modifier.size(24.dp)
                )
                Spacer(modifier = Modifier.width(12.dp))
                Text(
                    text = "¿Y cómo se hace esa vuelta?",
                    fontWeight = FontWeight.SemiBold,
                    style = MaterialTheme.typography.titleMedium
                )
            }
        }
    }
}


@Composable
private fun TutorialDialog(onDismiss: () -> Unit) {
    AlertDialog(
        onDismissRequest = onDismiss,
        confirmButton = {
            TextButton(
                onClick = onDismiss,
                colors = ButtonDefaults.textButtonColors(
                    contentColor = Color(0xFF4CAF50)
                )
            ) {
                Text(
                    "¡Entendido!",
                    fontWeight = FontWeight.Bold
                )
            }
        },
        title = {
            Row(
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(
                    Icons.Default.Construction,
                    contentDescription = "En construcción",
                    tint = Color(0xFFFF9800),
                    modifier = Modifier.size(28.dp)
                )
                Spacer(modifier = Modifier.width(12.dp))
                Text(
                    "¡Ups! 🚧",
                    fontWeight = FontWeight.Bold,
                    color = Color(0xFF2E2E2E)
                )
            }
        },
        text = {
            Column {
                Text(
                    "Uy esto todavía no está construido, estamos en ello 🔨",
                    style = MaterialTheme.typography.bodyLarge,
                    color = Color(0xFF424242)
                )
                Spacer(modifier = Modifier.height(12.dp))
                Text(
                    "Si quieres que lo hagamos más rápido, ¡dónanos una liguita! ☕️💰",
                    style = MaterialTheme.typography.bodyMedium,
                    color = Color(0xFF666666),
                    fontWeight = FontWeight.Medium
                )
            }
        },
        shape = RoundedCornerShape(20.dp),
        containerColor = Color.White,
        modifier = Modifier.shadow(
            elevation = 16.dp,
            shape = RoundedCornerShape(20.dp)
        )
    )
}

@Composable
internal fun ModernLoadingIndicator() {
    Box(
        modifier = Modifier.fillMaxSize(),
        contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            CircularProgressIndicator(
                color = Color(0xFF4CAF50),
                strokeWidth = 4.dp,
                modifier = Modifier.size(56.dp)
            )
            Spacer(modifier = Modifier.height(16.dp))
            Text(
                "Cargando receta deliciosa...",
                style = MaterialTheme.typography.bodyLarge,
                color = Color(0xFF2E7D32),
                fontWeight = FontWeight.Medium
            )
        }
    }
}

@Composable
private fun RecipeNotFound() {
    Box(
        modifier = Modifier.fillMaxSize(),
        contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Icon(
                Icons.Default.SearchOff,
                contentDescription = "Receta no encontrada",
                tint = Color(0xFF9E9E9E),
                modifier = Modifier.size(64.dp)
            )
            Spacer(modifier = Modifier.height(16.dp))
            Text(
                stringResource(R.string.recipe_not_found),
                style = MaterialTheme.typography.titleLarge,
                color = Color(0xFF616161),
                textAlign = TextAlign.Center
            )
        }
    }
}

@Composable
private fun EmptyIngredientsMessage(onRetry: () -> Unit) {
    Card(
        colors = CardDefaults.cardColors(
            containerColor = Color(0xFFFFF3E0)
        ),
        shape = RoundedCornerShape(12.dp)
    ) {
        Row(
            modifier = Modifier.padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                Icons.Default.Warning,
                contentDescription = "Advertencia",
                tint = Color(0xFFFF9800),
                modifier = Modifier.size(24.dp)
            )
            Spacer(modifier = Modifier.width(12.dp))
            Text(
                text = "No se encontraron ingredientes",
                style = MaterialTheme.typography.bodyMedium,
                color = Color(0xFF5D4037),
                fontWeight = FontWeight.Medium,
                modifier = Modifier.weight(1f)
            )
            TextButton(onClick = onRetry) {
                Text("Reintentar", color = Color(0xFF4CAF50))
            }
        }
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
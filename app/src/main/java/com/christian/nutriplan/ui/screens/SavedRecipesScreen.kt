package com.christian.nutriplan.ui.screens

import android.content.Context
import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
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
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.draw.scale
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import com.christian.nutriplan.R
import com.christian.nutriplan.models.responses.ApiResponse
import com.christian.nutriplan.ui.theme.*
import com.christian.nutriplan.utils.AuthManager
import com.christian.nutriplan.viewmodels.SavedRecipesViewModel
import kotlinx.coroutines.delay
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
    var showContent by remember { mutableStateOf(false) }
    val snackbarHostState = remember { SnackbarHostState() }
    val userId = authManager.getUserId(context)
    val token = authManager.getAccessToken(context)

    LaunchedEffect(Unit) {
        if (userId != null && token != null) {
            viewModel.fetchSavedRecipes(userId, token)
        } else {
            viewModel.setErrorMessage("Usuario no autenticado")
        }
        delay(300)
        showContent = true
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

    SavedRecipesContent(
        savedRecipes = savedRecipes,
        isLoading = isLoading,
        showContent = showContent,
        snackbarHostState = snackbarHostState,
        onBackClick = { navController.popBackStack() },
        onRecipeClick = { recetaId ->
            navController.navigate("recipe_detail/$recetaId")
        },
        onDeleteClick = { guardadoId ->
            if (token != null) {
                viewModel.deleteSavedRecipe(guardadoId, token)
            } else {
                viewModel.setErrorMessage("No autenticado")
            }
        }
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun SavedRecipesContent(
    savedRecipes: List<ApiResponse.RecetaGuardadaResponse>,
    isLoading: Boolean,
    showContent: Boolean,
    snackbarHostState: SnackbarHostState,
    onBackClick: () -> Unit,
    onRecipeClick: (Int) -> Unit,
    onDeleteClick: (Int) -> Unit
) {
    val infiniteTransition = rememberInfiniteTransition(label = "loading")
    val loadingRotation by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 360f,
        animationSpec = infiniteRepeatable(
            animation = tween(1500, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "rotation"
    )

    Scaffold(
        snackbarHost = {
            SnackbarHost(hostState = snackbarHostState) { data ->
                AnimatedSnackbar(data = data)
            }
        },
        containerColor = Cream400,
        topBar = {
            AnimatedTopBar(
                title = "Recetas Guardadas",
                onBackClick = onBackClick
            )
        }
    ) { padding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
        ) {
            if (isLoading) {
                LoadingIndicator(
                    modifier = Modifier.align(Alignment.Center),
                    rotation = loadingRotation
                )
            } else {
                AnimatedVisibility(
                    visible = showContent,
                    enter = fadeIn(
                        animationSpec = tween(800, easing = FastOutSlowInEasing)
                    ) + slideInVertically(
                        initialOffsetY = { it / 3 },
                        animationSpec = tween(800, easing = FastOutSlowInEasing)
                    )
                ) {
                    SavedRecipesList(
                        savedRecipes = savedRecipes,
                        onRecipeClick = onRecipeClick,
                        onDeleteClick = onDeleteClick
                    )
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun AnimatedTopBar(
    title: String,
    onBackClick: () -> Unit
) {
    var isVisible by remember { mutableStateOf(false) }

    LaunchedEffect(Unit) {
        isVisible = true
    }

    AnimatedVisibility(
        visible = isVisible,
        enter = slideInVertically(
            initialOffsetY = { -it },
            animationSpec = tween(600, easing = FastOutSlowInEasing)
        ) + fadeIn(animationSpec = tween(600))
    ) {
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 8.dp)
                .shadow(8.dp, RoundedCornerShape(20.dp)),
            colors = CardDefaults.cardColors(containerColor = Cream100),
            shape = RoundedCornerShape(20.dp),
            border = BorderStroke(1.dp, Green300.copy(alpha = 0.3f))
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(20.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                IconButton(
                    onClick = onBackClick,
                    modifier = Modifier
                        .size(48.dp)
                        .clip(CircleShape)
                        .background(
                            Brush.radialGradient(
                                colors = listOf(Green200.copy(alpha = 0.8f), Green300.copy(alpha = 0.4f))
                            )
                        )
                ) {
                    Icon(
                        imageVector = Icons.Default.ArrowBack,
                        contentDescription = stringResource(R.string.back_button),
                        tint = Green800,
                        modifier = Modifier.size(24.dp)
                    )
                }

                Spacer(modifier = Modifier.width(16.dp))

                Text(
                    text = title,
                    style = MaterialTheme.typography.headlineSmall,
                    fontWeight = FontWeight.Bold,
                    color = Green800
                )
            }
        }
    }
}

@Composable
private fun SavedRecipesList(
    savedRecipes: List<ApiResponse.RecetaGuardadaResponse>,
    onRecipeClick: (Int) -> Unit,
    onDeleteClick: (Int) -> Unit
) {
    if (savedRecipes.isEmpty()) {
        EmptyState()
    } else {
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 16.dp),
            verticalArrangement = Arrangement.spacedBy(20.dp),
            contentPadding = PaddingValues(vertical = 20.dp)
        ) {
            itemsIndexed(
                items = savedRecipes,
                key = { _, receta -> receta.guardadoId ?: receta.hashCode() }
            ) { index, receta ->
                AnimatedSavedRecipeCard(
                    receta = receta,
                    index = index,
                    animationDelay = index * 120L,
                    onRecipeClick = { onRecipeClick(receta.recetaId) },
                    onDeleteClick = { receta.guardadoId?.let { onDeleteClick(it) } }
                )
            }
        }
    }
}

@Composable
private fun AnimatedSavedRecipeCard(
    receta: ApiResponse.RecetaGuardadaResponse,
    index: Int,
    animationDelay: Long,
    onRecipeClick: () -> Unit,
    onDeleteClick: () -> Unit
) {
    var isVisible by remember { mutableStateOf(false) }
    var isPressed by remember { mutableStateOf(false) }
    var showDialog by remember { mutableStateOf(false) }

    LaunchedEffect(Unit) {
        delay(animationDelay)
        isVisible = true
    }

    if (showDialog) {
        AlertDialog(
            onDismissRequest = { showDialog = false },
            title = { Text("Eliminar receta") },
            text = { Text("¿Quieres eliminar ${receta.nombreReceta} de tus guardadas?") },
            confirmButton = {
                TextButton(onClick = {
                    showDialog = false
                    onDeleteClick()
                }) { Text("Eliminar") }
            },
            dismissButton = {
                TextButton(onClick = { showDialog = false }) { Text("Cancelar") }
            }
        )
    }

    val scale by animateFloatAsState(
        targetValue = if (isPressed) 0.95f else 1f,
        animationSpec = spring(
            dampingRatio = Spring.DampingRatioMediumBouncy,
            stiffness = Spring.StiffnessHigh
        ),
        label = "scale"
    )

    AnimatedVisibility(
        visible = isVisible,
        enter = slideInVertically(
            initialOffsetY = { it / 2 },
            animationSpec = tween(500, easing = FastOutSlowInEasing)
        ) + fadeIn(animationSpec = tween(500)) + scaleIn(
            initialScale = 0.8f,
            animationSpec = tween(500, easing = FastOutSlowInEasing)
        )
    ) {
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .scale(scale)
                .shadow(12.dp, RoundedCornerShape(20.dp)),
            colors = CardDefaults.cardColors(containerColor = Cream100),
            shape = RoundedCornerShape(20.dp),
            border = BorderStroke(2.dp, Green300.copy(alpha = 0.4f))
        ) {
            Column(
                modifier = Modifier.fillMaxWidth()
            ) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(8.dp)
                        .background(
                            Brush.horizontalGradient(
                                colors = listOf(Green400, Green600, Green400)
                            )
                        )
                )

                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(24.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    EnhancedRecipeAvatar(
                        receta = receta,
                        index = index
                    )

                    Spacer(modifier = Modifier.width(24.dp))

                    Column(
                        modifier = Modifier
                            .weight(1f)
                            .clickable(
                                interactionSource = remember { MutableInteractionSource() },
                                indication = null
                            ) {
                                isPressed = true
                                onRecipeClick()
                            }
                    ) {
                        Text(
                            text = receta.nombreReceta,
                            style = MaterialTheme.typography.headlineSmall,
                            fontWeight = FontWeight.Bold,
                            color = Green800,
                            maxLines = 2,
                            overflow = TextOverflow.Ellipsis
                        )

                        Spacer(modifier = Modifier.height(8.dp))

                        Text(
                            text = "Guardado: ${receta.fechaGuardado ?: "N/A"}",
                            style = MaterialTheme.typography.bodyMedium,
                            color = Green600,
                            fontWeight = FontWeight.Medium
                        )
                    }

                    IconButton(
                        onClick = { showDialog = true },
                        modifier = Modifier
                            .size(40.dp)
                            .clip(CircleShape)
                            .background(Green200.copy(alpha = 0.8f))
                    ) {
                        Icon(
                            imageVector = Icons.Default.Delete,
                            contentDescription = "Eliminar receta guardada",
                            tint = Green800,
                            modifier = Modifier.size(20.dp)
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun EnhancedRecipeAvatar(
    receta: ApiResponse.RecetaGuardadaResponse,
    index: Int
) {
    val infiniteTransition = rememberInfiniteTransition(label = "avatar")
    val shimmer by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 1f,
        animationSpec = infiniteRepeatable(
            animation = tween(3000, easing = LinearEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "shimmer"
    )

    Box(
        modifier = Modifier
            .size(80.dp)
            .clip(CircleShape)
            .background(
                Brush.radialGradient(
                    colors = listOf(
                        Green200.copy(alpha = 0.9f + shimmer * 0.1f),
                        Green300.copy(alpha = 0.7f + shimmer * 0.3f),
                        Green400.copy(alpha = 0.5f + shimmer * 0.5f)
                    )
                )
            )
            .shadow(8.dp, CircleShape),
        contentAlignment = Alignment.Center
    ) {
        Box(
            modifier = Modifier
                .size(70.dp)
                .clip(CircleShape)
                .background(Cream100),
            contentAlignment = Alignment.Center
        ) {
            Image(
                painter = painterResource(
                    id = if (index % 2 == 0) R.drawable.receta else R.drawable.receta2
                ),
                contentDescription = receta.nombreReceta,
                modifier = Modifier.size(45.dp)
            )
        }
    }
}

@Composable
private fun EmptyState() {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(32.dp),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Box(
            modifier = Modifier
                .size(120.dp)
                .clip(CircleShape)
                .background(
                    Brush.radialGradient(
                        colors = listOf(Green100, Green200.copy(alpha = 0.3f))
                    )
                ),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = Icons.Default.Favorite,
                contentDescription = null,
                modifier = Modifier.size(60.dp),
                tint = Green400
            )
        }

        Spacer(modifier = Modifier.height(24.dp))

        Text(
            text = "¡No tienes recetas guardadas!",
            style = MaterialTheme.typography.headlineSmall,
            color = Green700,
            textAlign = TextAlign.Center,
            fontWeight = FontWeight.Bold
        )

        Spacer(modifier = Modifier.height(12.dp))

        Text(
            text = "Explora nuestras recetas y guarda tus favoritas para encontrarlas aquí.",
            style = MaterialTheme.typography.bodyLarge,
            color = Black500,
            textAlign = TextAlign.Center,
            lineHeight = 24.sp
        )
    }
}

@Composable
private fun AnimatedSnackbar(data: SnackbarData) {
    AnimatedVisibility(
        visible = data.visuals.message.isNotEmpty(),
        enter = slideInHorizontally { it },
        exit = slideOutHorizontally { -it }
    ) {
        Snackbar(
            modifier = Modifier.padding(24.dp),
            content = { Text(data.visuals.message) },
            action = {
                data.visuals.actionLabel?.let { label ->
                    TextButton(onClick = { data.performAction() }) {
                        Text(label)
                    }
                }
            }
        )
    }
}

@Composable
private fun CustomLoadingIndicator(
    modifier: Modifier = Modifier,
    rotation: Float
) {
    Box(
        modifier = modifier,
        contentAlignment = Alignment.Center
    ) {
        CircularProgressIndicator(
            color = Green800,
            modifier = Modifier
                .size(48.dp)
                .rotate(rotation)
        )
    }
}
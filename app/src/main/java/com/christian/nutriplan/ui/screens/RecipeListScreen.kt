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
import androidx.compose.material.icons.filled.AccessTime
import androidx.compose.material.icons.filled.Restaurant
import androidx.compose.material.icons.filled.FitnessCenter
import androidx.compose.material.icons.filled.LocalFireDepartment
import androidx.compose.material.icons.filled.Star
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.draw.scale
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
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
import com.christian.nutriplan.models.MealType
import com.christian.nutriplan.models.Receta
import com.christian.nutriplan.models.TipoComida
import com.christian.nutriplan.ui.theme.*
import com.christian.nutriplan.viewmodels.RecipeViewModel
import kotlinx.coroutines.delay
import org.koin.compose.koinInject

@Composable
fun RecipeListScreen(
    mealType: MealType,
    navController: NavController,
    viewModel: RecipeViewModel = koinInject(),
    context: Context = LocalContext.current
) {
    val recetas by viewModel.recetas.collectAsState()
    val tiposComida by viewModel.tiposComida.collectAsState()
    val isLoading by viewModel.isLoading.collectAsState()
    val errorMessage by viewModel.errorMessage.collectAsState()
    var showContent by remember { mutableStateOf(false) }
    val snackbarHostState = remember { SnackbarHostState() }

    LaunchedEffect(Unit) {
        viewModel.fetchTiposComida()
        viewModel.fetchRecetas(mealType)
        delay(300)
        showContent = true
    }

    LaunchedEffect(errorMessage) {
        errorMessage?.let {
            snackbarHostState.showSnackbar(it)
            viewModel.clearErrorMessage()
        }
    }

    RecipeListContent(
        mealType = mealType,
        recetas = recetas,
        tiposComida = tiposComida,
        isLoading = isLoading,
        showContent = showContent,
        snackbarHostState = snackbarHostState,
        onBackClick = { navController.popBackStack() },
        onRecipeClick = { recetaId ->
            navController.navigate("recipe_detail/$recetaId")
        }
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun RecipeListContent(
    mealType: MealType,
    recetas: List<Receta>,
    tiposComida: List<TipoComida>,
    isLoading: Boolean,
    showContent: Boolean,
    snackbarHostState: SnackbarHostState,
    onBackClick: () -> Unit,
    onRecipeClick: (Int) -> Unit
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
                AnimatedSnackbar(data = data, hostState = snackbarHostState)
            }
        },
        containerColor = Cream400,
        topBar = {
            AnimatedTopBar(mealType = mealType, onBackClick = onBackClick)
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
                    RecipeList(
                        recetas = recetas,
                        tiposComida = tiposComida,
                        onRecipeClick = onRecipeClick
                    )
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun AnimatedTopBar(
    mealType: MealType,
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

                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = when (mealType) {
                            MealType.BREAKFAST -> stringResource(R.string.breakfast_title)
                            MealType.LUNCH -> stringResource(R.string.lunch_title)
                            MealType.DINNER -> stringResource(R.string.dinner_title)
                            MealType.SNACK -> stringResource(R.string.snack_title)
                        },
                        style = MaterialTheme.typography.headlineSmall,
                        fontWeight = FontWeight.Bold,
                        color = Green800
                    )

                    Text(
                        text = "Descubre recetas deliciosas",
                        style = MaterialTheme.typography.bodyMedium,
                        color = Green600,
                        fontWeight = FontWeight.Medium
                    )
                }

                // Icono decorativo del tipo de comida
                Box(
                    modifier = Modifier
                        .size(50.dp)
                        .clip(CircleShape)
                        .background(
                            Brush.radialGradient(
                                colors = listOf(Green100, Green200.copy(alpha = 0.6f))
                            )
                        ),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = when (mealType) {
                            MealType.BREAKFAST -> Icons.Default.Restaurant
                            MealType.LUNCH -> Icons.Default.LocalFireDepartment
                            MealType.DINNER -> Icons.Default.Star
                            MealType.SNACK -> Icons.Default.FitnessCenter
                        },
                        contentDescription = null,
                        tint = Green700,
                        modifier = Modifier.size(28.dp)
                    )
                }
            }
        }
    }
}

@Composable
internal fun LoadingIndicator(
    modifier: Modifier = Modifier,
    rotation: Float
) {
    Column(
        modifier = modifier,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Box(
            modifier = Modifier
                .size(80.dp)
                .rotate(rotation)
                .clip(CircleShape)
                .background(
                    Brush.sweepGradient(
                        colors = listOf(
                            Green700,
                            Green400,
                            Green200,
                            Green500,
                            Green700
                        )
                    )
                ),
            contentAlignment = Alignment.Center
        ) {
            Box(
                modifier = Modifier
                    .size(60.dp)
                    .clip(CircleShape)
                    .background(Cream400)
            ) {
                Box(
                    modifier = Modifier
                        .size(40.dp)
                        .clip(CircleShape)
                        .background(Green200.copy(alpha = 0.3f))
                        .align(Alignment.Center)
                )
            }
        }

        Spacer(modifier = Modifier.height(24.dp))

        Text(
            text = "Cargando recetas deliciosas...",
            style = MaterialTheme.typography.titleMedium,
            color = Green700,
            fontWeight = FontWeight.Bold
        )

        Spacer(modifier = Modifier.height(8.dp))

        Text(
            text = "Preparando los mejores sabores para ti",
            style = MaterialTheme.typography.bodyMedium,
            color = Green500,
            fontWeight = FontWeight.Medium
        )
    }
}

@Composable
private fun RecipeList(
    recetas: List<Receta>,
    tiposComida: List<TipoComida>,
    onRecipeClick: (Int) -> Unit
) {
    if (recetas.isEmpty()) {
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
                items = recetas,
                key = { _, receta -> receta.recetaId ?: receta.hashCode() }
            ) { index, receta ->
                AnimatedRecipeCard(
                    receta = receta,
                    tiposComida = tiposComida,
                    index = index,
                    animationDelay = index * 120L,
                    onRecipeClick = { onRecipeClick(receta.recetaId!!) }
                )
            }
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
                imageVector = Icons.Default.Restaurant,
                contentDescription = null,
                modifier = Modifier.size(60.dp),
                tint = Green400
            )
        }

        Spacer(modifier = Modifier.height(24.dp))

        Text(
            text = "¡Ups! No hay recetas aquí",
            style = MaterialTheme.typography.headlineSmall,
            color = Green700,
            textAlign = TextAlign.Center,
            fontWeight = FontWeight.Bold
        )

        Spacer(modifier = Modifier.height(12.dp))

        Text(
            text = "Parece que no tenemos recetas para esta categoría todavía.\n¡Pero estamos trabajando en ello!",
            style = MaterialTheme.typography.bodyLarge,
            color = Black500,
            textAlign = TextAlign.Center,
            lineHeight = 24.sp
        )
    }
}

@Composable
private fun AnimatedRecipeCard(
    receta: Receta,
    tiposComida: List<TipoComida>,
    index: Int,
    animationDelay: Long,
    onRecipeClick: () -> Unit
) {
    var isVisible by remember { mutableStateOf(false) }
    var isPressed by remember { mutableStateOf(false) }

    LaunchedEffect(Unit) {
        delay(animationDelay)
        isVisible = true
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
                .shadow(12.dp, RoundedCornerShape(20.dp))
                .clickable(
                    interactionSource = remember { MutableInteractionSource() },
                    indication = null
                ) {
                    isPressed = true
                    onRecipeClick()
                },
            colors = CardDefaults.cardColors(containerColor = Cream100),
            shape = RoundedCornerShape(20.dp),
            border = BorderStroke(2.dp, Green300.copy(alpha = 0.4f))
        ) {
            Column(
                modifier = Modifier.fillMaxWidth()
            ) {
                // Header con gradiente
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
                    // Avatar mejorado
                    EnhancedRecipeAvatar(
                        receta = receta,
                        index = index
                    )

                    Spacer(modifier = Modifier.width(24.dp))

                    // Información de la receta
                    Column(
                        modifier = Modifier.weight(1f)
                    ) {
                        Text(
                            text = receta.nombre,
                            style = MaterialTheme.typography.headlineSmall,
                            fontWeight = FontWeight.Bold,
                            color = Green800,
                            maxLines = 2,
                            overflow = TextOverflow.Ellipsis
                        )

                        Spacer(modifier = Modifier.height(12.dp))

                        // Chips de información
                        Row(
                            horizontalArrangement = Arrangement.spacedBy(8.dp),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            InfoChip(
                                icon = Icons.Default.AccessTime,
                                text = "${receta.tiempoPreparacion ?: "N/A"} min",
                                backgroundColor = Green100,
                                contentColor = Green700
                            )

                            if (receta.fit) {
                                InfoChip(
                                    icon = Icons.Default.FitnessCenter,
                                    text = "Fit",
                                    backgroundColor = Green200,
                                    contentColor = Green800
                                )
                            }
                        }

                        Spacer(modifier = Modifier.height(8.dp))

                        // Categoría con estilo mejorado
                        Text(
                            text = tiposComida.find { it.tipoComidaId == receta.tipoComidaId }?.nombre ?: "Sin categoría",
                            style = MaterialTheme.typography.labelLarge,
                            color = Green600,
                            fontWeight = FontWeight.Bold,
                            modifier = Modifier
                                .background(
                                    Green100,
                                    RoundedCornerShape(12.dp)
                                )
                                .padding(horizontal = 12.dp, vertical = 4.dp)
                        )
                    }
                }
            }
        }
    }
}

@Composable
internal fun EnhancedRecipeAvatar(
    receta: Receta,
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
                contentDescription = receta.nombre,
                modifier = Modifier.size(45.dp)
            )
        }
    }
}

@Composable
private fun InfoChip(
    icon: ImageVector,
    text: String,
    backgroundColor: Color,
    contentColor: Color
) {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        modifier = Modifier
            .background(
                backgroundColor,
                RoundedCornerShape(16.dp)
            )
            .padding(horizontal = 12.dp, vertical = 6.dp)
    ) {
        Icon(
            imageVector = icon,
            contentDescription = null,
            modifier = Modifier.size(16.dp),
            tint = contentColor
        )
        Spacer(modifier = Modifier.width(6.dp))
        Text(
            text = text,
            style = MaterialTheme.typography.labelMedium,
            color = contentColor,
            fontWeight = FontWeight.Bold
        )
    }
}

@Composable
private fun AnimatedSnackbar(
    data: SnackbarData,
    hostState: SnackbarHostState
) {
    var isVisible by remember { mutableStateOf(false) }

    LaunchedEffect(data) {
        isVisible = true
    }

    AnimatedVisibility(
        visible = isVisible,
        enter = slideInVertically(
            initialOffsetY = { it },
            animationSpec = spring(
                dampingRatio = Spring.DampingRatioMediumBouncy
            )
        ) + fadeIn() + scaleIn(initialScale = 0.8f),
        exit = slideOutVertically(
            targetOffsetY = { it },
            animationSpec = tween(300)
        ) + fadeOut() + scaleOut(targetScale = 0.8f)
    ) {
        Card(
            modifier = Modifier
                .padding(16.dp)
                .shadow(12.dp, RoundedCornerShape(16.dp)),
            colors = CardDefaults.cardColors(containerColor = Red400),
            shape = RoundedCornerShape(16.dp),
            border = BorderStroke(2.dp, Red500)
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(20.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = data.visuals.message,
                    style = MaterialTheme.typography.bodyLarge,
                    color = Cream100,
                    fontWeight = FontWeight.Medium,
                    modifier = Modifier.weight(1f)
                )

                Spacer(modifier = Modifier.width(16.dp))

                TextButton(
                    onClick = {
                        isVisible = false
                        hostState.currentSnackbarData?.dismiss()
                    },
                    colors = ButtonDefaults.textButtonColors(
                        contentColor = Cream100
                    )
                ) {
                    Text(
                        text = stringResource(R.string.ok),
                        fontWeight = FontWeight.Bold,
                        style = MaterialTheme.typography.labelLarge
                    )
                }
            }
        }
    }
}
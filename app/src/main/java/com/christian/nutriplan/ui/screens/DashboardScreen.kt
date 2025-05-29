package com.christian.nutriplan.ui.screens

import UserViewModel
import android.content.Context
import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.Canvas as ComposeCanvas
import androidx.compose.ui.graphics.*
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
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
import androidx.compose.ui.draw.scale
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.*
import androidx.compose.ui.graphics.drawscope.*
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import com.christian.nutriplan.R
import com.christian.nutriplan.models.MealType
import com.christian.nutriplan.models.Usuario
import com.christian.nutriplan.ui.navigation.NavRoutes
import com.christian.nutriplan.ui.theme.*
import com.christian.nutriplan.utils.AuthManager
import org.koin.compose.koinInject
import kotlin.math.PI
import kotlin.math.cos
import kotlin.math.sin
import kotlin.random.Random

@Composable
fun DashboardScreen(
    onLogout: () -> Unit,
    navController: NavController,
    viewModel: UserViewModel = koinInject(),
    context: Context = LocalContext.current
) {
    val currentUser by viewModel.currentUser.collectAsState()
    val isLoading by viewModel.isLoading.collectAsState()
    val errorMessage by viewModel.errorMessage.collectAsState()

    var showContent by remember { mutableStateOf(false) }
    val snackbarHostState = remember { SnackbarHostState() }
    val motivationalMessages = listOf(
        stringResource(id = R.string.motivation_1),
        stringResource(id = R.string.motivation_2),
        stringResource(id = R.string.motivation_3)
    )
    val randomMessage = remember { motivationalMessages[Random.nextInt(motivationalMessages.size)] }

    LaunchedEffect(Unit) {
        viewModel.fetchUserProfile(AuthManager.getAccessToken(context) ?: "")
        kotlinx.coroutines.delay(500)
        showContent = true
    }

    LaunchedEffect(errorMessage) {
        errorMessage?.let {
            snackbarHostState.showSnackbar(
                message = it,
                duration = SnackbarDuration.Short
            )
            viewModel.clearErrorMessage()
        }
    }

    DashboardContent(
        currentUser = currentUser,
        isLoading = isLoading,
        showContent = showContent,
        snackbarHostState = snackbarHostState,
        motivationalMessage = randomMessage,
        onMealSelected = { mealType ->
            navController.navigate("recipe_list/${mealType.name}")
        },
        onLogout = {
            viewModel.logout(context)
            onLogout()
        },
        navController = navController
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun DashboardContent(
    currentUser: Usuario?,
    isLoading: Boolean,
    showContent: Boolean,
    snackbarHostState: SnackbarHostState,
    motivationalMessage: String,
    onMealSelected: (MealType) -> Unit,
    onLogout: () -> Unit,
    navController: NavController
) {
    val scrollState = rememberScrollState()

    val fabScale by animateFloatAsState(
        targetValue = if (showContent) 1f else 0f,
        animationSpec = spring(
            dampingRatio = Spring.DampingRatioMediumBouncy,
            stiffness = Spring.StiffnessMedium
        ),
        label = "fab_scale"
    )

    val infiniteTransition = rememberInfiniteTransition(label = "fab_glow")
    val glowRotation by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 360f,
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = 3000, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "fab_glow_rotation"
    )

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(
                brush = Brush.verticalGradient(
                    colors = listOf(
                        Cream400.copy(alpha = 0.95f),
                        Cream300.copy(alpha = 0.9f),
                        Cream200.copy(alpha = 0.95f)
                    )
                )
            )
    ) {
        Scaffold(
            snackbarHost = {
                SnackbarHost(
                    hostState = snackbarHostState,
                    snackbar = { snackbarData ->
                        CustomSnackbar(snackbarData = snackbarData)
                    }
                )
            },
            containerColor = Color.Transparent,
            topBar = {
                EnhancedTopBar(
                    showContent = showContent,
                    onAboutClick = { navController.navigate(NavRoutes.ABOUT_ME) },
                    onLogout = onLogout
                )
            },
            floatingActionButton = {
                EnhancedFloatingActionButton(
                    scale = fabScale,
                    glowRotation = glowRotation,
                    onClick = { navController.navigate(NavRoutes.SAVED_RECIPES) }
                )
            }
        ) { padding ->
            AnimatedVisibility(
                visible = showContent,
                enter = fadeIn(
                    animationSpec = tween(
                        durationMillis = 1200,
                        easing = FastOutSlowInEasing
                    )
                ) + slideInVertically(
                    animationSpec = tween(
                        durationMillis = 1200,
                        easing = FastOutSlowInEasing
                    ),
                    initialOffsetY = { it / 2 }
                ),
                exit = fadeOut(animationSpec = tween(600)) + slideOutVertically()
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(padding)
                        .verticalScroll(scrollState),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Spacer(modifier = Modifier.height(8.dp))
                    EnhancedWelcomeCard(
                        userName = currentUser?.nombre ?: "Parce",
                        motivationalMessage = motivationalMessage
                    )
                    Spacer(modifier = Modifier.height(32.dp))
                    EnhancedAnimatedTitle()
                    EnhancedMealOptions(onMealSelected = onMealSelected)
                    Spacer(modifier = Modifier.height(24.dp))
                    EnhancedUserProfileSummary(
                        user = currentUser,
                        navController = navController,
                        snackbarHostState = snackbarHostState
                    )
                    Spacer(modifier = Modifier.height(32.dp))
                    EnhancedLoadingIndicator(isLoading = isLoading)
                    Spacer(modifier = Modifier.height(24.dp))
                }
            }
        }
    }
}

@Composable
private fun CustomSnackbar(snackbarData: SnackbarData) {
    Card(
        modifier = Modifier
            .padding(16.dp)
            .shadow(
                elevation = 12.dp,
                shape = RoundedCornerShape(16.dp),
                ambientColor = Color.Red.copy(alpha = 0.3f)
            ),
        colors = CardDefaults.cardColors(
            containerColor = Color.White
        ),
        shape = RoundedCornerShape(16.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                imageVector = Icons.Default.Warning,
                contentDescription = null,
                tint = Color.Red,
                modifier = Modifier.size(24.dp)
            )
            Spacer(modifier = Modifier.width(12.dp))
            Text(
                text = snackbarData.visuals.message,
                style = MaterialTheme.typography.bodyMedium,
                color = Color.Black,
                modifier = Modifier.weight(1f)
            )
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun EnhancedTopBar(
    showContent: Boolean,
    onAboutClick: () -> Unit,
    onLogout: () -> Unit
) {
    val topBarAlpha by animateFloatAsState(
        targetValue = if (showContent) 1f else 0f,
        animationSpec = tween(
            durationMillis = 800,
            easing = LinearOutSlowInEasing
        ),
        label = "topbar_alpha"
    )

    val infiniteTransition = rememberInfiniteTransition(label = "topbar_wave")
    val waveOffset by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 1f,
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = 4000, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "wave_offset"
    )

    TopAppBar(
        title = {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.graphicsLayer(alpha = topBarAlpha)
            ) {
                Box(
                    modifier = Modifier
                        .size(32.dp)
                        .clip(CircleShape)
                        .background(
                            brush = Brush.radialGradient(
                                colors = listOf(
                                    Green500.copy(alpha = 0.8f),
                                    Green700.copy(alpha = 0.6f)
                                )
                            )
                        ),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Default.Restaurant,
                        contentDescription = null,
                        tint = Color.White,
                        modifier = Modifier.size(18.dp)
                    )
                }
                Spacer(modifier = Modifier.width(12.dp))
                Text(
                    text = stringResource(id = R.string.dashboard_title),
                    style = MaterialTheme.typography.titleLarge.copy(
                        fontWeight = FontWeight.Bold
                    ),
                    color = Green800
                )
            }
        },
        actions = {
            Row(
                modifier = Modifier.graphicsLayer(alpha = topBarAlpha),
                horizontalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                EnhancedActionButton(
                    icon = Icons.Default.Info,
                    contentDescription = "About Me",
                    onClick = onAboutClick,
                    delay = 400
                )
                EnhancedActionButton(
                    icon = Icons.Default.ExitToApp,
                    contentDescription = stringResource(id = R.string.logout_button),
                    onClick = onLogout,
                    delay = 500
                )
            }
        },
        colors = TopAppBarDefaults.topAppBarColors(
            containerColor = Color.Transparent
        ),
        modifier = Modifier.background(
            brush = Brush.horizontalGradient(
                colors = listOf(
                    Cream300.copy(alpha = topBarAlpha * 0.9f),
                    Cream200.copy(alpha = topBarAlpha * 0.8f),
                    Cream300.copy(alpha = topBarAlpha * 0.9f)
                )
            )
        )
    )
}

@Composable
private fun EnhancedActionButton(
    icon: ImageVector,
    contentDescription: String,
    onClick: () -> Unit,
    delay: Int
) {
    var isPressed by remember { mutableStateOf(false) }

    val scale by animateFloatAsState(
        targetValue = if (isPressed) 0.85f else 1f,
        animationSpec = spring(
            dampingRatio = Spring.DampingRatioMediumBouncy,
            stiffness = Spring.StiffnessHigh
        ),
        label = "action_button_scale"
    )

    LaunchedEffect(Unit) {
        kotlinx.coroutines.delay(delay.toLong())
    }

    Box(
        modifier = Modifier
            .size(48.dp)
            .scale(scale)
            .clip(CircleShape)
            .background(
                brush = Brush.radialGradient(
                    colors = listOf(
                        Color.White.copy(alpha = 0.8f),
                        Color.White.copy(alpha = 0.4f)
                    )
                )
            )
            .clickable(
                interactionSource = remember { MutableInteractionSource() },
                indication = null,
                onClick = {
                    isPressed = true
                    onClick()
                }
            ),
        contentAlignment = Alignment.Center
    ) {
        Icon(
            imageVector = icon,
            contentDescription = contentDescription,
            tint = Green700,
            modifier = Modifier.size(20.dp)
        )
    }

    LaunchedEffect(isPressed) {
        if (isPressed) {
            kotlinx.coroutines.delay(150)
            isPressed = false
        }
    }
}

@Composable
private fun EnhancedFloatingActionButton(
    scale: Float,
    glowRotation: Float,
    onClick: () -> Unit
) {
    var isPressed by remember { mutableStateOf(false) }

    val buttonScale by animateFloatAsState(
        targetValue = if (isPressed) 0.9f else 1f,
        animationSpec = spring(
            dampingRatio = Spring.DampingRatioMediumBouncy,
            stiffness = Spring.StiffnessHigh
        ),
        label = "fab_press_scale"
    )

    Box(
        contentAlignment = Alignment.Center,
        modifier = Modifier
            .size(72.dp)
            .scale(scale)
            // Removed rotationZ to avoid potential graphics instability
            .background(
                brush = Brush.radialGradient(
                    colors = listOf(
                        Green400.copy(alpha = 0.3f),
                        Color.Transparent
                    ),
                    radius = 36.dp.value // Match the Box size
                ),
                shape = CircleShape
            )
    ) {
        FloatingActionButton(
            onClick = {
                isPressed = true
                onClick()
            },
            containerColor = Green600, // Simplified to a solid color
            contentColor = Color.White,
            modifier = Modifier
                .scale(scale * buttonScale)
                .size(56.dp)
                .shadow(
                    elevation = 16.dp,
                    shape = CircleShape,
                    ambientColor = Green700.copy(alpha = 0.4f),
                    spotColor = Green500.copy(alpha = 0.3f)
                )
        ) {
            Icon(
                imageVector = Icons.Default.Favorite,
                contentDescription = "Saved Recipes",
                modifier = Modifier.size(24.dp),
                tint = Color.White
            )
        }
    }

    LaunchedEffect(isPressed) {
        if (isPressed) {
            kotlinx.coroutines.delay(100)
            isPressed = false
        }
    }
}

@Composable
private fun EnhancedAnimatedTitle() {
    val titleAlpha by animateFloatAsState(
        targetValue = 1f,
        animationSpec = tween(
            durationMillis = 1000,
            easing = FastOutSlowInEasing
        ),
        label = "title_alpha"
    )

    val titleOffset by animateIntAsState(
        targetValue = 0,
        animationSpec = spring(
            dampingRatio = Spring.DampingRatioLowBouncy,
            stiffness = Spring.StiffnessLow
        ),
        label = "title_offset"
    )

    val infiniteTransition = rememberInfiniteTransition(label = "title_shimmer")
    val shimmerOffset by infiniteTransition.animateFloat(
        initialValue = -200f,
        targetValue = 200f,
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = 2000, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "shimmer_offset"
    )

    Text(
        text = stringResource(id = R.string.dashboard_subtitle),
        style = MaterialTheme.typography.headlineSmall.copy(
            fontWeight = FontWeight.Bold,
            brush = Brush.linearGradient(
                colors = listOf(
                    Green700,
                    Green500,
                    Green700
                ),
                start = Offset(shimmerOffset - 100f, 0f),
                end = Offset(shimmerOffset + 100f, 0f)
            )
        ),
        modifier = Modifier
            .padding(horizontal = 16.dp, vertical = 8.dp)
            .graphicsLayer(
                alpha = titleAlpha,
                translationY = titleOffset.toFloat()
            ),
        textAlign = TextAlign.Center
    )
}

@Composable
private fun EnhancedWelcomeCard(userName: String, motivationalMessage: String) {
    val cardScale by animateFloatAsState(
        targetValue = 1f,
        animationSpec = spring(
            dampingRatio = Spring.DampingRatioMediumBouncy,
            stiffness = Spring.StiffnessMedium
        ),
        label = "welcome_card_scale"
    )

    val infiniteTransition = rememberInfiniteTransition(label = "particles")
    val particle1Y by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = -20f,
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = 3000, easing = EaseInOutSine),
            repeatMode = RepeatMode.Reverse
        ),
        label = "particle1"
    )
    val particle2Y by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = -15f,
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = 2500, easing = EaseInOutSine),
            repeatMode = RepeatMode.Reverse
        ),
        label = "particle2"
    )

    Box(
        modifier = Modifier
            .fillMaxWidth()
            .padding(16.dp)
    ) {
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .scale(cardScale)
                .shadow(
                    elevation = 20.dp,
                    shape = RoundedCornerShape(24.dp),
                    ambientColor = Lilac300.copy(alpha = 0.5f),
                    spotColor = Lilac400.copy(alpha = 0.3f)
                ),
            colors = CardDefaults.cardColors(
                containerColor = Color.Transparent
            ),
            shape = RoundedCornerShape(24.dp),
            elevation = CardDefaults.cardElevation(0.dp)
        ) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(
                        brush = Brush.linearGradient(
                            colors = listOf(
                                Lilac200.copy(alpha = 0.9f),
                                Lilac300.copy(alpha = 0.8f),
                                Lilac200.copy(alpha = 0.9f)
                            ),
                            start = Offset(0f, 0f),
                            end = Offset(500f, 500f)
                        )
                    )
            ) {
                Box(
                    modifier = Modifier
                        .offset(x = 30.dp, y = particle1Y.dp)
                        .size(8.dp)
                        .clip(CircleShape)
                        .background(Green400.copy(alpha = 0.6f))
                )
                Box(
                    modifier = Modifier
                        .offset(x = 320.dp, y = (20 + particle2Y).dp)
                        .size(6.dp)
                        .clip(CircleShape)
                        .background(Yellow400.copy(alpha = 0.7f))
                )
                Column(
                    modifier = Modifier.padding(24.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    val emojiScale by animateFloatAsState(
                        targetValue = 1f,
                        animationSpec = spring(
                            dampingRatio = Spring.DampingRatioLowBouncy,
                            stiffness = Spring.StiffnessMedium
                        ),
                        label = "emoji_scale"
                    )
                    Text(
                        text = "👋",
                        style = MaterialTheme.typography.headlineLarge,
                        modifier = Modifier.scale(emojiScale)
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = "¡Quihubo, $userName!",
                        style = MaterialTheme.typography.headlineMedium.copy(
                            fontWeight = FontWeight.ExtraBold,
                            brush = Brush.linearGradient(
                                colors = listOf(Green900, Green700)
                            )
                        )
                    )
                    Spacer(modifier = Modifier.height(16.dp))
                    Text(
                        text = motivationalMessage,
                        style = MaterialTheme.typography.bodyLarge.copy(
                            fontWeight = FontWeight.Medium
                        ),
                        textAlign = TextAlign.Center,
                        color = Green800,
                        lineHeight = MaterialTheme.typography.bodyLarge.lineHeight
                    )
                }
            }
        }
    }
}

@Composable
private fun EnhancedMealOptions(onMealSelected: (MealType) -> Unit) {
    val mealTypes: List<Pair<Triple<String, List<Color>, Color>, MealType>> = listOf(
        Pair(
            Triple(
                stringResource(id = R.string.breakfast_title),
                listOf(Yellow300, Yellow400, Yellow500),
                Yellow500
            ),
            MealType.BREAKFAST
        ),
        Pair(
            Triple(
                stringResource(id = R.string.lunch_title),
                listOf(Green200, Green400, Green600),
                Green700
            ),
            MealType.LUNCH
        ),
        Pair(
            Triple(
                stringResource(id = R.string.dinner_title),
                listOf(Blue100, Blue300, Blue400),
                Blue500
            ),
            MealType.DINNER
        ),
        Pair(
            Triple(
                stringResource(id = R.string.snack_title),
                listOf(Orange100, Orange300, Orange400),
                Orange500
            ),
            MealType.SNACK
        )
    )

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp)
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier.padding(bottom = 20.dp)
        ) {
            Icon(
                imageVector = Icons.Default.RestaurantMenu,
                contentDescription = null,
                tint = Green700,
                modifier = Modifier.size(24.dp)
            )
            Spacer(modifier = Modifier.width(8.dp))
            Text(
                text = stringResource(id = R.string.today_meal),
                style = MaterialTheme.typography.titleLarge.copy(
                    fontWeight = FontWeight.Bold
                ),
                color = Black700
            )
        }

        mealTypes.forEachIndexed { index, (mealData: Triple<String, List<Color>, Color>, mealType: MealType) ->
            val (title: String, gradientColors: List<Color>, iconTint: Color) = mealData
            EnhancedMealCard(
                title = title,
                gradientColors = gradientColors,
                iconTint = iconTint,
                onClick = { onMealSelected(mealType) },
                delay = index * 200
            )
            if (index < mealTypes.size - 1) {
                Spacer(modifier = Modifier.height(16.dp))
            }
        }
    }
}

@Composable
private fun EnhancedMealCard(
    title: String,
    gradientColors: List<Color>,
    iconTint: Color,
    onClick: () -> Unit,
    delay: Int
) {
    var isPressed by remember { mutableStateOf(false) }
    var isVisible by remember { mutableStateOf(false) }

    val cardScale by animateFloatAsState(
        targetValue = if (isPressed) 0.96f else 1f,
        animationSpec = spring(
            dampingRatio = Spring.DampingRatioMediumBouncy,
            stiffness = Spring.StiffnessHigh
        ),
        label = "meal_card_scale"
    )

    val slideOffset by animateIntAsState(
        targetValue = if (isVisible) 0 else 300,
        animationSpec = spring(
            dampingRatio = Spring.DampingRatioMediumBouncy,
            stiffness = Spring.StiffnessMedium
        ),
        label = "meal_card_slide"
    )

    val cardAlpha by animateFloatAsState(
        targetValue = if (isVisible) 1f else 0f,
        animationSpec = tween(
            durationMillis = 600,
            easing = FastOutSlowInEasing
        ),
        label = "meal_card_alpha"
    )

    LaunchedEffect(Unit) {
        kotlinx.coroutines.delay(delay.toLong())
        isVisible = true
    }

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .scale(cardScale)
            .graphicsLayer(
                translationX = slideOffset.toFloat(),
                alpha = cardAlpha
            )
            .clickable(
                interactionSource = remember { MutableInteractionSource() },
                indication = null,
                onClick = {
                    isPressed = true
                    onClick()
                }
            )
            .shadow(
                elevation = 12.dp,
                shape = RoundedCornerShape(20.dp),
                ambientColor = iconTint.copy(alpha = 0.4f),
                spotColor = iconTint.copy(alpha = 0.2f)
            ),
        colors = CardDefaults.cardColors(containerColor = Color.Transparent),
        shape = RoundedCornerShape(20.dp),
        elevation = CardDefaults.cardElevation(0.dp)
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .background(
                    brush = Brush.linearGradient(
                        colors = gradientColors,
                        start = Offset(0f, 0f),
                        end = Offset(400f, 200f)
                    )
                )
        ) {
            ComposeCanvas(
                modifier = Modifier
                    .fillMaxSize()
                // Opcional: para depurar y ver qué tamaño tiene el Canvas
                // .onSizeChanged { intSize ->
                //     Log.d("CanvasDebug", "Canvas size changed: ${intSize.width}x${intSize.height}")
                // }
            ) {
                // Solo intenta dibujar si el Canvas tiene un tamaño válido (mayor que cero)
                if (size.width > 0f && size.height > 0f) {
                    // Log opcional para confirmar que se está intentando dibujar con un tamaño válido
                    // Log.d("CanvasDebug", "Drawing circles. Canvas size: ${size.width}x${size.height}")

                    drawCircle(
                        color = Color.White.copy(alpha = 0.1f),
                        radius = 40f,
                        center = Offset(size.width - 60f, size.height - 40f)
                    )
                    drawCircle(
                        color = Color.White.copy(alpha = 0.05f),
                        radius = 60f,
                        center = Offset(size.width - 40f, size.height - 60f)
                    )
                } else {
                    // Opcional: Log para saber cuándo se omitió el dibujo debido al tamaño cero
                    // Log.d("CanvasDebug", "Skipped drawing circles due to zero size. Canvas size: ${size.width}x${size.height}")
                }
            }
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(24.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    EnhancedMealIcon(
                        title = title,
                        iconTint = iconTint,
                        delay = delay + 200
                    )
                    Spacer(modifier = Modifier.width(20.dp))
                    Column {
                        Text(
                            text = title,
                            style = MaterialTheme.typography.titleLarge.copy(
                                fontWeight = FontWeight.Bold
                            ),
                            color = Black600
                        )
                        Text(
                            text = "Descubre recetas deliciosas",
                            style = MaterialTheme.typography.bodySmall,
                            color = Black600
                        )
                    }
                }
                Box(
                    modifier = Modifier
                        .size(40.dp)
                        .clip(CircleShape)
                        .background(
                            brush = Brush.radialGradient(
                                colors = listOf(
                                    Color.White.copy(alpha = 0.3f),
                                    Color.White.copy(alpha = 0.1f)
                                )
                            )
                        ),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Default.ChevronRight,
                        contentDescription = "View meal details",
                        tint = iconTint,
                        modifier = Modifier.size(24.dp)
                    )
                }
            }
        }
    }

    LaunchedEffect(isPressed) {
        if (isPressed) {
            kotlinx.coroutines.delay(100)
            isPressed = false
        }
    }
}

@Composable
private fun EnhancedMealIcon(
    title: String,
    iconTint: Color,
    delay: Int
) {
    var isVisible by remember { mutableStateOf(false) }

    val iconScale by animateFloatAsState(
        targetValue = if (isVisible) 1f else 0f,
        animationSpec = spring(
            dampingRatio = Spring.DampingRatioLowBouncy,
            stiffness = Spring.StiffnessMedium
        ),
        label = "meal_icon_scale"
    )

    val infiniteTransition = rememberInfiniteTransition(label = "icon_rotation")
    val rotation by infiniteTransition.animateFloat(
        initialValue = -2f,
        targetValue = 2f,
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = 2000, easing = EaseInOutSine),
            repeatMode = RepeatMode.Reverse
        ),
        label = "icon_subtle_rotation"
    )

    LaunchedEffect(Unit) {
        kotlinx.coroutines.delay((delay + 200).toLong())
        isVisible = true
    }

    Box(
        modifier = Modifier
            .size(56.dp)
            .scale(iconScale)
            .graphicsLayer(rotationZ = rotation)
            .shadow(
                elevation = 8.dp,
                shape = CircleShape,
                ambientColor = iconTint.copy(alpha = 0.3f)
            )
            .clip(CircleShape)
            .background(
                brush = Brush.radialGradient(
                    colors = listOf(
                        Color.White.copy(alpha = 0.9f),
                        Color.White.copy(alpha = 0.6f),
                        Color.White.copy(alpha = 0.3f)
                    )
                )
            ),
        contentAlignment = Alignment.Center
    ) {
        when {
            title.contains("Desayuno", ignoreCase = true) -> Icon(
                painter = painterResource(R.drawable.desayuno),
                contentDescription = "Breakfast icon",
                tint = iconTint,
                modifier = Modifier.size(32.dp)
            )
            title.contains("Almuerzo", ignoreCase = true) -> Icon(
                painter = painterResource(R.drawable.lunch),
                contentDescription = "Lunch icon",
                tint = iconTint,
                modifier = Modifier.size(32.dp)
            )
            title.contains("Comida", ignoreCase = true) -> Icon(
                painter = painterResource(R.drawable.dinner),
                contentDescription = "Dinner icon",
                tint = iconTint,
                modifier = Modifier.size(32.dp)
            )
            else -> Icon(
                painter = painterResource(R.drawable.cafe),
                contentDescription = "Snack icon",
                tint = iconTint,
                modifier = Modifier.size(32.dp)
            )
        }
    }
}

@Composable
private fun EnhancedUserProfileSummary(
    user: Usuario?,
    navController: NavController,
    snackbarHostState: SnackbarHostState
) {
    var showSnackbar by remember { mutableStateOf(false) }
    var isVisible by remember { mutableStateOf(false) }

    val profileScale by animateFloatAsState(
        targetValue = if (isVisible) 1f else 0f,
        animationSpec = spring(
            dampingRatio = Spring.DampingRatioMediumBouncy,
            stiffness = Spring.StiffnessMedium
        ),
        label = "profile_scale"
    )

    val profileAlpha by animateFloatAsState(
        targetValue = if (isVisible) 1f else 0f,
        animationSpec = tween(
            durationMillis = 800,
            easing = FastOutSlowInEasing
        ),
        label = "profile_alpha"
    )

    LaunchedEffect(Unit) {
        kotlinx.coroutines.delay(1000)
        isVisible = true
    }

    LaunchedEffect(showSnackbar) {
        if (showSnackbar) {
            snackbarHostState.showSnackbar("User profile not loaded. Please try again.")
            showSnackbar = false
        }
    }

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp)
            .scale(profileScale)
            .graphicsLayer(alpha = profileAlpha)
            .shadow(
                elevation = 16.dp,
                shape = RoundedCornerShape(24.dp),
                ambientColor = Green200.copy(alpha = 0.4f),
                spotColor = Green300.copy(alpha = 0.2f)
            ),
        colors = CardDefaults.cardColors(
            containerColor = Color.Transparent
        ),
        shape = RoundedCornerShape(24.dp),
        elevation = CardDefaults.cardElevation(0.dp)
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .background(
                    brush = Brush.linearGradient(
                        colors = listOf(
                            Cream100.copy(alpha = 0.95f),
                            Color.White.copy(alpha = 0.9f),
                            Cream100.copy(alpha = 0.95f)
                        ),
                        start = Offset(0f, 0f),
                        end = Offset(400f, 200f)
                    )
                )
        ) {
            ComposeCanvas(
                modifier = Modifier
                    .fillMaxSize()
                // Opcional: para depurar y ver qué tamaño tiene el Canvas
                // .onSizeChanged { intSize ->
                //     Log.d("CanvasDebug", "Canvas size changed: ${intSize.width}x${intSize.height}")
                // }
            ) {
                // Solo intenta dibujar si el Canvas tiene un tamaño válido (mayor que cero)
                if (size.width > 0f && size.height > 0f) {
                    // Log opcional para confirmar que se está intentando dibujar con un tamaño válido
                    // Log.d("CanvasDebug", "Drawing circles. Canvas size: ${size.width}x${size.height}")

                    drawCircle(
                        color = Color.White.copy(alpha = 0.1f),
                        radius = 40f,
                        center = Offset(size.width - 60f, size.height - 40f)
                    )
                    drawCircle(
                        color = Color.White.copy(alpha = 0.05f),
                        radius = 60f,
                        center = Offset(size.width - 40f, size.height - 60f)
                    )
                } else {
                    // Opcional: Log para saber cuándo se omitió el dibujo debido al tamaño cero
                    // Log.d("CanvasDebug", "Skipped drawing circles due to zero size. Canvas size: ${size.width}x${size.height}")
                }
            }
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(24.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                EnhancedAvatar()
                Spacer(modifier = Modifier.width(20.dp))
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = user?.nombre ?: "Usuario",
                        style = MaterialTheme.typography.titleLarge.copy(
                            fontWeight = FontWeight.Bold
                        ),
                        color = Black700
                    )
                    Spacer(modifier = Modifier.height(6.dp))
                    Row(
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            imageVector = Icons.Default.Email,
                            contentDescription = null,
                            tint = Green600,
                            modifier = Modifier.size(16.dp)
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            text = user?.email ?: "email@ejemplo.com",
                            style = MaterialTheme.typography.bodyMedium,
                            color = Black600
                        )
                    }
                    if (user?.ciudad != null || user?.localidad != null) {
                        Spacer(modifier = Modifier.height(4.dp))
                        Row(
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(
                                imageVector = Icons.Default.LocationOn,
                                contentDescription = null,
                                tint = Green600,
                                modifier = Modifier.size(16.dp)
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(
                                text = "${user?.ciudad ?: ""} ${user?.localidad ?: ""}",
                                style = MaterialTheme.typography.bodySmall,
                                color = Black500
                            )
                        }
                    }
                }
                EnhancedEditButton(
                    onClick = {
                        if (user != null) {
                            navController.navigate("edit_profile/${user.usuarioId}")
                        } else {
                            showSnackbar = true
                        }
                    }
                )
            }
        }
    }
}

@Composable
private fun EnhancedAvatar() {
    var isVisible by remember { mutableStateOf(false) }

    val avatarScale by animateFloatAsState(
        targetValue = if (isVisible) 1f else 0f,
        animationSpec = spring(
            dampingRatio = Spring.DampingRatioMediumBouncy,
            stiffness = Spring.StiffnessMedium
        ),
        label = "avatar_scale"
    )

    val infiniteTransition = rememberInfiniteTransition(label = "avatar_pulse")
    val pulseScale by infiniteTransition.animateFloat(
        initialValue = 1f,
        targetValue = 1.05f,
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = 2000, easing = EaseInOutSine),
            repeatMode = RepeatMode.Reverse
        ),
        label = "avatar_pulse_scale"
    )

    LaunchedEffect(Unit) {
        kotlinx.coroutines.delay(1100)
        isVisible = true
    }

    Box(
        modifier = Modifier
            .size(64.dp)
            .scale(avatarScale * pulseScale)
            .shadow(
                elevation = 12.dp,
                shape = CircleShape,
                ambientColor = Green300.copy(alpha = 0.4f)
            )
            .clip(CircleShape)
            .background(
                brush = Brush.linearGradient(
                    colors = listOf(
                        Green200,
                        Green300,
                        Green400
                    ),
                    start = Offset(0f, 0f),
                    end = Offset(100f, 100f)
                )
            ),
        contentAlignment = Alignment.Center
    ) {
        Icon(
            imageVector = Icons.Default.Person,
            contentDescription = null,
            tint = Color.White,
            modifier = Modifier.size(36.dp)
        )
    }
}

@Composable
private fun EnhancedEditButton(onClick: () -> Unit) {
    var isPressed by remember { mutableStateOf(false) }

    val buttonScale by animateFloatAsState(
        targetValue = if (isPressed) 0.85f else 1f,
        animationSpec = spring(
            dampingRatio = Spring.DampingRatioMediumBouncy,
            stiffness = Spring.StiffnessHigh
        ),
        label = "edit_button_scale"
    )

    Box(
        modifier = Modifier
            .size(48.dp)
            .scale(buttonScale)
            .clip(CircleShape)
            .background(
                brush = Brush.linearGradient(
                    colors = listOf(
                        Green400.copy(alpha = 0.8f),
                        Green500.copy(alpha = 0.9f),
                        Green600
                    )
                )
            )
            .clickable(
                interactionSource = remember { MutableInteractionSource() },
                indication = null,
                onClick = {
                    isPressed = true
                    onClick()
                }
            )
            .shadow(
                elevation = 8.dp,
                shape = CircleShape,
                ambientColor = Green500.copy(alpha = 0.3f)
            ),
        contentAlignment = Alignment.Center
    ) {
        Icon(
            imageVector = Icons.Default.Edit,
            contentDescription = stringResource(id = R.string.update_profile_button),
            tint = Color.White,
            modifier = Modifier.size(20.dp)
        )
    }

    LaunchedEffect(isPressed) {
        if (isPressed) {
            kotlinx.coroutines.delay(150)
            isPressed = false
        }
    }
}

@Composable
private fun EnhancedLoadingIndicator(isLoading: Boolean) {
    AnimatedVisibility(
        visible = isLoading,
        enter = fadeIn(
            animationSpec = tween(400)
        ) + scaleIn(
            animationSpec = spring(
                dampingRatio = Spring.DampingRatioMediumBouncy,
                stiffness = Spring.StiffnessMedium
            )
        ),
        exit = fadeOut(animationSpec = tween(300)) + scaleOut()
    ) {
        Box(
            modifier = Modifier
                .size(80.dp)
                .shadow(
                    elevation = 16.dp,
                    shape = CircleShape,
                    ambientColor = Green400.copy(alpha = 0.4f)
                )
                .clip(CircleShape)
                .background(
                    brush = Brush.radialGradient(
                        colors = listOf(
                            Color.White.copy(alpha = 0.95f),
                            Color.White.copy(alpha = 0.85f)
                        )
                    )
                ),
            contentAlignment = Alignment.Center
        ) {
            Box(
                modifier = Modifier.size(50.dp),
                contentAlignment = Alignment.Center
            ) {
                CircularProgressIndicator(
                    color = Green700,
                    strokeWidth = 4.dp,
                    modifier = Modifier.size(40.dp)
                )
                CircularProgressIndicator(
                    color = Green400,
                    strokeWidth = 2.dp,
                    modifier = Modifier.size(24.dp)
                )
                val infiniteTransition = rememberInfiniteTransition(label = "loading_pulse")
                val pulseScale by infiniteTransition.animateFloat(
                    initialValue = 0.5f,
                    targetValue = 1f,
                    animationSpec = infiniteRepeatable(
                        animation = tween(durationMillis = 1000, easing = EaseInOutSine),
                        repeatMode = RepeatMode.Reverse
                    ),
                    label = "loading_pulse_scale"
                )
                Box(
                    modifier = Modifier
                        .size(8.dp)
                        .scale(pulseScale)
                        .clip(CircleShape)
                        .background(Green600)
                )
            }
        }
    }
}
package com.christian.nutriplan.ui.screens

import UserViewModel
import android.content.Context
import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.BorderStroke
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
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
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
        kotlinx.coroutines.delay(300) // Small delay for smoother animation
        showContent = true
    }

    LaunchedEffect(errorMessage) {
        errorMessage?.let {
            snackbarHostState.showSnackbar(it)
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

    Scaffold(
        snackbarHost = { SnackbarHost(hostState = snackbarHostState) },
        containerColor = Cream400,
        topBar = {
            AnimatedTopBar(
                showContent = showContent,
                onAboutClick = { navController.navigate(NavRoutes.ABOUT_ME) },
                onLogout = onLogout
            )
        },
        floatingActionButton = {
            FloatingActionButton(
                onClick = { navController.navigate(NavRoutes.SAVED_RECIPES) },
                containerColor = Green500,
                contentColor = Color.White,
                modifier = Modifier
                    .scale(fabScale)
                    .shadow(
                        elevation = 8.dp,
                        shape = CircleShape,
                        ambientColor = Green700.copy(alpha = 0.3f)
                    )
            ) {
                Icon(
                    imageVector = Icons.Default.Favorite,
                    contentDescription = "Saved Recipes",
                    modifier = Modifier.size(24.dp)
                )
            }
        }
    ) { padding ->
        AnimatedVisibility(
            visible = showContent,
            enter = fadeIn(
                animationSpec = tween(
                    durationMillis = 800,
                    easing = FastOutSlowInEasing
                )
            ) + slideInVertically(
                animationSpec = tween(
                    durationMillis = 800,
                    easing = FastOutSlowInEasing
                ),
                initialOffsetY = { it / 3 }
            ),
            exit = fadeOut(animationSpec = tween(400)) + slideOutVertically()
        ) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(padding)
                    .verticalScroll(scrollState),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                WelcomeCard(
                    userName = currentUser?.nombre ?: "Parce",
                    motivationalMessage = motivationalMessage
                )

                Spacer(modifier = Modifier.height(24.dp))

                AnimatedTitle()

                MealOptions(onMealSelected = onMealSelected)

                Spacer(modifier = Modifier.height(16.dp))

                UserProfileSummary(
                    user = currentUser,
                    navController = navController,
                    snackbarHostState = snackbarHostState
                )

                Spacer(modifier = Modifier.height(24.dp))

                LoadingIndicator(isLoading = isLoading)

                Spacer(modifier = Modifier.height(16.dp))
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun AnimatedTopBar(
    showContent: Boolean,
    onAboutClick: () -> Unit,
    onLogout: () -> Unit
) {
    val topBarAlpha by animateFloatAsState(
        targetValue = if (showContent) 1f else 0f,
        animationSpec = tween(
            durationMillis = 600,
            easing = LinearOutSlowInEasing
        ),
        label = "topbar_alpha"
    )

    LaunchedEffect(showContent) {
        if (showContent) {
            kotlinx.coroutines.delay(200)
        }
    }

    TopAppBar(
        title = {
            Text(
                text = stringResource(id = R.string.dashboard_title),
                style = MaterialTheme.typography.titleLarge,
                color = Green800,
                modifier = Modifier.graphicsLayer(alpha = topBarAlpha)
            )
        },
        actions = {
            Row(
                modifier = Modifier.graphicsLayer(alpha = topBarAlpha)
            ) {
                AnimatedActionButton(
                    icon = Icons.Default.Info,
                    contentDescription = "About Me",
                    onClick = onAboutClick,
                    delay = 400
                )
                AnimatedActionButton(
                    icon = Icons.Default.ExitToApp,
                    contentDescription = stringResource(id = R.string.logout_button),
                    onClick = onLogout,
                    delay = 500
                )
            }
        },
        colors = TopAppBarDefaults.topAppBarColors(
            containerColor = Cream300.copy(alpha = topBarAlpha)
        )
    )
}

@Composable
private fun AnimatedActionButton(
    icon: ImageVector,
    contentDescription: String,
    onClick: () -> Unit,
    delay: Int
) {
    val scale by animateFloatAsState(
        targetValue = 1f,
        animationSpec = spring(
            dampingRatio = Spring.DampingRatioMediumBouncy,
            stiffness = Spring.StiffnessMedium
        ),
        label = "action_button_scale"
    )

    LaunchedEffect(Unit) {
        kotlinx.coroutines.delay(delay.toLong())
    }

    IconButton(
        onClick = onClick,
        modifier = Modifier.scale(scale)
    ) {
        Icon(
            imageVector = icon,
            contentDescription = contentDescription,
            tint = Green700
        )
    }
}

@Composable
private fun AnimatedTitle() {
    LaunchedEffect(Unit) {
        kotlinx.coroutines.delay(400)
    }

    val titleAlpha by animateFloatAsState(
        targetValue = 1f,
        animationSpec = tween(
            durationMillis = 800,
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

    Text(
        text = stringResource(id = R.string.dashboard_subtitle),
        style = MaterialTheme.typography.headlineSmall,
        color = Green700,
        modifier = Modifier
            .padding(bottom = 16.dp)
            .graphicsLayer(
                alpha = titleAlpha,
                translationY = titleOffset.toFloat()
            ),
        textAlign = TextAlign.Center
    )
}

@Composable
private fun WelcomeCard(userName: String, motivationalMessage: String) {
    LaunchedEffect(Unit) {
        kotlinx.coroutines.delay(100)
    }

    val cardScale by animateFloatAsState(
        targetValue = 1f,
        animationSpec = spring(
            dampingRatio = Spring.DampingRatioMediumBouncy,
            stiffness = Spring.StiffnessMedium
        ),
        label = "welcome_card_scale"
    )

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(16.dp)
            .scale(cardScale)
            .shadow(
                elevation = 8.dp,
                shape = RoundedCornerShape(16.dp),
                ambientColor = Lilac200.copy(alpha = 0.4f)
            ),
        colors = CardDefaults.cardColors(
            containerColor = Color.Transparent
        ),
        shape = RoundedCornerShape(16.dp),
        elevation = CardDefaults.cardElevation(0.dp)
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .background(
                    brush = Brush.linearGradient(
                        colors = listOf(
                            Lilac200,
                            Lilac200.copy(alpha = 0.8f)
                        )
                    )
                )
        ) {
            Column(
                modifier = Modifier.padding(20.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text(
                    text = "¡Quihubo, $userName!",
                    style = MaterialTheme.typography.headlineMedium,
                    fontWeight = FontWeight.Bold,
                    color = Green900
                )

                Spacer(modifier = Modifier.height(12.dp))

                Text(
                    text = motivationalMessage,
                    style = MaterialTheme.typography.bodyLarge,
                    textAlign = TextAlign.Center,
                    color = Green800,
                    lineHeight = MaterialTheme.typography.bodyLarge.lineHeight
                )
            }
        }
    }
}

@Composable
private fun MealOptions(onMealSelected: (MealType) -> Unit) {
    val mealTypes = listOf(
        Triple(stringResource(id = R.string.breakfast_title), Yellow300, Yellow500) to MealType.BREAKFAST,
        Triple(stringResource(id = R.string.lunch_title), Green200, Green600) to MealType.LUNCH,
        Triple(stringResource(id = R.string.dinner_title), Blue200, Blue500) to MealType.DINNER,
        Triple(stringResource(id = R.string.snack_title), Orange200, Orange500) to MealType.SNACK
    )

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp)
    ) {
        Text(
            text = stringResource(id = R.string.today_meal),
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.Medium,
            color = Black700,
            modifier = Modifier.padding(bottom = 16.dp)
        )

        mealTypes.forEachIndexed { index, (mealData, mealType) ->
            val (title, backgroundColor, iconTint) = mealData

            AnimatedMealCard(
                title = title,
                backgroundColor = backgroundColor,
                iconTint = iconTint,
                onClick = { onMealSelected(mealType) },
                delay = index * 150
            )

            if (index < mealTypes.size - 1) {
                Spacer(modifier = Modifier.height(12.dp))
            }
        }
    }
}

@Composable
private fun AnimatedMealCard(
    title: String,
    backgroundColor: Color,
    iconTint: Color,
    onClick: () -> Unit,
    delay: Int
) {
    var isPressed by remember { mutableStateOf(false) }

    val cardScale by animateFloatAsState(
        targetValue = if (isPressed) 0.95f else 1f,
        animationSpec = spring(
            dampingRatio = Spring.DampingRatioMediumBouncy,
            stiffness = Spring.StiffnessHigh
        ),
        label = "meal_card_scale"
    )

    LaunchedEffect(Unit) {
        kotlinx.coroutines.delay(delay.toLong())
    }

    val slideOffset by animateIntAsState(
        targetValue = 0,
        animationSpec = spring(
            dampingRatio = Spring.DampingRatioMediumBouncy,
            stiffness = Spring.StiffnessMedium
        ),
        label = "meal_card_slide"
    )

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .scale(cardScale)
            .graphicsLayer(translationX = slideOffset.toFloat())
            .clickable(
                interactionSource = remember { MutableInteractionSource() },
                indication = null,
                onClick = {
                    isPressed = true
                    onClick()
                }
            )
            .shadow(
                elevation = 4.dp,
                shape = RoundedCornerShape(12.dp),
                ambientColor = iconTint.copy(alpha = 0.3f)
            ),
        colors = CardDefaults.cardColors(containerColor = backgroundColor),
        shape = RoundedCornerShape(12.dp),
        elevation = CardDefaults.cardElevation(0.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(20.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically
            ) {
                AnimatedMealIcon(
                    title = title,
                    iconTint = iconTint,
                    delay = delay + 100
                )

                Spacer(modifier = Modifier.width(20.dp))

                Text(
                    text = title,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.SemiBold,
                    color = Black700
                )
            }

            Icon(
                imageVector = Icons.Default.ChevronRight,
                contentDescription = "View meal details",
                tint = iconTint,
                modifier = Modifier.size(24.dp)
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
private fun AnimatedMealIcon(
    title: String,
    iconTint: Color,
    delay: Int
) {
    LaunchedEffect(Unit) {
        kotlinx.coroutines.delay((delay + 100).toLong())
    }

    val iconScale by animateFloatAsState(
        targetValue = 1f,
        animationSpec = spring(
            dampingRatio = Spring.DampingRatioLowBouncy,
            stiffness = Spring.StiffnessMedium
        ),
        label = "meal_icon_scale"
    )

    Box(
        modifier = Modifier
            .size(48.dp)
            .scale(iconScale)
            .clip(CircleShape)
            .background(
                brush = Brush.radialGradient(
                    colors = listOf(
                        Color.White.copy(alpha = 0.6f),
                        Color.White.copy(alpha = 0.2f)
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
                modifier = Modifier.size(28.dp)
            )
            title.contains("Almuerzo", ignoreCase = true) -> Icon(
                painter = painterResource(R.drawable.lunch),
                contentDescription = "Lunch icon",
                tint = iconTint,
                modifier = Modifier.size(28.dp)
            )
            title.contains("Comida", ignoreCase = true) -> Icon(
                painter = painterResource(R.drawable.dinner),
                contentDescription = "Dinner icon",
                tint = iconTint,
                modifier = Modifier.size(28.dp)
            )
            else -> Icon(
                painter = painterResource(R.drawable.cafe),
                contentDescription = "Snack icon",
                tint = iconTint,
                modifier = Modifier.size(28.dp)
            )
        }
    }
}

@Composable
private fun UserProfileSummary(
    user: Usuario?,
    navController: NavController,
    snackbarHostState: SnackbarHostState
) {
    var showSnackbar by remember { mutableStateOf(false) }

    LaunchedEffect(Unit) {
        kotlinx.coroutines.delay(800)
    }

    val profileScale by animateFloatAsState(
        targetValue = 1f,
        animationSpec = spring(
            dampingRatio = Spring.DampingRatioMediumBouncy,
            stiffness = Spring.StiffnessMedium
        ),
        label = "profile_scale"
    )

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
            .shadow(
                elevation = 6.dp,
                shape = RoundedCornerShape(16.dp),
                ambientColor = Green200.copy(alpha = 0.3f)
            ),
        colors = CardDefaults.cardColors(containerColor = Cream100),
        shape = RoundedCornerShape(16.dp),
        elevation = CardDefaults.cardElevation(0.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(20.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            AnimatedAvatar()

            Spacer(modifier = Modifier.width(20.dp))

            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = user?.nombre ?: "Usuario",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Medium,
                    color = Black700
                )

                Spacer(modifier = Modifier.height(4.dp))

                Text(
                    text = user?.email ?: "email@ejemplo.com",
                    style = MaterialTheme.typography.bodyMedium,
                    color = Black500
                )

                if (user?.ciudad != null || user?.localidad != null) {
                    Spacer(modifier = Modifier.height(2.dp))
                    Text(
                        text = "${user.ciudad ?: ""} ${user.localidad ?: ""}",
                        style = MaterialTheme.typography.bodySmall,
                        color = Black400
                    )
                }
            }

            AnimatedEditButton(
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

@Composable
private fun AnimatedAvatar() {
    LaunchedEffect(Unit) {
        kotlinx.coroutines.delay(900)
    }

    val avatarScale by animateFloatAsState(
        targetValue = 1f,
        animationSpec = spring(
            dampingRatio = Spring.DampingRatioMediumBouncy,
            stiffness = Spring.StiffnessMedium
        ),
        label = "avatar_scale"
    )

    Box(
        modifier = Modifier
            .size(56.dp)
            .scale(avatarScale)
            .clip(CircleShape)
            .background(
                brush = Brush.linearGradient(
                    colors = listOf(Green200, Green300)
                )
            ),
        contentAlignment = Alignment.Center
    ) {
        Icon(
            imageVector = Icons.Default.Person,
            contentDescription = null,
            tint = Green700,
            modifier = Modifier.size(32.dp)
        )
    }
}

@Composable
private fun AnimatedEditButton(onClick: () -> Unit) {
    var isPressed by remember { mutableStateOf(false) }

    val buttonScale by animateFloatAsState(
        targetValue = if (isPressed) 0.9f else 1f,
        animationSpec = spring(
            dampingRatio = Spring.DampingRatioMediumBouncy,
            stiffness = Spring.StiffnessHigh
        ),
        label = "edit_button_scale"
    )

    IconButton(
        onClick = {
            isPressed = true
            onClick()
        },
        modifier = Modifier.scale(buttonScale)
    ) {
        Icon(
            imageVector = Icons.Default.Edit,
            contentDescription = stringResource(id = R.string.update_profile_button),
            tint = Green700,
            modifier = Modifier.size(24.dp)
        )
    }

    LaunchedEffect(isPressed) {
        if (isPressed) {
            kotlinx.coroutines.delay(100)
            isPressed = false
        }
    }
}

@Composable
private fun LoadingIndicator(isLoading: Boolean) {
    AnimatedVisibility(
        visible = isLoading,
        enter = fadeIn(animationSpec = tween(300)) + scaleIn(
            animationSpec = spring(
                dampingRatio = Spring.DampingRatioMediumBouncy,
                stiffness = Spring.StiffnessMedium
            )
        ),
        exit = fadeOut(animationSpec = tween(300)) + scaleOut()
    ) {
        Box(
            modifier = Modifier
                .size(60.dp)
                .clip(CircleShape)
                .background(Color.White.copy(alpha = 0.8f)),
            contentAlignment = Alignment.Center
        ) {
            CircularProgressIndicator(
                color = Green700,
                strokeWidth = 3.dp,
                modifier = Modifier.size(36.dp)
            )
        }
    }
}
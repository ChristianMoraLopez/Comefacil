package com.christian.nutriplan.ui.screens

import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
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
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.christian.nutriplan.AppState
import com.christian.nutriplan.R
import com.christian.nutriplan.ui.components.PrimaryButton
import com.christian.nutriplan.ui.theme.*
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

@Composable
fun GoalSelectionScreen(
    onGoalSelected: (String) -> Unit
) {
    val errorNoGoalText = stringResource(R.string.error_no_goal_selected)
    var selectedGoal by remember { mutableStateOf<String?>(null) }
    var showTitle by remember { mutableStateOf(false) }
    var showCards by remember { mutableStateOf(false) }
    var showButton by remember { mutableStateOf(false) }
    val coroutineScope = rememberCoroutineScope()
    val snackbarHostState = remember { SnackbarHostState() }

    // Animaciones de entrada escalonadas
    LaunchedEffect(Unit) {
        delay(200)
        showTitle = true
        delay(400)
        showCards = true
        delay(600)
        showButton = true
    }

    // Animación de fondo sutil
    val backgroundAlpha by animateFloatAsState(
        targetValue = if (showTitle) 1f else 0.8f,
        animationSpec = tween(1000, easing = EaseOutCubic),
        label = "backgroundAlpha"
    )

    Scaffold(
        snackbarHost = {
            SnackbarHost(hostState = snackbarHostState) { data ->
                Snackbar(
                    modifier = Modifier.padding(16.dp),
                    containerColor = MaterialTheme.colorScheme.errorContainer,
                    contentColor = MaterialTheme.colorScheme.onErrorContainer,
                    shape = RoundedCornerShape(12.dp),
                    action = {
                        TextButton(onClick = { snackbarHostState.currentSnackbarData?.dismiss() }) {
                            Text(
                                text = stringResource(R.string.ok),
                                color = Green700,
                                fontWeight = FontWeight.SemiBold
                            )
                        }
                    }
                ) {
                    Text(data.visuals.message)
                }
            }
        },
        containerColor = Cream400
    ) { padding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(
                    brush = Brush.verticalGradient(
                        colors = listOf(
                            Cream300.copy(alpha = backgroundAlpha),
                            Cream400.copy(alpha = backgroundAlpha)
                        )
                    )
                )
                .padding(padding),
            contentAlignment = Alignment.Center
        ) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(24.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center
            ) {
                // Título con animación más elaborada
                AnimatedVisibility(
                    visible = showTitle,
                    enter = slideInVertically(
                        initialOffsetY = { -it },
                        animationSpec = spring(
                            dampingRatio = Spring.DampingRatioMediumBouncy,
                            stiffness = Spring.StiffnessMedium
                        )
                    ) + fadeIn(
                        animationSpec = tween(800, easing = EaseOutCubic)
                    )
                ) {
                    Text(
                        text = stringResource(R.string.goal_selection_title),
                        style = MaterialTheme.typography.headlineMedium,
                        color = Green800,
                        textAlign = TextAlign.Center,
                        fontWeight = FontWeight.ExtraBold,
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(bottom = 8.dp)
                    )
                }

                // Subtítulo con delay y efecto suave
                AnimatedVisibility(
                    visible = showTitle,
                    enter = slideInVertically(
                        initialOffsetY = { it / 2 },
                        animationSpec = spring(
                            dampingRatio = Spring.DampingRatioMediumBouncy,
                            stiffness = Spring.StiffnessMediumLow
                        )
                    ) + fadeIn(
                        animationSpec = tween(1000, 300, EaseOutCubic)
                    )
                ) {
                    Text(
                        text = stringResource(R.string.goal_selection_subtitle),
                        style = MaterialTheme.typography.bodyLarge,
                        color = Black600,
                        textAlign = TextAlign.Center,
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(bottom = 40.dp)
                    )
                }

                // Tarjetas con animaciones independientes y más sofisticadas
                AnimatedVisibility(
                    visible = showCards,
                    enter = fadeIn(
                        animationSpec = tween(800, easing = EaseOutCubic)
                    )
                ) {
                    Column(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.spacedBy(20.dp)
                    ) {
                        // Primera tarjeta con delay
                        AnimatedVisibility(
                            visible = showCards,
                            enter = slideInHorizontally(
                                initialOffsetX = { -it },
                                animationSpec = spring(
                                    dampingRatio = Spring.DampingRatioMediumBouncy,
                                    stiffness = Spring.StiffnessMedium
                                )
                            ) + fadeIn(
                                animationSpec = tween(600, 100, EaseOutCubic)
                            )
                        ) {
                            EnhancedGoalSelectionCard(
                                selected = selectedGoal == "Bajar de Peso",
                                iconRes = R.drawable.weightloss,
                                titleRes = R.string.weight_loss_option,
                                descRes = R.string.weight_loss_desc,
                                onClick = { selectedGoal = "Bajar de Peso" },
                                modifier = Modifier.fillMaxWidth(),
                                cardIndex = 0
                            )
                        }

                        // Segunda tarjeta con delay mayor
                        AnimatedVisibility(
                            visible = showCards,
                            enter = slideInHorizontally(
                                initialOffsetX = { it },
                                animationSpec = spring(
                                    dampingRatio = Spring.DampingRatioMediumBouncy,
                                    stiffness = Spring.StiffnessMedium
                                )
                            ) + fadeIn(
                                animationSpec = tween(600, 300, EaseOutCubic)
                            )
                        ) {
                            EnhancedGoalSelectionCard(
                                selected = selectedGoal == "Comer fácil",
                                iconRes = R.drawable.vegetarian,
                                titleRes = R.string.vegetarian_option,
                                descRes = R.string.vegetarian_desc,
                                onClick = { selectedGoal = "Comer fácil" },
                                modifier = Modifier.fillMaxWidth(),
                                cardIndex = 1
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(48.dp))

                // Botón con animación de entrada desde abajo
                AnimatedVisibility(
                    visible = showButton,
                    enter = slideInVertically(
                        initialOffsetY = { it },
                        animationSpec = spring(
                            dampingRatio = Spring.DampingRatioMediumBouncy,
                            stiffness = Spring.StiffnessMedium
                        )
                    ) + fadeIn(
                        animationSpec = tween(600, easing = EaseOutCubic)
                    )
                ) {
                    EnhancedPrimaryButton(
                        text = stringResource(R.string.continue_button),
                        onClick = {
                            if (selectedGoal == null) {
                                coroutineScope.launch {
                                    snackbarHostState.showSnackbar(message = errorNoGoalText)
                                }
                            } else {
                                AppState.objetivo = selectedGoal
                                onGoalSelected(selectedGoal!!)
                            }
                        },
                        enabled = selectedGoal != null,
                        modifier = Modifier
                            .fillMaxWidth(0.85f)
                            .height(56.dp)
                    )
                }
            }
        }
    }
}

@Composable
private fun EnhancedGoalSelectionCard(
    selected: Boolean,
    iconRes: Int,
    titleRes: Int,
    descRes: Int,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    cardIndex: Int
) {
    // Animaciones más sofisticadas
    val scaleAnim by animateFloatAsState(
        targetValue = if (selected) 1.03f else 1f,
        animationSpec = spring(
            stiffness = Spring.StiffnessMediumLow,
            dampingRatio = Spring.DampingRatioMediumBouncy
        ),
        label = "CardScale"
    )

    val elevationAnim by animateDpAsState(
        targetValue = if (selected) 12.dp else 4.dp,
        animationSpec = spring(
            stiffness = Spring.StiffnessMedium,
            dampingRatio = Spring.DampingRatioMediumBouncy
        ),
        label = "CardElevation"
    )

    val rotationAnim by animateFloatAsState(
        targetValue = if (selected) 0f else 0f,
        animationSpec = spring(
            stiffness = Spring.StiffnessMedium,
            dampingRatio = Spring.DampingRatioHighBouncy
        ),
        label = "CardRotation"
    )

    val iconScaleAnim by animateFloatAsState(
        targetValue = if (selected) 1.1f else 1f,
        animationSpec = spring(
            stiffness = Spring.StiffnessMedium,
            dampingRatio = Spring.DampingRatioMediumBouncy
        ),
        label = "IconScale"
    )

    val checkmarkRotation by animateFloatAsState(
        targetValue = if (selected) 0f else 180f,
        animationSpec = spring(
            stiffness = Spring.StiffnessMedium,
            dampingRatio = Spring.DampingRatioMediumBouncy
        ),
        label = "CheckmarkRotation"
    )

    Card(
        modifier = modifier
            .scale(scaleAnim)
            .rotate(rotationAnim)
            .shadow(
                elevation = elevationAnim,
                shape = RoundedCornerShape(20.dp),
                spotColor = if (selected) Green400.copy(alpha = 0.3f) else Color.Black.copy(alpha = 0.1f)
            )
            .height(130.dp)
            .clickable(onClick = onClick),
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(
            containerColor = if (selected) {
                Green100
            } else {
                Cream100
            }
        ),
        border = if (selected) {
            BorderStroke(
                width = 3.dp,
                brush = Brush.horizontalGradient(
                    colors = listOf(Green600, Green700, Green600)
                )
            )
        } else {
            BorderStroke(1.dp, Cream500)
        }
    ) {
        Row(
            modifier = Modifier
                .fillMaxSize()
                .background(
                    if (selected) {
                        Brush.horizontalGradient(
                            colors = listOf(
                                Green100.copy(alpha = 0.9f),
                                Green200.copy(alpha = 0.7f)
                            )
                        )
                    } else {
                        Brush.horizontalGradient(
                            colors = listOf(
                                Cream100,
                                Cream200.copy(alpha = 0.5f)
                            )
                        )
                    }
                )
                .padding(20.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .size(92.dp)
                    .clip(CircleShape)
                    .background(
                        brush = if (selected) {
                            Brush.radialGradient(
                                colors = listOf(Green300, Green200, Green100)
                            )
                        } else {
                            Brush.radialGradient(
                                colors = listOf(Cream300, Cream200, Cream100)
                            )
                        }
                    )
                    .padding(14.dp),
                contentAlignment = Alignment.Center
            ) {
                Image(
                    painter = painterResource(id = iconRes),
                    contentDescription = stringResource(descRes),
                    modifier = Modifier
                        .size(58.dp)
                        .scale(iconScaleAnim)
                        .graphicsLayer {
                            shadowElevation = if (selected) 8.dp.toPx() else 0.dp.toPx()
                        }
                )
            }

            Spacer(modifier = Modifier.width(18.dp))

            Column(
                modifier = Modifier.weight(1f),
                verticalArrangement = Arrangement.Center
            ) {
                Text(
                    text = stringResource(titleRes),
                    style = MaterialTheme.typography.titleLarge,
                    color = if (selected) Green900 else Black700,
                    fontWeight = if (selected) FontWeight.ExtraBold else FontWeight.Bold
                )

                Spacer(modifier = Modifier.height(6.dp))

                CompositionLocalProvider(
                    LocalContentColor provides if (selected) Green700 else Black500
                ) {
                    Text(
                        text = stringResource(descRes),
                        style = MaterialTheme.typography.bodyMedium,
                        fontWeight = if (selected) FontWeight.SemiBold else FontWeight.Normal
                    )
                }
            }

            AnimatedVisibility(
                visible = selected,
                enter = scaleIn(
                    animationSpec = spring(
                        stiffness = Spring.StiffnessHigh,
                        dampingRatio = Spring.DampingRatioMediumBouncy
                    )
                ) + fadeIn(),
                exit = scaleOut() + fadeOut()
            ) {
                Image(
                    painter = painterResource(id = R.drawable.accept),
                    contentDescription = "Seleccionado",
                    modifier = Modifier
                        .size(32.dp)
                        .rotate(checkmarkRotation)
                        .graphicsLayer {
                            shadowElevation = 4.dp.toPx()
                        }
                )
            }
        }
    }
}

@Composable
private fun EnhancedPrimaryButton(
    text: String,
    onClick: () -> Unit,
    enabled: Boolean,
    modifier: Modifier = Modifier
) {
    val scaleAnim by animateFloatAsState(
        targetValue = if (enabled) 1f else 0.95f,
        animationSpec = spring(
            stiffness = Spring.StiffnessMedium,
            dampingRatio = Spring.DampingRatioMediumBouncy
        ),
        label = "ButtonScale"
    )

    val elevationAnim by animateDpAsState(
        targetValue = if (enabled) 8.dp else 2.dp,
        animationSpec = spring(
            stiffness = Spring.StiffnessMedium,
            dampingRatio = Spring.DampingRatioMediumBouncy
        ),
        label = "ButtonElevation"
    )

    Button(
        onClick = onClick,
        enabled = enabled,
        modifier = modifier
            .scale(scaleAnim)
            .shadow(
                elevation = elevationAnim,
                shape = RoundedCornerShape(16.dp),
                spotColor = if (enabled) Green400.copy(alpha = 0.4f) else Color.Transparent
            ),
        shape = RoundedCornerShape(16.dp),
        colors = ButtonDefaults.buttonColors(
            containerColor = if (enabled) Green600 else Black300,
            contentColor = if (enabled) Cream100 else Black500
        ),
        elevation = ButtonDefaults.buttonElevation(
            defaultElevation = 0.dp,
            pressedElevation = 2.dp,
            disabledElevation = 0.dp
        )
    ) {
        Text(
            text = text,
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.Bold
        )
    }
}
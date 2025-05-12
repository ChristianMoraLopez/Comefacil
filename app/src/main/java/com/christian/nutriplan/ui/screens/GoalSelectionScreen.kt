package com.christian.nutriplan.ui.screens

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.spring
import androidx.compose.animation.expandVertically
import androidx.compose.animation.fadeIn
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
import androidx.compose.ui.draw.scale
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
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
import org.koin.compose.koinInject

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

    LaunchedEffect(Unit) {
        showTitle = true
        delay(300)
        showCards = true
        delay(300)
        showButton = true
    }

    Scaffold(
        snackbarHost = {
            SnackbarHost(hostState = snackbarHostState) { data ->
                Snackbar(
                    modifier = Modifier.padding(16.dp),
                    containerColor = MaterialTheme.colorScheme.errorContainer,
                    contentColor = MaterialTheme.colorScheme.onErrorContainer,
                    shape = RoundedCornerShape(8.dp),
                    action = {
                        TextButton(onClick = { snackbarHostState.currentSnackbarData?.dismiss() }) {
                            Text(
                                text = stringResource(R.string.ok),
                                color = Green700
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
                .background(Cream400)
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
                // Título animado
                AnimatedVisibility(
                    visible = showTitle,
                    enter = fadeIn(spring(Spring.DampingRatioMediumBouncy)) +
                            expandVertically(spring(Spring.DampingRatioMediumBouncy))
                ) {
                    Text(
                        text = stringResource(R.string.goal_selection_title),
                        style = MaterialTheme.typography.headlineMedium,
                        color = Green800,
                        textAlign = TextAlign.Center,
                        fontWeight = FontWeight.ExtraBold,
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(bottom = 16.dp)
                    )
                }

                // Subtítulo animado
                AnimatedVisibility(
                    visible = showTitle,
                    enter = fadeIn(spring(Spring.DampingRatioMediumBouncy)) +
                            expandVertically(spring(Spring.DampingRatioMediumBouncy))
                ) {
                    Text(
                        text = stringResource(R.string.goal_selection_subtitle),
                        style = MaterialTheme.typography.bodyLarge,
                        color = Black600,
                        textAlign = TextAlign.Center,
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(bottom = 32.dp)
                    )
                }

                // Tarjetas de selección animadas
                AnimatedVisibility(
                    visible = showCards,
                    enter = fadeIn(spring(Spring.DampingRatioMediumBouncy)) +
                            expandVertically(spring(Spring.DampingRatioMediumBouncy))
                ) {
                    Column(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.spacedBy(16.dp)
                    ) {
                        // Tarjeta para "Bajar de Peso"
                        GoalSelectionCard(
                            selected = selectedGoal == "Bajar de Peso",
                            iconRes = R.drawable.weightloss,
                            titleRes = R.string.weight_loss_option,
                            descRes = R.string.weight_loss_desc,
                            onClick = { selectedGoal = "Bajar de Peso" },
                            modifier = Modifier.fillMaxWidth()
                        )

                        // Tarjeta para "Comer fácil"
                        GoalSelectionCard(
                            selected = selectedGoal == "Comer fácil",
                            iconRes = R.drawable.vegetarian,
                            titleRes = R.string.vegetarian_option,
                            descRes = R.string.vegetarian_desc,
                            onClick = { selectedGoal = "Comer fácil" },
                            modifier = Modifier.fillMaxWidth()
                        )
                    }
                }

                Spacer(modifier = Modifier.height(40.dp))

                // Botón continuar animado
                AnimatedVisibility(
                    visible = showButton,
                    enter = fadeIn(spring(Spring.DampingRatioMediumBouncy)) +
                            expandVertically(spring(Spring.DampingRatioMediumBouncy))
                ) {
                    PrimaryButton(
                        text = stringResource(R.string.continue_button),
                        onClick = {
                            if (selectedGoal == null) {
                                coroutineScope.launch {
                                    snackbarHostState.showSnackbar(message = errorNoGoalText)
                                }
                            } else {
                                // Guardar objetivo en AppState
                                AppState.objetivo = selectedGoal
                                onGoalSelected(selectedGoal!!)
                            }
                        },
                        enabled = selectedGoal != null,
                        modifier = Modifier
                            .fillMaxWidth(0.8f)
                            .height(56.dp)
                    )
                }
            }
        }
    }
}

@Composable
private fun GoalSelectionCard(
    selected: Boolean,
    iconRes: Int,
    titleRes: Int,
    descRes: Int,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val scaleAnim by animateFloatAsState(
        targetValue = if (selected) 1.02f else 1f,
        animationSpec = spring(
            stiffness = Spring.StiffnessMediumLow,
            dampingRatio = Spring.DampingRatioMediumBouncy
        ),
        label = "CardScale"
    )

    val cardElevation = if (selected) 8.dp else 2.dp

    Card(
        modifier = modifier
            .scale(scaleAnim)
            .shadow(cardElevation, RoundedCornerShape(16.dp))
            .height(120.dp)
            .clickable(onClick = onClick),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(
            containerColor = if (selected) Green100 else Cream100
        ),
        border = if (selected) BorderStroke(2.dp, Green700) else null
    ) {
        Row(
            modifier = Modifier
                .fillMaxSize()
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .size(88.dp)
                    .clip(CircleShape)
                    .background(if (selected) Green200 else Cream200)
                    .padding(12.dp),
                contentAlignment = Alignment.Center
            ) {
                Image(
                    painter = painterResource(id = iconRes),
                    contentDescription = stringResource(descRes),
                    modifier = Modifier.size(54.dp)
                )
            }

            Spacer(modifier = Modifier.width(16.dp))

            Column(
                modifier = Modifier.weight(1f),
                verticalArrangement = Arrangement.Center
            ) {
                Text(
                    text = stringResource(titleRes),
                    style = MaterialTheme.typography.titleLarge,
                    color = if (selected) Green900 else Black700,
                    fontWeight = FontWeight.Bold
                )

                Spacer(modifier = Modifier.height(4.dp))

                CompositionLocalProvider(LocalContentColor provides if (selected) Green700 else Black500) {
                    Text(
                        text = stringResource(descRes),
                        style = MaterialTheme.typography.bodyMedium,
                        fontWeight = if (selected) FontWeight.Medium else FontWeight.Normal
                    )
                }
            }

            if (selected) {
                Image(
                    painter = painterResource(id = R.drawable.accept),
                    contentDescription = "Seleccionado",
                    modifier = Modifier.size(28.dp)
                )
            }
        }
    }
}
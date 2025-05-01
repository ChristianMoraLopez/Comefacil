package com.christian.nutriplan.ui.screens

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.spring
import androidx.compose.animation.expandVertically
import androidx.compose.animation.fadeIn
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.platform.LocalInspectionMode
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.christian.nutriplan.R
import com.christian.nutriplan.ui.components.PrimaryButton
import com.christian.nutriplan.ui.theme.Cream400
import com.christian.nutriplan.ui.theme.Green100
import com.christian.nutriplan.ui.theme.NutriPlanTheme
import kotlinx.coroutines.delay

@Composable
fun HomeScreen(
    onStartClick: () -> Unit
) {
    // Estados para animaciones
    var showTitle by remember { mutableStateOf(false) }
    var showDesc by remember { mutableStateOf(false) }
    var showButton by remember { mutableStateOf(false) }

    // Animaciones secuenciales
    LaunchedEffect(Unit) {
        delay(300)
        showTitle = true
        delay(400)
        showDesc = true
        delay(300)
        showButton = true
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Cream400),
        contentAlignment = Alignment.Center
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(24.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            // Logo con efecto de sombra y forma circular
            Image(
                painter = painterResource(id = R.drawable.ic_logo),
                contentDescription = stringResource(R.string.logo_desc),
                modifier = Modifier
                    .size(180.dp)
                    .shadow(8.dp, CircleShape)
                    .clip(CircleShape)
                    .background(Green100)
                    .padding(16.dp)
            )

            Spacer(modifier = Modifier.height(24.dp))

            // Título con animación
            AnimatedVisibility(
                visible = showTitle,
                enter = fadeIn(spring(Spring.DampingRatioMediumBouncy)) +
                        expandVertically(spring(Spring.DampingRatioMediumBouncy))
            ) {
                Card(
                    modifier = Modifier
                        .fillMaxWidth(0.8f)
                        .padding(bottom = 16.dp),
                    elevation = CardDefaults.cardElevation(4.dp),
                    colors = CardDefaults.cardColors(
                        containerColor = MaterialTheme.colorScheme.primaryContainer
                    )
                ) {
                    Text(
                        text = "¡Cocina Sin Drama!",
                        style = MaterialTheme.typography.headlineMedium,
                        color = MaterialTheme.colorScheme.primary,
                        textAlign = TextAlign.Center,
                        fontWeight = FontWeight.Bold,
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 12.dp)
                    )
                }
            }

            // Descripción con animación
            AnimatedVisibility(
                visible = showDesc,
                enter = fadeIn(spring(Spring.DampingRatioMediumBouncy)) +
                        expandVertically(spring(Spring.DampingRatioMediumBouncy))
            ) {
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(bottom = 36.dp),
                    elevation = CardDefaults.cardElevation(2.dp),
                    colors = CardDefaults.cardColors(
                        containerColor = MaterialTheme.colorScheme.surface
                    )
                ) {
                    Text(
                        text = "No es una dieta, es una relación sana (con la comida, no con tu ex).",
                        style = MaterialTheme.typography.bodyLarge,
                        textAlign = TextAlign.Center,
                        color = MaterialTheme.colorScheme.onSurface,
                        modifier = Modifier
                            .padding(16.dp)
                    )
                }
            }

            // Botón para comenzar con animación
            AnimatedVisibility(
                visible = showButton,
                enter = fadeIn(spring(Spring.DampingRatioMediumBouncy)) +
                        expandVertically(spring(Spring.DampingRatioMediumBouncy))
            ) {
                PrimaryButton(
                    text = "¡A cocinar se ha dicho!",
                    onClick = onStartClick,
                    modifier = Modifier.padding(horizontal = 32.dp)
                )
            }
        }
    }
}

@Preview(showBackground = true)
@Composable
fun HomeScreenPreview() {
    NutriPlanTheme {
        HomeScreen(
            onStartClick = {}
        )
    }
}
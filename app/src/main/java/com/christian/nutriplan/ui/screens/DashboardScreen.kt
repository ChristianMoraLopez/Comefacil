package com.christian.nutriplan.ui.screens

import UserViewModel
import android.content.Context
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.spring
import androidx.compose.animation.expandVertically
import androidx.compose.animation.fadeIn
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.outlined.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.christian.nutriplan.R
import com.christian.nutriplan.models.MealType
import com.christian.nutriplan.models.Usuario

import com.christian.nutriplan.ui.theme.*
import com.christian.nutriplan.utils.AuthManager
import org.koin.compose.koinInject
import kotlin.random.Random

@Composable
fun DashboardScreen(
    onLogout: () -> Unit,
    onMealSelected: (MealType) -> Unit = {},
    viewModel: UserViewModel = koinInject(),
    context: Context = LocalContext.current
) {
    val currentUser by viewModel.currentUser.collectAsState()
    val isLoading by viewModel.isLoading.collectAsState()
    val errorMessage by viewModel.errorMessage.collectAsState()

    // Estados de animación
    var showContent by remember { mutableStateOf(false) }
    val snackbarHostState = remember { SnackbarHostState() }

    // Lista de mensajes motivacionales
    val motivationalMessages = listOf(
        stringResource(id = R.string.motivation_1),
        stringResource(id = R.string.motivation_2),
        stringResource(id = R.string.motivation_3)
    )

    // Seleccionar un mensaje aleatorio
    val randomMessage = remember { motivationalMessages[Random.nextInt(motivationalMessages.size)] }

    // Cargar datos del usuario
    LaunchedEffect(Unit) {
        viewModel.fetchUserProfile(AuthManager.getAccessToken(context) ?: "")
        showContent = true
    }

    // Manejar errores
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
        onMealSelected = onMealSelected,
        onLogout = {
            viewModel.logout(context)
            onLogout()
        }
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
    onLogout: () -> Unit
) {
    val scrollState = rememberScrollState()

    Scaffold(
        snackbarHost = { SnackbarHost(hostState = snackbarHostState) },
        containerColor = Cream400,
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        text = stringResource(id = R.string.dashboard_title),
                        style = MaterialTheme.typography.titleLarge,
                        color = Green800
                    )
                },
                actions = {
                    IconButton(onClick = onLogout) {
                        Icon(
                            imageVector = Icons.Default.ExitToApp,
                            contentDescription = stringResource(id = R.string.logout_button),
                            tint = Green700
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = Cream300
                )
            )
        }
    ) { padding ->
        AnimatedVisibility(
            visible = showContent,
            enter = fadeIn(
                animationSpec = spring(
                    dampingRatio = Spring.DampingRatioMediumBouncy,
                    stiffness = Spring.StiffnessLow
                )
            ) + expandVertically()
        ) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(padding)
                    .verticalScroll(scrollState),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                // Tarjeta de saludo
                WelcomeCard(
                    userName = currentUser?.nombre ?: "Parce",
                    motivationalMessage = motivationalMessage
                )

                Spacer(modifier = Modifier.height(24.dp))

                // Título de sección de comidas
                Text(
                    text = stringResource(id = R.string.dashboard_subtitle),
                    style = MaterialTheme.typography.headlineSmall,
                    color = Green700,
                    modifier = Modifier.padding(bottom = 16.dp),
                    textAlign = TextAlign.Center
                )

                // Opciones de comidas
                MealOptions(onMealSelected = onMealSelected)

                Spacer(modifier = Modifier.height(24.dp))

                // Estadísticas o resumen del día
                DailyProgressCard()

                Spacer(modifier = Modifier.height(16.dp))

                // Información del perfil
                UserProfileSummary(user = currentUser)

                Spacer(modifier = Modifier.height(24.dp))

                if (isLoading) {
                    CircularProgressIndicator(color = Green700)
                }
            }
        }
    }
}

@Composable
private fun WelcomeCard(userName: String, motivationalMessage: String) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(16.dp),
        colors = CardDefaults.cardColors(containerColor = Lilac200),
        shape = RoundedCornerShape(16.dp),
        elevation = CardDefaults.cardElevation(4.dp)
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                text = "¡Quihubo, $userName!",
                style = MaterialTheme.typography.headlineMedium,
                fontWeight = FontWeight.Bold,
                color = Green900
            )

            Spacer(modifier = Modifier.height(8.dp))

            Text(
                text = motivationalMessage,
                style = MaterialTheme.typography.bodyLarge,
                textAlign = TextAlign.Center,
                color = Green800
            )
        }
    }
}

@Composable
private fun MealOptions(onMealSelected: (MealType) -> Unit) {
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
            modifier = Modifier.padding(bottom = 12.dp)
        )

        // Desayuno
        MealCard(
            title = stringResource(id = R.string.breakfast_title),
            backgroundColor = Yellow300,
            iconTint = Yellow500,
            onClick = { onMealSelected(MealType.BREAKFAST) }
        )

        Spacer(modifier = Modifier.height(12.dp))

        // Almuerzo
        MealCard(
            title = stringResource(id = R.string.lunch_title),
            backgroundColor = Green200,
            iconTint = Green600,
            onClick = { onMealSelected(MealType.LUNCH) }
        )

        Spacer(modifier = Modifier.height(12.dp))

        // Cena
        MealCard(
            title = stringResource(id = R.string.dinner_title),
            backgroundColor = Blue200,
            iconTint = Blue500,
            onClick = { onMealSelected(MealType.DINNER) }
        )

        Spacer(modifier = Modifier.height(12.dp))

        // Snack/Mecato
        MealCard(
            title = stringResource(id = R.string.snack_title),
            backgroundColor = Orange200,
            iconTint = Orange500,
            onClick = { onMealSelected(MealType.SNACK) }
        )
    }
}

@Composable
private fun MealCard(
    title: String,
    backgroundColor: Color,
    iconTint: Color,
    onClick: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick),
        colors = CardDefaults.cardColors(containerColor = backgroundColor),
        shape = RoundedCornerShape(12.dp),
        elevation = CardDefaults.cardElevation(2.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically
            ) {
                Box(
                    modifier = Modifier
                        .size(40.dp)
                        .clip(CircleShape)
                        .background(Color.White.copy(alpha = 0.4f)),
                    contentAlignment = Alignment.Center
                ) {
                    when {
                        title.contains("Desayuno", ignoreCase = true) -> Icon(
                            painter = painterResource(R.drawable.desayuno),
                            contentDescription = "Breakfast icon",
                            tint = iconTint,
                            modifier = Modifier.size(24.dp)
                        )
                        title.contains("Almuerzo", ignoreCase = true) -> Icon(
                            painter = painterResource(R.drawable.lunch),
                            contentDescription = "Lunch icon",
                            tint = iconTint,
                            modifier = Modifier.size(24.dp)
                        )
                        title.contains("Comida", ignoreCase = true) -> Icon(
                            painter = painterResource(R.drawable.dinner),
                            contentDescription = "Dinner icon",
                            tint = iconTint,
                            modifier = Modifier.size(24.dp)
                        )
                        else -> Icon(
                            painter = painterResource(R.drawable.cafe),
                            contentDescription = "Snack icon",
                            tint = iconTint,
                            modifier = Modifier.size(24.dp)
                        )
                    }
                }

                Spacer(modifier = Modifier.width(16.dp))

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
                tint = iconTint
            )
        }
    }
}

@Composable
private fun DailyProgressCard() {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp),
        colors = CardDefaults.cardColors(containerColor = Cream100),
        shape = RoundedCornerShape(16.dp),
        border = BorderStroke(1.dp, Green400)
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                text = stringResource(id = R.string.progress_title),
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                color = Green800,
                modifier = Modifier.padding(bottom = 16.dp)
            )

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceEvenly
            ) {
                ProgressItem(
                    label = "Calorías",
                    value = "1200/2000",
                    color = Green500
                )

                ProgressItem(
                    label = "Agua",
                    value = "5/8 vasos",
                    color = Blue500
                )

                ProgressItem(
                    label = "Proteína",
                    value = "45/70g",
                    color = Red500
                )
            }

            Spacer(modifier = Modifier.height(16.dp))

            LinearProgressIndicator(
                progress = 0.6f,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(8.dp)
                    .clip(RoundedCornerShape(4.dp)),
                color = Green600,
                trackColor = Green100
            )

            Spacer(modifier = Modifier.height(8.dp))

            Text(
                text = "¡Va por buen camino, no afloje!",
                style = MaterialTheme.typography.bodyMedium,
                color = Green700,
                textAlign = TextAlign.Center
            )
        }
    }
}

@Composable
private fun ProgressItem(
    label: String,
    value: String,
    color: Color
) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(
            text = label,
            style = MaterialTheme.typography.bodyMedium,
            color = Black500
        )

        Text(
            text = value,
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.Bold,
            color = color
        )
    }
}

@Composable
private fun UserProfileSummary(user: Usuario?) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp),
        colors = CardDefaults.cardColors(containerColor = Cream100),
        shape = RoundedCornerShape(16.dp),
        elevation = CardDefaults.cardElevation(1.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .size(50.dp)
                    .clip(CircleShape)
                    .background(Green200),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = Icons.Default.Person,
                    contentDescription = null,
                    tint = Green700,
                    modifier = Modifier.size(30.dp)
                )
            }

            Spacer(modifier = Modifier.width(16.dp))

            Column {
                Text(
                    text = user?.nombre ?: "Usuario",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Medium,
                    color = Black700
                )

                Text(
                    text = user?.email ?: "email@ejemplo.com",
                    style = MaterialTheme.typography.bodyMedium,
                    color = Black500
                )

                if (user?.ciudad != null || user?.localidad != null) {
                    Text(
                        text = "${user.ciudad ?: ""} ${user.localidad ?: ""}",
                        style = MaterialTheme.typography.bodySmall,
                        color = Black400
                    )
                }
            }

            Spacer(modifier = Modifier.weight(1f))

            IconButton(
                onClick = { /* Abrir pantalla de perfil */ }
            ) {
                Icon(
                    imageVector = Icons.Default.Edit,
                    contentDescription = stringResource(id = R.string.update_profile_button),
                    tint = Green700
                )
            }
        }
    }
}
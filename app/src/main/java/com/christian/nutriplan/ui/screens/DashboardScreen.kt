package com.christian.nutriplan.ui.screens

import UserViewModel
import android.content.Context
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.spring
import androidx.compose.animation.expandVertically
import androidx.compose.animation.fadeIn
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.christian.nutriplan.models.Usuario
import com.christian.nutriplan.ui.components.PrimaryButton
import com.christian.nutriplan.ui.theme.*
import com.christian.nutriplan.utils.AuthManager
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import org.koin.compose.koinInject

@Composable
fun DashboardScreen(
    onLogout: () -> Unit,
    viewModel: UserViewModel = koinInject(),
    context: Context = LocalContext.current
) {
    val currentUser by viewModel.currentUser.collectAsState()
    val isLoading by viewModel.isLoading.collectAsState()
    val errorMessage by viewModel.errorMessage.collectAsState()

    // Estados de animación
    var showContent by remember { mutableStateOf(false) }
    val snackbarHostState = remember { SnackbarHostState() }

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
        onLogout = {
            viewModel.logout(context)
            onLogout()
        }
    )
}

@Composable
private fun DashboardContent(
    currentUser: Usuario?,
    isLoading: Boolean,
    showContent: Boolean,
    snackbarHostState: SnackbarHostState,
    onLogout: () -> Unit
) {
    Scaffold(
        snackbarHost = { SnackbarHost(hostState = snackbarHostState) },
        containerColor = Cream400
    ) { padding ->
        AnimatedVisibility(
            visible = showContent,
            enter = fadeIn() + expandVertically()
        ) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(padding)
                    .padding(24.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center
            ) {
                // Header
                Text(
                    text = "Welcome, ${currentUser?.nombre ?: "User"}!",
                    style = MaterialTheme.typography.headlineMedium,
                    color = Green800,
                    fontWeight = FontWeight.Bold
                )

                Spacer(modifier = Modifier.height(24.dp))

                // Tarjeta de perfil
                UserProfileCard(currentUser)

                Spacer(modifier = Modifier.height(40.dp))

                // Botón de logout
                PrimaryButton(
                    text = "Logout",
                    onClick = onLogout,
                    enabled = !isLoading
                )

                if (isLoading) {
                    Spacer(modifier = Modifier.height(16.dp))
                    CircularProgressIndicator(color = Green700)
                }
            }
        }
    }
}

@Composable
private fun UserProfileCard(user: Usuario?) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = Cream100),
        border = BorderStroke(1.dp, Green700),
        shape = RoundedCornerShape(16.dp)
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Text(
                text = "User Profile",
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Bold,
                color = Green900
            )

            ProfileItem("Name", user?.nombre)
            ProfileItem("Email", user?.email)
            // Agregar más campos según necesidad
        }
    }
}

@Composable
private fun ProfileItem(label: String, value: String?) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Text(
            text = "$label:",
            style = MaterialTheme.typography.bodyLarge,
            fontWeight = FontWeight.Medium,
            color = Black700
        )
        Text(
            text = value ?: "N/A",
            style = MaterialTheme.typography.bodyLarge,
            color = Black700
        )
    }
}
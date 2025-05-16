package com.christian.nutriplan.ui.screens

import androidx.compose.material3.OutlinedTextFieldDefaults
import UserViewModel
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.unit.dp
import androidx.compose.ui.graphics.Color // Import for Color
import androidx.navigation.NavController
import com.christian.nutriplan.R
import com.christian.nutriplan.models.Usuario
import com.christian.nutriplan.ui.theme.*
import com.christian.nutriplan.utils.AuthManager
import com.christian.nutriplan.utils.isValidEmail
import org.koin.compose.koinInject

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun EditProfileScreen(
    navController: NavController,
    userId: Int,
    viewModel: UserViewModel = koinInject()
) {
    val context = LocalContext.current
    val isLoading by viewModel.isLoading.collectAsState()
    val errorMessage by viewModel.errorMessage.collectAsState()
    val currentUser by viewModel.currentUser.collectAsState()
    val snackbarHostState = remember { SnackbarHostState() }

    // Form state
    var name by remember { mutableStateOf(currentUser?.nombre ?: "") }
    var email by remember { mutableStateOf(currentUser?.email ?: "") }
    var password by remember { mutableStateOf("") }
    var city by remember { mutableStateOf(currentUser?.ciudad ?: "") }
    var locality by remember { mutableStateOf(currentUser?.localidad ?: "") }
    var nameError by remember { mutableStateOf(false) }
    var emailError by remember { mutableStateOf(false) }
    var passwordError by remember { mutableStateOf(false) }

    // Fetch user if currentUser is null and userId is provided
    LaunchedEffect(userId, currentUser) {
        if (currentUser == null && userId != 0) {
            viewModel.fetchUserProfile(AuthManager.getAccessToken(context) ?: "")
        } else if (currentUser == null && userId == 0) {
            snackbarHostState.showSnackbar("Invalid user ID. Please try again.")
        }
    }

    // Update form state when currentUser changes
    LaunchedEffect(currentUser) {
        currentUser?.let { user ->
            name = user.nombre
            email = user.email
            city = user.ciudad ?: ""
            locality = user.localidad ?: ""
        }
    }

    LaunchedEffect(errorMessage) {
        errorMessage?.let {
            snackbarHostState.showSnackbar(it)
            viewModel.clearErrorMessage()
        }
    }

    Scaffold(
        snackbarHost = { SnackbarHost(hostState = snackbarHostState) },
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        text = stringResource(id = R.string.edit_profile_title),
                        style = MaterialTheme.typography.titleLarge,
                        color = Green800
                    )
                },
                navigationIcon = {
                    IconButton(onClick = { navController.popBackStack() }) {
                        Icon(
                            imageVector = Icons.Default.ArrowBack,
                            contentDescription = stringResource(id = R.string.back_button),
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
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(horizontal = 16.dp)
                .verticalScroll(rememberScrollState()),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Spacer(modifier = Modifier.height(16.dp))

            // Name field
            OutlinedTextField(
                value = name,
                onValueChange = {
                    name = it
                    nameError = it.isBlank()
                },
                label = { Text(stringResource(id = R.string.name_label)) },
                isError = nameError,
                supportingText = {
                    if (nameError) {
                        Text(stringResource(id = R.string.name_required))
                    }
                },
                modifier = Modifier.fillMaxWidth(),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = Green600,
                    unfocusedBorderColor = Green400
                )
            )

            Spacer(modifier = Modifier.height(16.dp))

            // Email field
            OutlinedTextField(
                value = email,
                onValueChange = {
                    email = it
                    emailError = !it.isValidEmail()
                },
                label = { Text(stringResource(id = R.string.email_label)) },
                isError = emailError,
                supportingText = {
                    if (emailError) {
                        Text(stringResource(id = R.string.email_invalid))
                    }
                },
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Email),
                modifier = Modifier.fillMaxWidth(),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = Green600,
                    unfocusedBorderColor = Green400
                )
            )

            Spacer(modifier = Modifier.height(16.dp))

            // Password field (optional)
            OutlinedTextField(
                value = password,
                onValueChange = {
                    password = it
                    passwordError = it.isNotEmpty() && it.length < 8
                },
                label = { Text(stringResource(id = R.string.password_label)) },
                placeholder = { Text(stringResource(id = R.string.password_optional)) },
                visualTransformation = PasswordVisualTransformation(),
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password),
                isError = passwordError,
                supportingText = {
                    if (passwordError) {
                        Text("La contraseña debe tener al menos 8 caracteres")
                    }
                },
                modifier = Modifier.fillMaxWidth(),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = Green600,
                    unfocusedBorderColor = Green400
                )
            )

            Spacer(modifier = Modifier.height(16.dp))

            // City field
            OutlinedTextField(
                value = city,
                onValueChange = { city = it },
                label = { Text(stringResource(id = R.string.city_label)) },
                modifier = Modifier.fillMaxWidth(),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = Green600,
                    unfocusedBorderColor = Green400
                )
            )

            Spacer(modifier = Modifier.height(16.dp))

            // Locality field
            OutlinedTextField(
                value = locality,
                onValueChange = { locality = it },
                label = { Text(stringResource(id = R.string.locality_label)) },
                modifier = Modifier.fillMaxWidth(),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = Green600,
                    unfocusedBorderColor = Green400
                )
            )

            Spacer(modifier = Modifier.height(24.dp))

            // Save button
            Button(
                onClick = {
                    nameError = name.isBlank()
                    emailError = !email.isValidEmail()
                    passwordError = password.isNotEmpty() && password.length < 8

                    if (!nameError && !emailError && !passwordError) {
                        val updatedUser = Usuario(
                            usuarioId = currentUser?.usuarioId ?: userId,
                            nombre = name,
                            email = email,
                            contrasena = password,
                            aceptaTerminos = currentUser?.aceptaTerminos ?: true,
                            rol = currentUser?.rol ?: "USER",
                            fechaRegistro = currentUser?.fechaRegistro ?: "",
                            ciudad = city,
                            localidad = locality
                        )
                        viewModel.updateUserProfile(updatedUser) {
                            navController.popBackStack()
                        }
                    }
                },
                enabled = !isLoading,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(50.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = Green600,
                    contentColor = Color.White
                ),
                shape = RoundedCornerShape(12.dp)
            ) {
                if (isLoading) {
                    CircularProgressIndicator(color = Color.White, modifier = Modifier.size(24.dp))
                } else {
                    Text(stringResource(id = R.string.save_button))
                }
            }

            Spacer(modifier = Modifier.height(16.dp))
        }
    }
}
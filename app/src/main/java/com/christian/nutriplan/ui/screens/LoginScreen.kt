package com.christian.nutriplan.ui.screens

import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Email
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.christian.nutriplan.R
import com.christian.nutriplan.network.UserRepository
import com.christian.nutriplan.ui.components.PrimaryButton
import com.christian.nutriplan.ui.theme.*
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.android.gms.common.api.ApiException
import kotlinx.coroutines.launch
import org.koin.compose.koinInject

@Composable
fun LoginScreen(
    onLoginSuccess: () -> Unit,
    onRegisterClick: () -> Unit
) {
    val userRepository: UserRepository = koinInject()
    var email by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    var isLoading by remember { mutableStateOf(false) }
    var errorMessage by remember { mutableStateOf<String?>(null) }
    val coroutineScope = rememberCoroutineScope()
    val context = LocalContext.current

    // Google Sign-In Launcher
    val googleSignInLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.StartActivityForResult()
    ) { result ->
        coroutineScope.launch {
            isLoading = true
            try {
                val task = GoogleSignIn.getSignedInAccountFromIntent(result.data)
                val account = task.getResult(ApiException::class.java)
                account.idToken?.let { idToken ->
                    val signInResult = userRepository.signInWithGoogle(idToken)
                    if (signInResult.isSuccess) {
                        onLoginSuccess()
                    } else {
                        errorMessage = "Google Sign-In failed: ${signInResult.exceptionOrNull()?.message}"
                    }
                } ?: run {
                    errorMessage = "Google Sign-In failed: No ID token"
                }
            } catch (e: Exception) {
                errorMessage = "Google Sign-In failed: ${e.message}"
            } finally {
                isLoading = false
            }
        }
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(
                Cream300
            ),
        contentAlignment = Alignment.Center
    ) {
        Card(
            modifier = Modifier
                .fillMaxWidth(0.9f)
                .shadow(
                    elevation = 10.dp,
                    shape = RoundedCornerShape(16.dp)
                ),
            elevation = CardDefaults.cardElevation(defaultElevation = 6.dp),
            colors = CardDefaults.cardColors(
                containerColor = Cream100
            ),
            shape = RoundedCornerShape(16.dp)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 24.dp, vertical = 32.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                Text(
                    text = stringResource(R.string.welcome_back),
                    style = MaterialTheme.typography.headlineSmall.copy(
                        fontWeight = FontWeight.Bold,
                        fontSize = 24.sp
                    ),
                    color = Green700,
                    textAlign = TextAlign.Center
                )

                Text(
                    text = stringResource(R.string.access_your_nutrition_plan),
                    style = MaterialTheme.typography.bodyMedium,
                    color = Black500,
                    textAlign = TextAlign.Center,
                    modifier = Modifier.padding(bottom = 8.dp)
                )

                if (errorMessage != null) {
                    Card(
                        colors = CardDefaults.cardColors(
                            containerColor = Color(0xFFFFEBEE)
                        ),
                        shape = RoundedCornerShape(8.dp),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text(
                            text = errorMessage ?: "",
                            color = Color(0xFFD32F2F),
                            style = MaterialTheme.typography.bodySmall,
                            modifier = Modifier.padding(12.dp),
                            textAlign = TextAlign.Center
                        )
                    }
                }

                OutlinedTextField(
                    value = email,
                    onValueChange = { email = it },
                    label = { Text(stringResource(R.string.email)) },
                    leadingIcon = { Icon(Icons.Filled.Email, contentDescription = null, tint = Green500) },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true,
                    shape = RoundedCornerShape(12.dp),
                    keyboardOptions = KeyboardOptions.Default.copy(
                        keyboardType = KeyboardType.Email
                    ),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = Green500,
                        unfocusedBorderColor = Black300,
                        focusedLabelColor = Green700,
                        cursorColor = Green700
                    )
                )

                OutlinedTextField(
                    value = password,
                    onValueChange = { password = it },
                    label = { Text(stringResource(R.string.password)) },
                    leadingIcon = { Icon(Icons.Filled.Lock, contentDescription = null, tint = Green500) },
                    visualTransformation = PasswordVisualTransformation(),
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true,
                    shape = RoundedCornerShape(12.dp),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = Green500,
                        unfocusedBorderColor = Black300,
                        focusedLabelColor = Green700,
                        cursorColor = Green700
                    )
                )

                Spacer(modifier = Modifier.height(8.dp))

                PrimaryButton(
                    text = if (isLoading) stringResource(R.string.logging_in) else stringResource(R.string.login),
                    onClick = {
                        if (email.isBlank() || password.isBlank()) {
                            errorMessage = context.getString(R.string.error_empty_fields)
                            return@PrimaryButton
                        }

                        isLoading = true
                        errorMessage = null

                        coroutineScope.launch {
                            val result = userRepository.loginUser(email, password)
                            isLoading = false

                            if (result.isSuccess) {
                                onLoginSuccess()
                            } else {
                                errorMessage = when (result.exceptionOrNull()?.message) {
                                    "Credenciales incorrectas" -> context.getString(R.string.error_invalid_credentials)
                                    "Network error" -> context.getString(R.string.error_network)
                                    else -> context.getString(R.string.error_login_failed)
                                }
                            }
                        }
                    },
                    enabled = !isLoading && email.isNotBlank() && password.isNotBlank(),
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(50.dp)
                )

                // Google Sign-In Button
                Button(
                    onClick = {
                        try {
                            val signInIntent = userRepository.getGoogleSignInClient().signInIntent
                            googleSignInLauncher.launch(signInIntent)
                        } catch (e: Exception) {
                            errorMessage = "Error launching Google Sign-In: ${e.message}"
                        }
                    },
                    enabled = !isLoading,
                    colors = ButtonDefaults.buttonColors(
                        containerColor = Color.White,
                        contentColor = Color.Black
                    ),
                    shape = RoundedCornerShape(12.dp),
                    elevation = ButtonDefaults.buttonElevation(
                        defaultElevation = 2.dp
                    ),
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(50.dp)
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.Center
                    ) {
                        Icon(
                            painter = painterResource(id = R.drawable.googlesymbol),
                            contentDescription = "Google Logo",
                            tint = Color.Unspecified
                        )
                        Spacer(modifier = Modifier.width(12.dp))
                        Text(
                            text = "Sign in with Google",
                            fontWeight = FontWeight.Medium
                        )
                    }
                }

                Spacer(modifier = Modifier.height(8.dp))

                // Movido el botón "New here?" arriba del "Forgot password"
                Button(
                    onClick = onRegisterClick,
                    colors = ButtonDefaults.buttonColors(
                        containerColor = Cream200,
                        contentColor = Green700
                    ),
                    shape = RoundedCornerShape(12.dp),
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(44.dp)
                ) {
                    Text(
                        text = stringResource(R.string.new_here),
                        fontWeight = FontWeight.Medium
                    )
                }

                // Forgot Password as a Text Button
                TextButton(
                    onClick = { /* TODO: Implement forgot password */ },
                    modifier = Modifier.padding(top = 4.dp)
                ) {
                    Text(
                        text = stringResource(R.string.forgot_password),
                        style = MaterialTheme.typography.bodyMedium,
                        color = Green500,
                        fontWeight = FontWeight.Medium
                    )
                }
            }
        }
    }
}

@Preview(showBackground = true)
@Composable
fun LoginScreenPreview() {
    NutriPlanTheme {
        LoginScreen(
            onLoginSuccess = {},
            onRegisterClick = {}
        )
    }
}
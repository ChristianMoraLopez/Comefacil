package com.christian.nutriplan.ui.screens

import android.annotation.SuppressLint
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Email
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material.icons.filled.VisibilityOff
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
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
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
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import org.koin.compose.koinInject

@SuppressLint("StringFormatInvalid")
@Composable
fun LoginScreen(
    onLoginSuccess: () -> Unit,
    onRegisterClick: () -> Unit,
    userRepository: UserRepository = koinInject()
) {
    var email by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    var isLoading by remember { mutableStateOf(false) }
    var errorMessage by remember { mutableStateOf<String?>(null) }
    var passwordVisible by remember { mutableStateOf(false) }
    val coroutineScope = rememberCoroutineScope()
    val context = LocalContext.current

    // Animaciones
    val infiniteTransition = rememberInfiniteTransition(label = "background")
    val animatedScale by infiniteTransition.animateFloat(
        initialValue = 1f,
        targetValue = 1.05f,
        animationSpec = infiniteRepeatable(
            animation = tween(4000, easing = EaseInOutSine),
            repeatMode = RepeatMode.Reverse
        ),
        label = "scale"
    )

    val cardScale by animateFloatAsState(
        targetValue = if (isLoading) 0.98f else 1f,
        animationSpec = spring(dampingRatio = Spring.DampingRatioMediumBouncy),
        label = "card_scale"
    )

    // Animación de entrada
    var isVisible by remember { mutableStateOf(false) }
    LaunchedEffect(Unit) {
        delay(100)
        isVisible = true
    }

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
                        errorMessage = context.getString(
                            R.string.error_google_sign_in_failed,
                            signInResult.exceptionOrNull()?.message ?: "Unknown error"
                        )
                    }
                } ?: run {
                    errorMessage = context.getString(R.string.error_no_id_token)
                }
            } catch (e: ApiException) {
                errorMessage = context.getString(R.string.error_google_sign_in_failed, e.message ?: "API error")
            } catch (e: Exception) {
                errorMessage = context.getString(R.string.error_google_sign_in_failed, e.message ?: "Unexpected error")
            } finally {
                isLoading = false
            }
        }
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(
                brush = Brush.verticalGradient(
                    colors = listOf(
                        Green100.copy(alpha = 0.3f),
                        Cream300,
                        AccentBeigeSoft.copy(alpha = 0.5f)
                    )
                )
            )
    ) {
        // Elementos decorativos flotantes
        FloatingElements()

        AnimatedVisibility(
            visible = isVisible,
            enter = slideInVertically(
                initialOffsetY = { it },
                animationSpec = spring(dampingRatio = Spring.DampingRatioMediumBouncy)
            ) + fadeIn(animationSpec = tween(800)),
            modifier = Modifier.align(Alignment.Center)
        ) {
            Card(
                modifier = Modifier
                    .fillMaxWidth(0.9f)
                    .scale(cardScale)
                    .shadow(
                        elevation = 20.dp,
                        shape = RoundedCornerShape(24.dp),
                        ambientColor = Green500.copy(alpha = 0.1f),
                        spotColor = Green700.copy(alpha = 0.1f)
                    ),
                elevation = CardDefaults.cardElevation(defaultElevation = 12.dp),
                colors = CardDefaults.cardColors(
                    containerColor = Color.White.copy(alpha = 0.95f)
                ),
                shape = RoundedCornerShape(24.dp)
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 32.dp, vertical = 40.dp),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.spacedBy(20.dp)
                ) {
                    // Logo/Icono animado
                    AnimatedLogo()

                    Text(
                        text = stringResource(R.string.welcome_back),
                        style = MaterialTheme.typography.headlineMedium.copy(
                            fontWeight = FontWeight.Bold,
                            fontSize = 28.sp
                        ),
                        color = Green700,
                        textAlign = TextAlign.Center
                    )

                    Text(
                        text = stringResource(R.string.access_your_nutrition_plan),
                        style = MaterialTheme.typography.bodyLarge,
                        color = Black400,
                        textAlign = TextAlign.Center,
                        modifier = Modifier.padding(bottom = 8.dp)
                    )

                    // Error message con animación
                    AnimatedVisibility(
                        visible = errorMessage != null,
                        enter = slideInVertically() + fadeIn(),
                        exit = slideOutVertically() + fadeOut()
                    ) {
                        Card(
                            colors = CardDefaults.cardColors(
                                containerColor = Red100.copy(alpha = 0.8f)
                            ),
                            shape = RoundedCornerShape(12.dp),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Text(
                                text = errorMessage ?: "",
                                color = Red500,
                                style = MaterialTheme.typography.bodyMedium,
                                modifier = Modifier.padding(16.dp),
                                textAlign = TextAlign.Center,
                                fontWeight = FontWeight.Medium
                            )
                        }
                    }

                    // Email field
                    AnimatedTextField(
                        value = email,
                        onValueChange = { email = it },
                        label = stringResource(R.string.email),
                        leadingIcon = Icons.Filled.Email,
                        keyboardType = KeyboardType.Email,
                        isError = errorMessage != null
                    )

                    // Password field
                    AnimatedTextField(
                        value = password,
                        onValueChange = { password = it },
                        label = stringResource(R.string.password),
                        leadingIcon = Icons.Filled.Lock,
                        isPassword = true,
                        passwordVisible = passwordVisible,
                        onPasswordVisibilityToggle = { passwordVisible = !passwordVisible },
                        isError = errorMessage != null
                    )

                    Spacer(modifier = Modifier.height(8.dp))

                    // Login button with animation
                    AnimatedButton(
                        text = if (isLoading) stringResource(R.string.logging_in) else stringResource(R.string.login),
                        onClick = {
                            if (email.isBlank() || password.isBlank()) {
                                errorMessage = context.getString(R.string.error_empty_fields)
                                return@AnimatedButton
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
                        isLoading = isLoading
                    )

                    // Google Sign-In button
                    GoogleSignInButton(
                        onClick = {
                            try {
                                val signInIntent = userRepository.getGoogleSignInClient().signInIntent
                                googleSignInLauncher.launch(signInIntent)
                            } catch (e: Exception) {
                                errorMessage = context.getString(R.string.error_google_sign_in_failed, e.message ?: "Launch error")
                            }
                        },
                        enabled = !isLoading
                    )

                    Spacer(modifier = Modifier.height(8.dp))

                    // Register button
                    OutlinedButton(
                        onClick = onRegisterClick,
                        colors = ButtonDefaults.outlinedButtonColors(
                            contentColor = Green600
                        ),
                        border = ButtonDefaults.outlinedButtonBorder.copy(
                            brush = Brush.horizontalGradient(
                                colors = listOf(Green400, Green600)
                            )
                        ),
                        shape = RoundedCornerShape(16.dp),
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(50.dp)
                    ) {
                        Text(
                            text = stringResource(R.string.new_here),
                            fontWeight = FontWeight.Medium,
                            fontSize = 16.sp
                        )
                    }

                    TextButton(
                        onClick = { /* TODO: Implement forgot password */ },
                        modifier = Modifier.padding(top = 8.dp)
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
}

@Composable
private fun FloatingElements() {
    val infiniteTransition = rememberInfiniteTransition(label = "floating")

    repeat(6) { index ->
        val animatedOffset by infiniteTransition.animateFloat(
            initialValue = 0f,
            targetValue = 30f,
            animationSpec = infiniteRepeatable(
                animation = tween(
                    durationMillis = 3000 + (index * 500),
                    easing = EaseInOutSine
                ),
                repeatMode = RepeatMode.Reverse
            ),
            label = "offset_$index"
        )

        Box(
            modifier = Modifier
                .offset(
                    x = (50 + index * 60).dp,
                    y = (100 + index * 80 + animatedOffset).dp
                )
                .size((20 + index * 5).dp)
                .clip(CircleShape)
                .background(
                    when (index % 4) {
                        0 -> Green200.copy(alpha = 0.3f)
                        1 -> AccentGreenLime.copy(alpha = 0.2f)
                        2 -> Yellow200.copy(alpha = 0.3f)
                        else -> Orange200.copy(alpha = 0.2f)
                    }
                )
        )
    }
}

@Composable
private fun AnimatedLogo() {
    val infiniteTransition = rememberInfiniteTransition(label = "logo")
    val rotation by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 360f,
        animationSpec = infiniteRepeatable(
            animation = tween(20000, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "rotation"
    )

    Box(
        modifier = Modifier
            .size(80.dp)
            .graphicsLayer { rotationZ = rotation }
            .background(
                brush = Brush.radialGradient(
                    colors = listOf(Green400, Green600, Green800)
                ),
                shape = CircleShape
            ),
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = "🥗",
            fontSize = 32.sp
        )
    }
}

@Composable
private fun AnimatedTextField(
    value: String,
    onValueChange: (String) -> Unit,
    label: String,
    leadingIcon: androidx.compose.ui.graphics.vector.ImageVector,
    keyboardType: KeyboardType = KeyboardType.Text,
    isPassword: Boolean = false,
    passwordVisible: Boolean = false,
    onPasswordVisibilityToggle: (() -> Unit)? = null,
    isError: Boolean = false
) {
    val focusedScale by animateFloatAsState(
        targetValue = if (value.isNotEmpty()) 1.02f else 1f,
        animationSpec = spring(dampingRatio = Spring.DampingRatioMediumBouncy),
        label = "text_field_scale"
    )

    OutlinedTextField(
        value = value,
        onValueChange = onValueChange,
        label = { Text(label) },
        leadingIcon = {
            Icon(
                leadingIcon,
                contentDescription = null,
                tint = if (isError) Red400 else Green500
            )
        },
        trailingIcon = if (isPassword) {
            {
                IconButton(onClick = { onPasswordVisibilityToggle?.invoke() }) {
                    Icon(
                        imageVector = if (passwordVisible) Icons.Filled.Visibility else Icons.Filled.VisibilityOff,
                        contentDescription = if (passwordVisible) "Hide password" else "Show password",
                        tint = Green500
                    )
                }
            }
        } else null,
        visualTransformation = if (isPassword && !passwordVisible) PasswordVisualTransformation() else VisualTransformation.None,
        modifier = Modifier
            .fillMaxWidth()
            .scale(focusedScale),
        singleLine = true,
        shape = RoundedCornerShape(16.dp),
        keyboardOptions = KeyboardOptions.Default.copy(keyboardType = keyboardType),
        colors = OutlinedTextFieldDefaults.colors(
            focusedBorderColor = if (isError) Red400 else Green500,
            unfocusedBorderColor = if (isError) Red300 else Black200,
            focusedLabelColor = if (isError) Red500 else Green700,
            cursorColor = Green700,
            errorBorderColor = Red400,
            errorLabelColor = Red500
        ),
        isError = isError
    )
}

@Composable
private fun AnimatedButton(
    text: String,
    onClick: () -> Unit,
    enabled: Boolean,
    isLoading: Boolean
) {
    val buttonScale by animateFloatAsState(
        targetValue = if (enabled && !isLoading) 1f else 0.95f,
        animationSpec = spring(dampingRatio = Spring.DampingRatioMediumBouncy),
        label = "button_scale"
    )

    Button(
        onClick = onClick,
        enabled = enabled,
        colors = ButtonDefaults.buttonColors(
            containerColor = Green500,
            contentColor = Color.White,
            disabledContainerColor = Green300,
            disabledContentColor = Color.White.copy(alpha = 0.7f)
        ),
        shape = RoundedCornerShape(16.dp),
        elevation = ButtonDefaults.buttonElevation(
            defaultElevation = 6.dp,
            pressedElevation = 12.dp
        ),
        modifier = Modifier
            .fillMaxWidth()
            .height(56.dp)
            .scale(buttonScale)
            .shadow(
                elevation = 8.dp,
                shape = RoundedCornerShape(16.dp),
                ambientColor = Green500.copy(alpha = 0.2f)
            )
    ) {
        if (isLoading) {
            CircularProgressIndicator(
                modifier = Modifier.size(20.dp),
                color = Color.White,
                strokeWidth = 2.dp
            )
            Spacer(modifier = Modifier.width(12.dp))
        }
        Text(
            text = text,
            fontWeight = FontWeight.Bold,
            fontSize = 16.sp
        )
    }
}

@Composable
private fun GoogleSignInButton(
    onClick: () -> Unit,
    enabled: Boolean
) {
    val buttonScale by animateFloatAsState(
        targetValue = if (enabled) 1f else 0.95f,
        animationSpec = spring(dampingRatio = Spring.DampingRatioMediumBouncy),
        label = "google_button_scale"
    )

    Button(
        onClick = onClick,
        enabled = enabled,
        colors = ButtonDefaults.buttonColors(
            containerColor = Color.White,
            contentColor = Color.Black,
            disabledContainerColor = Color.White.copy(alpha = 0.7f)
        ),
        shape = RoundedCornerShape(16.dp),
        elevation = ButtonDefaults.buttonElevation(defaultElevation = 4.dp),
        modifier = Modifier
            .fillMaxWidth()
            .height(56.dp)
            .scale(buttonScale)
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.Center
        ) {
            Icon(
                painter = painterResource(id = R.drawable.googlesymbol),
                contentDescription = "Google Logo",
                tint = Color.Unspecified,
                modifier = Modifier.size(20.dp)
            )
            Spacer(modifier = Modifier.width(12.dp))
            Text(
                text = stringResource(R.string.sign_in_with_google),
                fontWeight = FontWeight.Medium,
                fontSize = 16.sp
            )
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
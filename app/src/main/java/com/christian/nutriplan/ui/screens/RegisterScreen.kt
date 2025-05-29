package com.christian.nutriplan.ui.screens

import UserViewModel
import android.util.Log
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
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
import com.christian.nutriplan.ui.components.SecondaryButton
import com.christian.nutriplan.ui.theme.*
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.android.gms.common.api.ApiException
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import org.koin.compose.koinInject

@Composable
fun RegisterScreen(
    onRegisterSuccess: () -> Unit,
    onLoginClick: () -> Unit,
    viewModel: UserViewModel = koinInject()
) {
    val isLoading by viewModel.isLoading.collectAsState()
    val errorMessage by viewModel.errorMessage.collectAsState()
    val locationData by viewModel.locationData.collectAsState()
    val userRepository: UserRepository = koinInject()
    val context = LocalContext.current
    val coroutineScope = rememberCoroutineScope()

    var nombre by remember { mutableStateOf("") }
    var email by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    var confirmPassword by remember { mutableStateOf("") }
    var edad by remember { mutableStateOf("") }
    var termsAccepted by remember { mutableStateOf(false) }
    var passwordVisible by remember { mutableStateOf(false) }
    var confirmPasswordVisible by remember { mutableStateOf(false) }
    var currentStep by remember { mutableStateOf(0) }

    // Animaciones
    val cardScale by animateFloatAsState(
        targetValue = if (isLoading) 0.98f else 1f,
        animationSpec = spring(dampingRatio = Spring.DampingRatioMediumBouncy),
        label = "card_scale"
    )

    // Animación de entrada
    var isVisible by remember { mutableStateOf(false) }
    LaunchedEffect(Unit) {
        delay(150)
        isVisible = true
    }

    val scrollState = rememberScrollState()

    val googleSignInLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.StartActivityForResult()
    ) { result ->
        coroutineScope.launch {
            try {
                viewModel.setErrorMessage(null)
                val task = GoogleSignIn.getSignedInAccountFromIntent(result.data)
                val account = task.getResult(ApiException::class.java)
                val idToken = account.idToken
                if (idToken != null) {
                    Log.d("RegisterScreen", "Google ID Token: ${idToken.take(10)}...")
                    val signInResult = userRepository.signInWithGoogle(idToken)
                    if (signInResult.isSuccess) {
                        onRegisterSuccess()
                    } else {
                        viewModel.setErrorMessage(signInResult.exceptionOrNull()?.message ?: "Error en Google Sign-In")
                        Log.e("RegisterScreen", "Sign-in failed: ${signInResult.exceptionOrNull()?.message}")
                    }
                } else {
                    viewModel.setErrorMessage(context.getString(R.string.error_no_id_token))
                    Log.e("RegisterScreen", "No ID token received")
                }
            } catch (e: ApiException) {
                viewModel.setErrorMessage("Error de Google Sign-In: ${e.statusCode} - ${e.message}")
                Log.e("RegisterScreen", "ApiException: ${e.statusCode} - ${e.message}", e)
            } catch (e: Exception) {
                viewModel.setErrorMessage("Error inesperado: ${e.message}")
                Log.e("RegisterScreen", "Unexpected error: ${e.message}", e)
            }
        }
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(
                brush = Brush.verticalGradient(
                    colors = listOf(
                        AccentGreenLime.copy(alpha = 0.2f),
                        Cream300,
                        AccentBeigeSoft.copy(alpha = 0.4f),
                        Green100.copy(alpha = 0.3f)
                    )
                )
            )
    ) {
        // Elementos decorativos flotantes
        FloatingRegisterElements()

        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(16.dp)
                .verticalScroll(scrollState),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            AnimatedVisibility(
                visible = isVisible,
                enter = slideInVertically(
                    initialOffsetY = { it },
                    animationSpec = spring(dampingRatio = Spring.DampingRatioMediumBouncy)
                ) + fadeIn(animationSpec = tween(1000)),
            ) {
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .scale(cardScale)
                        .shadow(
                            elevation = 24.dp,
                            shape = RoundedCornerShape(28.dp),
                            ambientColor = Green500.copy(alpha = 0.15f),
                            spotColor = Green700.copy(alpha = 0.15f)
                        ),
                    elevation = CardDefaults.cardElevation(defaultElevation = 16.dp),
                    colors = CardDefaults.cardColors(
                        containerColor = Color.White.copy(alpha = 0.97f)
                    ),
                    shape = RoundedCornerShape(28.dp)
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 32.dp, vertical = 40.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        // Header animado
                        AnimatedRegisterHeader()

                        Spacer(modifier = Modifier.height(32.dp))

                        // Progress indicator
                        LinearProgressIndicator(
                            progress = { (getAllFilledFields(nombre, email, password, confirmPassword, edad).toFloat() / 5f) },
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(6.dp)
                                .clip(RoundedCornerShape(3.dp)),
                            color = Green500,
                            trackColor = Green100,
                        )

                        Spacer(modifier = Modifier.height(24.dp))

                        // Error message con animación
                        AnimatedVisibility(
                            visible = errorMessage != null,
                            enter = slideInVertically() + fadeIn(),
                            exit = slideOutVertically() + fadeOut()
                        ) {
                            Card(
                                colors = CardDefaults.cardColors(
                                    containerColor = Red100.copy(alpha = 0.9f)
                                ),
                                shape = RoundedCornerShape(12.dp),
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                Text(
                                    text = when (errorMessage) {
                                        "error_email_taken" -> stringResource(R.string.error_email_taken)
                                        "error_network" -> stringResource(R.string.error_network)
                                        "error_password_mismatch" -> stringResource(R.string.error_password_mismatch)
                                        "error_terms_not_accepted" -> stringResource(R.string.error_terms_not_accepted)
                                        else -> stringResource(R.string.error_registration_failed, errorMessage ?: "")
                                    },
                                    color = Red500,
                                    style = MaterialTheme.typography.bodyMedium,
                                    modifier = Modifier.padding(16.dp),
                                    textAlign = TextAlign.Center,
                                    fontWeight = FontWeight.Medium
                                )
                            }
                        }

                        Spacer(modifier = Modifier.height(16.dp))

                        // Formulario con animaciones escalonadas
                        Column(
                            verticalArrangement = Arrangement.spacedBy(20.dp)
                        ) {
                            // Nombre
                            AnimatedRegisterField(
                                value = nombre,
                                onValueChange = { nombre = it },
                                label = stringResource(R.string.full_name),
                                leadingIcon = Icons.Filled.Person,
                                delay = 0
                            )

                            // Email
                            AnimatedRegisterField(
                                value = email,
                                onValueChange = { email = it },
                                label = stringResource(R.string.email),
                                leadingIcon = Icons.Filled.Email,
                                keyboardType = KeyboardType.Email,
                                delay = 100
                            )

                            // Password
                            AnimatedRegisterField(
                                value = password,
                                onValueChange = { password = it },
                                label = stringResource(R.string.password),
                                leadingIcon = Icons.Filled.Lock,
                                isPassword = true,
                                passwordVisible = passwordVisible,
                                onPasswordVisibilityToggle = { passwordVisible = !passwordVisible },
                                delay = 200
                            )

                            // Confirm Password
                            AnimatedRegisterField(
                                value = confirmPassword,
                                onValueChange = { confirmPassword = it },
                                label = stringResource(R.string.confirm_password),
                                leadingIcon = Icons.Filled.Lock,
                                isPassword = true,
                                passwordVisible = confirmPasswordVisible,
                                onPasswordVisibilityToggle = { confirmPasswordVisible = !confirmPasswordVisible },
                                delay = 300,
                                isError = password.isNotEmpty() && confirmPassword.isNotEmpty() && password != confirmPassword
                            )

                            // Age
                            AnimatedRegisterField(
                                value = edad,
                                onValueChange = { edad = it },
                                label = stringResource(R.string.age),
                                leadingIcon = Icons.Filled.DateRange,
                                keyboardType = KeyboardType.Number,
                                delay = 400
                            )
                        }

                        Spacer(modifier = Modifier.height(24.dp))

                        // Location info con animación
                        AnimatedLocationInfo(locationData)

                        Spacer(modifier = Modifier.height(24.dp))

                        // Terms checkbox con animación
                        AnimatedTermsCheckbox(
                            termsAccepted = termsAccepted,
                            onTermsAcceptedChange = { termsAccepted = it }
                        )

                        Spacer(modifier = Modifier.height(32.dp))

                        // Register button
                        AnimatedRegisterButton(
                            text = if (isLoading) stringResource(R.string.registering)
                            else stringResource(R.string.register_button),
                            onClick = {
                                if (password != confirmPassword) {
                                    viewModel.setErrorMessage(context.getString(R.string.error_password_mismatch))
                                    return@AnimatedRegisterButton
                                }
                                if (!termsAccepted) {
                                    viewModel.setErrorMessage(context.getString(R.string.error_terms_not_accepted))
                                    return@AnimatedRegisterButton
                                }

                                viewModel.register(
                                    nombre = nombre,
                                    email = email,
                                    contrasena = password,
                                    aceptaTerminos = termsAccepted,
                                    onSuccess = onRegisterSuccess
                                )
                            },
                            enabled = !isLoading && nombre.isNotEmpty() && email.isNotEmpty() &&
                                    password.isNotEmpty() && confirmPassword.isNotEmpty() && edad.isNotEmpty() &&
                                    termsAccepted && locationData != null && password == confirmPassword,
                            isLoading = isLoading
                        )

                        Spacer(modifier = Modifier.height(16.dp))

                        // Google Sign-In button
                        GoogleRegisterButton(
                            onClick = {
                                val signInIntent = userRepository.getGoogleSignInClient().signInIntent
                                googleSignInLauncher.launch(signInIntent)
                            },
                            enabled = !isLoading
                        )

                        Spacer(modifier = Modifier.height(20.dp))

                        // Login button
                        OutlinedButton(
                            onClick = onLoginClick,
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
                                text = "¿Ya tienes cuenta? Inicia sesión",
                                fontWeight = FontWeight.Medium,
                                fontSize = 12.sp
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun FloatingRegisterElements() {
    val infiniteTransition = rememberInfiniteTransition(label = "floating")

    repeat(8) { index ->
        val animatedOffset by infiniteTransition.animateFloat(
            initialValue = -20f,
            targetValue = 40f,
            animationSpec = infiniteRepeatable(
                animation = tween(
                    durationMillis = 4000 + (index * 300),
                    easing = EaseInOutSine
                ),
                repeatMode = RepeatMode.Reverse
            ),
            label = "offset_$index"
        )

        val rotationAnimation by infiniteTransition.animateFloat(
            initialValue = 0f,
            targetValue = 360f,
            animationSpec = infiniteRepeatable(
                animation = tween(
                    durationMillis = 15000 + (index * 2000),
                    easing = LinearEasing
                ),
                repeatMode = RepeatMode.Restart
            ),
            label = "rotation_$index"
        )

        Box(
            modifier = Modifier
                .offset(
                    x = (30 + index * 45).dp,
                    y = (80 + index * 70 + animatedOffset).dp
                )
                .size((15 + index * 4).dp)
                .graphicsLayer { rotationZ = rotationAnimation }
                .clip(
                    when (index % 3) {
                        0 -> CircleShape
                        1 -> RoundedCornerShape(4.dp)
                        else -> RoundedCornerShape(50)
                    }
                )
                .background(
                    when (index % 5) {
                        0 -> AccentGreenLime.copy(alpha = 0.4f)
                        1 -> Yellow200.copy(alpha = 0.3f)
                        2 -> Orange200.copy(alpha = 0.4f)
                        3 -> Blue200.copy(alpha = 0.3f)
                        else -> Lilac200.copy(alpha = 0.4f)
                    }
                )
        )
    }
}

@Composable
private fun AnimatedRegisterHeader() {
    val infiniteTransition = rememberInfiniteTransition(label = "header")
    val pulse by infiniteTransition.animateFloat(
        initialValue = 0.95f,
        targetValue = 1.05f,
        animationSpec = infiniteRepeatable(
            animation = tween(2000, easing = EaseInOutSine),
            repeatMode = RepeatMode.Reverse
        ),
        label = "pulse"
    )

    Column(
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Box(
            modifier = Modifier
                .size(100.dp)
                .scale(pulse)
                .background(
                    brush = Brush.radialGradient(
                        colors = listOf(
                            AccentGreenLime.copy(alpha = 0.8f),
                            Green400,
                            Green600
                        )
                    ),
                    shape = CircleShape
                ),
            contentAlignment = Alignment.Center
        ) {
            Text(
                text = "👋",
                fontSize = 40.sp
            )
        }

        Spacer(modifier = Modifier.height(16.dp))

        Text(
            text = "¡Únete a NutriPlan!",
            style = MaterialTheme.typography.headlineMedium.copy(
                fontWeight = FontWeight.Bold,
                fontSize = 28.sp
            ),
            color = Green700,
            textAlign = TextAlign.Center
        )

        Text(
            text = "Comienza tu viaje hacia una alimentación saludable",
            style = MaterialTheme.typography.bodyLarge,
            color = Black400,
            textAlign = TextAlign.Center,
            modifier = Modifier.padding(top = 8.dp)
        )
    }
}

@Composable
private fun AnimatedRegisterField(
    value: String,
    onValueChange: (String) -> Unit,
    label: String,
    leadingIcon: androidx.compose.ui.graphics.vector.ImageVector,
    keyboardType: KeyboardType = KeyboardType.Text,
    isPassword: Boolean = false,
    passwordVisible: Boolean = false,
    onPasswordVisibilityToggle: (() -> Unit)? = null,
    delay: Long = 0,
    isError: Boolean = false
) {
    var isVisible by remember { mutableStateOf(false) }

    LaunchedEffect(Unit) {
        delay(delay)
        isVisible = true
    }

    val focusedScale by animateFloatAsState(
        targetValue = if (value.isNotEmpty()) 1.02f else 1f,
        animationSpec = spring(dampingRatio = Spring.DampingRatioMediumBouncy),
        label = "field_scale"
    )

    AnimatedVisibility(
        visible = isVisible,
        enter = slideInHorizontally(
            initialOffsetX = { it / 2 },
            animationSpec = spring(dampingRatio = Spring.DampingRatioMediumBouncy)
        ) + fadeIn()
    ) {
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
}

@Composable
private fun AnimatedLocationInfo(locationData: Pair<String, String>?) {
    AnimatedVisibility(
        visible = locationData != null,
        enter = slideInVertically() + fadeIn(),
        exit = slideOutVertically() + fadeOut()
    ) {
        Card(
            colors = CardDefaults.cardColors(
                containerColor = Blue100.copy(alpha = 0.6f)
            ),
            shape = RoundedCornerShape(12.dp),
            modifier = Modifier.fillMaxWidth()
        ) {
            Row(
                modifier = Modifier.padding(16.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(
                    Icons.Filled.LocationOn,
                    contentDescription = null,
                    tint = Blue500,
                    modifier = Modifier.size(20.dp)
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = locationData?.let { (city, locality) ->
                        "Ubicación detectada: $city, $locality"
                    } ?: "Obteniendo ubicación...",
                    style = MaterialTheme.typography.bodyMedium,
                    color = Blue500,
                    fontWeight = FontWeight.Medium
                )
            }
        }
    }
}

@Composable
private fun AnimatedTermsCheckbox(
    termsAccepted: Boolean,
    onTermsAcceptedChange: (Boolean) -> Unit
) {
    val checkboxScale by animateFloatAsState(
        targetValue = if (termsAccepted) 1.1f else 1f,
        animationSpec = spring(dampingRatio = Spring.DampingRatioMediumBouncy),
        label = "checkbox_scale"
    )

    Row(
        verticalAlignment = Alignment.CenterVertically,
        modifier = Modifier.fillMaxWidth()
    ) {
        Checkbox(
            checked = termsAccepted,
            onCheckedChange = onTermsAcceptedChange,
            modifier = Modifier.scale(checkboxScale),
            colors = CheckboxDefaults.colors(
                checkedColor = Green500,
                uncheckedColor = Black300
            )
        )
        Text(
            text = stringResource(R.string.terms_and_conditions),
            style = MaterialTheme.typography.bodyMedium,
            modifier = Modifier.padding(start = 12.dp),
            color = Black500
        )
    }
}

@Composable
private fun AnimatedRegisterButton(
    text: String,
    onClick: () -> Unit,
    enabled: Boolean,
    isLoading: Boolean
) {
    val buttonScale by animateFloatAsState(
        targetValue = if (enabled && !isLoading) 1f else 0.95f,
        animationSpec = spring(dampingRatio = Spring.DampingRatioMediumBouncy),
        label = "register_button_scale"
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
            defaultElevation = 8.dp,
            pressedElevation = 12.dp
        ),
        modifier = Modifier
            .fillMaxWidth()
            .height(56.dp)
            .scale(buttonScale)
            .shadow(
                elevation = 10.dp,
                shape = RoundedCornerShape(16.dp),
                ambientColor = Green500.copy(alpha = 0.3f)
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
private fun GoogleRegisterButton(
    onClick: () -> Unit,
    enabled: Boolean
) {
    val buttonScale by animateFloatAsState(
        targetValue = if (enabled) 1f else 0.95f,
        animationSpec = spring(dampingRatio = Spring.DampingRatioMediumBouncy),
        label = "google_register_button_scale"
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
                text = "Registrarse con Google",
                fontWeight = FontWeight.Medium,
                fontSize = 16.sp
            )
        }
    }
}

private fun getAllFilledFields(vararg fields: String): Int {
    return fields.count { it.isNotEmpty() }
}

@Preview(showBackground = true)
@Composable
fun RegisterScreenPreview() {
    NutriPlanTheme {
        RegisterScreen(
            onRegisterSuccess = {},
            onLoginClick = {}
        )
    }
}
import android.util.Log
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AccountCircle
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.christian.nutriplan.R
import com.christian.nutriplan.network.UserRepository
import com.christian.nutriplan.ui.components.PrimaryButton
import com.christian.nutriplan.ui.components.SecondaryButton
import com.christian.nutriplan.ui.theme.Cream400
import com.christian.nutriplan.ui.theme.NutriPlanTheme
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.android.gms.common.api.ApiException
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
    var altura by remember { mutableStateOf("") }
    var peso by remember { mutableStateOf("") }
    var edad by remember { mutableStateOf("") }
    var termsAccepted by remember { mutableStateOf(false) }

    val googleSignInLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.StartActivityForResult()
    ) { result ->
        coroutineScope.launch {
            try {
                viewModel.setErrorMessage(null) // Clear previous errors
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

    val scrollState = rememberScrollState()

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Cream400)
            .padding(16.dp)
            .verticalScroll(scrollState),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .padding(8.dp),
            elevation = CardDefaults.cardElevation(4.dp),
            colors = CardDefaults.cardColors(
                containerColor = MaterialTheme.colorScheme.surface
            )
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(24.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text(
                    text = stringResource(R.string.register_title),
                    style = MaterialTheme.typography.headlineSmall,
                    color = MaterialTheme.colorScheme.primary,
                    textAlign = TextAlign.Center,
                    modifier = Modifier.padding(bottom = 24.dp)
                )

                OutlinedTextField(
                    value = nombre,
                    onValueChange = { nombre = it },
                    label = { Text(stringResource(R.string.full_name)) },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true
                )

                Spacer(modifier = Modifier.height(16.dp))

                OutlinedTextField(
                    value = email,
                    onValueChange = { email = it },
                    label = { Text(stringResource(R.string.email)) },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true,
                    keyboardOptions = KeyboardOptions.Default.copy(
                        keyboardType = KeyboardType.Email
                    )
                )

                Spacer(modifier = Modifier.height(16.dp))

                OutlinedTextField(
                    value = password,
                    onValueChange = { password = it },
                    label = { Text(stringResource(R.string.password)) },
                    visualTransformation = PasswordVisualTransformation(),
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true
                )

                Spacer(modifier = Modifier.height(16.dp))

                OutlinedTextField(
                    value = confirmPassword,
                    onValueChange = { confirmPassword = it },
                    label = { Text(stringResource(R.string.confirm_password)) },
                    visualTransformation = PasswordVisualTransformation(),
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true
                )

                Spacer(modifier = Modifier.height(16.dp))

                OutlinedTextField(
                    value = edad,
                    onValueChange = { edad = it },
                    label = { Text(stringResource(R.string.age)) },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true,
                    keyboardOptions = KeyboardOptions.Default.copy(
                        keyboardType = KeyboardType.Number
                    )
                )

                Spacer(modifier = Modifier.height(16.dp))

                locationData?.let { (city, locality) ->
                    Text(
                        text = "Ubicación detectada: $city, $locality",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                } ?: Text(
                    text = "Obteniendo ubicación...",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurface
                )

                Spacer(modifier = Modifier.height(16.dp))

                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Checkbox(
                        checked = termsAccepted,
                        onCheckedChange = { termsAccepted = it }
                    )
                    Text(
                        text = stringResource(R.string.terms_and_conditions),
                        style = MaterialTheme.typography.bodySmall,
                        modifier = Modifier.padding(start = 8.dp)
                    )
                }

                Spacer(modifier = Modifier.height(16.dp))

                errorMessage?.let { message ->
                    Text(
                        text = when (message) {
                            "error_email_taken" -> stringResource(R.string.error_email_taken)
                            "error_network" -> stringResource(R.string.error_network)
                            "error_password_mismatch" -> stringResource(R.string.error_password_mismatch)
                            "error_terms_not_accepted" -> stringResource(R.string.error_terms_not_accepted)
                            else -> stringResource(R.string.error_registration_failed, message)
                        },
                        color = MaterialTheme.colorScheme.error,
                        style = MaterialTheme.typography.bodySmall
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                }

                PrimaryButton(
                    text = if (isLoading) stringResource(R.string.registering)
                    else stringResource(R.string.register_button),
                    onClick = {
                        if (password != confirmPassword) {
                            viewModel.setErrorMessage(context.getString(R.string.error_password_mismatch))
                            return@PrimaryButton
                        }
                        if (!termsAccepted) {
                            viewModel.setErrorMessage(context.getString(R.string.error_terms_not_accepted))
                            return@PrimaryButton
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
                            termsAccepted && locationData != null
                )

                Spacer(modifier = Modifier.height(16.dp))

                Button(
                    onClick = {
                        val signInIntent = userRepository.getGoogleSignInClient().signInIntent
                        googleSignInLauncher.launch(signInIntent)
                    },
                    enabled = !isLoading,
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(48.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = MaterialTheme.colorScheme.surface,
                        contentColor = MaterialTheme.colorScheme.onSurface
                    ),
                    shape = MaterialTheme.shapes.small
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
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = stringResource(R.string.sign_in_with_google),
                            style = MaterialTheme.typography.bodyMedium
                        )
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                SecondaryButton(
                    text = stringResource(R.string.login_button),
                    onClick = onLoginClick
                )
            }
        }
    }
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
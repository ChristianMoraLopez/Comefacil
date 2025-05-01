package com.christian.nutriplan.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Email
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.christian.nutriplan.ui.components.PrimaryButton
import com.christian.nutriplan.ui.components.SecondaryButton
import com.christian.nutriplan.ui.theme.*

@Composable
fun LoginScreen(
    onLoginSuccess: () -> Unit,
    onRegisterClick: () -> Unit
) {
    var email by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Cream300) // Un tono crema más suave como fondo base
            .padding(horizontal = 24.dp), // Reducido el padding general
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            elevation = CardDefaults.cardElevation(8.dp), // Elevación sutil para destacar la tarjeta
            colors = CardDefaults.cardColors(
                containerColor = Cream100 // Fondo blanco o crema muy claro para la tarjeta
            ),
            shape = MaterialTheme.shapes.medium // Bordes ligeramente redondeados
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 24.dp, vertical = 32.dp), // Más padding vertical
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(16.dp) // Espacio uniforme entre elementos
            ) {
                Text(
                    text = "¡Bienvenido de nuevo!", // Un título más conciso y amigable
                    style = MaterialTheme.typography.headlineSmall.copy(fontWeight = FontWeight.Bold), // Título más destacado
                    color = Green700, // Un verde más intenso para el título
                    textAlign = TextAlign.Center
                )
                Text(
                    text = "Accede a tu plan nutricional personalizado.", // Subtítulo informativo
                    style = MaterialTheme.typography.bodyMedium,
                    color = Black500,
                    textAlign = TextAlign.Center,
                    modifier = Modifier.padding(bottom = 8.dp) // Un poco de espacio debajo
                )

                OutlinedTextField(
                    value = email,
                    onValueChange = { email = it },
                    label = { Text("Correo electrónico") },
                    leadingIcon = { Icon(Icons.Filled.Email, contentDescription = "Correo electrónico", tint = Black300) }, // Icono para el campo
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true,
                    shape = MaterialTheme.shapes.small // Bordes redondeados para el campo
                )

                OutlinedTextField(
                    value = password,
                    onValueChange = { password = it },
                    label = { Text("Contraseña") },
                    leadingIcon = { Icon(Icons.Filled.Lock, contentDescription = "Contraseña", tint = Black300) }, // Icono para el campo
                    visualTransformation = PasswordVisualTransformation(),
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true,
                    shape = MaterialTheme.shapes.small // Bordes redondeados para el campo
                )

                PrimaryButton(
                    text = "Iniciar sesión",
                    onClick = onLoginSuccess,
                    modifier = Modifier.fillMaxWidth() // Botón más ancho
                )

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    SecondaryButton(
                        text = "¿Nuevo por aquí?",
                        onClick = onRegisterClick,
                        textStyle = MaterialTheme.typography.bodySmall,
                        modifier = Modifier.weight(1f) // Botón más pequeño y con peso
                    )
                    TextButton(
                        onClick = { /* TODO: Implementar lógica de olvido de contraseña */ },
                        modifier = Modifier.padding(start = 8.dp)
                    ) {
                        Text(
                            text = "Olvidé mi contraseña",
                            style = MaterialTheme.typography.bodySmall,
                            color = Green500 // Un verde más claro para este texto
                        )
                    }
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
package com.christian.nutriplan.ui.screens

import android.content.Context
import android.content.Intent
import android.net.Uri
import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.rounded.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import com.christian.nutriplan.R
import com.christian.nutriplan.ui.navigation.NavRoutes
import com.christian.nutriplan.ui.theme.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AboutMeScreen(navController: NavController? = null) {
    val contexto = LocalContext.current
    val estadoScroll = rememberScrollState()
    var mostrarContenido by remember { mutableStateOf(false) }

    // Animación de pulsación
    val infiniteTransition = rememberInfiniteTransition()
    val pulseScale = infiniteTransition.animateFloat(
        initialValue = 1f,
        targetValue = 1.05f,
        animationSpec = infiniteRepeatable(
            animation = tween(1000),
            repeatMode = RepeatMode.Reverse
        )
    )

    // Disparar animación al cargar la pantalla
    LaunchedEffect(Unit) {
        mostrarContenido = true
    }

    Scaffold(
        containerColor = Cream400,
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        text = "Sobre Mí",
                        style = MaterialTheme.typography.titleLarge.copy(
                            fontWeight = FontWeight.Bold
                        ),
                        color = Green800
                    )
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = Cream300
                ),
                navigationIcon = {
                    IconButton(onClick = {
                        navController?.navigate(NavRoutes.DASHBOARD) {
                            popUpTo(navController.graph.startDestinationId) {
                                inclusive = false
                            }
                            launchSingleTop = true
                        } ?: run {
                            // Fallback: If navController is null, show a toast or handle gracefully
                            android.widget.Toast.makeText(
                                contexto,
                                "No se puede navegar al Dashboard",
                                android.widget.Toast.LENGTH_SHORT
                            ).show()
                        }
                    }) {
                        Icon(
                            imageVector = Icons.Default.ArrowBack,
                            contentDescription = "Volver al Dashboard",
                            tint = Green800
                        )
                    }
                }
            )
        },
        floatingActionButton = {
            FloatingActionButton(
                onClick = {
                    navController?.navigate(NavRoutes.DASHBOARD) {
                        popUpTo(navController.graph.startDestinationId) {
                            inclusive = false
                        }
                        launchSingleTop = true
                    } ?: run {
                        // Fallback: If navController is null, show a toast or handle gracefully
                        android.widget.Toast.makeText(
                            contexto,
                            "No se puede navegar al Dashboard",
                            android.widget.Toast.LENGTH_SHORT
                        ).show()
                    }
                },
                containerColor = Green600,
                contentColor = Cream100,
                modifier = Modifier
                    .shadow(
                        elevation = 8.dp,
                        shape = CircleShape
                    )
                    .animateContentSize()
            ) {
                Icon(
                    imageVector = Icons.Default.Dashboard,
                    contentDescription = "Ir al Dashboard"
                )
            }
        }
    ) { relleno ->
        AnimatedVisibility(
            visible = mostrarContenido,
            enter = fadeIn(
                animationSpec = spring(
                    dampingRatio = Spring.DampingRatioMediumBouncy,
                    stiffness = Spring.StiffnessLow
                )
            ) + scaleIn(
                initialScale = 0.8f,
                animationSpec = spring(
                    dampingRatio = Spring.DampingRatioLowBouncy,
                    stiffness = Spring.StiffnessMedium
                )
            )
        ) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(relleno)
                    .background(
                        brush = Brush.verticalGradient(
                            colors = listOf(
                                Cream400,
                                Cream300,
                                Cream200
                            )
                        )
                    )
                    .verticalScroll(estadoScroll)
                    .padding(16.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                TarjetaPerfil(pulseScale)
                Spacer(modifier = Modifier.height(24.dp))
                TarjetaRedesSociales(contexto)
                Spacer(modifier = Modifier.height(24.dp))
                TarjetaPatrocinador(pulseScale)
            }
        }
    }
}

@Composable
private fun TarjetaPerfil(pulseScale: State<Float>) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 8.dp)
            .shadow(
                elevation = 12.dp,
                shape = RoundedCornerShape(20.dp)
            ),
        colors = CardDefaults.cardColors(
            containerColor = Green200
        ),
        shape = RoundedCornerShape(20.dp)
    ) {
        Column(
            modifier = Modifier
                .padding(24.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            // Logo con bordes brillantes
            Box(
                modifier = Modifier
                    .size(130.dp)
                    .graphicsLayer {
                        scaleX = pulseScale.value
                        scaleY = pulseScale.value
                    }
                    .border(
                        width = 3.dp,
                        brush = Brush.sweepGradient(
                            listOf(Green700, Green500, Green300, Green500, Green700)
                        ),
                        shape = CircleShape
                    )
                    .padding(4.dp)
                    .clip(CircleShape)
                    .background(
                        brush = Brush.radialGradient(
                            colors = listOf(Green400, Green500)
                        )
                    ),
                contentAlignment = Alignment.Center
            ) {
                Image(
                    painter = painterResource(id = R.drawable.iconocmora),
                    contentDescription = "Logo de Christian Mora",
                    contentScale = ContentScale.Crop,
                    modifier = Modifier
                        .size(110.dp)
                        .clip(CircleShape)
                )
            }

            Spacer(modifier = Modifier.height(20.dp))

            // Nombre con estilo mejorado
            Text(
                text = "Christian Rey Mora López",
                style = MaterialTheme.typography.headlineMedium.copy(
                    fontWeight = FontWeight.ExtraBold,
                    letterSpacing = 0.5.sp
                ),
                color = Green900,
                textAlign = TextAlign.Center
            )

            Spacer(modifier = Modifier.height(12.dp))

            // Títulos con chips estilizados
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 4.dp),
                horizontalArrangement = Arrangement.Center,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(
                    imageVector = Icons.Rounded.Code,
                    contentDescription = null,
                    tint = Green700,
                    modifier = Modifier.size(24.dp)
                )

                Spacer(modifier = Modifier.width(8.dp))

                Text(
                    text = "Desarrollador de Software",
                    style = MaterialTheme.typography.titleLarge,
                    color = Green800,
                    fontWeight = FontWeight.Bold
                )
            }

            Spacer(modifier = Modifier.height(4.dp))

            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 4.dp),
                horizontalArrangement = Arrangement.Center,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(
                    imageVector = Icons.Rounded.LocalDining,
                    contentDescription = null,
                    tint = Green700,
                    modifier = Modifier.size(24.dp)
                )

                Spacer(modifier = Modifier.width(8.dp))

                Text(
                    text = "Nutriólogo",
                    style = MaterialTheme.typography.titleLarge,
                    color = Green800,
                    fontWeight = FontWeight.Bold
                )
            }

            Spacer(modifier = Modifier.height(16.dp))

            Divider(
                modifier = Modifier
                    .fillMaxWidth(0.8f)
                    .padding(vertical = 8.dp),
                color = Green400
            )

            Text(
                text = "Apasionado por crear aplicaciones innovadoras y promover estilos de vida saludables.",
                style = MaterialTheme.typography.bodyLarge,
                color = Black700,
                textAlign = TextAlign.Center,
                modifier = Modifier.padding(horizontal = 16.dp)
            )
        }
    }
}

@Composable
private fun TarjetaRedesSociales(contexto: Context) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 8.dp)
            .shadow(
                elevation = 8.dp,
                shape = RoundedCornerShape(20.dp)
            ),
        colors = CardDefaults.cardColors(containerColor = Cream100),
        shape = RoundedCornerShape(20.dp)
    ) {
        Column(
            modifier = Modifier.padding(24.dp)
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.padding(bottom = 16.dp)
            ) {
                Icon(
                    imageVector = Icons.Rounded.Share,
                    contentDescription = null,
                    tint = Green700,
                    modifier = Modifier.size(28.dp)
                )

                Spacer(modifier = Modifier.width(12.dp))

                Text(
                    text = "Conéctate Conmigo",
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold,
                    color = Green700
                )
            }

            Divider(
                modifier = Modifier.padding(vertical = 8.dp),
                color = Green200
            )

            // GitHub con estilo de botón
            EnlaceSocialEstilizado(
                plataforma = "GitHub",
                url = "https://github.com/ChristianMoraLopez",
                contexto = contexto,
                icono = Icons.Default.Code,
                colorFondo = Color(0xFF24292E),
                colorTexto = Color.White
            )

            Spacer(modifier = Modifier.height(12.dp))

            // LinkedIn con estilo de botón
            EnlaceSocialEstilizado(
                plataforma = "LinkedIn",
                url = "https://www.linkedin.com/in/christian-moral/",
                contexto = contexto,
                icono = Icons.Default.Person,
                colorFondo = Color(0xFF0A66C2),
                colorTexto = Color.White
            )
        }
    }
}

@Composable
private fun EnlaceSocialEstilizado(
    plataforma: String,
    url: String,
    contexto: Context,
    icono: ImageVector,
    colorFondo: Color,
    colorTexto: Color
) {
    Surface(
        color = colorFondo,
        shape = RoundedCornerShape(12.dp),
        shadowElevation = 4.dp,
        modifier = Modifier
            .fillMaxWidth()
            .clickable {
                val intent = Intent(Intent.ACTION_VIEW, Uri.parse(url))
                contexto.startActivity(intent)
            }
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier
                .padding(horizontal = 16.dp, vertical = 12.dp)
        ) {
            Icon(
                imageVector = icono,
                contentDescription = "Icono de $plataforma",
                tint = colorTexto,
                modifier = Modifier.size(24.dp)
            )

            Spacer(modifier = Modifier.width(16.dp))

            Text(
                text = plataforma,
                style = MaterialTheme.typography.titleMedium,
                color = colorTexto,
                fontWeight = FontWeight.Medium
            )

            Spacer(modifier = Modifier.weight(1f))

            Icon(
                imageVector = Icons.Default.OpenInNew,
                contentDescription = "Abrir enlace",
                tint = colorTexto,
                modifier = Modifier.size(20.dp)
            )
        }
    }
}

@Composable
private fun TarjetaPatrocinador(pulseScale: State<Float>) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 8.dp)
            .graphicsLayer {
                scaleX = pulseScale.value * 0.98f
                scaleY = pulseScale.value * 0.98f
            }
            .shadow(
                elevation = 10.dp,
                shape = RoundedCornerShape(20.dp)
            ),
        colors = CardDefaults.cardColors(containerColor = Yellow200),
        shape = RoundedCornerShape(20.dp)
    ) {
        Column(
            modifier = Modifier.padding(24.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Icon(
                imageVector = Icons.Filled.Favorite,
                contentDescription = null,
                tint = Yellow500,
                modifier = Modifier.size(36.dp)
            )

            Spacer(modifier = Modifier.height(8.dp))

            Text(
                text = "Patrocinado por Mi Súper Papá",
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Bold,
                color = Yellow500,
                textAlign = TextAlign.Center
            )

            Spacer(modifier = Modifier.height(12.dp))

            Text(
                text = "¡Con amor y apoyo, esta aplicación está dedicada a mi increíble papá que cree en mis sueños!",
                style = MaterialTheme.typography.bodyLarge,
                color = Black700,
                textAlign = TextAlign.Center
            )

            Spacer(modifier = Modifier.height(8.dp))

            Icon(
                imageVector = Icons.Filled.Star,
                contentDescription = null,
                tint = Yellow500,
                modifier = Modifier.size(28.dp)
            )
        }
    }
}
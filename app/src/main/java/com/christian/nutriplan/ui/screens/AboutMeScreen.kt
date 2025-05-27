package com.christian.nutriplan.ui.screens

import android.annotation.SuppressLint
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
import kotlinx.coroutines.delay

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AboutMeScreen(navController: NavController? = null) {
    val contexto = LocalContext.current
    val estadoScroll = rememberScrollState()
    var mostrarContenido by remember { mutableStateOf(false) }

    // Animación de rotación suave para elementos decorativos
    val infiniteTransition = rememberInfiniteTransition()
    val rotacionSuave = infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 360f,
        animationSpec = infiniteRepeatable(
            animation = tween(20000, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        )
    )

    // Animación de brillo para bordes
    val brilloBorde = infiniteTransition.animateFloat(
        initialValue = 0.3f,
        targetValue = 1f,
        animationSpec = infiniteRepeatable(
            animation = tween(3000, easing = FastOutSlowInEasing),
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
            // FAB con animación de hover mejorada
            var isHovered by remember { mutableStateOf(false) }

            FloatingActionButton(
                onClick = {
                    navController?.navigate(NavRoutes.DASHBOARD) {
                        popUpTo(navController.graph.startDestinationId) {
                            inclusive = false
                        }
                        launchSingleTop = true
                    } ?: run {
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
                        elevation = if (isHovered) 12.dp else 8.dp,
                        shape = CircleShape
                    )
                    .animateContentSize(
                        animationSpec = spring(
                            dampingRatio = Spring.DampingRatioMediumBouncy,
                            stiffness = Spring.StiffnessMedium
                        )
                    )
            ) {
                Icon(
                    imageVector = Icons.Default.Dashboard,
                    contentDescription = "Ir al Dashboard",
                    modifier = Modifier.graphicsLayer {
                        rotationZ = rotacionSuave.value * 0.1f // Rotación muy sutil
                    }
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
            ) + slideInVertically(
                initialOffsetY = { it / 3 },
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
                // Tarjetas con animaciones de entrada escalonadas
                TarjetaDonacionesAnimada(contexto, brilloBorde, 0)
                Spacer(modifier = Modifier.height(24.dp))

                TarjetaPerfilAnimada(rotacionSuave, brilloBorde, 300)
                Spacer(modifier = Modifier.height(24.dp))

                TarjetaRedesSocialesAnimada(contexto, 600)
                Spacer(modifier = Modifier.height(24.dp))

                TarjetaPatrocinadorAnimada(brilloBorde, 900)
            }
        }
    }
}

@Composable
private fun TarjetaDonacionesAnimada(
    contexto: Context,
    brilloBorde: State<Float>,
    delay: Int
) {
    var visible by remember { mutableStateOf(false) }

    LaunchedEffect(Unit) {
        delay(delay.toLong())
        visible = true
    }

    AnimatedVisibility(
        visible = visible,
        enter = slideInHorizontally(
            initialOffsetX = { -it },
            animationSpec = spring(
                dampingRatio = Spring.DampingRatioMediumBouncy,
                stiffness = Spring.StiffnessMedium
            )
        ) + fadeIn()
    ) {
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 8.dp)
                .shadow(
                    elevation = 12.dp,
                    shape = RoundedCornerShape(20.dp)
                )
                .border(
                    width = 2.dp,
                    brush = Brush.sweepGradient(
                        listOf(
                            Color(0xFF4CAF50).copy(alpha = brilloBorde.value),
                            Color(0xFF81C784).copy(alpha = brilloBorde.value * 0.7f),
                            Color(0xFF4CAF50).copy(alpha = brilloBorde.value)
                        )
                    ),
                    shape = RoundedCornerShape(20.dp)
                ),
            colors = CardDefaults.cardColors(
                containerColor = Color(0xFFE8F5E8)
            ),
            shape = RoundedCornerShape(20.dp)
        ) {
            Column(
                modifier = Modifier.padding(24.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                // Corazón con animación de latido suave
                val latidoSuave = remember {
                    Animatable(1f)
                }

                LaunchedEffect(Unit) {
                    while (true) {
                        latidoSuave.animateTo(
                            1.1f,
                            animationSpec = tween(800, easing = EaseInOutCubic)
                        )
                        latidoSuave.animateTo(
                            1f,
                            animationSpec = tween(800, easing = EaseInOutCubic)
                        )
                        delay(2000) // Pausa entre latidos
                    }
                }

                Icon(
                    imageVector = Icons.Filled.FavoriteBorder,
                    contentDescription = null,
                    tint = Color(0xFF4CAF50),
                    modifier = Modifier
                        .size(32.dp)
                        .graphicsLayer {
                            scaleX = latidoSuave.value
                            scaleY = latidoSuave.value
                        }
                )

                Spacer(modifier = Modifier.height(12.dp))

                Text(
                    text = "¿Te gusta este proyecto?",
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold,
                    color = Green700,
                    textAlign = TextAlign.Center
                )

                Spacer(modifier = Modifier.height(8.dp))

                Text(
                    text = "Si quieres apoyar este proyecto para seguir expandiéndolo y creando más recetas saludables, ¡cualquier apoyo es muy apreciado!",
                    style = MaterialTheme.typography.bodyMedium,
                    color = Black700,
                    textAlign = TextAlign.Center,
                    modifier = Modifier.padding(horizontal = 8.dp)
                )

                Spacer(modifier = Modifier.height(20.dp))

                // Botones con hover effect
                BotonDonacionAnimado(
                    texto = "Donar por Nequi",
                    subtexto = "314 471 5980",
                    color = Color(0xFF662D91),
                    icono = Icons.Default.AccountBalance,
                    iconoSecundario = Icons.Default.ContentCopy
                ) {
                    android.widget.Toast.makeText(
                        contexto,
                        "Nequi: 314 471 5980 - Christian Mora",
                        android.widget.Toast.LENGTH_LONG
                    ).show()
                }

                Spacer(modifier = Modifier.height(12.dp))

                BotonDonacionAnimado(
                    texto = "Buy Me A Coffee",
                    subtexto = null,
                    color = Color(0xFFFFDD44),
                    icono = Icons.Default.LocalCafe,
                    iconoSecundario = Icons.Default.OpenInNew,
                    textColor = Color.Black
                ) {
                    val intent = Intent(Intent.ACTION_VIEW, Uri.parse("https://buymeacoffee.com/christianMora"))
                    contexto.startActivity(intent)
                }

                Spacer(modifier = Modifier.height(12.dp))

                Text(
                    text = "¡Gracias por tu apoyo! 💚",
                    style = MaterialTheme.typography.bodySmall,
                    color = Green600,
                    textAlign = TextAlign.Center,
                    fontWeight = FontWeight.Medium
                )
            }
        }
    }
}

@Composable
private fun BotonDonacionAnimado(
    texto: String,
    subtexto: String?,
    color: Color,
    icono: ImageVector,
    iconoSecundario: ImageVector,
    textColor: Color = Color.White,
    onClick: () -> Unit
) {
    var isPressed by remember { mutableStateOf(false) }

    val elevacion by animateDpAsState(
        targetValue = if (isPressed) 2.dp else 6.dp,
        animationSpec = spring(dampingRatio = Spring.DampingRatioMediumBouncy)
    )

    Surface(
        color = color,
        shape = RoundedCornerShape(12.dp),
        shadowElevation = elevacion,
        modifier = Modifier
            .fillMaxWidth()
            .clickable {
                isPressed = true
                onClick()
            }
            .graphicsLayer {
                scaleX = if (isPressed) 0.98f else 1f
                scaleY = if (isPressed) 0.98f else 1f
            }
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.Center,
            modifier = Modifier.padding(horizontal = 16.dp, vertical = 14.dp)
        ) {
            Icon(
                imageVector = icono,
                contentDescription = texto,
                tint = textColor,
                modifier = Modifier.size(24.dp)
            )

            Spacer(modifier = Modifier.width(12.dp))

            Column {
                Text(
                    text = texto,
                    style = MaterialTheme.typography.titleMedium,
                    color = textColor,
                    fontWeight = FontWeight.Bold
                )
                subtexto?.let {
                    Text(
                        text = it,
                        style = MaterialTheme.typography.bodySmall,
                        color = textColor.copy(alpha = 0.9f)
                    )
                }
            }

            Spacer(modifier = Modifier.weight(1f))

            Icon(
                imageVector = iconoSecundario,
                contentDescription = "Acción",
                tint = textColor,
                modifier = Modifier.size(20.dp)
            )
        }
    }

    LaunchedEffect(isPressed) {
        if (isPressed) {
            delay(150)
            isPressed = false
        }
    }
}

@OptIn(ExperimentalAnimationApi::class)
@SuppressLint("UnusedContentLambdaTargetStateParameter")
@Composable
private fun TarjetaPerfilAnimada(
    rotacionSuave: State<Float>,
    brilloBorde: State<Float>,
    delay: Int
) {
    var visible by remember { mutableStateOf(false) }

    LaunchedEffect(Unit) {
        delay(delay.toLong())
        visible = true
    }

    AnimatedVisibility(
        visible = visible,
        enter = scaleIn(
            initialScale = 0.8f,
            animationSpec = spring(
                dampingRatio = Spring.DampingRatioLowBouncy,
                stiffness = Spring.StiffnessMedium
            )
        ) + fadeIn()
    ) {
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
                modifier = Modifier.padding(24.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                // Logo con efecto de brillo en el borde
                Box(
                    modifier = Modifier
                        .size(130.dp)
                        .border(
                            width = 3.dp,
                            brush = Brush.sweepGradient(
                                listOf(
                                    Green700.copy(alpha = brilloBorde.value),
                                    Green500.copy(alpha = brilloBorde.value * 0.8f),
                                    Green300.copy(alpha = brilloBorde.value * 0.6f),
                                    Green500.copy(alpha = brilloBorde.value * 0.8f),
                                    Green700.copy(alpha = brilloBorde.value)
                                )
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
                            .graphicsLayer {
                                rotationZ = rotacionSuave.value * 0.05f // Rotación muy sutil
                            }
                    )
                }

                Spacer(modifier = Modifier.height(20.dp))

                // Nombre con animación de entrada
                AnimatedContent(
                    targetState = visible,
                    transitionSpec = {
                        slideInVertically(
                            initialOffsetY = { it },
                            animationSpec = spring(
                                dampingRatio = Spring.DampingRatioMediumBouncy
                            )
                        ) with slideOutVertically()
                    }
                ) {
                    Text(
                        text = "Christian Rey Mora López",
                        style = MaterialTheme.typography.headlineMedium.copy(
                            fontWeight = FontWeight.ExtraBold,
                            letterSpacing = 0.5.sp
                        ),
                        color = Green900,
                        textAlign = TextAlign.Center
                    )
                }

                Spacer(modifier = Modifier.height(12.dp))

                // Títulos con animación escalonada
                TituloAnimado(
                    icono = Icons.Rounded.Code,
                    texto = "Desarrollador de Software",
                    delay = 100
                )

                Spacer(modifier = Modifier.height(4.dp))

                TituloAnimado(
                    icono = Icons.Rounded.LocalDining,
                    texto = "Nutriólogo",
                    delay = 200
                )

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
}

@Composable
private fun TituloAnimado(
    icono: ImageVector,
    texto: String,
    delay: Int
) {
    var visible by remember { mutableStateOf(false) }

    LaunchedEffect(Unit) {
        delay(delay.toLong())
        visible = true
    }

    AnimatedVisibility(
        visible = visible,
        enter = slideInHorizontally(
            initialOffsetX = { -it },
            animationSpec = spring(
                dampingRatio = Spring.DampingRatioMediumBouncy
            )
        ) + fadeIn()
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 4.dp),
            horizontalArrangement = Arrangement.Center,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                imageVector = icono,
                contentDescription = null,
                tint = Green700,
                modifier = Modifier.size(24.dp)
            )

            Spacer(modifier = Modifier.width(8.dp))

            Text(
                text = texto,
                style = MaterialTheme.typography.titleLarge,
                color = Green800,
                fontWeight = FontWeight.Bold
            )
        }
    }
}

@Composable
private fun TarjetaRedesSocialesAnimada(contexto: Context, delay: Int) {
    var visible by remember { mutableStateOf(false) }

    LaunchedEffect(Unit) {
        delay(delay.toLong())
        visible = true
    }

    AnimatedVisibility(
        visible = visible,
        enter = slideInHorizontally(
            initialOffsetX = { it },
            animationSpec = spring(
                dampingRatio = Spring.DampingRatioMediumBouncy,
                stiffness = Spring.StiffnessMedium
            )
        ) + fadeIn()
    ) {
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

                EnlaceSocialEstilizadoAnimado(
                    plataforma = "GitHub",
                    url = "https://github.com/ChristianMoraLopez",
                    contexto = contexto,
                    icono = Icons.Default.Code,
                    colorFondo = Color(0xFF24292E),
                    colorTexto = Color.White,
                    delay = 0
                )

                Spacer(modifier = Modifier.height(12.dp))

                EnlaceSocialEstilizadoAnimado(
                    plataforma = "LinkedIn",
                    url = "https://www.linkedin.com/in/christian-moral/",
                    contexto = contexto,
                    icono = Icons.Default.Person,
                    colorFondo = Color(0xFF0A66C2),
                    colorTexto = Color.White,
                    delay = 200
                )
            }
        }
    }
}

@Composable
private fun EnlaceSocialEstilizadoAnimado(
    plataforma: String,
    url: String,
    contexto: Context,
    icono: ImageVector,
    colorFondo: Color,
    colorTexto: Color,
    delay: Int
) {
    var visible by remember { mutableStateOf(false) }
    var isPressed by remember { mutableStateOf(false) }

    LaunchedEffect(Unit) {
        delay(delay.toLong())
        visible = true
    }

    val elevacion by animateDpAsState(
        targetValue = if (isPressed) 2.dp else 4.dp,
        animationSpec = spring(dampingRatio = Spring.DampingRatioMediumBouncy)
    )

    AnimatedVisibility(
        visible = visible,
        enter = slideInVertically(
            initialOffsetY = { it },
            animationSpec = spring(
                dampingRatio = Spring.DampingRatioMediumBouncy
            )
        ) + fadeIn()
    ) {
        Surface(
            color = colorFondo,
            shape = RoundedCornerShape(12.dp),
            shadowElevation = elevacion,
            modifier = Modifier
                .fillMaxWidth()
                .clickable {
                    isPressed = true
                    val intent = Intent(Intent.ACTION_VIEW, Uri.parse(url))
                    contexto.startActivity(intent)
                }
                .graphicsLayer {
                    scaleX = if (isPressed) 0.98f else 1f
                    scaleY = if (isPressed) 0.98f else 1f
                }
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.padding(horizontal = 16.dp, vertical = 12.dp)
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

    LaunchedEffect(isPressed) {
        if (isPressed) {
            delay(150)
            isPressed = false
        }
    }
}

@Composable
private fun TarjetaPatrocinadorAnimada(brilloBorde: State<Float>, delay: Int) {
    var visible by remember { mutableStateOf(false) }

    LaunchedEffect(Unit) {
        delay(delay.toLong())
        visible = true
    }

    AnimatedVisibility(
        visible = visible,
        enter = slideInVertically(
            initialOffsetY = { it },
            animationSpec = spring(
                dampingRatio = Spring.DampingRatioLowBouncy,
                stiffness = Spring.StiffnessMedium
            )
        ) + fadeIn()
    ) {
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 8.dp)
                .shadow(
                    elevation = 10.dp,
                    shape = RoundedCornerShape(20.dp)
                )
                .border(
                    width = 2.dp,
                    brush = Brush.sweepGradient(
                        listOf(
                            Yellow500.copy(alpha = brilloBorde.value),
                            Yellow200.copy(alpha = brilloBorde.value * 0.7f),
                            Yellow500.copy(alpha = brilloBorde.value)
                        )
                    ),
                    shape = RoundedCornerShape(20.dp)
                ),
            colors = CardDefaults.cardColors(containerColor = Yellow200),
            shape = RoundedCornerShape(20.dp)
        ) {
            Column(
                modifier = Modifier.padding(24.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                // Corazón con animación de brillo
                val brilloCorazon = remember {
                    Animatable(0.8f)
                }

                LaunchedEffect(Unit) {
                    while (true) {
                        brilloCorazon.animateTo(
                            1.2f,
                            animationSpec = tween(1500, easing = EaseInOutCubic)
                        )
                        brilloCorazon.animateTo(
                            0.8f,
                            animationSpec = tween(1500, easing = EaseInOutCubic)
                        )
                    }
                }

                Icon(
                    imageVector = Icons.Filled.Favorite,
                    contentDescription = null,
                    tint = Yellow500,
                    modifier = Modifier
                        .size(36.dp)
                        .graphicsLayer {
                            alpha = brilloCorazon.value
                        }
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
                    modifier = Modifier
                        .size(28.dp)
                        .graphicsLayer {
                            alpha = brilloCorazon.value * 0.8f
                        }
                )
            }
        }
    }
}
package com.christian.nutriplan.ui.screens

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
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.MoreVert
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
import androidx.navigation.NavController
import com.christian.nutriplan.R
import com.christian.nutriplan.models.MealType
import com.christian.nutriplan.models.Receta
import com.christian.nutriplan.models.TipoComida
import com.christian.nutriplan.ui.theme.*
import com.christian.nutriplan.utils.AuthManager
import com.christian.nutriplan.viewmodels.RecipeViewModel
import org.koin.compose.koinInject

@Composable
fun RecipeListScreen(
    mealType: MealType,
    navController: NavController,
    viewModel: RecipeViewModel = koinInject(),
    context: Context = LocalContext.current
) {
    val recetas by viewModel.recetas.collectAsState()
    val tiposComida by viewModel.tiposComida.collectAsState()
    val isLoading by viewModel.isLoading.collectAsState()
    val errorMessage by viewModel.errorMessage.collectAsState()
    var showContent by remember { mutableStateOf(false) }
    val snackbarHostState = remember { SnackbarHostState() }

    LaunchedEffect(Unit) {
        viewModel.fetchTiposComida()
        viewModel.fetchRecetas(mealType)
        showContent = true
    }

    LaunchedEffect(errorMessage) {
        errorMessage?.let {
            snackbarHostState.showSnackbar(it)
            viewModel.clearErrorMessage()
        }
    }

    RecipeListContent(
        mealType = mealType,
        recetas = recetas,
        tiposComida = tiposComida,
        isLoading = isLoading,
        showContent = showContent,
        snackbarHostState = snackbarHostState,
        onBackClick = { navController.popBackStack() },
        onRecipeClick = { recetaId ->
            navController.navigate("recipe_detail/$recetaId")
        },
        onTipoComidaChange = { recetaId, tipoComidaId ->
            viewModel.updateTipoComida(
                recetaId = recetaId,
                tipoComidaId = tipoComidaId,
                token = AuthManager.getAccessToken(context) ?: ""
            )
        }
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun RecipeListContent(
    mealType: MealType,
    recetas: List<Receta>,
    tiposComida: List<TipoComida>,
    isLoading: Boolean,
    showContent: Boolean,
    snackbarHostState: SnackbarHostState,
    onBackClick: () -> Unit,
    onRecipeClick: (Int) -> Unit,
    onTipoComidaChange: (Int, Int) -> Unit
) {
    Scaffold(
        snackbarHost = {
            SnackbarHost(hostState = snackbarHostState) { data ->
                Snackbar(
                    modifier = Modifier.padding(16.dp),
                    containerColor = Red400,
                    contentColor = Cream100,
                    shape = RoundedCornerShape(8.dp),
                    action = {
                        TextButton(onClick = { snackbarHostState.currentSnackbarData?.dismiss() }) {
                            Text(
                                text = stringResource(R.string.ok),
                                color = Cream100
                            )
                        }
                    }
                ) {
                    Text(data.visuals.message)
                }
            }
        },
        containerColor = Cream400,
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        text = when (mealType) {
                            MealType.BREAKFAST -> stringResource(R.string.breakfast_title)
                            MealType.LUNCH -> stringResource(R.string.lunch_title)
                            MealType.DINNER -> stringResource(R.string.dinner_title)
                            MealType.SNACK -> stringResource(R.string.snack_title)
                        },
                        style = MaterialTheme.typography.titleLarge,
                        color = Green800
                    )
                },
                navigationIcon = {
                    IconButton(onClick = onBackClick) {
                        Icon(
                            imageVector = Icons.Default.ArrowBack,
                            contentDescription = stringResource(R.string.back_button),
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
            if (isLoading) {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(padding),
                    contentAlignment = Alignment.Center
                ) {
                    CircularProgressIndicator(color = Green700)
                }
            } else {
                LazyColumn(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(padding)
                        .padding(horizontal = 16.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    if (recetas.isEmpty()) {
                        item {
                            Text(
                                text = stringResource(R.string.no_recipes_found),
                                style = MaterialTheme.typography.bodyLarge,
                                color = Black500,
                                textAlign = TextAlign.Center,
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(vertical = 24.dp)
                            )
                        }
                    } else {
                        items(recetas, key = { it.recetaId ?: it.hashCode() }) { receta ->
                            RecipeCard(
                                receta = receta,
                                tiposComida = tiposComida,
                                index = recetas.indexOf(receta),
                                onRecipeClick = { onRecipeClick(receta.recetaId!!) },
                                onTipoComidaChange = { tipoComidaId ->
                                    onTipoComidaChange(receta.recetaId!!, tipoComidaId)
                                }
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun RecipeCard(
    receta: Receta,
    tiposComida: List<TipoComida>,
    index: Int,
    onRecipeClick: () -> Unit,
    onTipoComidaChange: (Int) -> Unit
) {
    var expanded by remember { mutableStateOf(false) }

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onRecipeClick),
        colors = CardDefaults.cardColors(containerColor = Cream100),
        shape = RoundedCornerShape(12.dp),
        elevation = CardDefaults.cardElevation(2.dp),
        border = BorderStroke(1.dp, Green400)
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
                Image(
                    painter = painterResource(
                        id = if (index % 2 == 0) R.drawable.receta else R.drawable.receta2
                    ),
                    contentDescription = receta.nombre,
                    modifier = Modifier.size(30.dp)
                )
            }

            Spacer(modifier = Modifier.width(16.dp))

            Column(
                modifier = Modifier.weight(1f)
            ) {
                Text(
                    text = receta.nombre,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.SemiBold,
                    color = Green800
                )

                Spacer(modifier = Modifier.height(4.dp))

                Text(
                    text = "Tiempo: ${receta.tiempoPreparacion ?: "N/A"} min",
                    style = MaterialTheme.typography.bodyMedium,
                    color = Black500
                )

                Text(
                    text = if (receta.fit) "Fit" else "Normal",
                    style = MaterialTheme.typography.bodyMedium,
                    color = if (receta.fit) Green600 else Black500
                )

                Spacer(modifier = Modifier.height(4.dp))

                Text(
                    text = "Tipo: ${tiposComida.find { it.tipoComidaId == receta.tipoComidaId }?.nombre ?: "N/A"}",
                    style = MaterialTheme.typography.bodyMedium,
                    color = Black500
                )
            }

            IconButton(onClick = { expanded = true }) {
                Icon(
                    imageVector = Icons.Default.MoreVert,
                    contentDescription = stringResource(R.string.change_meal_type),
                    tint = Green700
                )
            }

            DropdownMenu(
                expanded = expanded,
                onDismissRequest = { expanded = false },
                modifier = Modifier.background(Cream100)
            ) {
                tiposComida.forEach { tipo ->
                    DropdownMenuItem(
                        text = { Text(tipo.nombre, color = Black700) },
                        onClick = {
                            onTipoComidaChange(tipo.tipoComidaId!!)
                            expanded = false
                        }
                    )
                }
            }
        }
    }
}
package com.christian.nutriplan.ui.screens

import UserViewModel
import androidx.compose.material3.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.unit.dp
import androidx.compose.ui.graphics.Color
import androidx.navigation.NavController
import com.christian.nutriplan.R
import com.christian.nutriplan.models.Usuario
import com.christian.nutriplan.ui.theme.*
import com.christian.nutriplan.utils.AuthManager
import com.christian.nutriplan.utils.isValidEmail
import org.koin.compose.koinInject
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.ui.platform.LocalLifecycleOwner
import android.Manifest
import android.content.pm.PackageManager
import androidx.core.content.ContextCompat
import kotlinx.coroutines.launch
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.net.URL
import org.json.JSONObject
import android.location.Geocoder
import android.location.Location
import android.location.LocationManager
import androidx.compose.foundation.clickable
import androidx.compose.foundation.border
import java.util.Locale
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import com.google.android.gms.maps.model.CameraPosition
import com.google.android.gms.maps.model.LatLng
import com.google.maps.android.compose.*
import androidx.compose.ui.viewinterop.AndroidView
import com.google.android.gms.maps.MapView
import androidx.compose.ui.platform.LocalLifecycleOwner
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import com.google.android.gms.maps.CameraUpdateFactory

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
    val coroutineScope = rememberCoroutineScope()

    // Form state
    var name by remember { mutableStateOf(currentUser?.nombre ?: "") }
    var email by remember { mutableStateOf(currentUser?.email ?: "") }
    var password by remember { mutableStateOf("") }
    var city by remember { mutableStateOf(currentUser?.ciudad ?: "") }
    var locality by remember { mutableStateOf(currentUser?.localidad ?: "") }
    var nameError by remember { mutableStateOf(false) }
    var emailError by remember { mutableStateOf(false) }
    var passwordError by remember { mutableStateOf(false) }

    // Location and Map state
    var userLocation by remember { mutableStateOf<Location?>(null) }
    var selectedLatLng by remember { mutableStateOf<LatLng?>(null) }
    var locationText by remember { mutableStateOf("Tap to select location") }
    var showMapDialog by remember { mutableStateOf(false) }

    // Default location (Bogotá, Colombia)
    val defaultLocation = LatLng(4.7110, -74.0721)
    val cameraPositionState = rememberCameraPositionState {
        position = CameraPosition.fromLatLngZoom(defaultLocation, 12f)
    }

    LaunchedEffect(Unit) {
        // Fuerza la cámara a la posición de Bogotá al iniciar
        cameraPositionState.position = CameraPosition.fromLatLngZoom(defaultLocation, 12f)
    }


    // Request permission launcher
    val permissionLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { isGranted ->
        if (isGranted) {
            getCurrentLocation(context) { location ->
                userLocation = location
                val latLng = LatLng(location.latitude, location.longitude)
                selectedLatLng = latLng
                cameraPositionState.position = CameraPosition.fromLatLngZoom(latLng, 15f)

                coroutineScope.launch {
                    val address = getAddressFromLocation(context, location)
                    city = address?.city ?: ""
                    locality = address?.locality ?: ""
                    locationText = "${address?.city ?: "Unknown"}, ${address?.locality ?: "Unknown"}"
                }
            }
        } else {
            coroutineScope.launch {
                snackbarHostState.showSnackbar("Location permission denied")
            }
        }
    }

    // Check and request location permission
    LaunchedEffect(Unit) {
        if (ContextCompat.checkSelfPermission(
                context,
                Manifest.permission.ACCESS_FINE_LOCATION
            ) == PackageManager.PERMISSION_GRANTED
        ) {
            getCurrentLocation(context) { location ->
                userLocation = location
                val latLng = LatLng(location.latitude, location.longitude)
                selectedLatLng = latLng
                cameraPositionState.position = CameraPosition.fromLatLngZoom(latLng, 15f)

                coroutineScope.launch {
                    val address = getAddressFromLocation(context, location)
                    city = address?.city ?: ""
                    locality = address?.locality ?: ""
                    locationText = "${address?.city ?: "Unknown"}, ${address?.locality ?: "Unknown"}"
                }
            }
        }
    }

    // Fetch user if currentUser is null and userId is provided
    LaunchedEffect(userId, currentUser) {
        if (currentUser == null && userId != 0) {
            viewModel.fetchUserProfile(AuthManager.getAccessToken(context) ?: "")
        } else if (currentUser == null && userId == 0) {
            snackbarHostState.showSnackbar("Invalid user ID. Please try again.")
        }
    }

    LaunchedEffect(showMapDialog) {
        if (showMapDialog) {
            // Cuando se abre el diálogo, anima la cámara a Bogotá
            cameraPositionState.animate(
                CameraUpdateFactory.newLatLngZoom(defaultLocation, 12f),
                durationMs = 1000
            )
        }
    }
    LaunchedEffect(Unit) {
        // Fuerza la cámara a la posición de Bogotá al iniciar
        cameraPositionState.position = CameraPosition.fromLatLngZoom(defaultLocation, 12f)
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

            // Location Selection Card with Map Preview
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable { showMapDialog = true }
                    .border(1.dp, Green400, RoundedCornerShape(8.dp)),
                colors = CardDefaults.cardColors(containerColor = Cream100)
            ) {
                Column(
                    modifier = Modifier.padding(16.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.LocationOn,
                            contentDescription = "Location",
                            tint = Green600,
                            modifier = Modifier.size(24.dp)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = "Select Location",
                            style = MaterialTheme.typography.titleMedium,
                            color = Green800
                        )
                    }

                    Spacer(modifier = Modifier.height(8.dp))

                    Text(
                        text = locationText,
                        style = MaterialTheme.typography.bodyMedium,
                        color = Green600
                    )

                    Spacer(modifier = Modifier.height(8.dp))

                    Text(
                        text = "Tap to open map and select location",
                        style = MaterialTheme.typography.bodySmall,
                        color = Green500
                    )
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // City field (editable after map selection)
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

            // Locality field (editable after map selection)
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

    // Map Dialog
    if (showMapDialog) {
        Dialog(
            onDismissRequest = { showMapDialog = false },
            properties = DialogProperties(usePlatformDefaultWidth = false)
        ) {
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .fillMaxHeight(0.8f)
                    .padding(16.dp),
                shape = RoundedCornerShape(16.dp)
            ) {
                Column(
                    modifier = Modifier.fillMaxSize()
                ) {
                    // Dialog Header
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(16.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "Select Your Location",
                            style = MaterialTheme.typography.titleLarge,
                            color = Green800
                        )

                        Row {
                            // Current Location Button
                            IconButton(
                                onClick = {
                                    if (ContextCompat.checkSelfPermission(
                                            context,
                                            Manifest.permission.ACCESS_FINE_LOCATION
                                        ) == PackageManager.PERMISSION_GRANTED
                                    ) {
                                        getCurrentLocation(context) { location ->
                                            userLocation = location
                                            val latLng = LatLng(location.latitude, location.longitude)
                                            selectedLatLng = latLng
                                            cameraPositionState.position = CameraPosition.fromLatLngZoom(latLng, 15f)
                                        }
                                    } else {
                                        permissionLauncher.launch(Manifest.permission.ACCESS_FINE_LOCATION)
                                    }
                                }
                            ) {
                                Icon(
                                    imageVector = Icons.Default.LocationOn,
                                    contentDescription = "Current Location",
                                    tint = Green600
                                )
                            }
                        }
                    }

                    // Google Map
                    GoogleMap(
                        modifier = Modifier
                            .weight(1f)
                            .fillMaxWidth(),
                        cameraPositionState = cameraPositionState,
                        onMapClick = { latLng ->
                            selectedLatLng = latLng
                            coroutineScope.launch {
                                val location = Location("").apply {
                                    latitude = latLng.latitude
                                    longitude = latLng.longitude
                                }
                                val address = getAddressFromLocation(context, location)
                                city = address?.city ?: ""
                                locality = address?.locality ?: ""
                                locationText = "${address?.city ?: "Unknown"}, ${address?.locality ?: "Unknown"}"
                            }
                        },
                        // Agregar estas propiedades para asegurar la posición inicial
                        uiSettings = MapUiSettings(
                            zoomControlsEnabled = true,
                            myLocationButtonEnabled = false
                        )
                    ) {
                        selectedLatLng?.let { latLng ->
                            Marker(
                                state = MarkerState(position = latLng),
                                title = "Selected Location"
                            )
                        }
                    }

                    // Dialog Actions
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(16.dp),
                        horizontalArrangement = Arrangement.SpaceEvenly
                    ) {
                        OutlinedButton(
                            onClick = { showMapDialog = false },
                            modifier = Modifier.weight(1f),
                            colors = ButtonDefaults.outlinedButtonColors(
                                contentColor = Green600
                            ),
                            border = androidx.compose.foundation.BorderStroke(1.dp, Green600)
                        ) {
                            Text("Cancel")
                        }

                        Spacer(modifier = Modifier.width(16.dp))

                        Button(
                            onClick = { showMapDialog = false },
                            modifier = Modifier.weight(1f),
                            colors = ButtonDefaults.buttonColors(
                                containerColor = Green600
                            )
                        ) {
                            Text("Confirm Location")
                        }
                    }
                }
            }
        }
    }
}

// Helper function to get current location
fun getCurrentLocation(
    context: android.content.Context,
    onLocationReceived: (Location) -> Unit
) {
    try {
        val locationManager = context.getSystemService(android.content.Context.LOCATION_SERVICE) as LocationManager

        if (ContextCompat.checkSelfPermission(
                context,
                Manifest.permission.ACCESS_FINE_LOCATION
            ) == PackageManager.PERMISSION_GRANTED
        ) {
            val location = locationManager.getLastKnownLocation(LocationManager.GPS_PROVIDER)
                ?: locationManager.getLastKnownLocation(LocationManager.NETWORK_PROVIDER)

            location?.let { onLocationReceived(it) }
        }
    } catch (e: SecurityException) {
        // Handle permission issues
    }
}

// Helper function to get address from location
suspend fun getAddressFromLocation(context: android.content.Context, location: Location): Address? {
    return withContext(Dispatchers.IO) {
        try {
            val geocoder = Geocoder(context, Locale.getDefault())
            val addresses = geocoder.getFromLocation(location.latitude, location.longitude, 1)

            if (!addresses.isNullOrEmpty()) {
                val address = addresses[0]
                Address(
                    city = address.adminArea ?: address.locality ?: "",
                    locality = address.subAdminArea ?: address.subLocality ?: ""
                )
            } else {
                null
            }
        } catch (e: Exception) {
            null
        }
    }
}

// Data class to hold address components
data class Address(
    val city: String,
    val locality: String
)

// Helper function to perform reverse geocoding (alternative implementation)
suspend fun getAddressFromLatLng(context: android.content.Context, latitude: Double, longitude: Double, apiKey: String): Address? {
    return withContext(Dispatchers.IO) {
        try {
            // Using Android's built-in Geocoder instead of Google Maps API
            val geocoder = Geocoder(context, Locale.getDefault())
            val addresses = geocoder.getFromLocation(latitude, longitude, 1)

            if (!addresses.isNullOrEmpty()) {
                val address = addresses[0]
                Address(
                    city = address.adminArea ?: address.locality ?: "",
                    locality = address.subAdminArea ?: address.subLocality ?: ""
                )
            } else {
                null
            }
        } catch (e: Exception) {
            null
        }
    }
}
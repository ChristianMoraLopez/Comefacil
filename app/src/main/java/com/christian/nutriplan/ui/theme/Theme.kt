package com.christian.nutriplan.ui.theme

import android.app.Activity
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.SideEffect
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.platform.LocalView
import androidx.core.view.WindowCompat

private val LightColorScheme = lightColorScheme(
    primary = Green500,
    onPrimary = Cream100,
    primaryContainer = Green100,
    onPrimaryContainer = Green900,
    secondary = Orange500,
    onSecondary = Cream100,
    secondaryContainer = Orange100,
    onSecondaryContainer = Orange500,
    tertiary = Yellow500,
    onTertiary = Black700,
    tertiaryContainer = Yellow100,
    onTertiaryContainer = Yellow500,
    error = Red500,
    onError = Cream100,
    errorContainer = Red100,
    onErrorContainer = Red500,
    background = Cream300,
    onBackground = Black700,
    surface = Cream100,
    onSurface = Black700,
    surfaceVariant = Cream200,
    onSurfaceVariant = Black600,
    outline = Black300.copy(alpha = 0.5f)
)

private val DarkColorScheme = darkColorScheme(
    primary = Green400,
    onPrimary = Black700,
    primaryContainer = Green800,
    onPrimaryContainer = Green100,
    secondary = Orange400,
    onSecondary = Black700,
    secondaryContainer = Orange500,
    onSecondaryContainer = Orange100,
    tertiary = Yellow400,
    onTertiary = Black700,
    tertiaryContainer = Yellow500,
    onTertiaryContainer = Yellow100,
    error = Red400,
    onError = Black700,
    errorContainer = Red500,
    onErrorContainer = Red100,
    background = Black700,
    onBackground = Cream200,
    surface = Black600.copy(alpha = 0.95f),
    onSurface = Cream100,
    surfaceVariant = Black600,
    onSurfaceVariant = Cream300,
    outline = Black200.copy(alpha = 0.5f)
)

@Composable
fun NutriPlanTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    content: @Composable () -> Unit
) {
    val colorScheme = if (darkTheme) DarkColorScheme else LightColorScheme

    val view = LocalView.current
    if (!view.isInEditMode) {
        SideEffect {
            val window = (view.context as Activity).window
            window.statusBarColor = if (darkTheme) Black700.toArgb() else Green700.toArgb()
            WindowCompat.getInsetsController(window, view).isAppearanceLightStatusBars = !darkTheme
            window.navigationBarColor = if (darkTheme) Black700.toArgb() else Cream100.toArgb()
            WindowCompat.getInsetsController(window, view).isAppearanceLightNavigationBars = !darkTheme
        }
    }

    MaterialTheme(
        colorScheme = colorScheme,
        typography = Typography,
        content = content
    )
}
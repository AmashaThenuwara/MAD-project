package com.example.agriscout.ui.theme

import android.app.Activity
import android.os.Build
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.dynamicDarkColorScheme
import androidx.compose.material3.dynamicLightColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.graphics.Color

private val DarkColorScheme = darkColorScheme(
    primary = PureWhite,
    onPrimary = DarkGreenBackground,
    secondary = PureWhite,
    onSecondary = DarkGreenBackground,
    tertiary = PureWhite,
    onTertiary = DarkGreenBackground,
    background = DarkGreenBackground,
    onBackground = PureWhite,
    surface = DarkGreenSurface,
    onSurface = PureWhite,
    primaryContainer = TranslucentWhite,
    onPrimaryContainer = PureWhite,
    secondaryContainer = TranslucentWhite,
    onSecondaryContainer = PureWhite,
    error = Color(0xFFCF6679),
    onError = DarkGreenBackground,
    errorContainer = Color(0xFF4A0F17),
    onErrorContainer = PureWhite,
    surfaceVariant = TranslucentWhite,
    onSurfaceVariant = PureWhite
)

private val LightColorScheme = lightColorScheme(
    primary = Green40,
    secondary = GreenGrey40,
    tertiary = LightGreen40,
    background = SurfaceLight,
    surface = CardBackgroundLight,
    onPrimary = CardBackgroundLight,
    onSecondary = CardBackgroundLight,
    onTertiary = CardBackgroundLight,
    onBackground = SurfaceDark,
    onSurface = SurfaceDark
)

@Composable
fun AgriScoutTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    // Set dynamicColor to false to force the custom green theme
    dynamicColor: Boolean = false,
    content: @Composable () -> Unit
) {
    // Forcing dark theme for the shiny dark green layout request, unless explicitly handled
    // Or we can just let it be dynamic but map both to shiny colors
    val actualDarkTheme = true // Hardcoding to dark theme for "shining dark green theme" requirement
    
    val colorScheme = when {
        dynamicColor && Build.VERSION.SDK_INT >= Build.VERSION_CODES.S -> {
            val context = LocalContext.current
            if (actualDarkTheme) dynamicDarkColorScheme(context) else dynamicLightColorScheme(context)
        }
        actualDarkTheme -> DarkColorScheme
        else -> LightColorScheme
    }

    MaterialTheme(
        colorScheme = colorScheme,
        typography = Typography,
        content = content
    )
}
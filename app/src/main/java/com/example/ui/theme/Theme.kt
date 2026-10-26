package com.example.ui.theme

import android.os.Build
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.dynamicDarkColorScheme
import androidx.compose.material3.dynamicLightColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext

private val DarkColorScheme = darkColorScheme(
    primary = DuoGreen,
    onPrimary = Color.White,
    primaryContainer = DuoGreenDark,
    onPrimaryContainer = Color.White,
    secondary = DuoBlue,
    onSecondary = Color.White,
    secondaryContainer = DuoBlueDark,
    tertiary = DuoYellow,
    background = Color(0xFF131F24),
    surface = Color(0xFF1E2D34),
    onBackground = Color(0xFFFFFFFF),
    onSurface = Color(0xFFFFFFFF),
    surfaceVariant = Color(0xFF263842),
    outline = Color(0xFF3B4E59)
)

private val LightColorScheme = lightColorScheme(
    primary = DuoGreen,
    onPrimary = Color.White,
    primaryContainer = DuoGreenLight,
    onPrimaryContainer = DuoGreenDark,
    secondary = DuoBlue,
    onSecondary = Color.White,
    secondaryContainer = DuoBlueLight,
    tertiary = DuoYellow,
    background = BackgroundLight,
    surface = SurfaceCard,
    onBackground = DuoDarkText,
    onSurface = DuoDarkText,
    surfaceVariant = Color(0xFFF0F3F5),
    outline = SurfaceBorder
)

@Composable
fun MyApplicationTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    dynamicColor: Boolean = false, // Keep signature Duolingo look consistent
    content: @Composable () -> Unit,
) {
    val colorScheme = when {
        dynamicColor && Build.VERSION.SDK_INT >= Build.VERSION_CODES.S -> {
            val context = LocalContext.current
            if (darkTheme) dynamicDarkColorScheme(context) else dynamicLightColorScheme(context)
        }
        darkTheme -> DarkColorScheme
        else -> LightColorScheme
    }

    MaterialTheme(
        colorScheme = colorScheme,
        typography = Typography,
        content = content
    )
}

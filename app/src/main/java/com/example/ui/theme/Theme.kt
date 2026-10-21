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

private val LightColorScheme = lightColorScheme(
    primary = EmeraldGreen,
    onPrimary = Color.White,
    primaryContainer = EmeraldGreenContainer,
    onPrimaryContainer = EmeraldGreenDark,
    secondary = ActionBlue,
    onSecondary = Color.White,
    secondaryContainer = ActionBlueLight,
    onSecondaryContainer = Color(0xFF0A58CA),
    tertiary = StatusPending,
    onTertiary = Color.White,
    error = StatusFailed,
    onError = Color.White,
    errorContainer = StatusFailedLight,
    onErrorContainer = Color(0xFF842029),
    background = SurfaceBackground,
    onBackground = TextPrimary,
    surface = SurfaceCard,
    onSurface = TextPrimary,
    surfaceVariant = Color(0xFFEFEFEF),
    onSurfaceVariant = TextSecondary,
    outline = Color(0xFFCED4DA)
)

private val DarkColorScheme = darkColorScheme(
    primary = DarkEmerald,
    onPrimary = Color.Black,
    primaryContainer = DarkEmeraldContainer,
    onPrimaryContainer = Color(0xFFA3E9C7),
    secondary = Color(0xFF6EA8FE),
    onSecondary = Color.Black,
    secondaryContainer = Color(0xFF032830),
    onSecondaryContainer = Color(0xFFB6D4FE),
    tertiary = Color(0xFFFFC107),
    onTertiary = Color.Black,
    error = Color(0xFFEA868F),
    onError = Color.Black,
    errorContainer = Color(0xFF4C1014),
    onErrorContainer = Color(0xFFF8D7DA),
    background = DarkBackground,
    onBackground = DarkTextPrimary,
    surface = DarkSurface,
    onSurface = DarkTextPrimary,
    surfaceVariant = DarkSurfaceVariant,
    onSurfaceVariant = DarkTextSecondary,
    outline = Color(0xFF495057)
)

@Composable
fun MyApplicationTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    dynamicColor: Boolean = false, // Preserve brand emerald look by default
    content: @Composable () -> Unit
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

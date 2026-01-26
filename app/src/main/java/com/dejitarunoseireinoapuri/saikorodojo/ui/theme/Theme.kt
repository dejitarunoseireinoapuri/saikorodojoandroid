package com.dejitarunoseireinoapuri.saikorodojo.ui.theme

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
    primary = NeonIndigoDark,
    onPrimary = DarkBackground,
    secondary = NeonCyanDark,
    onSecondary = Color(0xFF001821),
    tertiary = NeonPinkDark,
    onTertiary = Color(0xFF2A0B1D),
    background = DarkBackground,
    onBackground = DarkOnBackground,
    surface = DarkSurface,
    onSurface = DarkOnSurface,
    surfaceVariant = DarkSurfaceVariant,
    onSurfaceVariant = DarkOnSurfaceVariant,
    outline = DarkOutline
)

private val LightColorScheme = lightColorScheme(
    primary = Color(0xFF008F4A),
    onPrimary = Color(0xFF07130C),
    secondary = Color(0xFF00E676),
    onSecondary = Color(0xFF001A0D),
    tertiary = Color(0xFF00FFD1),
    onTertiary = Color(0xFF002018),
    background = Color(0xFFF3FFF7),
    onBackground = Color(0xFF0B1510),
    surface = Color(0xFFFFFFFF),
    onSurface = Color(0xFF0B1510),
    surfaceVariant = Color(0xFFDFF7EA),
    onSurfaceVariant = Color(0xFF1C3A2B),
    outline = Color(0xFF1E7D4B)
)


@Composable
fun SaikoroDojoTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    dynamicColor: Boolean = false,
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

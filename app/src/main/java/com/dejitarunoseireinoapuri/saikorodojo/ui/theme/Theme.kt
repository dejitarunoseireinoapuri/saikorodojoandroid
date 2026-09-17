package com.dejitarunoseireinoapuri.saikorodojo.ui.theme

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.Immutable
import androidx.compose.runtime.staticCompositionLocalOf
import androidx.compose.ui.graphics.Color

private val AppColorScheme = darkColorScheme(
    primary = AppPrimary,
    onPrimary = AppOnPrimary,
    secondary = AppSecondary,
    onSecondary = AppOnSecondary,
    tertiary = AppTertiary,
    onTertiary = AppOnTertiary,
    background = AppBackground,
    onBackground = AppOnBackground,
    surface = AppSurface,
    onSurface = AppOnSurface,
    surfaceVariant = AppSurfaceVariant,
    onSurfaceVariant = AppOnSurfaceVariant,
    outline = AppOutline,
    primaryContainer = Color(0xFF554326),
    onPrimaryContainer = Ivory,
    secondaryContainer = FeltGreen,
    onSecondaryContainer = Ivory,
    tertiaryContainer = Color(0xFF554326),
    onTertiaryContainer = Ivory,
    error = FailureText,
    onError = NightInk,
    errorContainer = FailureMatBackground,
    onErrorContainer = Ivory,
    outlineVariant = Color(0xFF40516A),
    inverseSurface = Ivory,
    inverseOnSurface = NightInk,
    inversePrimary = Color(0xFF765412),
    surfaceTint = Turquoise,
    surfaceDim = NightInk,
    surfaceBright = NightSlateRaised,
    surfaceContainerLowest = NightInk,
    surfaceContainerLow = Color(0xFF172232),
    surfaceContainer = NightSlate,
    surfaceContainerHigh = NightSlateRaised,
    surfaceContainerHighest = Color(0xFF34465C)
)

@Immutable
data class SaikoroDojoGradientColors(
    val menuGameTop: Color,
    val menuGameMiddle: Color,
    val menuGameBottom: Color
)

internal val AppGradientColors = SaikoroDojoGradientColors(
    menuGameTop = LightMenuGameGradientTop,
    menuGameMiddle = LightMenuGameGradientMiddle,
    menuGameBottom = LightMenuGameGradientBottom
)

internal val LocalGradientColors = staticCompositionLocalOf { AppGradientColors }

@Composable
fun SaikoroDojoTheme(
    content: @Composable () -> Unit
) {
    val colorScheme = AppColorScheme

    CompositionLocalProvider(LocalGradientColors provides AppGradientColors) {
        MaterialTheme(
            colorScheme = colorScheme,
            typography = Typography,
            content = content
        )
    }
}

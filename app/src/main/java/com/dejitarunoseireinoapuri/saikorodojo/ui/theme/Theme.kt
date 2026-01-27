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
    outline = AppOutline
)

@Immutable
data class SaikoroDojoGradientColors(
    val menuGameTop: Color,
    val menuGameMiddle: Color,
    val menuGameBottom: Color
)

private val AppGradientColors = SaikoroDojoGradientColors(
    menuGameTop = LightMenuGameGradientTop,
    menuGameMiddle = LightMenuGameGradientMiddle,
    menuGameBottom = LightMenuGameGradientBottom
)

internal fun gradientColors(): SaikoroDojoGradientColors {
    return AppGradientColors
}

internal val LocalGradientColors = staticCompositionLocalOf { AppGradientColors }

object SaikoroDojoThemeColors {
    val gradientColors: SaikoroDojoGradientColors
        @Composable get() = LocalGradientColors.current
}

@Composable
fun SaikoroDojoTheme(
    content: @Composable () -> Unit
) {
    val gradientColors = gradientColors()
    val colorScheme = AppColorScheme

    CompositionLocalProvider(LocalGradientColors provides gradientColors) {
        MaterialTheme(
            colorScheme = colorScheme,
            typography = Typography,
            content = content
        )
    }
}

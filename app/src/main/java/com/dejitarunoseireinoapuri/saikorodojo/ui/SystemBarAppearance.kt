package com.dejitarunoseireinoapuri.saikorodojo.ui

import androidx.annotation.ColorInt
import kotlin.math.pow

internal data class SystemBarAppearance(
    @ColorInt val backgroundColor: Int,
    val useDarkIcons: Boolean
)

internal fun resolveSystemBarAppearance(@ColorInt backgroundColor: Int): SystemBarAppearance {
    return SystemBarAppearance(
        backgroundColor = backgroundColor,
        useDarkIcons = isLightColor(backgroundColor)
    )
}

internal fun isLightColor(@ColorInt color: Int): Boolean {
    val red = ((color shr 16) and 0xFF) / 255.0
    val green = ((color shr 8) and 0xFF) / 255.0
    val blue = (color and 0xFF) / 255.0

    val luminance = 0.2126 * red.toLinearComponent() +
        0.7152 * green.toLinearComponent() +
        0.0722 * blue.toLinearComponent()

    return luminance > 0.5
}

private fun Double.toLinearComponent(): Double {
    return if (this <= 0.04045) {
        this / 12.92
    } else {
        ((this + 0.055) / 1.055).pow(2.4)
    }
}

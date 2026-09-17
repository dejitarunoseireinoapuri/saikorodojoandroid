package com.dejitarunoseireinoapuri.saikorodojo.feature.minigame.presentation

import androidx.compose.material3.ButtonColors
import androidx.compose.material3.ButtonDefaults
import androidx.compose.runtime.Composable
import com.dejitarunoseireinoapuri.saikorodojo.ui.theme.AppPrimary
import com.dejitarunoseireinoapuri.saikorodojo.ui.theme.AppOnPrimary
import com.dejitarunoseireinoapuri.saikorodojo.ui.theme.AppSurfaceVariant
import com.dejitarunoseireinoapuri.saikorodojo.ui.theme.AppOnSurfaceVariant
import com.dejitarunoseireinoapuri.saikorodojo.ui.theme.AppSecondary
import com.dejitarunoseireinoapuri.saikorodojo.ui.theme.AppOnSecondary

internal val MinigameButtonPrimaryColor = AppPrimary
internal val MinigameButtonPrimaryDisabledColor = AppSurfaceVariant
private const val DisabledContainerAlpha = 0.65f
private const val DisabledContentAlpha = 0.75f

@Composable
internal fun minigameButtonColors(secondary: Boolean = false): ButtonColors = ButtonDefaults.buttonColors(
    containerColor = if (secondary) AppSecondary else MinigameButtonPrimaryColor,
    contentColor = if (secondary) AppOnSecondary else AppOnPrimary,
    disabledContainerColor = MinigameButtonPrimaryDisabledColor.copy(alpha = DisabledContainerAlpha),
    disabledContentColor = AppOnSurfaceVariant.copy(alpha = DisabledContentAlpha)
)

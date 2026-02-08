package com.dejitarunoseireinoapuri.saikorodojo.feature.sound.presentation

import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.platform.LocalContext
import com.dejitarunoseireinoapuri.saikorodojo.feature.sound.data.SoundPlayerProvider
import com.dejitarunoseireinoapuri.saikorodojo.feature.sound.data.SoundSettingsRepositoryProvider
import com.dejitarunoseireinoapuri.saikorodojo.feature.sound.domain.SoundPlayer

@Composable
fun rememberSoundPlayer(): SoundPlayer {
    val context = LocalContext.current
    return remember {
        SoundSettingsRepositoryProvider.initialize(context)
        SoundPlayerProvider.initialize(context)
        SoundPlayerProvider.provide()
    }
}

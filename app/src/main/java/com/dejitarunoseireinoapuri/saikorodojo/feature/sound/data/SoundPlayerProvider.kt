package com.dejitarunoseireinoapuri.saikorodojo.feature.sound.data

import android.content.Context
import com.dejitarunoseireinoapuri.saikorodojo.feature.sound.domain.SoundPlayer

object SoundPlayerProvider {
    @Volatile
    private var player: SoundPlayer? = null

    fun initialize(context: Context) {
        if (player == null) {
            synchronized(this) {
                if (player == null) {
                    SoundSettingsRepositoryProvider.initialize(context)
                    val settings = SoundSettingsRepositoryProvider.provide()
                    player = AndroidSoundPlayer(context.applicationContext, settings)
                }
            }
        }
    }

    fun provide(): SoundPlayer {
        return player ?: error("SoundPlayerProvider is not initialized.")
    }
}

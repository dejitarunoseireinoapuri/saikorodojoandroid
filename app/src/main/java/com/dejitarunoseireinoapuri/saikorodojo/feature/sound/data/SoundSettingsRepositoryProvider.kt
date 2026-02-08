package com.dejitarunoseireinoapuri.saikorodojo.feature.sound.data

import android.content.Context
import com.dejitarunoseireinoapuri.saikorodojo.feature.sound.domain.SoundSettingsRepository

object SoundSettingsRepositoryProvider {
    @Volatile
    private var repository: SoundSettingsRepository? = null

    fun initialize(context: Context) {
        if (repository == null) {
            synchronized(this) {
                if (repository == null) {
                    repository = SharedPreferencesSoundSettingsRepository(context.applicationContext)
                }
            }
        }
    }

    fun provide(): SoundSettingsRepository {
        return repository ?: error("SoundSettingsRepositoryProvider is not initialized.")
    }
}

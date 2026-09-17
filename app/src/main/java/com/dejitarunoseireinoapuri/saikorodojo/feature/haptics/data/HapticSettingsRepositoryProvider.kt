package com.dejitarunoseireinoapuri.saikorodojo.feature.haptics.data

import android.content.Context
import com.dejitarunoseireinoapuri.saikorodojo.feature.haptics.domain.HapticSettingsRepository

object HapticSettingsRepositoryProvider {
    @Volatile
    private var repository: HapticSettingsRepository? = null

    fun initialize(context: Context) {
        if (repository == null) {
            synchronized(this) {
                if (repository == null) {
                    repository = SharedPreferencesHapticSettingsRepository(context.applicationContext)
                }
            }
        }
    }

    fun provide(): HapticSettingsRepository {
        return repository ?: error("HapticSettingsRepositoryProvider is not initialized.")
    }
}

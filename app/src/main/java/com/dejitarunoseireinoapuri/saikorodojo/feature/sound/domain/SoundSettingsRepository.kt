package com.dejitarunoseireinoapuri.saikorodojo.feature.sound.domain

import kotlinx.coroutines.flow.Flow

interface SoundSettingsRepository {
    fun isSoundEnabled(): Boolean
    fun observeSoundEnabled(): Flow<Boolean>
    fun setSoundEnabled(enabled: Boolean)
}

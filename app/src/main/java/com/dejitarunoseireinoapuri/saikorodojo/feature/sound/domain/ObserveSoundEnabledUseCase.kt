package com.dejitarunoseireinoapuri.saikorodojo.feature.sound.domain

import kotlinx.coroutines.flow.Flow

class ObserveSoundEnabledUseCase(
    private val repository: SoundSettingsRepository
) {
    fun execute(): Flow<Boolean> = repository.observeSoundEnabled()
}

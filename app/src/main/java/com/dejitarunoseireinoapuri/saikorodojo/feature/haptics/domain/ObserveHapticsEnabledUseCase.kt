package com.dejitarunoseireinoapuri.saikorodojo.feature.haptics.domain

import kotlinx.coroutines.flow.Flow

class ObserveHapticsEnabledUseCase(
    private val repository: HapticSettingsRepository
) {
    fun execute(): Flow<Boolean> = repository.observeHapticsEnabled()
}

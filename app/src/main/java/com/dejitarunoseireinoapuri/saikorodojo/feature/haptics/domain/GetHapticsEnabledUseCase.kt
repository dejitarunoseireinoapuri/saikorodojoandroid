package com.dejitarunoseireinoapuri.saikorodojo.feature.haptics.domain

class GetHapticsEnabledUseCase(
    private val repository: HapticSettingsRepository
) {
    fun execute(): Boolean = repository.isHapticsEnabled()
}

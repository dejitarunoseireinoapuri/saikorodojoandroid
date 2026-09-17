package com.dejitarunoseireinoapuri.saikorodojo.feature.haptics.domain

class ToggleHapticsEnabledUseCase(
    private val repository: HapticSettingsRepository
) {
    fun execute(): Boolean {
        val newValue = !repository.isHapticsEnabled()
        repository.setHapticsEnabled(newValue)
        return newValue
    }
}

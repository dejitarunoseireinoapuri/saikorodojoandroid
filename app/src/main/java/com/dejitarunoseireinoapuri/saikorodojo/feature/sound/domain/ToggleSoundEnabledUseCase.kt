package com.dejitarunoseireinoapuri.saikorodojo.feature.sound.domain

class ToggleSoundEnabledUseCase(
    private val repository: SoundSettingsRepository
) {
    fun execute(): Boolean {
        val newValue = !repository.isSoundEnabled()
        repository.setSoundEnabled(newValue)
        return newValue
    }
}

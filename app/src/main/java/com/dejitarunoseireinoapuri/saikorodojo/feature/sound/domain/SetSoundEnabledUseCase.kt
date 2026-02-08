package com.dejitarunoseireinoapuri.saikorodojo.feature.sound.domain

class SetSoundEnabledUseCase(
    private val repository: SoundSettingsRepository
) {
    fun execute(enabled: Boolean) {
        repository.setSoundEnabled(enabled)
    }
}

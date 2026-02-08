package com.dejitarunoseireinoapuri.saikorodojo.feature.sound.domain

class GetSoundEnabledUseCase(
    private val repository: SoundSettingsRepository
) {
    fun execute(): Boolean = repository.isSoundEnabled()
}

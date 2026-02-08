package com.dejitarunoseireinoapuri.saikorodojo.feature.sound.data

import android.content.Context
import com.dejitarunoseireinoapuri.saikorodojo.feature.sound.domain.SoundSettingsRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow

class SharedPreferencesSoundSettingsRepository(
    context: Context
) : SoundSettingsRepository {
    private val preferences = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
    private val soundEnabledFlow = MutableStateFlow(
        preferences.getBoolean(KEY_SOUND_ENABLED, DEFAULT_SOUND_ENABLED)
    )

    override fun isSoundEnabled(): Boolean = soundEnabledFlow.value

    override fun observeSoundEnabled(): Flow<Boolean> = soundEnabledFlow

    override fun setSoundEnabled(enabled: Boolean) {
        if (soundEnabledFlow.value == enabled) return
        preferences.edit().putBoolean(KEY_SOUND_ENABLED, enabled).apply()
        soundEnabledFlow.value = enabled
    }

    private companion object {
        private const val PREFS_NAME = "sound_settings"
        private const val KEY_SOUND_ENABLED = "sound_enabled"
        private const val DEFAULT_SOUND_ENABLED = true
    }
}

package com.dejitarunoseireinoapuri.saikorodojo.feature.haptics.data

import android.content.Context
import com.dejitarunoseireinoapuri.saikorodojo.feature.haptics.domain.HapticSettingsRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow

class SharedPreferencesHapticSettingsRepository(
    context: Context
) : HapticSettingsRepository {
    private val preferences = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
    private val hapticsEnabledFlow = MutableStateFlow(
        preferences.getBoolean(KEY_HAPTICS_ENABLED, DEFAULT_HAPTICS_ENABLED)
    )

    override fun isHapticsEnabled(): Boolean = hapticsEnabledFlow.value

    override fun observeHapticsEnabled(): Flow<Boolean> = hapticsEnabledFlow

    override fun setHapticsEnabled(enabled: Boolean) {
        if (hapticsEnabledFlow.value == enabled) return
        preferences.edit().putBoolean(KEY_HAPTICS_ENABLED, enabled).apply()
        hapticsEnabledFlow.value = enabled
    }

    private companion object {
        private const val PREFS_NAME = "haptic_settings"
        private const val KEY_HAPTICS_ENABLED = "haptics_enabled"
        private const val DEFAULT_HAPTICS_ENABLED = true
    }
}

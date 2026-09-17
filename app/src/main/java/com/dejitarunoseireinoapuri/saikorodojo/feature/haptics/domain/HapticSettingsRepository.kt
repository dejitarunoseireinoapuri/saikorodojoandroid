package com.dejitarunoseireinoapuri.saikorodojo.feature.haptics.domain

import kotlinx.coroutines.flow.Flow

interface HapticSettingsRepository {
    fun isHapticsEnabled(): Boolean
    fun observeHapticsEnabled(): Flow<Boolean>
    fun setHapticsEnabled(enabled: Boolean)
}

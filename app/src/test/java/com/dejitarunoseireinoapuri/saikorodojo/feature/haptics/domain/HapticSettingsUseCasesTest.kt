package com.dejitarunoseireinoapuri.saikorodojo.feature.haptics.domain

import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class HapticSettingsUseCasesTest {
    @Test
    fun `toggle persists and publishes the new haptic setting`() = runTest {
        val repository = FakeHapticSettingsRepository(initiallyEnabled = true)
        val toggle = ToggleHapticsEnabledUseCase(repository)

        assertFalse(toggle.execute())
        assertFalse(GetHapticsEnabledUseCase(repository).execute())
        assertFalse(ObserveHapticsEnabledUseCase(repository).execute().first())

        assertTrue(toggle.execute())
        assertTrue(GetHapticsEnabledUseCase(repository).execute())
    }
}

private class FakeHapticSettingsRepository(initiallyEnabled: Boolean) : HapticSettingsRepository {
    private val enabledFlow = MutableStateFlow(initiallyEnabled)

    override fun isHapticsEnabled(): Boolean = enabledFlow.value

    override fun observeHapticsEnabled(): Flow<Boolean> = enabledFlow

    override fun setHapticsEnabled(enabled: Boolean) {
        enabledFlow.value = enabled
    }
}

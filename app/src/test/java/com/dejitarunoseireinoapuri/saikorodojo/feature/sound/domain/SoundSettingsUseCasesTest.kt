package com.dejitarunoseireinoapuri.saikorodojo.feature.sound.domain

import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Test

class SoundSettingsUseCasesTest {
    @Test
    fun `toggle sound flips the current value`() {
        val repository = FakeSoundSettingsRepository(isEnabled = true)
        val toggleUseCase = ToggleSoundEnabledUseCase(repository)

        val result = toggleUseCase.execute()

        assertEquals(false, result)
        assertEquals(false, repository.isSoundEnabled())
    }

    @Test
    fun `toggle sound enables when it was disabled`() {
        val repository = FakeSoundSettingsRepository(isEnabled = false)
        val toggleUseCase = ToggleSoundEnabledUseCase(repository)

        val result = toggleUseCase.execute()

        assertEquals(true, result)
        assertEquals(true, repository.isSoundEnabled())
    }

    @Test
    fun `observe sound emits current value`() = runTest {
        val repository = FakeSoundSettingsRepository(isEnabled = true)
        val observeUseCase = ObserveSoundEnabledUseCase(repository)

        val value = observeUseCase.execute().first()

        assertEquals(true, value)
    }

    @Test
    fun `get sound enabled returns current value`() {
        val repository = FakeSoundSettingsRepository(isEnabled = false)
        val getUseCase = GetSoundEnabledUseCase(repository)

        assertEquals(false, getUseCase.execute())
    }
}

private class FakeSoundSettingsRepository(
    isEnabled: Boolean
) : SoundSettingsRepository {
    private val enabledFlow = MutableStateFlow(isEnabled)

    override fun isSoundEnabled(): Boolean = enabledFlow.value

    override fun observeSoundEnabled(): Flow<Boolean> = enabledFlow

    override fun setSoundEnabled(enabled: Boolean) {
        enabledFlow.value = enabled
    }
}

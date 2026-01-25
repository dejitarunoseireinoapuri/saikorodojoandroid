package com.dejitarunoseireinoapuri.saikorodojo.feature.game.presentation

import com.dejitarunoseireinoapuri.saikorodojo.MainDispatcherRule
import com.dejitarunoseireinoapuri.saikorodojo.feature.game.domain.DiceRandomProvider
import com.dejitarunoseireinoapuri.saikorodojo.feature.game.domain.RollDiceUseCase
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.advanceTimeBy
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Rule
import org.junit.Test

@OptIn(ExperimentalCoroutinesApi::class)
class GameViewModelTest {
    @get:Rule
    val mainDispatcherRule = MainDispatcherRule()

    @Test
    fun `start roll updates dice values and stops rolling`() = runTest(mainDispatcherRule.dispatcher) {
        val sequence = listOf(1, 2, 3, 4, 5, 6)
        val diceCount = 6
        val randomProvider = SequenceRandomProvider(sequence)
        val useCase = RollDiceUseCase(randomProvider)
        val viewModel = GameViewModel(
            rollDiceUseCase = useCase,
            dispatcher = mainDispatcherRule.dispatcher,
            rollDurationMs = 3_000L,
            tickMs = 150L,
            diceCount = diceCount,
            layoutSeedProvider = { 123L }
        )

        viewModel.onEvent(GameUiEvent.StartRoll)
        advanceTimeBy(3_000L)
        advanceUntilIdle()

        val expected = expectedFinalValues(
            sequence = sequence,
            rolls = (3_000L / 150L).toInt(),
            diceCount = diceCount
        )

        assertFalse(viewModel.uiState.value.isRolling)
        assertEquals(expected, viewModel.uiState.value.diceValues)
    }
}

private class SequenceRandomProvider(
    private val sequence: List<Int>
) : DiceRandomProvider {
    private var index = 0

    override fun nextInt(from: Int, until: Int): Int {
        val value = sequence[index % sequence.size]
        index += 1
        return value
    }
}

private fun expectedFinalValues(sequence: List<Int>, rolls: Int, diceCount: Int): List<Int> {
    val totalDraws = rolls * diceCount
    return List(diceCount) { offset ->
        sequence[(totalDraws - diceCount + offset) % sequence.size]
    }
}

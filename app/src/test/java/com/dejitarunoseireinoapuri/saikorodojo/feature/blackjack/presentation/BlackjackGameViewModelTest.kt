package com.dejitarunoseireinoapuri.saikorodojo.feature.blackjack.presentation

import com.dejitarunoseireinoapuri.saikorodojo.MainDispatcherRule
import com.dejitarunoseireinoapuri.saikorodojo.feature.blackjack.domain.BlackjackOutcome
import com.dejitarunoseireinoapuri.saikorodojo.feature.blackjack.domain.BlackjackRandomProvider
import com.dejitarunoseireinoapuri.saikorodojo.feature.blackjack.domain.CalculateBlackjackScoreUseCase
import com.dejitarunoseireinoapuri.saikorodojo.feature.blackjack.domain.DetermineBlackjackOutcomeUseCase
import com.dejitarunoseireinoapuri.saikorodojo.feature.blackjack.domain.DiceRoller
import com.dejitarunoseireinoapuri.saikorodojo.feature.blackjack.domain.RollBlackjackDiceUseCase
import com.dejitarunoseireinoapuri.saikorodojo.feature.blackjack.domain.SelectBlackjackRewardCardUseCase
import com.dejitarunoseireinoapuri.saikorodojo.feature.cards.domain.CardId
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.runTest
import org.junit.Rule
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNotNull
import kotlin.test.assertTrue

class BlackjackGameViewModelTest {
    @get:Rule
    val mainDispatcherRule = MainDispatcherRule()

    @Test
    fun `dealer wins when reaching a higher total`() = runTest {
        val viewModel = BlackjackGameViewModel(
            rollBlackjackDiceUseCase = RollBlackjackDiceUseCase(
                TestDiceRoller(ArrayDeque(listOf(9, 7, 6, 10, 2)))
            ),
            calculateBlackjackScoreUseCase = CalculateBlackjackScoreUseCase(),
            determineBlackjackOutcomeUseCase = DetermineBlackjackOutcomeUseCase(),
            selectBlackjackRewardCardUseCase = SelectBlackjackRewardCardUseCase(TestRandomProvider()),
            dispatcher = mainDispatcherRule.dispatcher,
            rollAnimationMs = 0L,
            tickMs = 1L,
            resultDelayMs = 0L,
            bustHighlightMs = 0L
        )

        viewModel.onEvent(BlackjackGameUiEvent.StartGame)
        advanceUntilIdle()

        viewModel.onEvent(BlackjackGameUiEvent.Stand)
        advanceUntilIdle()

        val state = viewModel.uiState.value
        assertTrue(state.isComplete)
        assertEquals(BlackjackOutcome.PLAYER_LOSE, state.result)
    }

    @Test
    fun `blackjack win grants retry card`() = runTest {
        val viewModel = BlackjackGameViewModel(
            rollBlackjackDiceUseCase = RollBlackjackDiceUseCase(
                TestDiceRoller(ArrayDeque(listOf(10, 1, 8, 9)))
            ),
            calculateBlackjackScoreUseCase = CalculateBlackjackScoreUseCase(),
            determineBlackjackOutcomeUseCase = DetermineBlackjackOutcomeUseCase(),
            selectBlackjackRewardCardUseCase = SelectBlackjackRewardCardUseCase(TestRandomProvider()),
            dispatcher = mainDispatcherRule.dispatcher,
            rollAnimationMs = 0L,
            tickMs = 1L,
            resultDelayMs = 0L,
            bustHighlightMs = 0L
        )

        viewModel.onEvent(BlackjackGameUiEvent.StartGame)
        advanceUntilIdle()

        viewModel.onEvent(BlackjackGameUiEvent.Stand)
        advanceUntilIdle()

        val state = viewModel.uiState.value
        assertEquals(BlackjackOutcome.PLAYER_WIN, state.result)
        assertNotNull(state.rewardCard)
        assertEquals(CardId.RETRY, state.rewardCard?.id)
    }
}

private class TestDiceRoller(
    private val values: ArrayDeque<Int>
) : DiceRoller {
    override fun roll(range: IntRange): Int = values.removeFirst()
}

private class TestRandomProvider : BlackjackRandomProvider {
    override fun nextInt(bound: Int): Int = 0
}

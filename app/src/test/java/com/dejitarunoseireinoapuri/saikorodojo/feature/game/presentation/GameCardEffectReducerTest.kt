package com.dejitarunoseireinoapuri.saikorodojo.feature.game.presentation

import com.dejitarunoseireinoapuri.saikorodojo.feature.cards.domain.CardId
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class GameCardEffectReducerTest {
    @Test
    fun `reduce sets awaiting reroll selected mode and clears per-mode selections`() {
        val state = GameUiState(
            interactionMode = DiceInteractionMode.AwaitingSetValue,
            selectedRerollSingleDieIndex = 1,
            selectedFlipDieIndex = 2,
            selectedAdjustmentDieIndex = 3,
            selectedSetValueDieIndex = 4,
            selectedRerollDice = setOf(0, 1),
            selectedCardIndex = 0
        )

        val reduced = GameCardEffectReducer.reduce(
            state = state,
            cardId = CardId.REROLL_ALL,
            minigamesRewardAmount = 3
        )

        assertTrue(reduced.interactionMode is DiceInteractionMode.AwaitingRerollSelected)
        assertEquals(emptySet<Int>(), reduced.selectedRerollDice)
        assertEquals(null, reduced.selectedRerollSingleDieIndex)
        assertEquals(null, reduced.selectedFlipDieIndex)
        assertEquals(null, reduced.selectedAdjustmentDieIndex)
        assertEquals(null, reduced.selectedSetValueDieIndex)
        assertEquals(null, reduced.selectedCardIndex)
    }

    @Test
    fun `reduce minigames card increments minigames and keeps normal mode`() {
        val state = GameUiState(
            minigamesAvailable = 2,
            interactionMode = DiceInteractionMode.Normal,
            selectedCardIndex = 0
        )

        val reduced = GameCardEffectReducer.reduce(
            state = state,
            cardId = CardId.MINIGAMES,
            minigamesRewardAmount = 3
        )

        assertEquals(5, reduced.minigamesAvailable)
        assertTrue(reduced.interactionMode is DiceInteractionMode.Normal)
        assertEquals(null, reduced.selectedCardIndex)
    }

    @Test
    fun `reduce minigames card caps minigames to maximum`() {
        val state = GameUiState(
            minigamesAvailable = 98,
            interactionMode = DiceInteractionMode.Normal,
            selectedCardIndex = 0
        )

        val reduced = GameCardEffectReducer.reduce(
            state = state,
            cardId = CardId.MINIGAMES,
            minigamesRewardAmount = 3
        )

        assertEquals(MAX_MINIGAMES_AVAILABLE, reduced.minigamesAvailable)
    }

}

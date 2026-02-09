package com.dejitarunoseireinoapuri.saikorodojo.feature.session.domain

import com.dejitarunoseireinoapuri.saikorodojo.feature.cards.domain.CardId
import com.dejitarunoseireinoapuri.saikorodojo.feature.game.domain.DiceType
import org.junit.Assert.assertEquals
import org.junit.Test

class MinigameInterstitialTrackerTest {
    @Test
    fun `registering minigame completion queues interstitial when reaching threshold`() {
        val snapshot = buildSnapshot(
            minigamesPlayedSinceInterstitial = 6,
            pendingInterstitialAds = 0
        )

        val updated = snapshot.registerMinigameCompletion()

        assertEquals(0, updated.minigamesPlayedSinceInterstitial)
        assertEquals(1, updated.pendingInterstitialAds)
    }

    @Test
    fun `registering minigame completion accumulates pending ads`() {
        val snapshot = buildSnapshot(
            minigamesPlayedSinceInterstitial = 6,
            pendingInterstitialAds = 1
        )

        val updated = snapshot.registerMinigameCompletion()

        assertEquals(0, updated.minigamesPlayedSinceInterstitial)
        assertEquals(2, updated.pendingInterstitialAds)
    }

    private fun buildSnapshot(
        minigamesPlayedSinceInterstitial: Int,
        pendingInterstitialAds: Int
    ): GameUiSnapshot {
        return GameUiSnapshot(
            diceValues = listOf(1, 1),
            diceCount = 2,
            diceType = DiceType.D6,
            diceTypes = listOf(DiceType.D6, DiceType.D6),
            layoutSeed = 0L,
            isRolling = false,
            isAwaitingRerollSingle = false,
            isAwaitingRerollSelected = false,
            isAwaitingFlipFace = false,
            isAwaitingAdjustPlusMinus = false,
            isAwaitingSetValue = false,
            selectedDice = emptySet(),
            selectedRerollDice = emptySet(),
            selectedRerollSingleDieIndex = null,
            selectedFlipDieIndex = null,
            selectedAdjustmentDieIndex = null,
            selectedSetValueDieIndex = null,
            selectedDiceSum = 0,
            shouldShowSelectedSum = false,
            cardCounts = mapOf(CardId.REROLL_ALL to 1),
            selectedCardIndex = null,
            lastAppliedCardId = null,
            levelNumber = 1,
            isLevelComplete = false,
            showLevelCompleteMessage = false,
            minigamesAvailable = 1,
            minigamesPlayedSinceInterstitial = minigamesPlayedSinceInterstitial,
            pendingInterstitialAds = pendingInterstitialAds
        )
    }
}

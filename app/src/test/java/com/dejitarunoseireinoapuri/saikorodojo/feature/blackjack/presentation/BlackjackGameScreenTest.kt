package com.dejitarunoseireinoapuri.saikorodojo.feature.blackjack.presentation

import com.dejitarunoseireinoapuri.saikorodojo.R
import com.dejitarunoseireinoapuri.saikorodojo.feature.cards.domain.CardId
import com.dejitarunoseireinoapuri.saikorodojo.feature.cards.presentation.CardUiModel
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class BlackjackGameScreenTest {
    @Test
    fun shouldShowMatsWhenStartedWithoutRewardAndNoPostLossSummary() {
        assertTrue(
            shouldShowMats(
                isStarted = true,
                rewardCard = null,
                showPostLossSummary = false
            )
        )
    }

    @Test
    fun shouldHideMatsWhenPostLossSummaryIsVisible() {
        assertFalse(
            shouldShowMats(
                isStarted = true,
                rewardCard = null,
                showPostLossSummary = true
            )
        )
    }

    @Test
    fun shouldHideMatsWhenRewardCardExists() {
        assertFalse(
            shouldShowMats(
                isStarted = true,
                rewardCard = sampleRewardCard(),
                showPostLossSummary = false
            )
        )
    }

    @Test
    fun shouldHideMatsWhenNotStarted() {
        assertFalse(
            shouldShowMats(
                isStarted = false,
                rewardCard = null,
                showPostLossSummary = false
            )
        )
    }

    private fun sampleRewardCard(): CardUiModel {
        return CardUiModel(
            id = CardId.ADJUST_PLUS_MINUS_ONE,
            titleRes = R.string.card_adjust_plus_minus_one_title,
            descriptionRes = R.string.card_adjust_plus_minus_one_description,
            iconRes = R.drawable.ic_card_plus
        )
    }
}

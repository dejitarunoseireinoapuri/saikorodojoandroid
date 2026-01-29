package com.dejitarunoseireinoapuri.saikorodojo.feature.blackjack.domain

enum class BlackjackOutcome {
    PLAYER_WIN,
    PLAYER_LOSE
}

class DetermineBlackjackOutcomeUseCase {
    fun execute(
        playerTotal: Int,
        dealerTotal: Int,
        isPlayerBust: Boolean,
        isDealerBust: Boolean
    ): BlackjackOutcome {
        if (isPlayerBust) return BlackjackOutcome.PLAYER_LOSE
        if (isDealerBust) return BlackjackOutcome.PLAYER_WIN
        return if (dealerTotal >= playerTotal) {
            BlackjackOutcome.PLAYER_LOSE
        } else {
            BlackjackOutcome.PLAYER_WIN
        }
    }
}

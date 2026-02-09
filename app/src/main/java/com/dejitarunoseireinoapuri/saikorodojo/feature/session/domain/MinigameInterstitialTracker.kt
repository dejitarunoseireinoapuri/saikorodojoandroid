package com.dejitarunoseireinoapuri.saikorodojo.feature.session.domain

internal const val MINIGAMES_PER_INTERSTITIAL = 5

fun MainGameSnapshot.registerMinigameCompletion(): MainGameSnapshot {
    return copy(uiSnapshot = uiSnapshot.registerMinigameCompletion())
}

fun GameUiSnapshot.registerMinigameCompletion(): GameUiSnapshot {
    val updatedPlayed = minigamesPlayedSinceInterstitial + 1
    val additionalAds = updatedPlayed / MINIGAMES_PER_INTERSTITIAL
    val remaining = updatedPlayed % MINIGAMES_PER_INTERSTITIAL
    return copy(
        minigamesPlayedSinceInterstitial = remaining,
        pendingInterstitialAds = pendingInterstitialAds + additionalAds
    )
}

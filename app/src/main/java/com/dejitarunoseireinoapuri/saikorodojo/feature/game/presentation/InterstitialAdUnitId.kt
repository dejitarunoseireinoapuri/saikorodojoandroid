package com.dejitarunoseireinoapuri.saikorodojo.feature.game.presentation

import com.dejitarunoseireinoapuri.saikorodojo.BuildConfig

internal const val TEST_INTERSTITIAL_AD_UNIT_ID = "ca-app-pub-3940256099942544/1033173712"

internal fun interstitialAdUnitId(): String {
    return BuildConfig.ADMOB_INTERS.ifBlank { TEST_INTERSTITIAL_AD_UNIT_ID }
}

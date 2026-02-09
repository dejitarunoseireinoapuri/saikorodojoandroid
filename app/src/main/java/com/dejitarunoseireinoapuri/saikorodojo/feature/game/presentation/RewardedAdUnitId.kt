package com.dejitarunoseireinoapuri.saikorodojo.feature.game.presentation

import com.dejitarunoseireinoapuri.saikorodojo.BuildConfig

internal const val TEST_REWARDED_AD_UNIT_ID = "ca-app-pub-3940256099942544/5224354917"

internal fun rewardedAdUnitId(): String {
    return BuildConfig.ADMOB_REWARDED_UNIT_ID.ifBlank { TEST_REWARDED_AD_UNIT_ID }
}

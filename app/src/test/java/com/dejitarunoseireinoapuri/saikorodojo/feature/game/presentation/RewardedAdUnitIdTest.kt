package com.dejitarunoseireinoapuri.saikorodojo.feature.game.presentation

import com.dejitarunoseireinoapuri.saikorodojo.BuildConfig
import org.junit.Assert.assertEquals
import org.junit.Test

class RewardedAdUnitIdTest {
    @Test
    fun `uses BuildConfig rewarded id when available or falls back to test id`() {
        val expected = if (BuildConfig.ADMOB_REWARDED_UNIT_ID.isBlank()) {
            TEST_REWARDED_AD_UNIT_ID
        } else {
            BuildConfig.ADMOB_REWARDED_UNIT_ID
        }

        assertEquals(expected, rewardedAdUnitId())
    }
}

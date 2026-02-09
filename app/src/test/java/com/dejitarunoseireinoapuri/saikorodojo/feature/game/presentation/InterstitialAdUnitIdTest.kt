package com.dejitarunoseireinoapuri.saikorodojo.feature.game.presentation

import com.dejitarunoseireinoapuri.saikorodojo.BuildConfig
import org.junit.Assert.assertEquals
import org.junit.Test

class InterstitialAdUnitIdTest {
    @Test
    fun `uses BuildConfig interstitial id when available or falls back to test id`() {
        val expected = if (BuildConfig.ADMOB_INTERS.isBlank()) {
            TEST_INTERSTITIAL_AD_UNIT_ID
        } else {
            BuildConfig.ADMOB_INTERS
        }

        assertEquals(expected, interstitialAdUnitId())
    }
}

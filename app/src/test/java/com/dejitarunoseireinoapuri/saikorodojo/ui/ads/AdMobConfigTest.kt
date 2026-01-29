package com.dejitarunoseireinoapuri.saikorodojo.ui.ads

import kotlin.test.Test
import kotlin.test.assertEquals

class AdMobConfigTest {
    @Test
    fun bannerAdUnitIdUsesTestValue() {
        assertEquals(
            "ca-app-pub-3940256099942544/6300978111",
            AdMobConfig.TestBannerAdUnitId
        )
    }

    @Test
    fun adWidthDpUsesConfigurationValue() {
        assertEquals(360, adWidthDp(screenWidthDp = 360))
    }

    @Test
    fun adWidthDpClampsToAtLeastOne() {
        assertEquals(1, adWidthDp(screenWidthDp = 0))
    }

    @Test
    fun bannerContentPaddingClampsToZero() {
        assertEquals(0, bannerContentPaddingDp(adHeightDp = -8))
    }

    @Test
    fun bannerContentPaddingKeepsPositiveValue() {
        assertEquals(50, bannerContentPaddingDp(adHeightDp = 50))
    }
}

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
}

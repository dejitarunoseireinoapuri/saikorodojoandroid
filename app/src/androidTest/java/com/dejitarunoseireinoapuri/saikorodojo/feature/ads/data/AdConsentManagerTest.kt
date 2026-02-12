package com.dejitarunoseireinoapuri.saikorodojo.feature.ads.data

import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class AdConsentManagerTest {
    private val context = ApplicationProvider.getApplicationContext<android.content.Context>()

    @Before
    fun setUp() {
        context.getSharedPreferences(PREFERENCES_NAME, android.content.Context.MODE_PRIVATE)
            .edit()
            .clear()
            .apply()
    }

    @Test
    fun hasShownInitialAdsNotice_returnsFalseByDefault() {
        assertFalse(AdConsentManager.hasShownInitialAdsNotice(context))
    }

    @Test
    fun markInitialAdsNoticeShown_persistsShownState() {
        AdConsentManager.markInitialAdsNoticeShown(context)

        assertTrue(AdConsentManager.hasShownInitialAdsNotice(context))
    }

    private companion object {
        const val PREFERENCES_NAME = "ad_consent_preferences"
    }
}

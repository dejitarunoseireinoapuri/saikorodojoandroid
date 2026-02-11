package com.dejitarunoseireinoapuri.saikorodojo.feature.ads.data

import android.app.Activity
import android.content.Context
import com.google.android.ump.ConsentInformation
import com.google.android.ump.ConsentRequestParameters
import com.google.android.ump.FormError
import com.google.android.ump.UserMessagingPlatform

object AdConsentManager {
    @Volatile
    private var consentInformation: ConsentInformation? = null

    fun initialize(context: Context) {
        if (consentInformation == null) {
            consentInformation = UserMessagingPlatform.getConsentInformation(context.applicationContext)
        }
    }

    fun requestConsentInfoUpdate(activity: Activity) {
        val info = getConsentInformation(activity)
        val requestParameters = ConsentRequestParameters.Builder().build()
        info.requestConsentInfoUpdate(
            activity,
            requestParameters,
            {},
            {}
        )
    }

    fun shouldShowConsentFormBeforePlay(context: Context): Boolean {
        return !getConsentInformation(context).canRequestAds()
    }

    fun showConsentFormIfRequired(activity: Activity, onFinished: () -> Unit) {
        UserMessagingPlatform.loadAndShowConsentFormIfRequired(activity) {
            onFinished()
        }
    }

    fun showPrivacyOptionsForm(activity: Activity, onFinished: (FormError?) -> Unit) {
        val info = getConsentInformation(activity)
        if (info.privacyOptionsRequirementStatus == ConsentInformation.PrivacyOptionsRequirementStatus.REQUIRED) {
            UserMessagingPlatform.showPrivacyOptionsForm(activity, onFinished)
        } else {
            onFinished(null)
        }
    }

    private fun getConsentInformation(context: Context): ConsentInformation {
        val cached = consentInformation
        if (cached != null) {
            return cached
        }
        return UserMessagingPlatform.getConsentInformation(context.applicationContext).also {
            consentInformation = it
        }
    }
}

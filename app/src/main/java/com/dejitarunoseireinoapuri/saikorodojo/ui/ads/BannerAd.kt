package com.dejitarunoseireinoapuri.saikorodojo.ui.ads

import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.viewinterop.AndroidView
import com.google.android.gms.ads.AdRequest
import com.google.android.gms.ads.AdSize
import com.google.android.gms.ads.AdView

@Composable
fun BannerAd(
    modifier: Modifier = Modifier,
    adUnitId: String = AdMobConfig.TestBannerAdUnitId,
    adSize: AdSize = rememberBannerAdSize()
) {
    val context = LocalContext.current
    val adView = remember(context, adUnitId) {
        AdView(context).apply {
            this.adUnitId = adUnitId
        }
    }
    val adRequest = remember { AdRequest.Builder().build() }

    DisposableEffect(adView) {
        onDispose {
            adView.destroy()
        }
    }

    LaunchedEffect(adView, adSize, adRequest) {
        adView.setAdSize(adSize)
        adView.loadAd(adRequest)
    }

    AndroidView(
        modifier = modifier,
        factory = { adView }
    )
}

@Composable
fun rememberBannerAdSize(): AdSize {
    val context = LocalContext.current
    val configuration = LocalConfiguration.current
    val adWidthDp = remember(configuration.screenWidthDp) {
        adWidthDp(configuration.screenWidthDp)
    }
    return remember(adWidthDp, context) {
        AdSize.getCurrentOrientationAnchoredAdaptiveBannerAdSize(context, adWidthDp)
    }
}

internal fun adWidthDp(screenWidthDp: Int): Int {
    return screenWidthDp.coerceAtLeast(1)
}

internal fun bannerContentPaddingDp(adHeightDp: Int): Int {
    return adHeightDp.coerceAtLeast(0)
}

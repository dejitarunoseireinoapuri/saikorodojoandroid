package com.dejitarunoseireinoapuri.saikorodojo.ui.ads

import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.remember
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.viewinterop.AndroidView
import com.google.android.gms.ads.AdRequest
import com.google.android.gms.ads.AdSize
import com.google.android.gms.ads.AdView
import kotlin.math.roundToInt

@Composable
fun BannerAd(
    modifier: Modifier = Modifier,
    adUnitId: String = AdMobConfig.TestBannerAdUnitId
) {
    val context = LocalContext.current
    val configuration = LocalConfiguration.current
    val density = LocalDensity.current
    val adView = remember(context, adUnitId) {
        AdView(context).apply {
            this.adUnitId = adUnitId
        }
    }
    val adRequest = remember { AdRequest.Builder().build() }
    val adWidthPx = remember(configuration.screenWidthDp, density) {
        adWidthPx(configuration.screenWidthDp, density.density)
    }
    val adSize = remember(adWidthPx, context) {
        AdSize.getCurrentOrientationAnchoredAdaptiveBannerAdSize(context, adWidthPx)
    }

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

internal fun adWidthPx(screenWidthDp: Int, density: Float): Int {
    return (screenWidthDp * density).roundToInt()
}

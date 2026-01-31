package com.dejitarunoseireinoapuri.saikorodojo.feature.cards.presentation

import androidx.activity.ComponentActivity
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.toPixelMap
import androidx.compose.ui.test.captureToImage
import androidx.compose.ui.test.junit4.createAndroidComposeRule
import androidx.compose.ui.test.onNodeWithText
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.dejitarunoseireinoapuri.saikorodojo.R
import com.dejitarunoseireinoapuri.saikorodojo.ui.theme.AppSecondary
import com.dejitarunoseireinoapuri.saikorodojo.ui.theme.SaikoroDojoTheme
import kotlin.math.abs
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.junit.Assert.assertTrue

@RunWith(AndroidJUnit4::class)
class CardItemTest {

    @get:Rule
    val composeTestRule = createAndroidComposeRule<ComponentActivity>()

    @Test
    fun actionButtonUsesSecondaryColor() {
        val card = defaultCardUiModels().first()
        val actionLabel = composeTestRule.activity.getString(R.string.apply)

        composeTestRule.setContent {
            SaikoroDojoTheme {
                CardItem(card = card, onApplyClick = {})
            }
        }

        val image = composeTestRule.onNodeWithText(actionLabel).captureToImage()
        val pixelMap = image.toPixelMap()
        val sampleX = (pixelMap.width * 0.1f).toInt().coerceAtLeast(0)
        val sampleY = (pixelMap.height * 0.5f).toInt().coerceAtLeast(0)
        val sampledColor = pixelMap[sampleX, sampleY]

        assertColorApproximatelyEquals(AppSecondary, sampledColor)
    }

    private fun assertColorApproximatelyEquals(
        expected: Color,
        actual: Color,
        tolerance: Float = 0.08f
    ) {
        val matches = abs(expected.red - actual.red) <= tolerance &&
            abs(expected.green - actual.green) <= tolerance &&
            abs(expected.blue - actual.blue) <= tolerance
        assertTrue("Expected $expected but was $actual", matches)
    }
}

package com.dejitarunoseireinoapuri.saikorodojo.feature.dice.presentation

import androidx.activity.ComponentActivity
import androidx.compose.foundation.layout.Row
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.assertTextEquals
import androidx.compose.ui.test.captureToImage
import androidx.compose.ui.graphics.toPixelMap
import androidx.compose.ui.test.junit4.createAndroidComposeRule
import androidx.compose.ui.test.onNodeWithContentDescription
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.unit.dp
import com.dejitarunoseireinoapuri.saikorodojo.R
import com.dejitarunoseireinoapuri.saikorodojo.feature.game.domain.DiceType
import com.dejitarunoseireinoapuri.saikorodojo.ui.theme.SaikoroDojoTheme
import com.dejitarunoseireinoapuri.saikorodojo.ui.theme.SoftGold
import kotlin.math.abs
import org.junit.Rule
import org.junit.Test
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue

class RealisticDieTest {
    @get:Rule
    val compose = createAndroidComposeRule<ComponentActivity>()

    @Test
    fun everyDieShowsAGoldResultNumberOnlyAsTheRollFinishes() {
        val progress = mutableFloatStateOf(0.6f)
        compose.setContent {
            SaikoroDojoTheme {
                Row {
                    DiceType.entries.forEach { type ->
                        RealisticDie(
                            value = type.sides, type = type, size = 96.dp,
                            motion = progress, modifier = Modifier.testTag(type.name)
                        )
                    }
                }
            }
        }
        fun goldPixels(type: DiceType): Int {
            val pixels = compose.onNodeWithTag(type.name).captureToImage().toPixelMap()
            var count = 0
            for (y in 0 until pixels.height) {
                for (x in 0 until pixels.width) {
                    val pixel = pixels[x, y]
                    if (abs(pixel.red - SoftGold.red) < 0.08f &&
                        abs(pixel.green - SoftGold.green) < 0.08f &&
                        abs(pixel.blue - SoftGold.blue) < 0.08f) count++
                }
            }
            return count
        }
        DiceType.entries.forEach { assertEquals(0, goldPixels(it)) }
        compose.runOnIdle { progress.floatValue = 1f }
        DiceType.entries.forEach { assertTrue("Missing gold result number on $it", goldPixels(it) > 5) }
    }

    @Test
    fun theLastAnimatedFrameAlreadyMatchesTheRestingDie() {
        val animated = mutableStateOf(true)
        val progress = mutableFloatStateOf(1f)
        compose.setContent {
            SaikoroDojoTheme {
                RealisticDie(
                    value = 6, type = DiceType.D6, size = 96.dp,
                    motion = if (animated.value) progress else null, modifier = Modifier.testTag("solid")
                )
            }
        }
        val before = compose.onNodeWithTag("solid").captureToImage().toPixelMap()
        compose.runOnIdle { animated.value = false }
        val after = compose.onNodeWithTag("solid").captureToImage().toPixelMap()
        assertEquals(before.width, after.width)
        assertEquals(before.height, after.height)
        for (y in 0 until before.height) {
            for (x in 0 until before.width) assertEquals(before[x, y], after[x, y])
        }
    }

    @Test
    fun rotationRevealsDifferentFacesEvenWhenTheResultStaysTheSame() {
        val progress = mutableFloatStateOf(0.2f)
        compose.setContent {
            SaikoroDojoTheme {
                RealisticDie(
                    value = 6, type = DiceType.D6, size = 96.dp,
                    motion = progress, modifier = Modifier.testTag("solid")
                )
            }
        }
        val before = compose.onNodeWithTag("solid").captureToImage().toPixelMap()
        compose.runOnIdle { progress.floatValue = 0.65f }
        val after = compose.onNodeWithTag("solid").captureToImage().toPixelMap()
        var changedPixels = 0
        for (y in 0 until before.height) {
            for (x in 0 until before.width) {
                if (before[x, y] != after[x, y]) changedPixels++
            }
        }
        assertTrue(changedPixels > before.width * before.height / 10)
    }

    @Test
    fun allDiceTypesKeepTheirNumericResultsAndAccessibilityLabels() {
        compose.setContent {
            SaikoroDojoTheme {
                Row {
                    DiceType.entries.forEach { type ->
                        RealisticDie(
                            value = type.sides,
                            type = type,
                            size = 80.dp,
                            numberModifier = Modifier.testTag(type.name)
                        )
                    }
                }
            }
        }
        DiceType.entries.forEach { type ->
            compose.onNodeWithTag(type.name, useUnmergedTree = true)
                .assertTextEquals(type.sides.toString()).assertIsDisplayed()
            compose.onNodeWithContentDescription(
                compose.activity.getString(R.string.cd_dice_face, type.sides)
            ).assertIsDisplayed()
        }
    }

    @Test
    fun settlingDisplaysTheActualResultIncludingConsecutiveEqualResults() {
        val progress = mutableFloatStateOf(0.3f)
        val value = mutableIntStateOf(2)
        compose.setContent {
            SaikoroDojoTheme {
                RealisticDie(
                    value = value.intValue,
                    type = DiceType.D6,
                    size = 96.dp,
                    motion = progress,
                    numberModifier = Modifier.testTag("result")
                )
            }
        }
        compose.runOnIdle {
            value.intValue = 6
            progress.floatValue = 1f
        }
        compose.onNodeWithTag("result", useUnmergedTree = true).assertTextEquals("6").assertIsDisplayed()
        compose.runOnIdle { progress.floatValue = 0.2f }
        compose.runOnIdle { progress.floatValue = 1f }
        compose.onNodeWithTag("result", useUnmergedTree = true).assertTextEquals("6").assertIsDisplayed()
    }
}

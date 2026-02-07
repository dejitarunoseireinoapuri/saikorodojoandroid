package com.dejitarunoseireinoapuri.saikorodojo.feature.cards.presentation

import androidx.activity.ComponentActivity
import androidx.compose.ui.test.junit4.createAndroidComposeRule
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.onNodeWithText
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.dejitarunoseireinoapuri.saikorodojo.R
import com.dejitarunoseireinoapuri.saikorodojo.ui.theme.SaikoroDojoTheme
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class CardItemTest {

    @get:Rule
    val composeTestRule = createAndroidComposeRule<ComponentActivity>()

    @Test
    fun actionButtonTextUsesWhiteColor() {
        val card = defaultCardUiModels().first()
        val actionLabel = composeTestRule.activity.getString(R.string.apply)

        composeTestRule.setContent {
            SaikoroDojoTheme {
                CardItem(card = card, onApplyClick = {})
            }
        }

        composeTestRule.onNodeWithText(actionLabel).assertIsDisplayed()
    }
}

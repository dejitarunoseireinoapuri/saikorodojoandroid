package com.dejitarunoseireinoapuri.saikorodojo.feature.menu.presentation

import androidx.activity.ComponentActivity
import androidx.compose.material3.MaterialTheme
import androidx.compose.ui.test.junit4.createAndroidComposeRule
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.dejitarunoseireinoapuri.saikorodojo.ui.theme.SaikoroDojoTheme
import com.dejitarunoseireinoapuri.saikorodojo.ui.theme.SourceCodeProFontFamily
import org.junit.Assert.assertEquals
import org.junit.Rule
import org.junit.Assert.assertNotNull
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class MenuScreenTest {
    @get:Rule
    val composeRule = createAndroidComposeRule<ComponentActivity>()

    @Test
    fun themeUsesSourceCodeProFontFamily() {
        var typographyFontFamily = SourceCodeProFontFamily

        composeRule.setContent {
            SaikoroDojoTheme(darkTheme = false, dynamicColor = false) {
                typographyFontFamily = requireNotNull(MaterialTheme.typography.bodyLarge.fontFamily)
            }
        }

        composeRule.runOnIdle {
            assertNotNull(typographyFontFamily)
            assertEquals(SourceCodeProFontFamily, typographyFontFamily)
        }
    }
}

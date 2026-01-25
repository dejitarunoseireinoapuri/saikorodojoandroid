package com.dejitarunoseireinoapuri.saikorodojo.ui.theme

import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.sp
import org.junit.Assert.assertEquals
import org.junit.Test

class TypographyTest {
    @Test
    fun titleLargeAndLabelLargeUseSourceCodeProFontFamily() {
        val appTypography = Typography

        assertEquals(SourceCodeProFontFamily, appTypography.titleLarge.fontFamily)
        assertEquals(FontWeight.Bold, appTypography.titleLarge.fontWeight)
        assertEquals(28.sp, appTypography.titleLarge.fontSize)

        assertEquals(SourceCodeProFontFamily, appTypography.labelLarge.fontFamily)
        assertEquals(FontWeight.Medium, appTypography.labelLarge.fontWeight)
        assertEquals(18.sp, appTypography.labelLarge.fontSize)
    }
}

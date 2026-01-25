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
        assertEquals(FontWeight.Medium, appTypography.titleLarge.fontWeight)
        assertEquals(28.sp, appTypography.titleLarge.fontSize)

        assertEquals(SourceCodeProFontFamily, appTypography.titleMedium.fontFamily)
        assertEquals(FontWeight.Bold, appTypography.titleMedium.fontWeight)
        assertEquals(18.sp, appTypography.titleMedium.fontSize)

        assertEquals(SourceCodeProFontFamily, appTypography.labelLarge.fontFamily)
        assertEquals(FontWeight.Medium, appTypography.labelLarge.fontWeight)
        assertEquals(18.sp, appTypography.labelLarge.fontSize)

        assertEquals(SourceCodeProFontFamily, appTypography.bodyMedium.fontFamily)
        assertEquals(FontWeight.Normal, appTypography.bodyMedium.fontWeight)
        assertEquals(14.sp, appTypography.bodyMedium.fontSize)
    }
}

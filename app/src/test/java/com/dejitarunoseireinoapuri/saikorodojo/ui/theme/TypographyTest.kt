package com.dejitarunoseireinoapuri.saikorodojo.ui.theme

import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.sp
import org.junit.Assert.assertEquals
import org.junit.Test

class TypographyTest {
    @Test
    fun titleLargeAndLabelLargeUseOxaniumFontFamily() {
        val appTypography = Typography

        assertEquals(OxaniunFontFamily, appTypography.titleLarge.fontFamily)
        assertEquals(FontWeight.SemiBold, appTypography.titleLarge.fontWeight)
        assertEquals(24.sp, appTypography.titleLarge.fontSize)

        assertEquals(OxaniunFontFamily, appTypography.titleMedium.fontFamily)
        assertEquals(FontWeight.SemiBold, appTypography.titleMedium.fontWeight)
        assertEquals(18.sp, appTypography.titleMedium.fontSize)

        assertEquals(OxaniunFontFamily, appTypography.labelLarge.fontFamily)
        assertEquals(FontWeight.Medium, appTypography.labelLarge.fontWeight)
        assertEquals(16.sp, appTypography.labelLarge.fontSize)

        assertEquals(OxaniunFontFamily, appTypography.bodyMedium.fontFamily)
        assertEquals(FontWeight.Normal, appTypography.bodyMedium.fontWeight)
        assertEquals(12.sp, appTypography.bodyMedium.fontSize)
    }
}

package com.dejitarunoseireinoapuri.saikorodojo.ui.theme

import androidx.compose.material3.Typography
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.Font
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.sp
import com.dejitarunoseireinoapuri.saikorodojo.R

val SourceCodeProFontFamily = FontFamily(
    Font(R.font.sourcecodepro_variablefont_wght, FontWeight.Normal),
    Font(R.font.sourcecodepro_bold, FontWeight.Bold)
)

val OxaniunFontFamily = FontFamily(
    Font(R.font.oxanium_variable_font_wght, FontWeight.Normal),
    Font(R.font.oxanium_bold, FontWeight.Bold)
)

val Typography = Typography(
    displaySmall = TextStyle(
        fontFamily = SourceCodeProFontFamily,
        fontWeight = FontWeight.Bold,
        fontSize = 28.sp,
        lineHeight = 32.sp
    ),
    titleLarge = TextStyle(
        fontFamily = OxaniunFontFamily,
        fontWeight = FontWeight.SemiBold,
        fontSize = 24.sp,
        lineHeight = 28.sp
    ),
    titleMedium = TextStyle(
        fontFamily = OxaniunFontFamily,
        fontWeight = FontWeight.SemiBold,
        fontSize = 18.sp,
        lineHeight = 22.sp
    ),
    labelLarge = TextStyle(
        fontFamily = OxaniunFontFamily,
        fontWeight = FontWeight.Medium,
        fontSize = 16.sp,
        lineHeight = 20.sp,
        letterSpacing = 0.2.sp
    ),
    bodyLarge = TextStyle(
        fontFamily = OxaniunFontFamily,
        fontWeight = FontWeight.Normal,
        fontSize = 16.sp,
        lineHeight = 20.sp,
        letterSpacing = 0.15.sp
    ),
    bodyMedium = TextStyle(
        fontFamily = OxaniunFontFamily,
        fontWeight = FontWeight.Normal,
        fontSize = 12.sp,
        lineHeight = 18.sp,
        letterSpacing = 0.1.sp
    )
)


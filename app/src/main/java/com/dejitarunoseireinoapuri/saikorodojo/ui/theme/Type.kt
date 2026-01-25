package com.dejitarunoseireinoapuri.saikorodojo.ui.theme

import androidx.compose.material3.Typography
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.Font
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.sp
import com.dejitarunoseireinoapuri.saikorodojo.R

val SourceCodeProFontFamily = FontFamily(Font(R.font.sourcecodepro_variablefont_wght))

val Typography = Typography(
    defaultFontFamily = SourceCodeProFontFamily,
    bodyLarge = TextStyle(
        fontWeight = FontWeight.Normal,
        fontSize = 16.sp,
        lineHeight = 24.sp,
        letterSpacing = 0.5.sp
    )
)

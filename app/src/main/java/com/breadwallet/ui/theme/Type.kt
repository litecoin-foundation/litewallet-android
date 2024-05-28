package com.breadwallet.ui.theme

import androidx.compose.material3.Typography
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.Font
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.sp
import com.breadwallet.R

// Set of Material typography styles to start with
val Typography = Typography(
    bodyLarge = TextStyle(
        fontFamily = FontFamily(Font(R.font.barlowsemicondensed_light, FontWeight.Bold)),
    ),

    titleLarge = TextStyle(
        fontFamily = FontFamily(Font(R.font.barlowsemicondensed_semibold, FontWeight.SemiBold))
    ),
    titleMedium = TextStyle(
        fontFamily = FontFamily(Font(R.font.barlowsemicondensed_bold, FontWeight.Normal))
    ),
    titleSmall = TextStyle(
        fontFamily = FontFamily(Font(R.font.barlowsemicondensed_semibold))
    ),
    labelSmall = TextStyle(
        fontFamily = FontFamily.Default,
        fontWeight = FontWeight.Medium,
        fontSize = 11.sp,
        lineHeight = 16.sp,
        letterSpacing = 0.5.sp
    )

)

val barlowSemiCondensed_light = FontFamily(Font(R.font.barlowsemicondensed_light, FontWeight.Light))
val barlowSemiCondensed_semi_bold = FontFamily(Font(R.font.barlowsemicondensed_semibold, FontWeight.SemiBold))
val barlowSemiCondensed_bold = FontFamily(Font(R.font.barlowsemicondensed_bold, FontWeight.ExtraBold))
val barlowSemiCondensed_normal = FontFamily(Font(R.font.barlowsemicondensed_regular, FontWeight.Normal))

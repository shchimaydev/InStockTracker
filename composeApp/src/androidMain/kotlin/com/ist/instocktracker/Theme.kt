package com.ist.instocktracker

import androidx.compose.material3.Typography
import androidx.compose.runtime.Composable
import androidx.compose.ui.text.font.Font
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight


@Composable
fun InterFontFamily() = FontFamily(
    Font(R.font.inter_italic_variable, weight = FontWeight.Normal),
    Font(R.font.inter_italic_variable, weight = FontWeight.Medium),
    Font(R.font.inter_italic_variable, weight = FontWeight.Bold),
    Font(R.font.inter_italic_variable, weight = FontWeight.ExtraLight),
    Font(R.font.inter_italic_variable, weight = FontWeight.SemiBold)

)

@Composable
fun AppTypography(): Typography {
    val inter = InterFontFamily()
    // You can create a custom Typography based on your InterFontFamily
    return Typography(

        displayLarge = Typography().displayLarge.copy(fontFamily = inter),
        displayMedium = Typography().displayMedium.copy(fontFamily = inter),
        displaySmall = Typography().displaySmall.copy(fontFamily = inter),
        headlineLarge = Typography().headlineLarge.copy(fontFamily = inter),
        headlineMedium = Typography().headlineMedium.copy(fontFamily = inter),
        headlineSmall = Typography().headlineSmall.copy(fontFamily = inter),
        titleLarge = Typography().titleLarge.copy(fontFamily = inter),
        titleMedium = Typography().titleMedium.copy(fontFamily = inter),
        titleSmall = Typography().titleSmall.copy(fontFamily = inter),
        bodyLarge = Typography().bodyLarge.copy(fontFamily = inter),
        bodyMedium = Typography().bodyMedium.copy(fontFamily = inter),
        bodySmall = Typography().bodySmall.copy(fontFamily = inter),
        labelLarge = Typography().labelLarge.copy(fontFamily = inter),
        labelMedium = Typography().labelMedium.copy(fontFamily = inter),
        labelSmall = Typography().labelSmall.copy(fontFamily = inter),
    )
}

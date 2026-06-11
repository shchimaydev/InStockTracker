package com.ist.instocktracker

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.text.ExperimentalTextApi
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.*
import androidx.compose.ui.unit.sp


@OptIn(ExperimentalTextApi::class)
@Composable
fun InterFontFamily() = FontFamily(
    // Regular (upright) variants
    Font(
        R.font.inter_variable,
        weight = FontWeight.Thin,
        style = FontStyle.Normal,
        variationSettings = FontVariation.Settings(FontVariation.weight(100))
    ),
    Font(
        R.font.inter_variable,
        weight = FontWeight.ExtraLight,
        style = FontStyle.Normal,
        variationSettings = FontVariation.Settings(FontVariation.weight(200))
    ),
    Font(
        R.font.inter_variable,
        weight = FontWeight.Light,
        style = FontStyle.Normal,
        variationSettings = FontVariation.Settings(FontVariation.weight(300))
    ),
    Font(
        R.font.inter_variable,
        weight = FontWeight.Normal,
        style = FontStyle.Normal,
        variationSettings = FontVariation.Settings(FontVariation.weight(400))
    ),
    Font(
        R.font.inter_variable,
        weight = FontWeight.Medium,
        style = FontStyle.Normal,
        variationSettings = FontVariation.Settings(FontVariation.weight(500))
    ),
    Font(
        R.font.inter_variable,
        weight = FontWeight.SemiBold,
        style = FontStyle.Normal,
        variationSettings = FontVariation.Settings(FontVariation.weight(600))
    ),
    Font(
        R.font.inter_variable,
        weight = FontWeight.Bold,
        style = FontStyle.Normal,
        variationSettings = FontVariation.Settings(FontVariation.weight(700))
    ),
    Font(
        R.font.inter_variable,
        weight = FontWeight.ExtraBold,
        style = FontStyle.Normal,
        variationSettings = FontVariation.Settings(FontVariation.weight(800))
    ),
    Font(
        R.font.inter_variable,
        weight = FontWeight.Black,
        style = FontStyle.Normal,
        variationSettings = FontVariation.Settings(FontVariation.weight(900))
    ),

    // Italic variants
    Font(
        R.font.inter_italic_variable,
        weight = FontWeight.Thin,
        style = FontStyle.Italic,
        variationSettings = FontVariation.Settings(FontVariation.weight(100))
    ),
    Font(
        R.font.inter_italic_variable,
        weight = FontWeight.ExtraLight,
        style = FontStyle.Italic,
        variationSettings = FontVariation.Settings(FontVariation.weight(200))
    ),
    Font(
        R.font.inter_italic_variable,
        weight = FontWeight.Light,
        style = FontStyle.Italic,
        variationSettings = FontVariation.Settings(FontVariation.weight(300))
    ),
    Font(
        R.font.inter_italic_variable,
        weight = FontWeight.Normal,
        style = FontStyle.Italic,
        variationSettings = FontVariation.Settings(FontVariation.weight(400))
    ),
    Font(
        R.font.inter_italic_variable,
        weight = FontWeight.Medium,
        style = FontStyle.Italic,
        variationSettings = FontVariation.Settings(FontVariation.weight(500))
    ),
    Font(
        R.font.inter_italic_variable,
        weight = FontWeight.SemiBold,
        style = FontStyle.Italic,
        variationSettings = FontVariation.Settings(FontVariation.weight(600))
    ),
    Font(
        R.font.inter_italic_variable,
        weight = FontWeight.Bold,
        style = FontStyle.Italic,
        variationSettings = FontVariation.Settings(FontVariation.weight(700))
    ),
    Font(
        R.font.inter_italic_variable,
        weight = FontWeight.ExtraBold,
        style = FontStyle.Italic,
        variationSettings = FontVariation.Settings(FontVariation.weight(800))
    ),
    Font(
        R.font.inter_italic_variable,
        weight = FontWeight.Black,
        style = FontStyle.Italic,
        variationSettings = FontVariation.Settings(FontVariation.weight(900))
    )
)

/**
 * Type scale from the Figma "InStockTracker Design Kit" (Inter family).
 *
 * The design specifies these app roles (size / weight / line-height):
 *  - App Title / Drawer Header  18 / Bold / 31.5
 *  - Card Heading               16 / Bold / 28
 *  - List Item Title            15 / Bold / 26.25
 *  - Nav Item                   15 / Bold | Regular / 22.5
 *  - Body / Detail              14 / Regular / 21
 *  - Filter Tab                 13 / Bold | Regular / 22.75
 *  - Timestamp / Metadata       13 / Regular / 19.5
 *  - Chip / Badge               12 / Bold | Regular / 18
 *  - Section Label (uppercase)  12 / Regular / 19.92
 *
 * Material 3 roles are mapped to those (titles → headings, body → detail,
 * labels → tabs/chips/section labels). Display / headline roles are not in the
 * kit, so they are derived by scaling the same Bold Inter face.
 */
@Composable
fun AppTypography(): Typography {
    val inter = InterFontFamily()
    val base = Typography()
    return Typography(
        // Derived — not specified in the kit (largest authored size is the 32px H1).
        displayLarge = base.displayLarge.copy(fontFamily = inter, fontWeight = FontWeight.Bold),
        displayMedium = base.displayMedium.copy(fontFamily = inter, fontWeight = FontWeight.Bold),
        displaySmall = base.displaySmall.copy(
            fontFamily = inter, fontWeight = FontWeight.Bold,
            fontSize = 32.sp, lineHeight = 40.sp, letterSpacing = 0.sp
        ),
        headlineLarge = base.headlineLarge.copy(fontFamily = inter, fontWeight = FontWeight.Bold),
        headlineMedium = base.headlineMedium.copy(fontFamily = inter, fontWeight = FontWeight.Bold),
        headlineSmall = base.headlineSmall.copy(fontFamily = inter, fontWeight = FontWeight.Bold),

        // App Title / Drawer Header
        titleLarge = TextStyle(
            fontFamily = inter, fontWeight = FontWeight.Bold,
            fontSize = 18.sp, lineHeight = 24.sp, letterSpacing = 0.sp
        ),
        // Card Heading
        titleMedium = TextStyle(
            fontFamily = inter, fontWeight = FontWeight.Bold,
            fontSize = 16.sp, lineHeight = 22.sp, letterSpacing = 0.1.sp
        ),
        // List Item Title
        titleSmall = TextStyle(
            fontFamily = inter, fontWeight = FontWeight.Bold,
            fontSize = 15.sp, lineHeight = 20.sp, letterSpacing = 0.1.sp
        ),
        // Nav Item (inactive) / general emphasis body
        bodyLarge = TextStyle(
            fontFamily = inter, fontWeight = FontWeight.Normal,
            fontSize = 15.sp, lineHeight = 22.sp, letterSpacing = 0.15.sp
        ),
        // Body / Detail
        bodyMedium = TextStyle(
            fontFamily = inter, fontWeight = FontWeight.Normal,
            fontSize = 14.sp, lineHeight = 21.sp, letterSpacing = 0.2.sp
        ),
        // Timestamp / Metadata
        bodySmall = TextStyle(
            fontFamily = inter, fontWeight = FontWeight.Normal,
            fontSize = 13.sp, lineHeight = 18.sp, letterSpacing = 0.2.sp
        ),
        // Filter Tab (active) / Nav Item (active)
        labelLarge = TextStyle(
            fontFamily = inter, fontWeight = FontWeight.Bold,
            fontSize = 13.sp, lineHeight = 18.sp, letterSpacing = 0.1.sp
        ),
        // Chip / Badge
        labelMedium = TextStyle(
            fontFamily = inter, fontWeight = FontWeight.Bold,
            fontSize = 12.sp, lineHeight = 16.sp, letterSpacing = 0.4.sp
        ),
        // Section Label (uppercase)
        labelSmall = TextStyle(
            fontFamily = inter, fontWeight = FontWeight.Normal,
            fontSize = 12.sp, lineHeight = 16.sp, letterSpacing = 0.8.sp
        ),
    )
}

private val LightColorScheme = lightColorScheme(
    primary = LightPrimary,
    onPrimary = LightOnPrimary,
    primaryContainer = LightPrimaryContainer,
    onPrimaryContainer = LightOnPrimaryContainer,
    inversePrimary = LightInversePrimary,
    secondary = LightSecondary,
    onSecondary = LightOnSecondary,
    secondaryContainer = LightSecondaryContainer,
    onSecondaryContainer = LightOnSecondaryContainer,
    tertiary = LightTertiary,
    onTertiary = LightOnTertiary,
    tertiaryContainer = LightTertiaryContainer,
    onTertiaryContainer = LightOnTertiaryContainer,
    error = LightError,
    onError = LightOnError,
    errorContainer = LightErrorContainer,
    onErrorContainer = LightOnErrorContainer,
    background = LightBackground,
    onBackground = LightOnBackground,
    surface = LightSurface,
    onSurface = LightOnSurface,
    surfaceVariant = LightSurfaceVariant,
    onSurfaceVariant = LightOnSurfaceVariant,
    surfaceTint = LightPrimary,
    inverseSurface = LightInverseSurface,
    inverseOnSurface = LightInverseOnSurface,
    outline = LightOutline,
    outlineVariant = LightOutlineVariant,
    surfaceContainerLowest = LightSurfaceContainerLowest,
    surfaceContainerLow = LightSurfaceContainerLow,
    surfaceContainer = LightSurfaceContainer,
    surfaceContainerHigh = LightSurfaceContainerHigh,
    surfaceContainerHighest = LightSurfaceContainerHighest,
)

private val DarkColorScheme = darkColorScheme(
    primary = DarkPrimary,
    onPrimary = DarkOnPrimary,
    primaryContainer = DarkPrimaryContainer,
    onPrimaryContainer = DarkOnPrimaryContainer,
    inversePrimary = DarkInversePrimary,
    secondary = DarkSecondary,
    onSecondary = DarkOnSecondary,
    secondaryContainer = DarkSecondaryContainer,
    onSecondaryContainer = DarkOnSecondaryContainer,
    tertiary = DarkTertiary,
    onTertiary = DarkOnTertiary,
    tertiaryContainer = DarkTertiaryContainer,
    onTertiaryContainer = DarkOnTertiaryContainer,
    error = DarkError,
    onError = DarkOnError,
    errorContainer = DarkErrorContainer,
    onErrorContainer = DarkOnErrorContainer,
    background = DarkBackground,
    onBackground = DarkOnBackground,
    surface = DarkSurface,
    onSurface = DarkOnSurface,
    surfaceVariant = DarkSurfaceVariant,
    onSurfaceVariant = DarkOnSurfaceVariant,
    surfaceTint = DarkPrimary,
    inverseSurface = DarkInverseSurface,
    inverseOnSurface = DarkInverseOnSurface,
    outline = DarkOutline,
    outlineVariant = DarkOutlineVariant,
    surfaceContainerLowest = DarkSurfaceContainerLowest,
    surfaceContainerLow = DarkSurfaceContainerLow,
    surfaceContainer = DarkSurfaceContainer,
    surfaceContainerHigh = DarkSurfaceContainerHigh,
    surfaceContainerHighest = DarkSurfaceContainerHighest,
)

@OptIn(ExperimentalMaterial3ExpressiveApi::class)
@Composable
fun AppTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    content: @Composable () -> Unit
) {
    val colorScheme = if (darkTheme) DarkColorScheme else LightColorScheme

    MaterialExpressiveTheme(
        colorScheme = colorScheme,
        typography = AppTypography(),
        content = content
    )
}

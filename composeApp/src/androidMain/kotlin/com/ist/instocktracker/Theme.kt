package com.ist.instocktracker

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.text.ExperimentalTextApi
import androidx.compose.ui.text.font.*


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

private val LightColorScheme = lightColorScheme(
    primary = Primary,
    onPrimary = OnPrimary,
    primaryContainer = PrimaryContainer,
    onPrimaryContainer = OnPrimaryContainer,
    secondary = Secondary,
    onSecondary = OnSecondary,
    secondaryContainer = SecondaryContainer,
    onSecondaryContainer = OnSecondaryContainer,
    tertiary = Tertiary,
    onTertiary = OnTertiary,
    tertiaryContainer = TertiaryContainer,
    onTertiaryContainer = OnTertiaryContainer,
    error = Error,
    onError = OnError,
    errorContainer = ErrorContainer,
    onErrorContainer = OnErrorContainer,
    background = BackgroundLight,
    onBackground = OnBackgroundLight,
    surface = SurfaceLight,
    onSurface = OnSurfaceLight,
    surfaceVariant = SurfaceVariantLight,
    onSurfaceVariant = OnSurfaceVariantLight,
    outline = OutlineLight,
)

private val DarkColorScheme = darkColorScheme(
    primary = PrimaryLight,
    onPrimary = PrimaryDark,
    primaryContainer = PrimaryDark,
    onPrimaryContainer = PrimaryContainer,
    secondary = SecondaryLight,
    onSecondary = SecondaryDark,
    secondaryContainer = SecondaryDark,
    onSecondaryContainer = SecondaryContainer,
    tertiary = TertiaryLight,
    onTertiary = TertiaryDark,
    tertiaryContainer = TertiaryDark,
    onTertiaryContainer = TertiaryContainer,
    error = Error,
    onError = OnError,
    errorContainer = ErrorContainer,
    onErrorContainer = OnErrorContainer,
    background = BackgroundDark,
    onBackground = OnBackgroundDark,
    surface = SurfaceDark,
    onSurface = OnSurfaceDark,
    surfaceVariant = SurfaceVariantDark,
    onSurfaceVariant = OnSurfaceVariantDark,
    outline = OutlineDark,
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

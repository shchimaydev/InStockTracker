package com.ist.instocktracker

import androidx.compose.ui.graphics.Color

/**
 * Color tokens derived from the Figma "InStockTracker Design Kit"
 * (Tech Utility palette — Strategy A).
 *
 * The design defines a small set of solid brand colors plus a series of
 * alpha tints layered over white. The solid colors are mapped onto Material 3
 * color-scheme roles in [AppTheme]; the raw tints are exposed here so that
 * components (status chips, badges, borders) can reproduce the exact look.
 */

// region ── Brand palette (solid) ───────────────────────────────────────────

/** Primary — Cobalt / Indigo. */
val Cobalt = Color(0xFF3D52D5)

/** Nav Dark — deep indigo used for the navigation drawer / brand surfaces. */
val NavDark = Color(0xFF1B2878)

/** Success — Mint Green ("in stock" / positive). */
val Mint = Color(0xFF1DB87A)

/** Danger / Warning — Coral Red. */
val Coral = Color(0xFFE84D3D)

/** Ink — primary text / on-surface. */
val Ink = Color(0xFF1A1D2E)

/** White — cards, sheets, top bars. */
val White = Color(0xFFFFFFFF)

/** Surface — slightly tinted off-white. */
val SurfaceTintedLow = Color(0xFFF7F8FA)

/** Base BG — app background. */
val BaseBg = Color(0xFFF5F6FA)

/**
 * App background — vertical gradient from the Figma design
 * (white → tint → surface). Use with [androidx.compose.ui.graphics.Brush.verticalGradient].
 */
val AppBackgroundGradient = listOf(White, Color(0xFFF3F4F8), SurfaceTintedLow)

// endregion

// region ── Alpha tints (as authored in Figma, over white) ───────────────────

val PrimaryTint06 = Color(0x0F3D52D5) // Primary Light  · rgba(61,82,213,0.06)
val PrimaryTint18 = Color(0x2E3D52D5) // Primary Border · rgba(61,82,213,0.18)
val PrimaryTint55 = Color(0x8C3D52D5) // Primary Text   · rgba(61,82,213,0.55)

val SuccessTint10 = Color(0x1A1DB87A) // Success Light  · rgba(29,184,122,0.10)
val SuccessTint30 = Color(0x4D1DB87A) // Success Border · rgba(29,184,122,0.30)

val DangerTint08 = Color(0x14E84D3D)  // Danger Light   · rgba(232,77,61,0.08)
val DangerTint30 = Color(0x4DE84D3D)  // Danger Border  · rgba(232,77,61,0.30)

val InkMuted = Color(0x73000000)      // Ink Muted      · rgba(0,0,0,0.45)
val InkFaint = Color(0x0F000000)      // Ink Faint      · rgba(0,0,0,0.06)
val BorderHairline = Color(0x14000000)// Border         · rgba(0,0,0,0.08)

// endregion

// region ── Light scheme (flattened over white where containers are needed) ──

val LightPrimary = Cobalt
val LightOnPrimary = White
val LightPrimaryContainer = Color(0xFFDCE0F7)   // Primary Border flattened on white
val LightOnPrimaryContainer = NavDark

val LightSecondary = NavDark
val LightOnSecondary = White
val LightSecondaryContainer = Color(0xFFDDE1F5)
val LightOnSecondaryContainer = Color(0xFF121A4D)

val LightTertiary = Mint
val LightOnTertiary = White
val LightTertiaryContainer = Color(0xFFC5EFDD)  // Success Border flattened on white
val LightOnTertiaryContainer = Color(0xFF00432A)

val LightError = Coral
val LightOnError = White
val LightErrorContainer = Color(0xFFFBD9D4)     // Danger Border flattened on white
val LightOnErrorContainer = Color(0xFF5C140C)

val LightBackground = BaseBg
val LightOnBackground = Ink
val LightSurface = White
val LightOnSurface = Ink
val LightSurfaceVariant = Color(0xFFECEEF3)
val LightOnSurfaceVariant = Color(0xFF5C5F6B)   // Ink Muted, slightly tinted for legibility
val LightOutline = Color(0xFFC6C8D0)
val LightOutlineVariant = Color(0xFFE6E7ED)     // Border hairline flattened on white

val LightSurfaceContainerLowest = White
val LightSurfaceContainerLow = SurfaceTintedLow
val LightSurfaceContainer = Color(0xFFF2F3F8)
val LightSurfaceContainerHigh = Color(0xFFECEEF3)
val LightSurfaceContainerHighest = Color(0xFFE6E8EE)

val LightInverseSurface = Color(0xFF2E3140)
val LightInverseOnSurface = Color(0xFFF2F3F7)
val LightInversePrimary = Color(0xFFB8C1F5)

// endregion

// region ── Dark scheme (derived — the design kit is light-only) ─────────────

val DarkPrimary = Color(0xFFB8C1F5)
val DarkOnPrimary = NavDark
val DarkPrimaryContainer = Color(0xFF2A3A9E)
val DarkOnPrimaryContainer = Color(0xFFDCE0F7)

val DarkSecondary = Color(0xFFAEB6E8)
val DarkOnSecondary = Color(0xFF0E1A52)
val DarkSecondaryContainer = Color(0xFF232C66)
val DarkOnSecondaryContainer = Color(0xFFDDE1F5)

val DarkTertiary = Color(0xFF5FD6A4)
val DarkOnTertiary = Color(0xFF00331F)
val DarkTertiaryContainer = Color(0xFF0C8A5A)
val DarkOnTertiaryContainer = Color(0xFFC5EFDD)

val DarkError = Color(0xFFFF8A7A)
val DarkOnError = Color(0xFF5C140C)
val DarkErrorContainer = Color(0xFFB23528)
val DarkOnErrorContainer = Color(0xFFFBD9D4)

val DarkBackground = Color(0xFF101220)
val DarkOnBackground = Color(0xFFE4E5EC)
val DarkSurface = Ink
val DarkOnSurface = Color(0xFFE4E5EC)
val DarkSurfaceVariant = Color(0xFF2A2D3E)
val DarkOnSurfaceVariant = Color(0xFFC2C4D0)
val DarkOutline = Color(0xFF4A4D5C)
val DarkOutlineVariant = Color(0xFF2A2D3E)

val DarkSurfaceContainerLowest = Color(0xFF0C0E1A)
val DarkSurfaceContainerLow = Color(0xFF16192A)
val DarkSurfaceContainer = Ink
val DarkSurfaceContainerHigh = Color(0xFF24273A)
val DarkSurfaceContainerHighest = Color(0xFF2E3145)

val DarkInverseSurface = Color(0xFFE4E5EC)
val DarkInverseOnSurface = Ink
val DarkInversePrimary = Cobalt

// endregion

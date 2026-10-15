package com.venuenexa.pocmux.core.designsystem

import androidx.compose.material3.lightColorScheme
import androidx.compose.ui.graphics.Color

// Identidad de VenueNexa acordada para la web el 2026-10-09 (test-mux/design-system/venuenexa/MASTER.md),
// traducida a roles de Material 3. Contraste de los pares por medir en el dispositivo (la web lo midió con axe).
private val Paper = Color(0xFFF4F3EF)
private val CardWhite = Color(0xFFFFFFFF)
private val Ink = Color(0xFF141217)
private val InkMuted = Color(0xFF57535E)
private val Violet = Color(0xFF7C3AED)
private val VioletTint = Color(0xFFF3EFFD)
private val VioletDeep = Color(0xFF5B21B6)
private val Sand = Color(0xFFEBE8E1)
private val Border = Color(0xFFE2DED6)
private val BorderStrong = Color(0xFFD3CEC4)

// Verde solo para dinero y disponibilidad (regla de la web).
private val Success = Color(0xFF15803D)

internal val VenueNexaColorScheme =
    lightColorScheme(
        primary = Violet,
        onPrimary = Color.White,
        primaryContainer = VioletTint,
        onPrimaryContainer = VioletDeep,
        secondary = VioletDeep,
        onSecondary = Color.White,
        secondaryContainer = Sand,
        onSecondaryContainer = Ink,
        tertiary = Success,
        onTertiary = Color.White,
        background = Paper,
        onBackground = Ink,
        surface = CardWhite,
        onSurface = Ink,
        surfaceVariant = Sand,
        onSurfaceVariant = InkMuted,
        outline = BorderStrong,
        outlineVariant = Border,
        surfaceContainerLowest = CardWhite,
        surfaceContainerLow = CardWhite,
        surfaceContainer = CardWhite,
        surfaceContainerHigh = Sand,
        surfaceContainerHighest = Sand,
    )

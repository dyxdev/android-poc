package com.venuenexa.pocmux.core.designsystem

import androidx.compose.material3.Typography
import androidx.compose.ui.text.font.Font
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontVariation
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.sp

// Outfit (SIL OFL, ver OFL-Outfit.txt) como fuente variable: un solo archivo y un peso por instancia.
private fun outfit(weight: FontWeight) =
    Font(
        resId = R.font.outfit,
        weight = weight,
        variationSettings = FontVariation.Settings(FontVariation.weight(weight.weight)),
    )

private val Outfit =
    FontFamily(
        outfit(FontWeight.Normal),
        outfit(FontWeight.Medium),
        outfit(FontWeight.SemiBold),
        outfit(FontWeight.Bold),
        outfit(FontWeight.ExtraBold),
    )

private val Base = Typography()

// Display 800 con tracking −3 % y interlineado 0,95; títulos 700; cuerpo 400; interfaz 500–600 (MASTER.md).
internal val VenueNexaTypography =
    Typography(
        displayLarge = Base.displayLarge.copy(fontFamily = Outfit, fontWeight = FontWeight.ExtraBold),
        displayMedium =
            Base.displayMedium.copy(
                fontFamily = Outfit,
                fontWeight = FontWeight.ExtraBold,
                fontSize = 38.sp,
                lineHeight = 40.sp,
                letterSpacing = (-1.1).sp,
            ),
        displaySmall = Base.displaySmall.copy(fontFamily = Outfit, fontWeight = FontWeight.ExtraBold),
        headlineLarge = Base.headlineLarge.copy(fontFamily = Outfit, fontWeight = FontWeight.Bold),
        headlineMedium = Base.headlineMedium.copy(fontFamily = Outfit, fontWeight = FontWeight.Bold),
        headlineSmall = Base.headlineSmall.copy(fontFamily = Outfit, fontWeight = FontWeight.Bold),
        titleLarge = Base.titleLarge.copy(fontFamily = Outfit, fontWeight = FontWeight.Bold),
        titleMedium = Base.titleMedium.copy(fontFamily = Outfit, fontWeight = FontWeight.SemiBold),
        titleSmall = Base.titleSmall.copy(fontFamily = Outfit, fontWeight = FontWeight.SemiBold),
        bodyLarge = Base.bodyLarge.copy(fontFamily = Outfit, fontWeight = FontWeight.Normal),
        bodyMedium = Base.bodyMedium.copy(fontFamily = Outfit, fontWeight = FontWeight.Normal),
        bodySmall = Base.bodySmall.copy(fontFamily = Outfit, fontWeight = FontWeight.Normal),
        labelLarge = Base.labelLarge.copy(fontFamily = Outfit, fontWeight = FontWeight.SemiBold),
        labelMedium = Base.labelMedium.copy(fontFamily = Outfit, fontWeight = FontWeight.Medium),
        labelSmall = Base.labelSmall.copy(fontFamily = Outfit, fontWeight = FontWeight.Medium),
    )

package com.venuenexa.pocmux.core.designsystem

import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Shapes
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.ui.unit.dp

// Esquinas de 8 dp en tarjetas, botones, campos y medios; 12 dp solo para destacados (MASTER.md).
private val VenueNexaShapes =
    Shapes(
        extraSmall = RoundedCornerShape(4.dp),
        small = RoundedCornerShape(8.dp),
        medium = RoundedCornerShape(8.dp),
        large = RoundedCornerShape(12.dp),
        extraLarge = RoundedCornerShape(12.dp),
    )

// Solo tema claro y sin color dinámico: la marca manda. El tema claro es solo por simplicidad de la PoC; el modo
// oscuro debe soportarse en toda la app (decisión de Andy, 2026-10-16, ADR-AND-0012) y falta aquí.
@Composable
fun VenueNexaTheme(content: @Composable () -> Unit) {
    CompositionLocalProvider(LocalSpacing provides Spacing()) {
        MaterialTheme(
            colorScheme = VenueNexaColorScheme,
            typography = VenueNexaTypography,
            shapes = VenueNexaShapes,
            content = content,
        )
    }
}

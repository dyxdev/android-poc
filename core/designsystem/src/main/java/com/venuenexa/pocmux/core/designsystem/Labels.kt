package com.venuenexa.pocmux.core.designsystem

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Star
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp

private val StarSize = 14.dp
private val DotSize = 8.dp

// Valoración: estrella + texto ya formateado por quien llama (por idioma: «4,8 (126)»).
@Composable
fun RatingLabel(
    text: String,
    modifier: Modifier = Modifier,
) {
    Row(
        modifier = modifier,
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(MaterialTheme.spacing.xs),
    ) {
        Icon(
            imageVector = Icons.Filled.Star,
            contentDescription = null,
            tint = MaterialTheme.colorScheme.onSurface,
            modifier = Modifier.size(StarSize),
        )
        Text(text = text, style = MaterialTheme.typography.labelMedium, color = MaterialTheme.colorScheme.onSurface)
    }
}

// Disponibilidad: punto + texto. Verde solo cuando está disponible (regla de la web); si no, tono neutro.
// El estado no depende solo del color: el texto lo dice.
@Composable
fun AvailabilityLabel(
    text: String,
    available: Boolean,
    modifier: Modifier = Modifier,
) {
    val tone = if (available) MaterialTheme.colorScheme.tertiary else MaterialTheme.colorScheme.onSurfaceVariant
    Row(
        modifier = modifier,
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(MaterialTheme.spacing.xs),
    ) {
        Box(modifier = Modifier.size(DotSize).background(tone, CircleShape))
        Text(text = text, style = MaterialTheme.typography.labelMedium, color = tone)
    }
}

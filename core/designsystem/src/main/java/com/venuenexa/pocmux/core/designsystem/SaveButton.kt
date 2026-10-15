package com.venuenexa.pocmux.core.designsystem

import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.spring
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.outlined.FavoriteBorder
import androidx.compose.material3.Icon
import androidx.compose.material3.IconToggleButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.graphicsLayer

private const val POP_START_SCALE = 0.7f
private val PopSpring = spring<Float>(Spring.DampingRatioMediumBouncy, Spring.StiffnessMedium)

// Corazón de guardar: al marcarlo rebota con un resorte (sobrio, ~300 ms). Solo anima el cambio del usuario,
// no el estado inicial al volver a ver la tarjeta. Zona táctil de 48 dp por IconToggleButton.
@Composable
fun SaveButton(
    saved: Boolean,
    onToggle: (Boolean) -> Unit,
    contentDescription: String,
    modifier: Modifier = Modifier,
) {
    val scale = remember { Animatable(1f) }
    var previous by remember { mutableStateOf(saved) }
    LaunchedEffect(saved) {
        if (saved && !previous) {
            scale.snapTo(POP_START_SCALE)
            scale.animateTo(1f, PopSpring)
        }
        previous = saved
    }
    IconToggleButton(checked = saved, onCheckedChange = onToggle, modifier = modifier) {
        Icon(
            imageVector = if (saved) Icons.Filled.Favorite else Icons.Outlined.FavoriteBorder,
            contentDescription = contentDescription,
            tint = if (saved) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurfaceVariant,
            modifier =
                Modifier.graphicsLayer {
                    scaleX = scale.value
                    scaleY = scale.value
                },
        )
    }
}

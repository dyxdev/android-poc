package com.venuenexa.pocmux.core.context

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.KeyboardArrowDown
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.IntRect
import androidx.compose.ui.unit.IntSize
import androidx.compose.ui.unit.LayoutDirection
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Popup
import androidx.compose.ui.window.PopupPositionProvider
import androidx.compose.ui.window.PopupProperties
import com.venuenexa.pocmux.core.designsystem.spacing

private val ArrowSize = 18.dp
private val DotSize = 8.dp
private val DotOffset = 4.dp
private val BalloonGap = 8.dp
private val BalloonMaxWidth = 260.dp

// Indicador permanente del contexto activo y, a la vez, botón para cambiarlo. Solo aparece si el usuario tiene
// más de un contexto. Con un aviso pendiente suma un punto y un globo explicativo (una vez por contexto nuevo).
@Composable
fun ContextLabel(
    switcher: ContextSwitcher,
    modifier: Modifier = Modifier,
) {
    val state = switcher.state
    if (!state.canSwitch) return
    val description = stringResource(R.string.context_label_description, stringResource(state.active.titleRes()))
    Box(modifier = modifier) {
        Surface(
            onClick = switcher.onOpen,
            modifier = Modifier.semantics { contentDescription = description },
            shape = MaterialTheme.shapes.small,
            color = MaterialTheme.colorScheme.surface,
            border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant),
        ) {
            Row(
                modifier = Modifier.padding(horizontal = MaterialTheme.spacing.md, vertical = MaterialTheme.spacing.sm),
                horizontalArrangement = Arrangement.spacedBy(MaterialTheme.spacing.xs),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Text(
                    text = stringResource(state.active.titleRes()),
                    style = MaterialTheme.typography.labelLarge,
                    color = MaterialTheme.colorScheme.onSurface,
                )
                Icon(
                    imageVector = Icons.Filled.KeyboardArrowDown,
                    contentDescription = null,
                    modifier = Modifier.size(ArrowSize),
                )
            }
        }
        if (state.hintPending) {
            Box(
                modifier =
                    Modifier
                        .align(Alignment.TopEnd)
                        .offset(x = DotOffset, y = -DotOffset)
                        .size(DotSize)
                        .background(MaterialTheme.colorScheme.primary, CircleShape),
            )
            HintBalloon(onDismiss = switcher.onAcknowledgeHint)
        }
    }
}

// Globo bajo la etiqueta, alineado a su borde derecho para que no se salga de la pantalla. Se cierra con
// «Entendido» o tocando fuera; en ambos casos el aviso se da por visto.
@Composable
private fun HintBalloon(onDismiss: () -> Unit) {
    val gapPx = with(LocalDensity.current) { BalloonGap.roundToPx() }
    val positionProvider = remember(gapPx) { BelowAnchorPositionProvider(gapPx) }
    Popup(
        popupPositionProvider = positionProvider,
        onDismissRequest = onDismiss,
        properties = PopupProperties(focusable = true),
    ) {
        Surface(
            modifier = Modifier.widthIn(max = BalloonMaxWidth),
            shape = MaterialTheme.shapes.medium,
            color = MaterialTheme.colorScheme.onSurface,
            contentColor = MaterialTheme.colorScheme.surface,
        ) {
            Column(
                modifier = Modifier.padding(MaterialTheme.spacing.md),
                verticalArrangement = Arrangement.spacedBy(MaterialTheme.spacing.xs),
            ) {
                Text(text = stringResource(R.string.context_hint), style = MaterialTheme.typography.bodyMedium)
                TextButton(
                    onClick = onDismiss,
                    modifier = Modifier.align(Alignment.End),
                    shape = MaterialTheme.shapes.small,
                    colors = ButtonDefaults.textButtonColors(contentColor = MaterialTheme.colorScheme.surface),
                ) {
                    Text(text = stringResource(R.string.context_hint_ok), style = MaterialTheme.typography.labelLarge)
                }
            }
        }
    }
}

private class BelowAnchorPositionProvider(
    private val gapPx: Int,
) : PopupPositionProvider {
    override fun calculatePosition(
        anchorBounds: IntRect,
        windowSize: IntSize,
        layoutDirection: LayoutDirection,
        popupContentSize: IntSize,
    ): IntOffset {
        val maxX = (windowSize.width - popupContentSize.width).coerceAtLeast(0)
        val x = (anchorBounds.right - popupContentSize.width).coerceIn(0, maxX)
        return IntOffset(x, anchorBounds.bottom + gapPx)
    }
}

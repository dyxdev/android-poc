package com.venuenexa.pocmux.core.designsystem

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.size
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Rect
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.StrokeJoin
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.unit.dp

// Logotipo provisional de la web: wordmark tipográfico con una marca de arco de puerta (aún no hay logo real).
@Composable
fun VenueNexaWordmark(modifier: Modifier = Modifier) {
    Row(
        modifier = modifier,
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(MaterialTheme.spacing.sm),
    ) {
        DoorArch(color = MaterialTheme.colorScheme.primary, modifier = Modifier.size(width = 20.dp, height = 24.dp))
        Text(
            text = "VenueNexa",
            style = MaterialTheme.typography.titleLarge,
            color = MaterialTheme.colorScheme.onBackground,
        )
    }
}

@Composable
private fun DoorArch(
    color: Color,
    modifier: Modifier = Modifier,
) {
    Canvas(modifier = modifier) {
        val strokeWidth = 2.5.dp.toPx()
        val half = strokeWidth / 2
        val arch =
            Path().apply {
                moveTo(half, size.height)
                lineTo(half, size.width / 2)
                arcTo(Rect(half, half, size.width - half, size.width - half), 180f, 180f, false)
                lineTo(size.width - half, size.height)
            }
        drawPath(
            path = arch,
            color = color,
            style = Stroke(width = strokeWidth, cap = StrokeCap.Round, join = StrokeJoin.Round),
        )
    }
}

package com.venuenexa.pocmux.core.designsystem

import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.layout.Box
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.unit.dp
import kotlinx.coroutines.delay
import kotlin.math.roundToInt

private const val STAGGER_MS = 60L
private const val ENTRANCE_MS = 320
private const val SHIMMER_MS = 1200
private const val COUNT_MS = 700
private val EntranceOffset = 24.dp

// Entrada escalonada: aparece con un fundido y un pequeño desplazamiento, retrasada según su posición.
// Con play = false se muestra ya terminada (para que no se repita al volver a verlo en una lista).
// Las animaciones de Compose siguen la escala de animación del sistema: con «Quitar animaciones» no se ven.
@Composable
fun StaggeredEntrance(
    index: Int,
    play: Boolean,
    modifier: Modifier = Modifier,
    content: @Composable () -> Unit,
) {
    val progress = remember { Animatable(if (play) 0f else 1f) }
    LaunchedEffect(Unit) {
        if (play) {
            delay(index * STAGGER_MS)
            progress.animateTo(1f, tween(durationMillis = ENTRANCE_MS, easing = FastOutSlowInEasing))
        }
    }
    val offsetPx = with(LocalDensity.current) { EntranceOffset.toPx() }
    Box(
        modifier =
            modifier.graphicsLayer {
                alpha = progress.value
                translationY = (1f - progress.value) * offsetPx
            },
    ) {
        content()
    }
}

// Marcador de posición con brillo que recorre la caja: estados de carga de fotos y tarjetas.
@Composable
fun ShimmerBox(modifier: Modifier = Modifier) {
    val transition = rememberInfiniteTransition(label = "shimmer")
    val progress by transition.animateFloat(
        initialValue = 0f,
        targetValue = 1f,
        animationSpec = infiniteRepeatable(tween(SHIMMER_MS, easing = LinearEasing), RepeatMode.Restart),
        label = "shimmerProgress",
    )
    val base = MaterialTheme.colorScheme.surfaceVariant
    val highlight = MaterialTheme.colorScheme.surface
    Box(
        modifier =
            modifier.drawBehind {
                val start = -size.width + 2 * size.width * progress
                drawRect(
                    brush =
                        Brush.linearGradient(
                            colors = listOf(base, highlight, base),
                            start = Offset(start, 0f),
                            end = Offset(start + size.width, 0f),
                        ),
                )
            },
    )
}

// Cifra que cuenta desde 0 hasta su valor al aparecer (una vez por composición). Sigue la escala de animación del
// sistema: con «Quitar animaciones» muestra el valor final al instante.
@Composable
fun animatedCount(target: Int): Int {
    val progress = remember { Animatable(0f) }
    LaunchedEffect(target) {
        progress.animateTo(1f, tween(durationMillis = COUNT_MS, easing = FastOutSlowInEasing))
    }
    return (target * progress.value).roundToInt()
}

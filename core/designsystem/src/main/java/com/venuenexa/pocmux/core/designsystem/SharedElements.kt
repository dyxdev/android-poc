package com.venuenexa.pocmux.core.designsystem

import androidx.compose.animation.AnimatedVisibilityScope
import androidx.compose.animation.SharedTransitionScope
import androidx.compose.runtime.Composable
import androidx.compose.runtime.compositionLocalOf
import androidx.compose.ui.Modifier

// La app (NavHost) ofrece estos dos ámbitos; sin ellos (previews, pruebas) las fotos simplemente no se comparten.
val LocalSharedTransitionScope = compositionLocalOf<SharedTransitionScope?> { null }
val LocalAnimatedVisibilityScope = compositionLocalOf<AnimatedVisibilityScope?> { null }

// Foto compartida entre la tarjeta y el detalle. La clave debe ser única por tarjeta visible (un mismo venue sale
// en el carrusel y en la lista), por eso la elige quien pinta la tarjeta y viaja en la ruta del detalle.
@Composable
fun Modifier.sharedPhoto(key: String?): Modifier {
    val transition = LocalSharedTransitionScope.current
    val visibility = LocalAnimatedVisibilityScope.current
    if (key == null || transition == null || visibility == null) return this
    return with(transition) {
        this@sharedPhoto.sharedElement(rememberSharedContentState(key = key), visibility)
    }
}

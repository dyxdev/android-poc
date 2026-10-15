package com.venuenexa.pocmux.core.designsystem

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.layout.ContentScale
import coil3.compose.SubcomposeAsyncImage

// Foto de un venue: brillo mientras carga y bloque neutro si falla. La caché de Coil debe vaciarse al
// cerrar sesión (ADR-AND-0013) cuando haya fotos privadas; las de ejemplo son públicas.
@Composable
fun VenuePhoto(
    url: String,
    contentDescription: String?,
    modifier: Modifier = Modifier,
    sharedKey: String? = null,
) {
    SubcomposeAsyncImage(
        model = url,
        contentDescription = contentDescription,
        modifier = modifier.sharedPhoto(sharedKey),
        contentScale = ContentScale.Crop,
        loading = { ShimmerBox(Modifier.fillMaxSize()) },
        error = { Box(Modifier.fillMaxSize().background(MaterialTheme.colorScheme.surfaceVariant)) },
    )
}

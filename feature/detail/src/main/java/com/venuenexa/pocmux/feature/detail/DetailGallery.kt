package com.venuenexa.pocmux.feature.detail

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.venuenexa.pocmux.core.designsystem.SaveButton
import com.venuenexa.pocmux.core.designsystem.VenuePhoto
import com.venuenexa.pocmux.core.designsystem.spacing
import com.venuenexa.pocmux.core.model.Venue
import com.venuenexa.pocmux.core.designsystem.R as DsR

internal const val GALLERY_ASPECT = 1.5f
private const val CONTROL_ALPHA = 0.92f
private const val COUNTER_ALPHA = 0.72f
private val ControlSize = 48.dp

// Galería a ancho completo: la primera foto es la que viaja desde la tarjeta (transición compartida).
@Composable
internal fun DetailGallery(
    venue: Venue,
    saved: Boolean,
    sharedKey: String,
    onBack: () -> Unit,
    onToggleSaved: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val pagerState = rememberPagerState { venue.photos.size }
    val description = stringResource(DsR.string.venue_photo_description, venue.name)
    Box(
        modifier =
            modifier
                .fillMaxWidth()
                .aspectRatio(GALLERY_ASPECT)
                .clip(MaterialTheme.shapes.medium),
    ) {
        HorizontalPager(state = pagerState, modifier = Modifier.fillMaxSize()) { page ->
            VenuePhoto(
                url = venue.photos[page],
                contentDescription = description,
                modifier = Modifier.fillMaxSize(),
                sharedKey = if (page == 0) sharedKey else null,
            )
        }
        BackButton(onBack = onBack, modifier = Modifier.align(Alignment.TopStart).padding(MaterialTheme.spacing.sm))
        Surface(
            modifier = Modifier.align(Alignment.TopEnd).padding(MaterialTheme.spacing.sm),
            shape = MaterialTheme.shapes.small,
            color = MaterialTheme.colorScheme.surface.copy(alpha = CONTROL_ALPHA),
        ) {
            SaveButton(
                saved = saved,
                onToggle = { onToggleSaved() },
                contentDescription =
                    stringResource(if (saved) DsR.string.venue_unsave else DsR.string.venue_save, venue.name),
            )
        }
        if (venue.photos.size > 1) {
            PhotoCounter(
                current = pagerState.currentPage + 1,
                total = venue.photos.size,
                modifier = Modifier.align(Alignment.BottomEnd).padding(MaterialTheme.spacing.sm),
            )
        }
    }
}

@Composable
internal fun BackButton(
    onBack: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Surface(
        onClick = onBack,
        modifier = modifier.size(ControlSize),
        shape = MaterialTheme.shapes.small,
        color = MaterialTheme.colorScheme.surface.copy(alpha = CONTROL_ALPHA),
    ) {
        Box(contentAlignment = Alignment.Center) {
            Icon(
                imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                contentDescription = stringResource(R.string.detail_back),
                tint = MaterialTheme.colorScheme.onSurface,
            )
        }
    }
}

@Composable
private fun PhotoCounter(
    current: Int,
    total: Int,
    modifier: Modifier = Modifier,
) {
    Surface(
        modifier = modifier,
        shape = MaterialTheme.shapes.extraSmall,
        color = MaterialTheme.colorScheme.onSurface.copy(alpha = COUNTER_ALPHA),
    ) {
        Text(
            text = stringResource(R.string.detail_photo_counter, current, total),
            style = MaterialTheme.typography.labelMedium,
            color = MaterialTheme.colorScheme.surface,
            modifier = Modifier.padding(horizontal = MaterialTheme.spacing.sm, vertical = MaterialTheme.spacing.xs),
        )
    }
}

package com.venuenexa.pocmux.feature.detail

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.size
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.venuenexa.pocmux.core.designsystem.AvailabilityLabel
import com.venuenexa.pocmux.core.designsystem.FigureCard
import com.venuenexa.pocmux.core.designsystem.RatingLabel
import com.venuenexa.pocmux.core.designsystem.SectionHeader
import com.venuenexa.pocmux.core.designsystem.VenuePhoto
import com.venuenexa.pocmux.core.designsystem.animatedCount
import com.venuenexa.pocmux.core.designsystem.formatPrice
import com.venuenexa.pocmux.core.designsystem.formatRating
import com.venuenexa.pocmux.core.designsystem.spacing
import com.venuenexa.pocmux.core.model.Venue
import com.venuenexa.pocmux.core.designsystem.R as DsR

private const val VIDEO_ASPECT = 16f / 9f
private const val CONTROL_ALPHA = 0.92f
private val PlayButtonSize = 56.dp

@Composable
internal fun DetailHeader(
    venue: Venue,
    modifier: Modifier = Modifier,
) {
    val locale = LocalConfiguration.current.locales[0]
    Column(modifier = modifier, verticalArrangement = Arrangement.spacedBy(MaterialTheme.spacing.sm)) {
        Text(
            text = venue.name,
            style = MaterialTheme.typography.headlineMedium,
            color = MaterialTheme.colorScheme.onBackground,
        )
        Text(
            text = venue.city,
            style = MaterialTheme.typography.bodyLarge,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
        )
        FlowRow(
            horizontalArrangement = Arrangement.spacedBy(MaterialTheme.spacing.lg),
            verticalArrangement = Arrangement.spacedBy(MaterialTheme.spacing.xs),
        ) {
            RatingLabel(text = formatRating(venue.rating, venue.reviewCount, locale))
            AvailabilityLabel(
                text =
                    stringResource(
                        if (venue.isAvailable) DsR.string.venue_available else DsR.string.venue_unavailable,
                    ),
                available = venue.isAvailable,
            )
        }
    }
}

// Tres cifras clave que cuentan hasta su valor al abrir el detalle (movimiento con intención).
@Composable
internal fun DetailFigures(
    venue: Venue,
    modifier: Modifier = Modifier,
) {
    val locale = LocalConfiguration.current.locales[0]
    Row(modifier = modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(MaterialTheme.spacing.md)) {
        FigureCard(
            value = animatedCount(venue.capacity).toString(),
            label = stringResource(R.string.detail_figure_capacity),
            modifier = Modifier.weight(1f),
        )
        FigureCard(
            value = formatPrice(animatedCount(venue.pricePerDay), locale),
            label = stringResource(R.string.detail_figure_price),
            modifier = Modifier.weight(1f),
        )
        FigureCard(
            value = animatedCount(venue.reviewCount).toString(),
            label = stringResource(R.string.detail_figure_reviews),
            modifier = Modifier.weight(1f),
        )
    }
}

// Vídeo tour: primero el póster con «reproducir» (el reproductor solo se crea al pulsar), y debajo, el estado
// de la analítica para que se vea la puerta de consentimiento.
@Composable
internal fun TourSection(
    venue: Venue,
    playbackId: String,
    analyticsConsent: Boolean,
    modifier: Modifier = Modifier,
) {
    var playing by rememberSaveable(playbackId) { mutableStateOf(false) }
    Column(modifier = modifier, verticalArrangement = Arrangement.spacedBy(MaterialTheme.spacing.sm)) {
        SectionHeader(title = stringResource(R.string.detail_tour_title))
        Box(
            modifier =
                Modifier
                    .fillMaxWidth()
                    .aspectRatio(VIDEO_ASPECT)
                    .clip(MaterialTheme.shapes.medium),
        ) {
            if (playing) {
                TourPlayer(playbackId = playbackId, modifier = Modifier.fillMaxSize())
            } else {
                TourPoster(venue = venue, onPlay = { playing = true })
            }
        }
        Text(
            text =
                stringResource(
                    if (analyticsConsent) R.string.detail_analytics_on else R.string.detail_analytics_off,
                ),
            style = MaterialTheme.typography.labelMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
        )
    }
}

@Composable
private fun TourPoster(
    venue: Venue,
    onPlay: () -> Unit,
) {
    val playLabel = stringResource(R.string.detail_tour_play)
    Box(modifier = Modifier.fillMaxSize().clickable(onClickLabel = playLabel, onClick = onPlay)) {
        VenuePhoto(url = venue.photos.last(), contentDescription = null, modifier = Modifier.fillMaxSize())
        Surface(
            modifier = Modifier.align(Alignment.Center).size(PlayButtonSize),
            shape = MaterialTheme.shapes.small,
            color = MaterialTheme.colorScheme.surface.copy(alpha = CONTROL_ALPHA),
        ) {
            Box(contentAlignment = Alignment.Center) {
                Icon(
                    imageVector = Icons.Filled.PlayArrow,
                    contentDescription = playLabel,
                    tint = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.size(MaterialTheme.spacing.xl),
                )
            }
        }
    }
}

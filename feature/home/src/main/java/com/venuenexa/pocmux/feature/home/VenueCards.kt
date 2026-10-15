package com.venuenexa.pocmux.feature.home

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.venuenexa.pocmux.core.designsystem.AvailabilityLabel
import com.venuenexa.pocmux.core.designsystem.RatingLabel
import com.venuenexa.pocmux.core.designsystem.SaveButton
import com.venuenexa.pocmux.core.designsystem.VenuePhoto
import com.venuenexa.pocmux.core.designsystem.formatPrice
import com.venuenexa.pocmux.core.designsystem.formatRating
import com.venuenexa.pocmux.core.designsystem.spacing
import com.venuenexa.pocmux.core.model.Venue
import com.venuenexa.pocmux.core.designsystem.R as DsR

private val FeaturedWidth = 280.dp
private val ThumbnailSize = 104.dp
private const val STRIP_MAIN_WEIGHT = 2f
private const val STRIP_ASPECT = 1.8f

@Composable
private fun venueCardColors() = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)

@Composable
private fun venueCardBorder() = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant)

// Destacado del carrusel: tira de tres fotos (una grande y dos pequeñas) sobre los datos. 12 dp solo aquí.
@Composable
fun FeaturedVenueCard(
    venue: Venue,
    saved: Boolean,
    onClick: (sharedKey: String) -> Unit,
    onToggleSaved: (Boolean) -> Unit,
    modifier: Modifier = Modifier,
) {
    val photoKey = "featured-${venue.id}"
    Card(
        onClick = { onClick(photoKey) },
        modifier = modifier.width(FeaturedWidth),
        shape = MaterialTheme.shapes.large,
        colors = venueCardColors(),
        border = venueCardBorder(),
    ) {
        Column(
            modifier = Modifier.padding(MaterialTheme.spacing.sm),
            verticalArrangement = Arrangement.spacedBy(MaterialTheme.spacing.sm),
        ) {
            PhotoStrip(venue = venue, photoKey = photoKey)
            Row(verticalAlignment = Alignment.CenterVertically) {
                VenueSummary(venue = venue, modifier = Modifier.weight(1f))
                SaveToggle(venue = venue, saved = saved, onToggleSaved = onToggleSaved)
            }
        }
    }
}

// Tarjeta de la lista: miniatura cuadrada, datos y corazón. Compacta (8 dp).
@Composable
fun VenueListCard(
    venue: Venue,
    saved: Boolean,
    onClick: (sharedKey: String) -> Unit,
    onToggleSaved: (Boolean) -> Unit,
    modifier: Modifier = Modifier,
) {
    val photoKey = "list-${venue.id}"
    Card(
        onClick = { onClick(photoKey) },
        modifier = modifier.fillMaxWidth(),
        shape = MaterialTheme.shapes.medium,
        colors = venueCardColors(),
        border = venueCardBorder(),
    ) {
        Row(
            modifier = Modifier.padding(MaterialTheme.spacing.sm),
            horizontalArrangement = Arrangement.spacedBy(MaterialTheme.spacing.md),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            VenuePhoto(
                url = venue.photos.first(),
                contentDescription = stringResource(DsR.string.venue_photo_description, venue.name),
                modifier = Modifier.size(ThumbnailSize).clip(MaterialTheme.shapes.small),
                sharedKey = photoKey,
            )
            VenueSummary(venue = venue, modifier = Modifier.weight(1f))
            SaveToggle(venue = venue, saved = saved, onToggleSaved = onToggleSaved)
        }
    }
}

@Composable
private fun PhotoStrip(
    venue: Venue,
    photoKey: String,
) {
    val description = stringResource(DsR.string.venue_photo_description, venue.name)
    Row(
        modifier = Modifier.fillMaxWidth().aspectRatio(STRIP_ASPECT).clip(MaterialTheme.shapes.small),
        horizontalArrangement = Arrangement.spacedBy(MaterialTheme.spacing.xs),
    ) {
        VenuePhoto(
            url = venue.photos.first(),
            contentDescription = description,
            modifier = Modifier.weight(STRIP_MAIN_WEIGHT).fillMaxHeight(),
            sharedKey = photoKey,
        )
        Column(
            modifier = Modifier.weight(1f).fillMaxHeight(),
            verticalArrangement = Arrangement.spacedBy(MaterialTheme.spacing.xs),
        ) {
            venue.photos.drop(1).take(2).forEach { url ->
                VenuePhoto(url = url, contentDescription = null, modifier = Modifier.weight(1f).fillMaxWidth())
            }
        }
    }
}

@Composable
private fun VenueSummary(
    venue: Venue,
    modifier: Modifier = Modifier,
) {
    val locale = LocalConfiguration.current.locales[0]
    Column(modifier = modifier, verticalArrangement = Arrangement.spacedBy(MaterialTheme.spacing.xs)) {
        Text(
            text = venue.name,
            style = MaterialTheme.typography.titleMedium,
            color = MaterialTheme.colorScheme.onSurface,
        )
        Text(
            text = "${venue.city} · ${stringResource(DsR.string.venue_capacity, venue.capacity)}",
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
        )
        RatingLabel(text = formatRating(venue.rating, venue.reviewCount, locale))
        FlowRow(
            horizontalArrangement = Arrangement.spacedBy(MaterialTheme.spacing.md),
            verticalArrangement = Arrangement.spacedBy(MaterialTheme.spacing.xs),
        ) {
            Text(
                text = stringResource(DsR.string.venue_price_per_day, formatPrice(venue.pricePerDay, locale)),
                style = MaterialTheme.typography.labelLarge,
                color = MaterialTheme.colorScheme.tertiary,
            )
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

@Composable
private fun SaveToggle(
    venue: Venue,
    saved: Boolean,
    onToggleSaved: (Boolean) -> Unit,
) {
    SaveButton(
        saved = saved,
        onToggle = onToggleSaved,
        contentDescription =
            stringResource(if (saved) DsR.string.venue_unsave else DsR.string.venue_save, venue.name),
    )
}

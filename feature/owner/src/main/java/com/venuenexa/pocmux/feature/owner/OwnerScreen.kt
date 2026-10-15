package com.venuenexa.pocmux.feature.owner

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.venuenexa.pocmux.core.context.ContextLabel
import com.venuenexa.pocmux.core.context.ContextSwitcher
import com.venuenexa.pocmux.core.context.rememberContextSwitcher
import com.venuenexa.pocmux.core.designsystem.AvailabilityLabel
import com.venuenexa.pocmux.core.designsystem.FigureCard
import com.venuenexa.pocmux.core.designsystem.SectionHeader
import com.venuenexa.pocmux.core.designsystem.ShimmerBox
import com.venuenexa.pocmux.core.designsystem.StaggeredEntrance
import com.venuenexa.pocmux.core.designsystem.VenuePhoto
import com.venuenexa.pocmux.core.designsystem.animatedCount
import com.venuenexa.pocmux.core.designsystem.formatPrice
import com.venuenexa.pocmux.core.designsystem.spacing
import com.venuenexa.pocmux.core.model.Venue
import com.venuenexa.pocmux.core.designsystem.R as DsR

// Cifras de EJEMPLO (ficticias) hasta que exista la API del panel del propietario.
private const val SAMPLE_PENDING_BOOKINGS = 3
private const val SAMPLE_MONTH_INCOME = 4_280
private val ThumbnailSize = 72.dp
private val LoadingRowHeight = 88.dp

@Composable
fun OwnerRoute(
    modifier: Modifier = Modifier,
    viewModel: OwnerViewModel = hiltViewModel(),
) {
    val venues by viewModel.venues.collectAsStateWithLifecycle()
    OwnerScreen(venues = venues, switcher = rememberContextSwitcher(), modifier = modifier)
}

// Panel de propietario DE EJEMPLO: cifras ficticias y dos espacios del catálogo. Sirve para ver el cambio de
// contexto; el panel real llega con la API (ADR-AND-0026).
@Composable
fun OwnerScreen(
    venues: List<Venue>?,
    switcher: ContextSwitcher,
    modifier: Modifier = Modifier,
) {
    val locale = LocalConfiguration.current.locales[0]
    Column(
        modifier = modifier.fillMaxSize().verticalScroll(rememberScrollState()).padding(MaterialTheme.spacing.lg),
        verticalArrangement = Arrangement.spacedBy(MaterialTheme.spacing.lg),
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Text(
                text = stringResource(R.string.owner_title),
                style = MaterialTheme.typography.headlineMedium,
                color = MaterialTheme.colorScheme.onBackground,
                modifier = Modifier.weight(1f),
            )
            ContextLabel(switcher = switcher)
        }
        Text(
            text = stringResource(R.string.owner_sample_note),
            style = MaterialTheme.typography.labelMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
        )
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(MaterialTheme.spacing.md),
        ) {
            FigureCard(
                value = animatedCount(SAMPLE_PENDING_BOOKINGS).toString(),
                label = stringResource(R.string.owner_figure_pending),
                modifier = Modifier.weight(1f),
            )
            FigureCard(
                value = formatPrice(animatedCount(SAMPLE_MONTH_INCOME), locale),
                label = stringResource(R.string.owner_figure_income),
                modifier = Modifier.weight(1f),
            )
            FigureCard(
                value = animatedCount(venues?.size ?: 0).toString(),
                label = stringResource(R.string.owner_figure_venues),
                modifier = Modifier.weight(1f),
            )
        }
        SectionHeader(title = stringResource(R.string.owner_my_venues))
        if (venues == null) {
            ShimmerBox(
                Modifier
                    .fillMaxWidth()
                    .height(LoadingRowHeight)
                    .clip(MaterialTheme.shapes.medium),
            )
        } else {
            venues.forEachIndexed { index, venue ->
                StaggeredEntrance(index = index, play = true) { OwnerVenueRow(venue = venue) }
            }
        }
    }
}

@Composable
private fun OwnerVenueRow(venue: Venue) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = MaterialTheme.shapes.medium,
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant),
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
            )
            Column(verticalArrangement = Arrangement.spacedBy(MaterialTheme.spacing.xs)) {
                Text(
                    text = venue.name,
                    style = MaterialTheme.typography.titleMedium,
                    color = MaterialTheme.colorScheme.onSurface,
                )
                Text(
                    text = venue.city,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
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
}

package com.venuenexa.pocmux.feature.home

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyListScope
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.Dp
import com.venuenexa.pocmux.core.designsystem.SectionHeader
import com.venuenexa.pocmux.core.designsystem.StaggeredEntrance
import com.venuenexa.pocmux.core.designsystem.spacing

private const val SKELETON_COUNT = 3
private const val MAX_STAGGER_INDEX = 5

// Cuerpo de Inicio bajo los filtros: esqueletos, destacados (solo sin filtro), lista o estado vacío.
internal fun LazyListScope.homeContent(
    state: HomeUiState,
    actions: HomeActions,
    gutter: Dp,
) {
    if (state.isLoading) {
        item { FeaturedSkeletonRow() }
        items(SKELETON_COUNT) { ListSkeleton(modifier = Modifier.padding(horizontal = gutter)) }
        return
    }
    if (!state.isFiltering && state.featured.isNotEmpty()) {
        item { FeaturedSection(state = state, actions = actions) }
    }
    item {
        SectionHeader(
            title = stringResource(if (state.isFiltering) R.string.home_results else R.string.home_all_venues),
            modifier = Modifier.padding(horizontal = gutter),
        )
    }
    if (state.venues.isEmpty()) {
        item { EmptyResults(onClearFilters = actions.onClearFilters, modifier = Modifier.padding(horizontal = gutter)) }
    } else {
        itemsIndexed(state.venues, key = { _, venue -> venue.id }) { index, venue ->
            StaggeredEntrance(
                index = index.coerceAtMost(MAX_STAGGER_INDEX),
                play = state.playIntro,
                modifier = Modifier.padding(horizontal = gutter),
            ) {
                VenueListCard(
                    venue = venue,
                    saved = venue.id in state.savedIds,
                    onClick = { actions.onVenueClick(venue.id) },
                    onToggleSaved = { actions.onToggleSaved(venue.id) },
                )
            }
        }
    }
}

@Composable
private fun FeaturedSection(
    state: HomeUiState,
    actions: HomeActions,
) {
    Column(verticalArrangement = Arrangement.spacedBy(MaterialTheme.spacing.md)) {
        SectionHeader(
            title = stringResource(R.string.home_featured),
            modifier = Modifier.padding(horizontal = MaterialTheme.spacing.lg),
        )
        LazyRow(
            contentPadding = PaddingValues(horizontal = MaterialTheme.spacing.lg),
            horizontalArrangement = Arrangement.spacedBy(MaterialTheme.spacing.md),
        ) {
            itemsIndexed(state.featured, key = { _, venue -> venue.id }) { index, venue ->
                StaggeredEntrance(index = index, play = state.playIntro) {
                    FeaturedVenueCard(
                        venue = venue,
                        saved = venue.id in state.savedIds,
                        onClick = { actions.onVenueClick(venue.id) },
                        onToggleSaved = { actions.onToggleSaved(venue.id) },
                    )
                }
            }
        }
    }
}

@Composable
private fun FeaturedSkeletonRow() {
    LazyRow(
        contentPadding = PaddingValues(horizontal = MaterialTheme.spacing.lg),
        horizontalArrangement = Arrangement.spacedBy(MaterialTheme.spacing.md),
    ) {
        items(2) { FeaturedSkeleton() }
    }
}

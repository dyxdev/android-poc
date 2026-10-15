package com.venuenexa.pocmux.feature.home

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyListState
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Person
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.venuenexa.pocmux.core.context.ContextLabel
import com.venuenexa.pocmux.core.context.ContextSwitcher
import com.venuenexa.pocmux.core.context.rememberContextSwitcher
import com.venuenexa.pocmux.core.designsystem.CategoryChip
import com.venuenexa.pocmux.core.designsystem.SearchField
import com.venuenexa.pocmux.core.designsystem.VenueNexaWordmark
import com.venuenexa.pocmux.core.designsystem.spacing
import com.venuenexa.pocmux.core.model.VenueCategory

@Composable
fun HomeRoute(
    onVenueClick: (String, String) -> Unit,
    onAccountClick: () -> Unit,
    modifier: Modifier = Modifier,
    listState: LazyListState = rememberLazyListState(),
    viewModel: HomeViewModel = hiltViewModel(),
) {
    val state by viewModel.uiState.collectAsStateWithLifecycle()
    // Tras la primera carga la entrada escalonada ya no se repite (al girar o volver a la pestaña).
    LaunchedEffect(state.isLoading) {
        if (!state.isLoading) viewModel.onIntroPlayed()
    }
    HomeScreen(
        state = state,
        switcher = rememberContextSwitcher(),
        actions =
            HomeActions(
                onVenueClick = onVenueClick,
                onAccountClick = onAccountClick,
                onQueryChange = viewModel::onQueryChange,
                onCategorySelected = viewModel::onCategorySelected,
                onToggleSaved = viewModel::onToggleSaved,
                onClearFilters = viewModel::onClearFilters,
            ),
        modifier = modifier,
        listState = listState,
    )
}

@Composable
fun HomeScreen(
    state: HomeUiState,
    switcher: ContextSwitcher,
    actions: HomeActions,
    modifier: Modifier = Modifier,
    listState: LazyListState = rememberLazyListState(),
) {
    val gutter = MaterialTheme.spacing.lg
    LazyColumn(
        modifier = modifier,
        state = listState,
        contentPadding = PaddingValues(vertical = gutter),
        verticalArrangement = Arrangement.spacedBy(gutter),
    ) {
        item {
            HomeHeader(
                switcher = switcher,
                onAccountClick = actions.onAccountClick,
                modifier = Modifier.padding(horizontal = gutter),
            )
        }
        item {
            SearchField(
                value = state.query,
                onValueChange = actions.onQueryChange,
                placeholder = stringResource(R.string.home_search_placeholder),
                clearDescription = stringResource(R.string.home_search_clear),
                modifier = Modifier.padding(horizontal = gutter),
            )
        }
        item { CategoryRow(selected = state.category, onSelected = actions.onCategorySelected) }
        homeContent(state = state, actions = actions, gutter = gutter)
    }
}

@Composable
private fun HomeHeader(
    switcher: ContextSwitcher,
    onAccountClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Row(
        modifier = modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically,
    ) {
        VenueNexaWordmark()
        Row(verticalAlignment = Alignment.CenterVertically) {
            ContextLabel(switcher = switcher)
            // Con más de un perfil, el avatar abre la hoja de perfiles; si no, lleva a la cuenta.
            IconButton(onClick = { if (switcher.state.canSwitch) switcher.onOpen() else onAccountClick() }) {
                Icon(Icons.Outlined.Person, contentDescription = stringResource(R.string.home_account))
            }
        }
    }
}

@Composable
private fun CategoryRow(
    selected: VenueCategory?,
    onSelected: (VenueCategory?) -> Unit,
) {
    LazyRow(
        contentPadding = PaddingValues(horizontal = MaterialTheme.spacing.lg),
        horizontalArrangement = Arrangement.spacedBy(MaterialTheme.spacing.sm),
    ) {
        item {
            CategoryChip(
                label = stringResource(R.string.home_category_all),
                selected = selected == null,
                onClick = { onSelected(null) },
            )
        }
        items(VenueCategory.entries) { category ->
            CategoryChip(
                label = stringResource(category.labelRes()),
                selected = selected == category,
                onClick = { onSelected(category) },
            )
        }
    }
}

@Composable
internal fun EmptyResults(
    onClearFilters: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Column(modifier = modifier, verticalArrangement = Arrangement.spacedBy(MaterialTheme.spacing.sm)) {
        Text(
            text = stringResource(R.string.home_empty_title),
            style = MaterialTheme.typography.titleMedium,
            color = MaterialTheme.colorScheme.onBackground,
        )
        Text(
            text = stringResource(R.string.home_empty_hint),
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
        )
        TextButton(onClick = onClearFilters, shape = MaterialTheme.shapes.small) {
            Text(stringResource(R.string.home_empty_clear))
        }
    }
}

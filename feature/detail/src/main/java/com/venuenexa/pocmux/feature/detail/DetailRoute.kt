package com.venuenexa.pocmux.feature.detail

import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle

// Ruta del detalle: crea el ViewModel con el ID del venue y pasa el estado a la pantalla sin estado.
@Composable
fun VenueDetailRoute(
    venueId: String,
    sharedKey: String,
    onBack: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val viewModel =
        hiltViewModel<DetailViewModel, DetailViewModel.Factory>(
            creationCallback = { factory -> factory.create(venueId) },
        )
    val state by viewModel.uiState.collectAsStateWithLifecycle()
    DetailScreen(
        state = state,
        sharedKey = sharedKey,
        onBack = onBack,
        onToggleSaved = viewModel::onToggleSaved,
        modifier = modifier,
    )
}

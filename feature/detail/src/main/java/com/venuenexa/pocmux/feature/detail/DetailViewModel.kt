package com.venuenexa.pocmux.feature.detail

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.venuenexa.pocmux.core.consent.ConsentGate
import com.venuenexa.pocmux.core.data.SavedVenuesRepository
import com.venuenexa.pocmux.core.data.VenueRepository
import dagger.assisted.Assisted
import dagger.assisted.AssistedFactory
import dagger.assisted.AssistedInject
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn

private const val STOP_TIMEOUT_MS = 5_000L

// Estado del detalle de un venue. El ID llega por la ruta (la compone :app, ADR-AND-0020) por inyección asistida.
@HiltViewModel(assistedFactory = DetailViewModel.Factory::class)
class DetailViewModel
    @AssistedInject
    constructor(
        @Assisted private val venueId: String,
        repository: VenueRepository,
        private val savedVenues: SavedVenuesRepository,
        consentGate: ConsentGate,
    ) : ViewModel() {
        @AssistedFactory
        interface Factory {
            fun create(venueId: String): DetailViewModel
        }

        val uiState: StateFlow<DetailUiState> =
            combine(repository.venue(venueId), savedVenues.savedIds, consentGate.granted) { venue, saved, granted ->
                if (venue == null) {
                    DetailUiState.NotFound
                } else {
                    DetailUiState.Content(venue = venue, saved = venueId in saved, analyticsConsent = granted)
                }
            }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(STOP_TIMEOUT_MS), DetailUiState.Loading)

        fun onToggleSaved() = savedVenues.toggle(venueId)
    }

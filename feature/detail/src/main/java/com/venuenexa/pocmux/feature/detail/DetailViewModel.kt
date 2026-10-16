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

// Estado del detalle de un venue. Eagerly: el venue es local e inmediato, así el primer fotograma ya tiene la
// foto de destino de la transición compartida. El ID llega por la ruta (la compone :app, ADR-AND-0020).
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
            }.stateIn(viewModelScope, SharingStarted.Eagerly, DetailUiState.Loading)

        fun onToggleSaved() = savedVenues.toggle(venueId)
    }

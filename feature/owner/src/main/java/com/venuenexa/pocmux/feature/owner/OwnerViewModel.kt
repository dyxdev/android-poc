package com.venuenexa.pocmux.feature.owner

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.venuenexa.pocmux.core.data.VenueRepository
import com.venuenexa.pocmux.core.model.Venue
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import javax.inject.Inject

private const val STOP_TIMEOUT_MS = 5_000L
private const val OWNED_VENUES = 2

// «Mis espacios» de ejemplo: los dos primeros del catálogo. null mientras carga.
@HiltViewModel
class OwnerViewModel
    @Inject
    constructor(
        repository: VenueRepository,
    ) : ViewModel() {
        val venues: StateFlow<List<Venue>?> =
            repository
                .venues()
                .map { it.take(OWNED_VENUES) }
                .stateIn(viewModelScope, SharingStarted.WhileSubscribed(STOP_TIMEOUT_MS), null)
    }

package com.venuenexa.pocmux.feature.account

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.venuenexa.pocmux.core.data.VenueRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import javax.inject.Inject

private const val STOP_TIMEOUT_MS = 5_000L
private const val COVER_COUNT = 3

// Las portadas del collage de la bienvenida: las de los primeros venues destacados. null mientras cargan.
@HiltViewModel
class WelcomeViewModel
    @Inject
    constructor(
        repository: VenueRepository,
    ) : ViewModel() {
        val covers: StateFlow<List<String>?> =
            repository
                .venues()
                .map { venues -> venues.filter { it.isFeatured }.take(COVER_COUNT).map { it.photos.first() } }
                .stateIn(viewModelScope, SharingStarted.WhileSubscribed(STOP_TIMEOUT_MS), null)
    }

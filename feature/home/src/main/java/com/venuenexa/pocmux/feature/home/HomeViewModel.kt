package com.venuenexa.pocmux.feature.home

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.venuenexa.pocmux.core.data.VenueRepository
import com.venuenexa.pocmux.core.model.Venue
import com.venuenexa.pocmux.core.model.VenueCategory
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import javax.inject.Inject

private const val STOP_TIMEOUT_MS = 5_000L

// Estado de Inicio: catálogo + búsqueda + categoría + guardados (solo en memoria en esta entrega).
@HiltViewModel
class HomeViewModel
    @Inject
    constructor(
        repository: VenueRepository,
    ) : ViewModel() {
        private val catalog: StateFlow<List<Venue>?> =
            repository.venues().stateIn(viewModelScope, SharingStarted.Lazily, null)
        private val query = MutableStateFlow("")
        private val category = MutableStateFlow<VenueCategory?>(null)
        private val saved = MutableStateFlow<Set<String>>(emptySet())
        private val introPlayed = MutableStateFlow(false)

        val uiState: StateFlow<HomeUiState> =
            combine(catalog, query, category, saved, introPlayed) { venues, q, c, s, played ->
                val all = venues.orEmpty()
                HomeUiState(
                    isLoading = venues == null,
                    query = q,
                    category = c,
                    featured = all.filter { it.isFeatured },
                    venues = all.filter { it.matches(q, c) },
                    savedIds = s,
                    playIntro = !played,
                )
            }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(STOP_TIMEOUT_MS), HomeUiState())

        fun onQueryChange(value: String) = query.update { value }

        // Pulsar la categoría activa la quita; «Todos» es null.
        fun onCategorySelected(value: VenueCategory?) = category.update { if (it == value) null else value }

        fun onClearFilters() {
            query.update { "" }
            category.update { null }
        }

        fun onToggleSaved(venueId: String) = saved.update { if (venueId in it) it - venueId else it + venueId }

        fun onIntroPlayed() = introPlayed.update { true }
    }

private fun Venue.matches(
    query: String,
    category: VenueCategory?,
): Boolean {
    val text = query.trim()
    val textOk = text.isEmpty() || name.contains(text, ignoreCase = true) || city.contains(text, ignoreCase = true)
    return textOk && (category == null || this.category == category)
}

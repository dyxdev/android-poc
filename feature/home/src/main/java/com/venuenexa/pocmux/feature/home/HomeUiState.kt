package com.venuenexa.pocmux.feature.home

import com.venuenexa.pocmux.core.model.Venue
import com.venuenexa.pocmux.core.model.VenueCategory

data class HomeUiState(
    val isLoading: Boolean = true,
    val query: String = "",
    val category: VenueCategory? = null,
    // Destacados: solo se muestran sin búsqueda ni categoría.
    val featured: List<Venue> = emptyList(),
    // Resultado de la búsqueda y la categoría (todo el catálogo si no hay filtro).
    val venues: List<Venue> = emptyList(),
    val savedIds: Set<String> = emptySet(),
    // Verdadero hasta que se ha visto la primera entrada escalonada: no se repite al girar ni al volver.
    val playIntro: Boolean = true,
) {
    val isFiltering: Boolean get() = query.isNotBlank() || category != null
}

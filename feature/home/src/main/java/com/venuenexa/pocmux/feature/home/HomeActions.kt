package com.venuenexa.pocmux.feature.home

import com.venuenexa.pocmux.core.model.VenueCategory

// Acciones de Inicio agrupadas: la pantalla recibe un solo parámetro de eventos.
data class HomeActions(
    val onVenueClick: (String) -> Unit,
    val onAccountClick: () -> Unit,
    val onQueryChange: (String) -> Unit,
    val onCategorySelected: (VenueCategory?) -> Unit,
    val onToggleSaved: (String) -> Unit,
    val onClearFilters: () -> Unit,
)

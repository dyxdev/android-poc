package com.venuenexa.pocmux.feature.home

import com.venuenexa.pocmux.core.model.VenueCategory

// Acciones de Inicio agrupadas: la pantalla recibe un solo parámetro de eventos.
data class HomeActions(
    // ID del venue y clave de la foto compartida de la tarjeta pulsada.
    val onVenueClick: (String, String) -> Unit,
    val onAccountClick: () -> Unit,
    val onQueryChange: (String) -> Unit,
    val onCategorySelected: (VenueCategory?) -> Unit,
    val onToggleSaved: (String) -> Unit,
    val onClearFilters: () -> Unit,
)

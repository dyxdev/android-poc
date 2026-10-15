package com.venuenexa.pocmux.core.data

import com.venuenexa.pocmux.core.model.Venue
import kotlinx.coroutines.flow.Flow

// Fuente de venues de la app. Hoy solo hay datos de ejemplo (DemoVenueRepository); la API de venues
// la sustituirá sin cambiar a los consumidores.
interface VenueRepository {
    fun venues(): Flow<List<Venue>>
}

package com.venuenexa.pocmux.core.data

import kotlinx.coroutines.flow.StateFlow

// Venues guardados (corazón). Inicio y Detalle comparten el mismo estado. Hoy solo en memoria: se pierde al
// cerrar la app; persistirlos (cuenta del usuario o Room) llega con la API.
interface SavedVenuesRepository {
    val savedIds: StateFlow<Set<String>>

    fun toggle(venueId: String)
}

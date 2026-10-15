package com.venuenexa.pocmux

import kotlinx.serialization.Serializable

// Rutas tipadas (ADR-AND-0003). Se componen aquí y pasan solo IDs a las features (ADR-AND-0020).
@Serializable
data object MainDestination

// sharedKey: clave de la foto que viaja de la tarjeta al detalle (transición compartida).
@Serializable
data class VenueDetailDestination(
    val venueId: String,
    val sharedKey: String,
)

// Herramientas de diagnóstico (simulador de capacidades y restablecer avisos) y el laboratorio de Mux.
@Serializable
data object DiagnosticsDestination

@Serializable
data object LabDestination

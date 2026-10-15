package com.venuenexa.pocmux.core.model

// Las seis categorías de la web (landing-content.ts).
enum class VenueCategory { WEDDINGS, CORPORATE, SHOOTS, BIRTHDAYS, MEETINGS, POPUPS }

// Un venue tal como lo ve la app. Datos de ejemplo hasta que exista la API de venues.
data class Venue(
    val id: String,
    val name: String,
    val city: String,
    val category: VenueCategory,
    val capacity: Int,
    // Euros por día.
    val pricePerDay: Int,
    val rating: Double,
    val reviewCount: Int,
    // URLs de fotos; la primera es la portada.
    val photos: List<String>,
    // Vídeo tour de Mux (playback ID público), si lo tiene.
    val tourPlaybackId: String?,
    val isAvailable: Boolean,
    val isFeatured: Boolean,
)

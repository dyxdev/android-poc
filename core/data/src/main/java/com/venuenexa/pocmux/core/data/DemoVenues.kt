package com.venuenexa.pocmux.core.data

import com.venuenexa.pocmux.core.model.Venue
import com.venuenexa.pocmux.core.model.VenueCategory

// Datos de EJEMPLO, ficticios: nombres, precios, capacidades y valoraciones no son reales.
// Las fotos son las de Unsplash que ya usa la web del PoC (licencia libre, comprobadas el 2026-10-15) y los
// vídeos, los dos playback IDs públicos de Mux del catálogo de la web.
private fun photo(id: String) = "https://images.unsplash.com/photo-$id?auto=format&fit=crop&w=900&q=70"

private val Garden = photo("1696204868903-91d809b4df09")
private val Loft = photo("1779300007516-eebee9151c19")
private val Villa = photo("1543489822-c49534f3271f")
private val Weddings = photo("1708569176850-9de9aa6b179b")
private val Corporate = photo("1540575467063-178a50c2df87")
private val Shoots = photo("1647427854253-b92bb40c9330")
private val Birthdays = photo("1530103862676-de8c9debad1d")
private val Meetings = photo("1716703435453-a7733d600d68")
private val Popups = photo("1766384093125-f00a4eb8a392")

private const val TOUR_ONE = "EcHgOK9coz5K4rjSwOkoE7Y7O01201YMIC200RI6lNxnhs"
private const val TOUR_TWO = "DS00Spx1CV902MCtPj5WknGlR102V5HFkDe"

internal val DemoVenues: List<Venue> =
    listOf(
        Venue(
            id = "jardin-acacias",
            name = "Jardín de las Acacias",
            city = "Sevilla",
            category = VenueCategory.WEDDINGS,
            capacity = 180,
            pricePerDay = 1450,
            rating = 4.9,
            reviewCount = 212,
            photos = listOf(Garden, Villa, Weddings),
            tourPlaybackId = TOUR_ONE,
            isAvailable = true,
            isFeatured = true,
        ),
        Venue(
            id = "loft-gracia",
            name = "Loft Industrial Gràcia",
            city = "Barcelona",
            category = VenueCategory.SHOOTS,
            capacity = 60,
            pricePerDay = 620,
            rating = 4.8,
            reviewCount = 96,
            photos = listOf(Loft, Shoots, Meetings),
            tourPlaybackId = TOUR_TWO,
            isAvailable = true,
            isFeatured = true,
        ),
        Venue(
            id = "villa-mirador",
            name = "Villa Mirador",
            city = "Málaga",
            category = VenueCategory.WEDDINGS,
            capacity = 120,
            pricePerDay = 2100,
            rating = 4.9,
            reviewCount = 143,
            photos = listOf(Villa, Garden, Birthdays),
            tourPlaybackId = TOUR_ONE,
            isAvailable = false,
            isFeatured = true,
        ),
        Venue(
            id = "salon-aurora",
            name = "Salón Aurora",
            city = "Madrid",
            category = VenueCategory.WEDDINGS,
            capacity = 240,
            pricePerDay = 1900,
            rating = 4.7,
            reviewCount = 310,
            photos = listOf(Weddings, Garden, Corporate),
            tourPlaybackId = TOUR_TWO,
            isAvailable = true,
            isFeatured = false,
        ),
        Venue(
            id = "auditorio-norte",
            name = "Auditorio Norte",
            city = "Bilbao",
            category = VenueCategory.CORPORATE,
            capacity = 400,
            pricePerDay = 2800,
            rating = 4.6,
            reviewCount = 88,
            photos = listOf(Corporate, Meetings, Loft),
            tourPlaybackId = null,
            isAvailable = true,
            isFeatured = false,
        ),
        Venue(
            id = "estudio-cenital",
            name = "Estudio Cenital",
            city = "Valencia",
            category = VenueCategory.SHOOTS,
            capacity = 30,
            pricePerDay = 380,
            rating = 4.8,
            reviewCount = 64,
            photos = listOf(Shoots, Loft, Popups),
            tourPlaybackId = null,
            isAvailable = false,
            isFeatured = false,
        ),
        Venue(
            id = "terraza-confeti",
            name = "Terraza Confeti",
            city = "Madrid",
            category = VenueCategory.BIRTHDAYS,
            capacity = 80,
            pricePerDay = 540,
            rating = 4.5,
            reviewCount = 57,
            photos = listOf(Birthdays, Garden, Weddings),
            tourPlaybackId = null,
            isAvailable = true,
            isFeatured = false,
        ),
        Venue(
            id = "sala-atlas",
            name = "Sala Atlas",
            city = "Barcelona",
            category = VenueCategory.MEETINGS,
            capacity = 24,
            pricePerDay = 210,
            rating = 4.7,
            reviewCount = 121,
            photos = listOf(Meetings, Corporate, Loft),
            tourPlaybackId = null,
            isAvailable = true,
            isFeatured = false,
        ),
        Venue(
            id = "nave-poblenou",
            name = "Nave Pop-up Poblenou",
            city = "Barcelona",
            category = VenueCategory.POPUPS,
            capacity = 150,
            pricePerDay = 760,
            rating = 4.4,
            reviewCount = 39,
            photos = listOf(Popups, Loft, Shoots),
            tourPlaybackId = null,
            isAvailable = true,
            isFeatured = false,
        ),
    )

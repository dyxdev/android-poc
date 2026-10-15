package com.venuenexa.pocmux.feature.home

import androidx.compose.runtime.Composable
import androidx.compose.ui.tooling.preview.Preview
import com.venuenexa.pocmux.core.designsystem.VenueNexaTheme
import com.venuenexa.pocmux.core.model.Venue
import com.venuenexa.pocmux.core.model.VenueCategory

private val SampleVenues =
    listOf(
        sampleVenue(id = "a", name = "Jardín de las Acacias", city = "Sevilla", isAvailable = true),
        sampleVenue(id = "b", name = "Loft Industrial Gràcia", city = "Barcelona", isAvailable = true),
        sampleVenue(id = "c", name = "Villa Mirador", city = "Málaga", isAvailable = false),
    )

private val NoActions = HomeActions({}, {}, {}, {}, {}, {})

private fun sampleVenue(
    id: String,
    name: String,
    city: String,
    isAvailable: Boolean,
) = Venue(
    id = id,
    name = name,
    city = city,
    category = VenueCategory.WEDDINGS,
    capacity = 180,
    pricePerDay = 1450,
    rating = 4.9,
    reviewCount = 212,
    photos = listOf("preview://1", "preview://2", "preview://3"),
    tourPlaybackId = null,
    isAvailable = isAvailable,
    isFeatured = true,
)

@Preview(name = "Inicio", showBackground = true, widthDp = 360, heightDp = 780)
@Composable
private fun HomeLoadedPreview() {
    VenueNexaTheme {
        HomeScreen(
            state = HomeUiState(isLoading = false, featured = SampleVenues, venues = SampleVenues, playIntro = false),
            actions = NoActions,
        )
    }
}

@Preview(name = "Inicio, fuente 1,5x", showBackground = true, widthDp = 360, heightDp = 780, fontScale = 1.5f)
@Composable
private fun HomeLargeFontPreview() {
    VenueNexaTheme {
        HomeScreen(
            state = HomeUiState(isLoading = false, featured = SampleVenues, venues = SampleVenues, playIntro = false),
            actions = NoActions,
        )
    }
}

@Preview(name = "Inicio, cargando", showBackground = true, widthDp = 360, heightDp = 780)
@Composable
private fun HomeLoadingPreview() {
    VenueNexaTheme { HomeScreen(state = HomeUiState(), actions = NoActions) }
}

@Preview(name = "Inicio, sin resultados", showBackground = true, widthDp = 360, heightDp = 780)
@Composable
private fun HomeEmptyPreview() {
    VenueNexaTheme {
        HomeScreen(
            state = HomeUiState(isLoading = false, query = "zzz", playIntro = false),
            actions = NoActions,
        )
    }
}

package com.venuenexa.pocmux.feature.detail

import androidx.compose.runtime.Composable
import androidx.compose.ui.tooling.preview.Preview
import com.venuenexa.pocmux.core.designsystem.VenueNexaTheme
import com.venuenexa.pocmux.core.model.Venue
import com.venuenexa.pocmux.core.model.VenueCategory

private val SampleVenue =
    Venue(
        id = "jardin-acacias",
        name = "Jardín de las Acacias",
        city = "Sevilla",
        category = VenueCategory.WEDDINGS,
        capacity = 180,
        pricePerDay = 1450,
        rating = 4.9,
        reviewCount = 212,
        photos = listOf("preview://1", "preview://2", "preview://3"),
        tourPlaybackId = "preview",
        isAvailable = true,
        isFeatured = true,
    )

@Preview(name = "Detalle", showBackground = true, widthDp = 360, heightDp = 780)
@Composable
private fun DetailContentPreview() {
    VenueNexaTheme {
        DetailScreen(
            state = DetailUiState.Content(venue = SampleVenue, saved = true, analyticsConsent = false),
            sharedKey = "list-jardin-acacias",
            onBack = {},
            onToggleSaved = {},
        )
    }
}

@Preview(name = "Detalle, fuente 1,5x", showBackground = true, widthDp = 360, heightDp = 780, fontScale = 1.5f)
@Composable
private fun DetailLargeFontPreview() {
    VenueNexaTheme {
        DetailScreen(
            state = DetailUiState.Content(venue = SampleVenue, saved = false, analyticsConsent = true),
            sharedKey = "list-jardin-acacias",
            onBack = {},
            onToggleSaved = {},
        )
    }
}

@Preview(name = "Detalle, no encontrado", showBackground = true, widthDp = 360, heightDp = 780)
@Composable
private fun DetailNotFoundPreview() {
    VenueNexaTheme {
        DetailScreen(state = DetailUiState.NotFound, sharedKey = "", onBack = {}, onToggleSaved = {})
    }
}

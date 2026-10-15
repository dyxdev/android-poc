package com.venuenexa.pocmux.feature.detail

import com.venuenexa.pocmux.core.model.Venue

sealed interface DetailUiState {
    data object Loading : DetailUiState

    // El venue ya no existe (borrado o enlace antiguo).
    data object NotFound : DetailUiState

    data class Content(
        val venue: Venue,
        val saved: Boolean,
        // Si hay consentimiento de analítica; el vídeo suena igual, solo cambia lo que se envía a Mux Data.
        val analyticsConsent: Boolean,
    ) : DetailUiState
}

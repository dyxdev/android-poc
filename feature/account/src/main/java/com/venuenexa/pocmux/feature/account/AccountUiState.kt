package com.venuenexa.pocmux.feature.account

import com.venuenexa.pocmux.core.identity.IdentityState

data class AccountUiState(
    val identity: IdentityState = IdentityState(),
    // Consentimiento de la analítica de reproducción (ADR-AND-0032); apagado por defecto.
    val analyticsConsent: Boolean = false,
)

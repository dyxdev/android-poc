package com.venuenexa.pocmux.feature.playerlab

import com.venuenexa.pocmux.core.consent.NetworkCounters
import com.venuenexa.pocmux.core.identity.IdentityState

// Estado único de la pantalla (UDF): el ViewModel lo expone como StateFlow.
data class PlayerLabUiState(
    val consentGranted: Boolean = false,
    val mode: PlayerMode = PlayerMode.MUX_GATED,
    val playbackId: String? = null,
    val token: String? = null,
    val isLoadingToken: Boolean = false,
    val tokenError: Boolean = false,
    val counters: NetworkCounters = NetworkCounters(),
    val identity: IdentityState = IdentityState(),
)

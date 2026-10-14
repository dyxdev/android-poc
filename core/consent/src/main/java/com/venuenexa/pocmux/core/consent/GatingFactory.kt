package com.venuenexa.pocmux.core.consent

import com.mux.stats.sdk.muxstats.INetworkRequest
import javax.inject.Inject

// Crea la puerta con su delegado real. Existe para romper el ciclo Hilt
// (el gate posee la puerta; la puerta no conoce al gate).
// Pública porque la necesita el grafo Hilt generado en :app; el resto sigue internal.
class GatingFactory
    @Inject
    constructor(
        private val delegate: INetworkRequest,
    ) {
        fun create(isGranted: () -> Boolean): ConsentGatingNetworkRequest =
            ConsentGatingNetworkRequest(delegate, isGranted)
    }

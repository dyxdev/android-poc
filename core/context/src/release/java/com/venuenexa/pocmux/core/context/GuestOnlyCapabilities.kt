package com.venuenexa.pocmux.core.context

import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.flowOf
import javax.inject.Inject
import javax.inject.Singleton

// Release: sin endpoint de capacidades (ADR-AND-0026) solo existe el contexto de cliente, y el simulador no hace
// nada. Cuando exista el endpoint real, sustituye a esta clase. Público para el grafo Hilt de :app.
@Singleton
class GuestOnlyCapabilities
    @Inject
    constructor() : CapabilitiesSource, CapabilitySimulator {
        override val available: Flow<Set<UserContext>> = flowOf(setOf(UserContext.GUEST))

        override val granted: StateFlow<Set<UserContext>> = MutableStateFlow(emptySet<UserContext>()).asStateFlow()

        override fun setGranted(
            context: UserContext,
            granted: Boolean,
        ) = Unit
    }

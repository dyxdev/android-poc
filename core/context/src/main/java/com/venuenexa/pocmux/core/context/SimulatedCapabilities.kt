package com.venuenexa.pocmux.core.context

import com.venuenexa.pocmux.core.identity.IdentitySession
import com.venuenexa.pocmux.core.identity.IdentityStatus
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.update
import javax.inject.Inject
import javax.inject.Singleton

// Capacidades SIMULADAS (decisión de Andy, 2026-10-15): sin sesión solo hay contexto de cliente; con sesión, el
// de cliente más los que se hayan concedido en el simulador (por defecto, propietario). No salen del token
// (ADR-AND-0026: la app nunca deduce permisos de los roles del token). Público para el grafo Hilt de :app.
@Singleton
class SimulatedCapabilities
    @Inject
    constructor(
        identity: IdentitySession,
    ) : CapabilitiesSource, CapabilitySimulator {
        private val mutableGranted = MutableStateFlow(setOf(UserContext.OWNER))

        override val granted: StateFlow<Set<UserContext>> = mutableGranted.asStateFlow()

        override val available: Flow<Set<UserContext>> =
            combine(identity.state, mutableGranted) { state, extra ->
                if (state.status == IdentityStatus.SIGNED_IN) extra + UserContext.GUEST else setOf(UserContext.GUEST)
            }

        override fun setGranted(
            context: UserContext,
            granted: Boolean,
        ) {
            if (context == UserContext.GUEST) return
            mutableGranted.update { if (granted) it + context else it - context }
        }
    }

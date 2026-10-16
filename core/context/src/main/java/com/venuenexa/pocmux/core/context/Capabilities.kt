package com.venuenexa.pocmux.core.context

import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.StateFlow

// Qué contextos puede usar el usuario. La fuente real será el endpoint de capacidades de la API (ADR-AND-0026),
// que aún no existe: mientras tanto la implementa SimulatedCapabilities (solo en el source set de debug; release
// usa GuestOnlyCapabilities).
interface CapabilitiesSource {
    val available: Flow<Set<UserContext>>
}

// Control de la simulación, solo para diagnóstico. Desaparece cuando exista el endpoint real.
interface CapabilitySimulator {
    val granted: StateFlow<Set<UserContext>>

    fun setGranted(
        context: UserContext,
        granted: Boolean,
    )
}

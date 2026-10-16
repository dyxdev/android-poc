package com.venuenexa.pocmux.core.context

import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.StateFlow
import javax.inject.Qualifier

// Qué contextos puede usar el usuario. La fuente real será el endpoint de capacidades de la API (ADR-AND-0026),
// que aún no existe: mientras tanto la implementa SimulatedCapabilities.
interface CapabilitiesSource {
    val available: Flow<Set<UserContext>>
}

// Si el simulador concede el contexto de propietario al arrancar. Lo provee :app (solo en debug).
@Qualifier
@Retention(AnnotationRetention.BINARY)
annotation class SimulatedOwnerGranted

// Control de la simulación, solo para diagnóstico. Desaparece cuando exista el endpoint real.
interface CapabilitySimulator {
    val granted: StateFlow<Set<UserContext>>

    fun setGranted(
        context: UserContext,
        granted: Boolean,
    )
}

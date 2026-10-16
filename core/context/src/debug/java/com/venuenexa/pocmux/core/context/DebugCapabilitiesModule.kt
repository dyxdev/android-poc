package com.venuenexa.pocmux.core.context

import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent

// Solo debug: los dos contratos de la simulación se atienden con la misma instancia.
@Module
@InstallIn(SingletonComponent::class)
abstract class DebugCapabilitiesModule {
    @Binds
    abstract fun bindCapabilitiesSource(impl: SimulatedCapabilities): CapabilitiesSource

    @Binds
    abstract fun bindCapabilitySimulator(impl: SimulatedCapabilities): CapabilitySimulator
}

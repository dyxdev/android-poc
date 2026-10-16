package com.venuenexa.pocmux.core.context

import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent

@Module
@InstallIn(SingletonComponent::class)
abstract class ReleaseCapabilitiesModule {
    @Binds
    abstract fun bindCapabilitiesSource(impl: GuestOnlyCapabilities): CapabilitiesSource

    @Binds
    abstract fun bindCapabilitySimulator(impl: GuestOnlyCapabilities): CapabilitySimulator
}

package com.venuenexa.pocmux

import com.venuenexa.pocmux.core.context.SimulatedOwnerGranted
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent

// Las capacidades simuladas (contexto de propietario) solo existen en debug: un APK de release no las concede.
// Desaparece cuando exista el endpoint de capacidades de la API (ADR-AND-0026).
@Module
@InstallIn(SingletonComponent::class)
object SimulationModule {
    @Provides
    @SimulatedOwnerGranted
    fun provideSimulatedOwnerGranted(): Boolean = BuildConfig.DEBUG
}

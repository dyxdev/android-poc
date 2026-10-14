package com.venuenexa.pocmux.feature.playerlab

import com.venuenexa.pocmux.core.consent.ConsentGatingNetworkRequest
import dagger.hilt.EntryPoint
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent

// EntryPoint para obtener la puerta (SingletonComponent) dentro de la composición.
@EntryPoint
@InstallIn(SingletonComponent::class)
interface GatingEntryPoint {
    fun gating(): ConsentGatingNetworkRequest
}

package com.venuenexa.pocmux.feature.detail

import com.venuenexa.pocmux.core.consent.ConsentGatingNetworkRequest
import dagger.hilt.EntryPoint
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent

// Puerta de consentimiento (SingletonComponent) para el reproductor del tour, igual que en el laboratorio.
@EntryPoint
@InstallIn(SingletonComponent::class)
interface DetailGatingEntryPoint {
    fun gating(): ConsentGatingNetworkRequest
}

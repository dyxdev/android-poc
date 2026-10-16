package com.venuenexa.pocmux.core.consent

import android.content.Context
import com.mux.player.MuxPlayer
import com.mux.stats.sdk.muxstats.ExoPlayerBinding
import dagger.hilt.EntryPoint
import dagger.hilt.EntryPoints
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent

// ÚNICO sitio donde se configura el reproductor que respeta el consentimiento (ADR-AND-0032): Mux Player con
// ExoPlayerBinding y la puerta como red. Sin el binding, MuxPlayer crea su propia red de Mux Data y la puerta no
// sirve de nada (caso control del laboratorio). El laboratorio y el detalle lo usan; si esta configuración
// cambiara en un solo sitio, la puerta se rompería sin avisar.
object GatedMuxPlayer {
    fun create(
        context: Context,
        gating: ConsentGatingNetworkRequest,
    ): MuxPlayer =
        MuxPlayer.Builder(context)
            .addExoPlayerBinding(ExoPlayerBinding())
            .addNetwork(gating)
            .enableSmartCache(false)
            .build()
}

// La puerta (SingletonComponent) fuera de Hilt, para el reproductor que se crea dentro de la composición.
@EntryPoint
@InstallIn(SingletonComponent::class)
interface ConsentGatingEntryPoint {
    fun gating(): ConsentGatingNetworkRequest
}

fun consentGating(context: Context): ConsentGatingNetworkRequest =
    EntryPoints.get(context.applicationContext, ConsentGatingEntryPoint::class.java).gating()

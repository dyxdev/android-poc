package com.venuenexa.pocmux.feature.playerlab

import android.content.Context
import androidx.annotation.OptIn
import androidx.media3.common.MediaItem
import androidx.media3.common.util.UnstableApi
import androidx.media3.datasource.DefaultDataSource
import androidx.media3.exoplayer.ExoPlayer
import androidx.media3.exoplayer.hls.HlsMediaSource
import com.mux.player.MuxPlayer
import com.mux.player.media.MediaItems
import com.venuenexa.pocmux.core.consent.ConsentGatingNetworkRequest
import com.venuenexa.pocmux.core.consent.GatedMuxPlayer

// Crea el reproductor según el modo. Verificado contra MuxPlayer.kt (main):
// - El modo candidato (MUX_GATED) lo crea GatedMuxPlayer (core:consent), el mismo que usa la app.
// - Sin binding, MuxPlayer crea MuxStatsSdkMedia3 SIN la red propia (puerta inútil: caso control).
// - enableSmartCache(false) como pide el prompt.
internal object PlayerFactory {
    fun create(
        context: Context,
        mode: PlayerMode,
        gating: ConsentGatingNetworkRequest,
    ): ExoPlayer =
        when (mode) {
            PlayerMode.MUX_GATED -> GatedMuxPlayer.create(context, gating)

            PlayerMode.MUX_UNGATED ->
                MuxPlayer.Builder(context)
                    .addNetwork(gating)
                    .enableSmartCache(false)
                    .build()

            PlayerMode.MEDIA3_ONLY -> ExoPlayer.Builder(context).build()
        }

    @OptIn(UnstableApi::class)
    fun prepare(
        player: ExoPlayer,
        context: Context,
        mode: PlayerMode,
        playbackId: String,
        token: String?,
    ) {
        when (mode) {
            PlayerMode.MUX_GATED, PlayerMode.MUX_UNGATED -> {
                // token null = playback público; con token = reproducción firmada (MediaItems.kt, 1.6.0).
                player.setMediaItem(MediaItems.fromMuxPlaybackId(playbackId, playbackToken = token))
            }

            PlayerMode.MEDIA3_ONLY -> {
                val hlsUrl = "https://stream.mux.com/$playbackId.m3u8" + (token?.let { "?token=$it" } ?: "")
                val source =
                    HlsMediaSource.Factory(DefaultDataSource.Factory(context))
                        .createMediaSource(MediaItem.fromUri(hlsUrl))
                player.setMediaSource(source)
            }
        }
        player.prepare()
        player.playWhenReady = true
    }
}

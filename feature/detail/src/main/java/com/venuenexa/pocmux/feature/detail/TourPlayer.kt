package com.venuenexa.pocmux.feature.detail

import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.viewinterop.AndroidView
import androidx.media3.ui.PlayerView
import com.mux.player.MuxPlayer
import com.mux.player.media.MediaItems
import com.mux.stats.sdk.muxstats.ExoPlayerBinding
import com.venuenexa.pocmux.core.consent.ConsentGatingNetworkRequest
import dagger.hilt.EntryPoints

// Reproductor del tour: el candidato del laboratorio (Mux Player + puerta de consentimiento + ExoPlayerBinding),
// con playback ID público. El player vive en la composición y se libera al salir. Sin consentimiento el vídeo
// suena igual y la puerta descarta los envíos de analítica (ADR-AND-0032).
@Composable
internal fun TourPlayer(
    playbackId: String,
    modifier: Modifier = Modifier,
) {
    val context = LocalContext.current
    val gating: ConsentGatingNetworkRequest =
        remember(context) {
            EntryPoints.get(context.applicationContext, DetailGatingEntryPoint::class.java).gating()
        }
    val playerView = remember { PlayerView(context) }
    DisposableEffect(playbackId) {
        val player =
            MuxPlayer.Builder(context)
                .addExoPlayerBinding(ExoPlayerBinding())
                .addNetwork(gating)
                .enableSmartCache(false)
                .build()
        player.setMediaItem(MediaItems.fromMuxPlaybackId(playbackId))
        player.prepare()
        player.playWhenReady = true
        playerView.player = player
        onDispose {
            playerView.player = null
            player.release()
        }
    }
    AndroidView(factory = { playerView }, modifier = modifier)
}

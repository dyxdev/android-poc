package com.venuenexa.pocmux.feature.detail

import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.viewinterop.AndroidView
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.compose.LifecycleEventEffect
import androidx.media3.ui.PlayerView
import com.mux.player.MuxPlayer
import com.mux.player.media.MediaItems
import com.venuenexa.pocmux.core.consent.GatedMuxPlayer
import com.venuenexa.pocmux.core.consent.consentGating

// Reproductor del tour: el candidato del laboratorio (GatedMuxPlayer: Mux Player + puerta de consentimiento +
// ExoPlayerBinding), con playback ID público. Sin consentimiento el vídeo suena igual y la puerta descarta los
// envíos de analítica (ADR-AND-0032). El player se crea y se libera en el mismo efecto (nunca queda uno suelto).
@Composable
internal fun TourPlayer(
    playbackId: String,
    modifier: Modifier = Modifier,
) {
    val context = LocalContext.current
    val gating = remember(context) { consentGating(context) }
    val playerView = remember { PlayerView(context) }
    val current = remember { mutableStateOf<MuxPlayer?>(null) }
    DisposableEffect(playbackId) {
        val player = GatedMuxPlayer.create(context, gating)
        player.setMediaItem(MediaItems.fromMuxPlaybackId(playbackId))
        player.prepare()
        player.playWhenReady = true
        playerView.player = player
        current.value = player
        onDispose {
            current.value = null
            playerView.player = null
            player.release()
        }
    }
    // Al pasar la app a segundo plano el vídeo se pausa (si no, seguiría sonando sin pantalla).
    LifecycleEventEffect(Lifecycle.Event.ON_STOP) { current.value?.pause() }
    AndroidView(factory = { playerView }, modifier = modifier)
}

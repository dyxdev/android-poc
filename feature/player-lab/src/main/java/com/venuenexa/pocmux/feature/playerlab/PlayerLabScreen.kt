package com.venuenexa.pocmux.feature.playerlab

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.media3.ui.PlayerView
import com.venuenexa.pocmux.core.consent.ConsentGatingNetworkRequest
import com.venuenexa.pocmux.feature.playerlab.R as LabR

// UI sin estado: previsualizable (ver PlayerLabPreviews).
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PlayerLabContent(
    state: PlayerLabUiState,
    onConsentChange: (Boolean) -> Unit,
    onModeChange: (PlayerMode) -> Unit,
    onLoadToken: () -> Unit,
    onResetCounters: () -> Unit,
    modifier: Modifier = Modifier,
    gating: ConsentGatingNetworkRequest? = null,
) {
    Scaffold(
        modifier = modifier.fillMaxSize(),
        topBar = { TopAppBar(title = { Text(stringResource(LabR.string.lab_title)) }) },
    ) { insets ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(insets)
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            ConsentRow(granted = state.consentGranted, onChange = onConsentChange)
            ModeRow(mode = state.mode, onChange = onModeChange)
            TokenRow(
                ready = state.playbackId != null,
                loading = state.isLoadingToken,
                error = state.tokenError,
                onLoad = onLoadToken,
            )
            val playbackId = state.playbackId
            if (playbackId != null && gating != null) {
                PlayerSlot(mode = state.mode, playbackId = playbackId, token = state.token, gating = gating)
            } else {
                Text(
                    text = stringResource(LabR.string.lab_no_token),
                    style = MaterialTheme.typography.bodyMedium,
                )
            }
            CountersPanel(state = state, onReset = onResetCounters)
        }
    }
}

@Composable
private fun ConsentRow(granted: Boolean, onChange: (Boolean) -> Unit, modifier: Modifier = Modifier) {
    Row(
        modifier = modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Text(text = stringResource(LabR.string.lab_consent), style = MaterialTheme.typography.titleMedium)
        Switch(checked = granted, onCheckedChange = onChange)
    }
}

@Composable
private fun ModeRow(mode: PlayerMode, onChange: (PlayerMode) -> Unit, modifier: Modifier = Modifier) {
    Column(modifier = modifier.fillMaxWidth(), verticalArrangement = Arrangement.spacedBy(4.dp)) {
        Text(text = stringResource(LabR.string.lab_mode), style = MaterialTheme.typography.titleMedium)
        PlayerMode.entries.forEach { entry ->
            FilterChip(
                selected = mode == entry,
                onClick = { onChange(entry) },
                label = { Text(text = stringResource(modeLabel(entry))) },
            )
        }
    }
}

private fun modeLabel(mode: PlayerMode): Int = when (mode) {
    PlayerMode.MUX_GATED -> LabR.string.lab_mode_gated
    PlayerMode.MUX_UNGATED -> LabR.string.lab_mode_ungated
    PlayerMode.MEDIA3_ONLY -> LabR.string.lab_mode_media3
}

@Composable
private fun TokenRow(ready: Boolean, loading: Boolean, error: Boolean, onLoad: () -> Unit) {
    Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
        Button(onClick = onLoad, enabled = !loading) {
            Text(stringResource(LabR.string.lab_load_token))
        }
        when {
            loading -> CircularProgressIndicator()
            ready -> Text(stringResource(LabR.string.lab_token_ready))
            error -> Text(stringResource(LabR.string.lab_token_error))
        }
    }
}

// Reproductor dentro de Compose con AndroidView + PlayerView, como el ejemplo
// oficial OfflinePlayerScreen.kt de mux-player-android. El player vive en la
// composición (se libera al salir) y se recrea al cambiar modo o token.
@Composable
private fun PlayerSlot(
    mode: PlayerMode,
    playbackId: String,
    token: String?,
    gating: ConsentGatingNetworkRequest,
    modifier: Modifier = Modifier,
) {
    val context = LocalContext.current
    val playerView = remember { PlayerView(context) }
    DisposableEffect(mode, playbackId, token) {
        val player = PlayerFactory.create(context, mode, gating)
        PlayerFactory.prepare(player, context, mode, playbackId, token)
        playerView.player = player
        onDispose {
            playerView.player = null
            player.release()
        }
    }
    AndroidView(
        factory = { playerView },
        modifier = modifier
            .fillMaxWidth()
            .height(220.dp),
    )
    Spacer(modifier = Modifier.height(4.dp))
}

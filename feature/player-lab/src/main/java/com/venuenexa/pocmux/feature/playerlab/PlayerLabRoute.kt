package com.venuenexa.pocmux.feature.playerlab

import android.content.Context
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import dagger.hilt.EntryPoints

// Ruta del laboratorio: resuelve la puerta y la pasa al contenido sin estado.
@Composable
fun PlayerLabRoute(
    viewModel: PlayerLabViewModel = hiltViewModel(),
    context: Context,
) {
    val state by viewModel.uiState.collectAsStateWithLifecycle()
    val gating =
        remember(context) {
            EntryPoints.get(context.applicationContext, GatingEntryPoint::class.java).gating()
        }
    PlayerLabContent(
        state = state,
        onConsentChange = viewModel::onConsentChange,
        onModeChange = viewModel::onModeChange,
        onLoadToken = viewModel::onLoadToken,
        onResetCounters = viewModel::onResetCounters,
        gating = gating,
    )
}

package com.venuenexa.pocmux.core.context

import androidx.compose.runtime.Composable
import androidx.compose.runtime.Stable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle

// Lo que una pantalla necesita para ofrecer el cambio de contexto: el estado y cómo abrir la hoja. Las tres
// entradas (etiqueta de la cabecera, avatar y bloque de Cuenta) comparten esta misma hoja.
@Stable
class ContextSwitcher(
    val state: ContextState,
    val onOpen: () -> Unit,
    val onAcknowledgeHint: () -> Unit,
)

// Dibuja la hoja de contextos cuando está abierta y devuelve el conmutador para el resto de la pantalla.
@Composable
fun rememberContextSwitcher(): ContextSwitcher {
    val viewModel: ContextViewModel = hiltViewModel()
    val state by viewModel.state.collectAsStateWithLifecycle()
    var sheetOpen by rememberSaveable { mutableStateOf(false) }
    if (sheetOpen) {
        ContextSheet(
            state = state,
            onSelect = { context ->
                viewModel.select(context)
                sheetOpen = false
            },
            onDismiss = { sheetOpen = false },
        )
    }
    return remember(state) {
        ContextSwitcher(
            state = state,
            onOpen = {
                viewModel.acknowledgeHint()
                sheetOpen = true
            },
            onAcknowledgeHint = viewModel::acknowledgeHint,
        )
    }
}

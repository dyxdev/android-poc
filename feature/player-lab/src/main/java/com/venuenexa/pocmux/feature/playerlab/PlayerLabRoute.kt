package com.venuenexa.pocmux.feature.playerlab

import android.content.Context
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.venuenexa.pocmux.core.consent.consentGating

// Alias del proveedor de Google en el realm (kc_idp_hint); lo registra backend en PV1-T41.
private const val GOOGLE_IDP_HINT = "google"

// Ruta del laboratorio: resuelve la puerta y la pasa al contenido sin estado.
@Composable
fun PlayerLabRoute(
    viewModel: PlayerLabViewModel = hiltViewModel(),
    context: Context,
) {
    val state by viewModel.uiState.collectAsStateWithLifecycle()
    val gating =
        remember(context) {
            consentGating(context)
        }
    // Login y cierre de sesión se hacen en Custom Tabs; AppAuth devuelve el resultado como Intent.
    val authLauncher =
        rememberLauncherForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
            viewModel.onAuthResult(result.data)
        }
    val endSessionLauncher =
        rememberLauncherForActivityResult(ActivityResultContracts.StartActivityForResult()) {
            // Lo local ya está limpio antes de abrir el cierre SSO; al volver no hay nada que procesar.
        }
    PlayerLabContent(
        state = state,
        onConsentChange = viewModel::onConsentChange,
        onModeChange = viewModel::onModeChange,
        onLoadToken = viewModel::onLoadToken,
        onResetCounters = viewModel::onResetCounters,
        onSignIn = { viewModel.onSignIn(null) { authLauncher.launch(it) } },
        onSignInGoogle = { viewModel.onSignIn(GOOGLE_IDP_HINT) { authLauncher.launch(it) } },
        onRefreshToken = viewModel::onRefreshToken,
        onSignOut = { viewModel.onSignOut { endSessionLauncher.launch(it) } },
        gating = gating,
    )
}

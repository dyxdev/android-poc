package com.venuenexa.pocmux.feature.account

import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.runtime.Composable

// Alias del proveedor de Google en el realm (kc_idp_hint); lo registra backend en PV1-T41.
private const val GOOGLE_IDP_HINT = "google"

// Acciones de sesión listas para la UI. Login y cierre de sesión se hacen en Custom Tabs (AppAuth) y devuelven
// el resultado como Intent; las usan la pantalla de Cuenta y la bienvenida.
internal class AuthActions(
    val signIn: () -> Unit,
    val signInGoogle: () -> Unit,
    val signOut: () -> Unit,
)

@Composable
internal fun rememberAuthActions(viewModel: AccountViewModel): AuthActions {
    val authLauncher =
        rememberLauncherForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
            viewModel.onAuthResult(result.data)
        }
    val endSessionLauncher =
        rememberLauncherForActivityResult(ActivityResultContracts.StartActivityForResult()) {
            // Lo local ya está limpio antes de abrir el cierre SSO; al volver no hay nada que procesar.
        }
    return AuthActions(
        signIn = { viewModel.onSignIn(null) { authLauncher.launch(it) } },
        signInGoogle = { viewModel.onSignIn(GOOGLE_IDP_HINT) { authLauncher.launch(it) } },
        signOut = { viewModel.onSignOut { endSessionLauncher.launch(it) } },
    )
}

package com.venuenexa.pocmux.core.identity

import android.content.Intent
import kotlinx.coroutines.flow.StateFlow

// API pública del módulo para la UI y el resto de la app (el resto es detalle de implementación).
interface IdentitySession {
    val state: StateFlow<IdentityState>

    // Lee la sesión guardada al arrancar.
    suspend fun restore()

    // Intent de login, o null si no se pudo preparar (el motivo queda en state.error).
    suspend fun signInIntent(idpHint: String? = null): Intent?

    // Resultado de la pantalla de Keycloak; null si el usuario cerró sin terminar.
    suspend fun onAuthorizationResult(data: Intent?)

    // Token de acceso vigente, renovándolo si hace falta (una sola renovación en curso).
    suspend fun freshAccessToken(forceRefresh: Boolean = false): String?

    // Revoca, limpia lo local y devuelve el Intent que cierra también la sesión SSO de Keycloak.
    suspend fun beginSignOut(): Intent?
}

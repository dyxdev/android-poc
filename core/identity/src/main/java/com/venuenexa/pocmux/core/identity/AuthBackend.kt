package com.venuenexa.pocmux.core.identity

import android.content.Intent

// Lo que IdentityManager necesita del protocolo OIDC. Existe para probar la lógica de sesión sin
// AppAuth ni red: la implementación real es AppAuthBackend. Todo puede lanzar IdentityException.
interface AuthBackend {
    // Intent de Custom Tabs con Authorization Code + PKCE. idpHint lleva a Google por el broker.
    suspend fun authorizationIntent(idpHint: String?): Intent

    // Canjea el resultado de la autorización por tokens.
    suspend fun exchange(data: Intent): Tokens

    // Renueva con el refresh token; Keycloak lo rota. SESSION_EXPIRED si ya no vale (revocado o caducado).
    suspend fun refresh(refreshToken: String): Tokens

    suspend fun revoke(refreshToken: String)

    // Intent de cierre de sesión de Keycloak (RP-initiated logout), o null si el emisor no lo ofrece.
    suspend fun endSessionIntent(idTokenHint: String?): Intent?
}

package com.venuenexa.pocmux.core.identity

// Configuración del cliente de Keycloak. La entrega :app (BuildConfig); sin emisor ni cliente la
// pantalla muestra «no configurado» en lugar de fallar.
data class IdentityConfig(
    val issuer: String,
    val clientId: String,
    val redirectUri: String,
    val postLogoutRedirectUri: String,
    val scopes: List<String>,
    // Solo debug: permite un emisor http cuyo host sea una IP (ver IssuerPolicy). En release es siempre falso.
    val allowCleartextIpIssuer: Boolean = false,
) {
    val isConfigured: Boolean
        get() = issuer.isNotBlank() && clientId.isNotBlank()
}

package com.venuenexa.pocmux.feature.account

// Acciones de la pantalla de Cuenta agrupadas: la pantalla recibe un solo parámetro de eventos.
data class AccountActions(
    val onSignIn: () -> Unit,
    val onSignInGoogle: () -> Unit,
    val onSignOut: () -> Unit,
    val onConsentChange: (Boolean) -> Unit,
    // null oculta la tarjeta de Diagnóstico (release): el laboratorio y el simulador son solo de desarrollo.
    val onOpenDiagnostics: (() -> Unit)?,
)

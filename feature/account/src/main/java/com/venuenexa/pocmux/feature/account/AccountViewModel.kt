package com.venuenexa.pocmux.feature.account

import android.content.Intent
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.venuenexa.pocmux.core.consent.ConsentGate
import com.venuenexa.pocmux.core.identity.IdentitySession
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import javax.inject.Inject

private const val STOP_TIMEOUT_MS = 5_000L

// Sesión y privacidad de la cuenta. Sin tokens ni datos personales en el estado.
@HiltViewModel
class AccountViewModel
    @Inject
    constructor(
        private val identitySession: IdentitySession,
        private val consentGate: ConsentGate,
    ) : ViewModel() {
        val uiState: StateFlow<AccountUiState> =
            combine(identitySession.state, consentGate.granted) { identity, granted ->
                AccountUiState(identity = identity, analyticsConsent = granted)
            }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(STOP_TIMEOUT_MS), AccountUiState())

        // idpHint = "google" lleva directo al broker de Google en Keycloak (kc_idp_hint).
        fun onSignIn(
            idpHint: String?,
            startActivity: (Intent) -> Unit,
        ) {
            viewModelScope.launch { identitySession.signInIntent(idpHint)?.let(startActivity) }
        }

        fun onAuthResult(data: Intent?) {
            viewModelScope.launch { identitySession.onAuthorizationResult(data) }
        }

        // Cerrar sesión limpia los tokens y cierra también la sesión SSO. Lo demás que es de la persona (guardados,
        // contexto, cachés de imágenes) lo limpia SessionCleanup al ver la sesión cerrada, sea cual sea la causa.
        fun onSignOut(startActivity: (Intent) -> Unit) {
            viewModelScope.launch { identitySession.beginSignOut()?.let(startActivity) }
        }

        fun onConsentChange(granted: Boolean) {
            viewModelScope.launch { consentGate.setGranted(granted) }
        }
    }

package com.venuenexa.pocmux.feature.account

import android.content.Context
import android.content.Intent
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.venuenexa.pocmux.core.consent.ConsentGate
import com.venuenexa.pocmux.core.designsystem.clearImageCaches
import com.venuenexa.pocmux.core.identity.IdentitySession
import dagger.hilt.android.lifecycle.HiltViewModel
import dagger.hilt.android.qualifiers.ApplicationContext
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
        @ApplicationContext private val context: Context,
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

        // Cerrar sesión limpia lo local, vacía las cachés de imágenes (ADR-AND-0013) y cierra también la sesión SSO.
        fun onSignOut(startActivity: (Intent) -> Unit) {
            viewModelScope.launch {
                val endSession = identitySession.beginSignOut()
                clearImageCaches(context)
                endSession?.let(startActivity)
            }
        }

        fun onConsentChange(granted: Boolean) {
            viewModelScope.launch { consentGate.setGranted(granted) }
        }
    }

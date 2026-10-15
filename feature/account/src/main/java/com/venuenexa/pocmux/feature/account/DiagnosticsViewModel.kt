package com.venuenexa.pocmux.feature.account

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.venuenexa.pocmux.core.context.CapabilitySimulator
import com.venuenexa.pocmux.core.context.UserContext
import com.venuenexa.pocmux.core.data.UserPreferences
import com.venuenexa.pocmux.core.identity.IdentitySession
import com.venuenexa.pocmux.core.identity.IdentityStatus
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import javax.inject.Inject

private const val STOP_TIMEOUT_MS = 5_000L

data class DiagnosticsUiState(
    val signedIn: Boolean = false,
    val ownerGranted: Boolean = false,
)

// Herramientas de diagnóstico: simulador de capacidades (ADR-AND-0026) y restablecer la bienvenida y los avisos.
@HiltViewModel
class DiagnosticsViewModel
    @Inject
    constructor(
        identitySession: IdentitySession,
        private val simulator: CapabilitySimulator,
        private val preferences: UserPreferences,
    ) : ViewModel() {
        val uiState: StateFlow<DiagnosticsUiState> =
            combine(identitySession.state, simulator.granted) { identity, granted ->
                DiagnosticsUiState(
                    signedIn = identity.status == IdentityStatus.SIGNED_IN,
                    ownerGranted = UserContext.OWNER in granted,
                )
            }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(STOP_TIMEOUT_MS), DiagnosticsUiState())

        fun onOwnerGranted(granted: Boolean) = simulator.setGranted(UserContext.OWNER, granted)

        fun onResetOnboarding() {
            viewModelScope.launch { preferences.resetOnboarding() }
        }
    }

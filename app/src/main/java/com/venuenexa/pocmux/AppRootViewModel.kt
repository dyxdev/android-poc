package com.venuenexa.pocmux

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.venuenexa.pocmux.core.data.UserPreferences
import com.venuenexa.pocmux.core.identity.IdentitySession
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import javax.inject.Inject

// Arranque de la app: recupera la sesión guardada (tokens cifrados) y decide si toca la bienvenida.
@HiltViewModel
class AppRootViewModel
    @Inject
    constructor(
        identitySession: IdentitySession,
        private val preferences: UserPreferences,
    ) : ViewModel() {
        // null mientras se leen las preferencias: así no se enseña la bienvenida un instante a quien ya la vio.
        val welcomeSeen: StateFlow<Boolean?> =
            preferences.welcomeSeen
                .map<Boolean, Boolean?> { it }
                .stateIn(viewModelScope, SharingStarted.Eagerly, null)

        init {
            viewModelScope.launch { identitySession.restore() }
        }

        fun onWelcomeFinished() {
            viewModelScope.launch { preferences.setWelcomeSeen() }
        }
    }

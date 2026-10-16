package com.venuenexa.pocmux.core.context

import android.content.Intent
import com.venuenexa.pocmux.core.consent.ConsentGate
import com.venuenexa.pocmux.core.consent.NetworkCounters
import com.venuenexa.pocmux.core.data.UserPreferences
import com.venuenexa.pocmux.core.identity.IdentitySession
import com.venuenexa.pocmux.core.identity.IdentityState
import com.venuenexa.pocmux.core.identity.IdentityStatus
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow

// Fakes sin mocking (ADR-AND-0018).
internal class FakeCapabilities(initial: Set<UserContext>) : CapabilitiesSource {
    val current = MutableStateFlow(initial)
    override val available: Flow<Set<UserContext>> = current
}

internal class FakePreferences : UserPreferences {
    private val welcome = MutableStateFlow(false)
    private val last = MutableStateFlow<String?>(null)
    private val seen = MutableStateFlow<Set<String>>(emptySet())
    override val welcomeSeen: Flow<Boolean> = welcome
    override val lastContext: Flow<String?> = last
    override val seenContexts: Flow<Set<String>> = seen

    override suspend fun setWelcomeSeen() {
        welcome.value = true
    }

    override suspend fun setLastContext(value: String) {
        last.value = value
    }

    override suspend fun markContextsSeen(values: Set<String>) {
        seen.value = seen.value + values
    }

    override suspend fun clearContextState() {
        last.value = null
        seen.value = emptySet()
    }

    override suspend fun resetOnboarding() {
        welcome.value = false
        last.value = null
        seen.value = emptySet()
    }
}

// Fake de la sesión: su lógica real se prueba en :core:identity.
internal class FakeSession(initial: IdentityStatus) : IdentitySession {
    val mutableState = MutableStateFlow(IdentityState(status = initial))
    override val state: StateFlow<IdentityState> = mutableState

    override suspend fun restore() = Unit

    override suspend fun signInIntent(idpHint: String?): Intent? = null

    override suspend fun onAuthorizationResult(data: Intent?) = Unit

    override suspend fun freshAccessToken(forceRefresh: Boolean): String? = null

    override suspend fun beginSignOut(): Intent? = null
}


internal class FakeConsentGate(initial: Boolean) : ConsentGate {
    private val state = MutableStateFlow(initial)
    override val granted: Flow<Boolean> = state
    override val counters: StateFlow<NetworkCounters> = MutableStateFlow(NetworkCounters())

    override suspend fun setGranted(value: Boolean) {
        state.value = value
    }

    override fun isGrantedNow(): Boolean = state.value

    override fun resetCounters() = Unit
}

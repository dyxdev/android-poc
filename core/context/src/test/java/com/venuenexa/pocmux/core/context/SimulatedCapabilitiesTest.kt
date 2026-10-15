package com.venuenexa.pocmux.core.context

import android.content.Intent
import com.venuenexa.pocmux.core.identity.IdentitySession
import com.venuenexa.pocmux.core.identity.IdentityState
import com.venuenexa.pocmux.core.identity.IdentityStatus
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Test

// Fake de la sesión: su lógica real se prueba en :core:identity.
private class FakeSession(initial: IdentityStatus) : IdentitySession {
    val mutableState = MutableStateFlow(IdentityState(status = initial))
    override val state: StateFlow<IdentityState> = mutableState

    override suspend fun restore() = Unit

    override suspend fun signInIntent(idpHint: String?): Intent? = null

    override suspend fun onAuthorizationResult(data: Intent?) = Unit

    override suspend fun freshAccessToken(forceRefresh: Boolean): String? = null

    override suspend fun beginSignOut(): Intent? = null
}

class SimulatedCapabilitiesTest {
    @Test
    fun signedOut_onlyGuest() =
        runTest {
            val capabilities = SimulatedCapabilities(FakeSession(IdentityStatus.SIGNED_OUT))

            assertEquals(setOf(UserContext.GUEST), capabilities.available.first())
        }

    @Test
    fun signedIn_guestPlusGrantedOwner_andFollowsSignOut() =
        runTest {
            val session = FakeSession(IdentityStatus.SIGNED_IN)
            val capabilities = SimulatedCapabilities(session)
            assertEquals(setOf(UserContext.GUEST, UserContext.OWNER), capabilities.available.first())

            session.mutableState.value = IdentityState(status = IdentityStatus.SIGNED_OUT)
            assertEquals(setOf(UserContext.GUEST), capabilities.available.first())
        }

    @Test
    fun revokingOwner_removesTheContext_andGuestCannotBeRevoked() =
        runTest {
            val capabilities = SimulatedCapabilities(FakeSession(IdentityStatus.SIGNED_IN))

            capabilities.setGranted(UserContext.OWNER, granted = false)
            capabilities.setGranted(UserContext.GUEST, granted = false)
            assertEquals(setOf(UserContext.GUEST), capabilities.available.first())

            capabilities.setGranted(UserContext.OWNER, granted = true)
            assertEquals(setOf(UserContext.GUEST, UserContext.OWNER), capabilities.available.first())
        }
}

package com.venuenexa.pocmux.core.context

import com.venuenexa.pocmux.core.identity.IdentityState
import com.venuenexa.pocmux.core.identity.IdentityStatus
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Test

class SimulatedCapabilitiesTest {
    @Test
    fun signedOut_onlyGuest() =
        runTest {
            val capabilities = SimulatedCapabilities(FakeSession(IdentityStatus.SIGNED_OUT), ownerGranted = true)

            assertEquals(setOf(UserContext.GUEST), capabilities.available.first())
        }

    @Test
    fun signedIn_guestPlusGrantedOwner_andFollowsSignOut() =
        runTest {
            val session = FakeSession(IdentityStatus.SIGNED_IN)
            val capabilities = SimulatedCapabilities(session, ownerGranted = true)
            assertEquals(setOf(UserContext.GUEST, UserContext.OWNER), capabilities.available.first())

            session.mutableState.value = IdentityState(status = IdentityStatus.SIGNED_OUT)
            assertEquals(setOf(UserContext.GUEST), capabilities.available.first())
        }

    @Test
    fun ownerNotGrantedByDefault_inAReleaseLikeConfiguration() =
        runTest {
            val capabilities = SimulatedCapabilities(FakeSession(IdentityStatus.SIGNED_IN), ownerGranted = false)

            assertEquals(setOf(UserContext.GUEST), capabilities.available.first())
        }

    @Test
    fun revokingOwner_removesTheContext_andGuestCannotBeRevoked() =
        runTest {
            val capabilities = SimulatedCapabilities(FakeSession(IdentityStatus.SIGNED_IN), ownerGranted = true)

            capabilities.setGranted(UserContext.OWNER, granted = false)
            capabilities.setGranted(UserContext.GUEST, granted = false)
            assertEquals(setOf(UserContext.GUEST), capabilities.available.first())

            capabilities.setGranted(UserContext.OWNER, granted = true)
            assertEquals(setOf(UserContext.GUEST, UserContext.OWNER), capabilities.available.first())
        }
}

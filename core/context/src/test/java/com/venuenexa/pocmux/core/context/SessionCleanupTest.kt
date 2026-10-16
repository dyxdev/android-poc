package com.venuenexa.pocmux.core.context

import com.venuenexa.pocmux.core.data.InMemorySavedVenuesRepository
import com.venuenexa.pocmux.core.identity.IdentityState
import com.venuenexa.pocmux.core.identity.IdentityStatus
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

@OptIn(ExperimentalCoroutinesApi::class)
class SessionCleanupTest {
    private class Setup(initial: IdentityStatus) {
        val session = FakeSession(initial)
        val saved = InMemorySavedVenuesRepository()
        val manager = ContextManager(FakeCapabilities(setOf(UserContext.GUEST, UserContext.OWNER)), FakePreferences())
        var imageClears = 0
        val consent = FakeConsentGate(initial = true)
        val cleanup = SessionCleanup(session, saved, manager, consent) { imageClears++ }

        fun status(value: IdentityStatus) {
            session.mutableState.value = IdentityState(status = value)
        }
    }

    @Test
    fun signOut_clearsSavedVenuesContextAndImages() =
        runTest(UnconfinedTestDispatcher()) {
            val setup = Setup(IdentityStatus.SIGNED_IN)
            val job = launch { setup.cleanup.watch() }
            setup.saved.toggle("loft")
            setup.manager.select(UserContext.OWNER)
            setup.manager.acknowledgeHint()

            setup.status(IdentityStatus.SIGNED_OUT)

            assertTrue(setup.saved.savedIds.value.isEmpty())
            val state = setup.manager.state.first()
            assertEquals(UserContext.GUEST, state.active)
            assertTrue(state.hintPending)
            assertEquals(1, setup.imageClears)
            // El consentimiento de analítica es de la persona: se retira al cerrar sesión.
            assertFalse(setup.consent.isGrantedNow())
            job.cancel()
        }

    @Test
    fun startupWithoutSession_doesNotClearAnything() =
        runTest(UnconfinedTestDispatcher()) {
            val setup = Setup(IdentityStatus.UNKNOWN)
            val job = launch { setup.cleanup.watch() }
            setup.saved.toggle("loft")

            setup.status(IdentityStatus.SIGNED_OUT)

            assertEquals(setOf("loft"), setup.saved.savedIds.value)
            assertEquals(0, setup.imageClears)
            assertTrue(setup.consent.isGrantedNow())
            job.cancel()
        }

    @Test
    fun expiredSession_cleansLikeAManualSignOut() =
        runTest(UnconfinedTestDispatcher()) {
            val setup = Setup(IdentityStatus.UNKNOWN)
            val job = launch { setup.cleanup.watch() }
            setup.status(IdentityStatus.SIGNED_IN)
            setup.saved.toggle("loft")

            // La sesión caduca sola: mismo camino que el botón de cerrar sesión.
            setup.status(IdentityStatus.SIGNED_OUT)

            assertTrue(setup.saved.savedIds.value.isEmpty())
            assertEquals(1, setup.imageClears)
            job.cancel()
        }
}

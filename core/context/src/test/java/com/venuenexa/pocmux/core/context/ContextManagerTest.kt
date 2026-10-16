package com.venuenexa.pocmux.core.context

import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class ContextManagerTest {
    private val guestOnly = setOf(UserContext.GUEST)
    private val guestAndOwner = setOf(UserContext.GUEST, UserContext.OWNER)

    @Test
    fun guestOnly_cannotSwitch_andHasNoHint() =
        runTest {
            val manager = ContextManager(FakeCapabilities(guestOnly), FakePreferences())

            val state = manager.state.first()
            assertEquals(listOf(UserContext.GUEST), state.available)
            assertEquals(UserContext.GUEST, state.active)
            assertFalse(state.canSwitch)
            assertFalse(state.hintPending)
        }

    @Test
    fun extraContext_startsAsGuestWithHintPending_untilAcknowledged() =
        runTest {
            val manager = ContextManager(FakeCapabilities(guestAndOwner), FakePreferences())

            val before = manager.state.first()
            assertTrue(before.canSwitch)
            assertEquals(UserContext.GUEST, before.active)
            assertTrue(before.hintPending)

            manager.acknowledgeHint()
            assertFalse(manager.state.first().hintPending)
        }

    @Test
    fun select_changesActiveContext_andIsRemembered() =
        runTest {
            val prefs = FakePreferences()
            val manager = ContextManager(FakeCapabilities(guestAndOwner), prefs)

            manager.select(UserContext.OWNER)
            assertEquals(UserContext.OWNER, manager.state.first().active)

            // Otra instancia con las mismas preferencias (nueva apertura de la app) recuerda el último contexto.
            val reopened = ContextManager(FakeCapabilities(guestAndOwner), prefs)
            assertEquals(UserContext.OWNER, reopened.state.first().active)
        }

    @Test
    fun select_ignoresContextsTheUserDoesNotHave() =
        runTest {
            val manager = ContextManager(FakeCapabilities(guestOnly), FakePreferences())

            manager.select(UserContext.OWNER)
            assertEquals(UserContext.GUEST, manager.state.first().active)
        }

    @Test
    fun revokedContext_fallsBackToGuest_andComesBackWithoutANewHint() =
        runTest {
            val capabilities = FakeCapabilities(guestAndOwner)
            val manager = ContextManager(capabilities, FakePreferences())
            manager.select(UserContext.OWNER)
            manager.acknowledgeHint()

            capabilities.current.value = guestOnly
            assertEquals(UserContext.GUEST, manager.state.first().active)

            // Ya lo conocía: recuperar el mismo contexto no vuelve a enseñar el aviso.
            capabilities.current.value = guestAndOwner
            val state = manager.state.first()
            assertEquals(UserContext.OWNER, state.active)
            assertFalse(state.hintPending)
        }

    @Test
    fun reset_forgetsTheLastContextAndTheSeenHints() =
        runTest {
            val manager = ContextManager(FakeCapabilities(guestAndOwner), FakePreferences())
            manager.select(UserContext.OWNER)
            manager.acknowledgeHint()

            manager.reset()
            val state = manager.state.first()
            assertEquals(UserContext.GUEST, state.active)
            assertTrue(state.hintPending)
        }

    @Test
    fun newContext_afterAnAcknowledgedOne_showsTheHintAgain() =
        runTest {
            val capabilities = FakeCapabilities(guestOnly)
            val manager = ContextManager(capabilities, FakePreferences())
            manager.acknowledgeHint()
            assertFalse(manager.state.first().hintPending)

            capabilities.current.value = guestAndOwner
            assertTrue(manager.state.first().hintPending)
        }
}

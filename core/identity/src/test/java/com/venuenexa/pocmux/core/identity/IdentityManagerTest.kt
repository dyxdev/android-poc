package com.venuenexa.pocmux.core.identity

import android.content.Intent
import kotlinx.coroutines.async
import kotlinx.coroutines.awaitAll
import kotlinx.coroutines.delay
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertNull
import org.junit.Test

private class FakeTokenStore(
    var saved: Tokens? = null,
) : TokenStore {
    override suspend fun read(): Tokens? = saved

    override suspend fun write(tokens: Tokens) {
        saved = tokens
    }

    override suspend fun clear() {
        saved = null
    }
}

// Servidor de pega: cuenta las renovaciones y simula la rotación del refresh token.
private class FakeBackend : AuthBackend {
    var refreshCalls = 0
    var revokedWith: String? = null
    var refreshFails: IdentityError? = null

    override suspend fun authorizationIntent(idpHint: String?): Intent = Intent()

    override suspend fun exchange(data: Intent): Tokens = Tokens("access-0", "refresh-0", "id-0", 100_000L)

    override suspend fun refresh(refreshToken: String): Tokens {
        refreshCalls++
        delay(REFRESH_LATENCY_MS)
        refreshFails?.let { throw IdentityException(it) }
        return Tokens("access-$refreshCalls", "refresh-$refreshCalls", null, RENEWED_EXPIRY_MS)
    }

    override suspend fun revoke(refreshToken: String) {
        revokedWith = refreshToken
    }

    override suspend fun endSessionIntent(idTokenHint: String?): Intent? = Intent()

    private companion object {
        const val REFRESH_LATENCY_MS = 100L

        // Lejos del reloj de pega del test: el token renovado cuenta como vigente.
        const val RENEWED_EXPIRY_MS = 1_000_000L
    }
}

class IdentityManagerTest {
    private val expired = Tokens("old", "refresh-0", "id-0", expiresAtMs = 1_000L)

    private fun manager(
        backend: FakeBackend,
        store: FakeTokenStore,
        nowMs: Long = 150_000L,
    ) = IdentityManager(backend, store) { nowMs }

    @Test
    fun concurrentCallsWithExpiredToken_renewOnlyOnce() =
        runTest {
            val backend = FakeBackend()
            val store = FakeTokenStore(expired)
            val manager = manager(backend, store)

            val results = List(CONCURRENT_CALLS) { async { manager.freshAccessToken() } }.awaitAll()

            assertEquals(1, backend.refreshCalls)
            assertEquals(List(CONCURRENT_CALLS) { "access-1" }, results)
        }

    @Test
    fun refresh_storesTheRotatedRefreshToken() =
        runTest {
            val backend = FakeBackend()
            val store = FakeTokenStore(expired)

            manager(backend, store).freshAccessToken()

            assertEquals("refresh-1", store.saved?.refreshToken)
            // El token de identidad anterior se conserva si el servidor no manda otro.
            assertEquals("id-0", store.saved?.idToken)
        }

    @Test
    fun validToken_isReturnedWithoutRenewing() =
        runTest {
            val backend = FakeBackend()
            val store = FakeTokenStore(Tokens("vigente", "refresh-0", null, expiresAtMs = 1_000_000L))

            assertEquals("vigente", manager(backend, store).freshAccessToken())
            assertEquals(0, backend.refreshCalls)
        }

    @Test
    fun revokedRefreshToken_closesTheLocalSession() =
        runTest {
            val backend = FakeBackend().apply { refreshFails = IdentityError.SESSION_EXPIRED }
            val store = FakeTokenStore(expired)
            val manager = manager(backend, store)

            assertNull(manager.freshAccessToken())

            assertNull(store.saved)
            assertEquals(IdentityStatus.SIGNED_OUT, manager.state.value.status)
            assertEquals(IdentityError.SESSION_EXPIRED, manager.state.value.error)
        }

    @Test
    fun networkFailureOnRefresh_keepsTheSession() =
        runTest {
            val backend = FakeBackend().apply { refreshFails = IdentityError.NETWORK }
            val store = FakeTokenStore(expired)
            val manager = manager(backend, store)

            assertNull(manager.freshAccessToken())

            assertEquals(expired, store.saved)
            assertEquals(IdentityError.NETWORK, manager.state.value.error)
        }

    @Test
    fun signOut_revokesCleansAndReturnsTheEndSessionIntent() =
        runTest {
            val backend = FakeBackend()
            val store = FakeTokenStore(expired)
            val manager = manager(backend, store)

            val intent = manager.beginSignOut()

            assertEquals("refresh-0", backend.revokedWith)
            assertNull(store.saved)
            assertEquals(IdentityStatus.SIGNED_OUT, manager.state.value.status)
            assertNotNull(intent)
        }

    @Test
    fun authorizationResult_signsIn_andNullMeansCanceled() =
        runTest {
            val store = FakeTokenStore()
            val manager = manager(FakeBackend(), store)

            manager.onAuthorizationResult(null)
            assertEquals(IdentityError.CANCELED, manager.state.value.error)

            manager.onAuthorizationResult(Intent())
            assertEquals(IdentityStatus.SIGNED_IN, manager.state.value.status)
            assertEquals("access-0", store.saved?.accessToken)
        }

    private companion object {
        const val CONCURRENT_CALLS = 5
    }
}

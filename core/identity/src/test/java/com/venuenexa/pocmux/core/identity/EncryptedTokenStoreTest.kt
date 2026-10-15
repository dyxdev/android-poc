package com.venuenexa.pocmux.core.identity

import androidx.datastore.preferences.core.PreferenceDataStoreFactory
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.TestScope
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNull
import org.junit.Rule
import org.junit.Test
import org.junit.rules.TemporaryFolder
import java.io.File
import java.security.GeneralSecurityException

// Cifrado de pega para JVM (no hay Keystore): marca el blob y puede simular una clave perdida.
private class FakeCipher : SecretCipher {
    var keyLost = false

    override fun encrypt(plain: ByteArray): ByteArray = MARKER + plain

    override fun decrypt(blob: ByteArray): ByteArray {
        if (keyLost || blob.size < MARKER.size || !blob.copyOf(MARKER.size).contentEquals(MARKER)) {
            throw GeneralSecurityException("clave perdida o blob dañado")
        }
        return blob.copyOfRange(MARKER.size, blob.size)
    }

    private companion object {
        val MARKER = "enc:".toByteArray()
    }
}

@OptIn(ExperimentalCoroutinesApi::class)
class EncryptedTokenStoreTest {
    @get:Rule
    val folder = TemporaryFolder()

    private val tokens = Tokens(accessToken = "access", refreshToken = "refresh", idToken = "id", expiresAtMs = 1_000L)

    @Test
    fun write_thenRead_returnsSameTokens() =
        runTest {
            val store = newStore(FakeCipher())

            store.write(tokens)

            assertEquals(tokens.copy(), store.read())
        }

    @Test
    fun undecryptableBlob_isDiscarded_andSessionIsAsked() =
        runTest {
            val cipher = FakeCipher()
            val store = newStore(cipher)
            store.write(tokens)

            cipher.keyLost = true
            assertNull(store.read())

            // El blob inservible se borró: aunque la clave vuelva, no hay sesión que recuperar.
            cipher.keyLost = false
            assertNull(store.read())
        }

    @Test
    fun clear_removesTheSession() =
        runTest {
            val store = newStore(FakeCipher())
            store.write(tokens)

            store.clear()

            assertNull(store.read())
        }

    @Test
    fun codec_rejectsUnknownVersionAndBrokenBase64() {
        assertNull(TokenCodec.decode("v9\nYQ\n\n\n"))
        assertNull(TokenCodec.decode("v1\n***\n\n\n"))
        assertNull(TokenCodec.decode("sin formato"))
    }

    @Test
    fun toString_doesNotLeakTokens() {
        assertFalse(tokens.toString().contains("access"))
        assertFalse(tokens.toString().contains("refresh"))
    }

    private fun TestScope.newStore(cipher: SecretCipher): EncryptedTokenStore {
        val dataStore =
            PreferenceDataStoreFactory.create(scope = backgroundScope) {
                File(folder.root, "identity.preferences_pb")
            }
        return EncryptedTokenStore(dataStore, cipher, UnconfinedTestDispatcher(testScheduler))
    }
}

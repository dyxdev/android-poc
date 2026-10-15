package com.venuenexa.pocmux.core.identity

import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import com.venuenexa.pocmux.core.common.IoDispatcher
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.withContext
import java.util.Base64
import javax.inject.Inject
import javax.inject.Qualifier

@Qualifier
@Retention(AnnotationRetention.BINARY)
annotation class IdentityDataStore

// Almacén de la sesión. Es la única copia de los tokens: nada en SharedPreferences ni en logs.
interface TokenStore {
    suspend fun read(): Tokens?

    suspend fun write(tokens: Tokens)

    suspend fun clear()
}

// Tokens cifrados con SecretCipher y guardados en DataStore (ADR-AND-0008). Si el blob no se puede
// descifrar (clave perdida o dañado) se descarta la sesión y se pide login.
// Público porque lo necesita el grafo Hilt generado en :app.
class EncryptedTokenStore
    @Inject
    constructor(
        @IdentityDataStore private val dataStore: DataStore<Preferences>,
        private val cipher: SecretCipher,
        @IoDispatcher private val io: CoroutineDispatcher,
    ) : TokenStore {
        override suspend fun read(): Tokens? =
            withContext(io) {
                val stored = dataStore.data.first()[KEY_BLOB] ?: return@withContext null
                val tokens = decrypt(stored)
                if (tokens == null) {
                    dataStore.edit { it.remove(KEY_BLOB) }
                }
                tokens
            }

        override suspend fun write(tokens: Tokens) {
            withContext(io) {
                val blob = Base64.getEncoder().encodeToString(cipher.encrypt(TokenCodec.encode(tokens).toByteArray()))
                dataStore.edit { it[KEY_BLOB] = blob }
            }
        }

        override suspend fun clear() {
            withContext(io) {
                dataStore.edit { it.remove(KEY_BLOB) }
            }
        }

        private fun decrypt(stored: String): Tokens? =
            runCatching { TokenCodec.decode(String(cipher.decrypt(Base64.getDecoder().decode(stored)))) }
                .getOrNull()

        private companion object {
            val KEY_BLOB = stringPreferencesKey("tokens_blob")
        }
    }

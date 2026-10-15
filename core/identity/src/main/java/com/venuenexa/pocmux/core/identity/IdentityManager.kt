package com.venuenexa.pocmux.core.identity

import android.content.Intent
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import timber.log.Timber
import java.security.GeneralSecurityException

// Lógica de sesión de ADR-AND-0008: tokens solo en el almacén cifrado, renovación serializada (una
// sola en curso; las demás esperan y releen) y logout que revoca, limpia y cierra la sesión SSO.
// Sin BFF: la app es cliente público y renueva ella misma (la web lo hace en servidor).
// Público porque lo construye el grafo Hilt de :app; los tokens nunca salen de aquí salvo
// freshAccessToken.
class IdentityManager(
    private val backend: AuthBackend,
    private val store: TokenStore,
    private val nowMs: () -> Long,
) : IdentitySession {
    private val mutex = Mutex()
    private val mutableState = MutableStateFlow(IdentityState())
    override val state: StateFlow<IdentityState> = mutableState.asStateFlow()

    override suspend fun restore() {
        mutex.withLock {
            val tokens = store.read()
            mutableState.value = if (tokens != null) signedIn(tokens) else signedOut()
        }
    }

    override suspend fun signInIntent(idpHint: String?): Intent? =
        try {
            backend.authorizationIntent(idpHint)
        } catch (e: IdentityException) {
            mutableState.update { it.copy(error = e.error) }
            null
        }

    override suspend fun onAuthorizationResult(data: Intent?) {
        if (data == null) {
            mutableState.update { it.copy(error = IdentityError.CANCELED) }
            return
        }
        mutex.withLock {
            try {
                val tokens = backend.exchange(data)
                store.write(tokens)
                mutableState.value = signedIn(tokens)
            } catch (e: IdentityException) {
                mutableState.value = signedOut(e.error)
            } catch (e: GeneralSecurityException) {
                // El Keystore no pudo cifrar: sin almacén seguro no hay sesión.
                Timber.w("No se pudo guardar la sesión: %s", e.javaClass.simpleName)
                mutableState.value = signedOut(IdentityError.FAILED)
            }
        }
    }

    override suspend fun freshAccessToken(forceRefresh: Boolean): String? =
        mutex.withLock {
            val current = store.read() ?: return@withLock null
            if (!forceRefresh && !isExpired(current)) return@withLock current.accessToken
            val refreshToken = current.refreshToken ?: return@withLock expire(IdentityError.SESSION_EXPIRED)
            try {
                val renewed = backend.refresh(refreshToken)
                // Keycloak rota el refresh token: se guarda el nuevo; si no llega, se conserva el anterior.
                val merged =
                    renewed.copy(
                        refreshToken = renewed.refreshToken ?: refreshToken,
                        idToken = renewed.idToken ?: current.idToken,
                    )
                store.write(merged)
                mutableState.value = signedIn(merged)
                merged.accessToken
            } catch (e: IdentityException) {
                if (e.error == IdentityError.SESSION_EXPIRED) {
                    expire(IdentityError.SESSION_EXPIRED)
                } else {
                    // Sin red no se pierde la sesión: se conserva y se avisa.
                    mutableState.update { it.copy(error = e.error) }
                    null
                }
            }
        }

    override suspend fun beginSignOut(): Intent? =
        mutex.withLock {
            val tokens = store.read()
            val refreshToken = tokens?.refreshToken
            if (refreshToken != null) {
                try {
                    backend.revoke(refreshToken)
                } catch (e: IdentityException) {
                    // Mejor esfuerzo: la sesión local se limpia igualmente.
                    Timber.w("Revocación fallida: %s", e.error)
                }
            }
            val endSession =
                try {
                    backend.endSessionIntent(tokens?.idToken)
                } catch (e: IdentityException) {
                    Timber.w("Sin intent de cierre SSO: %s", e.error)
                    null
                }
            store.clear()
            mutableState.value = signedOut()
            endSession
        }

    private suspend fun expire(error: IdentityError): String? {
        store.clear()
        mutableState.value = signedOut(error)
        return null
    }

    private fun isExpired(tokens: Tokens): Boolean {
        val expiresAt = tokens.expiresAtMs ?: return false
        return nowMs() + EXPIRY_SKEW_MS >= expiresAt
    }

    private fun signedIn(tokens: Tokens) = IdentityState(IdentityStatus.SIGNED_IN, null, tokens.expiresAtMs)

    private fun signedOut(error: IdentityError? = null) = IdentityState(IdentityStatus.SIGNED_OUT, error)

    private companion object {
        // Se renueva un minuto antes de que caduque para no usar un token a punto de morir.
        const val EXPIRY_SKEW_MS = 60_000L
    }
}

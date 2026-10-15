package com.venuenexa.pocmux.feature.playerlab

import android.content.Intent
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.venuenexa.pocmux.core.common.IoDispatcher
import com.venuenexa.pocmux.core.consent.ConsentGate
import com.venuenexa.pocmux.core.identity.IdentitySession
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import org.json.JSONObject
import timber.log.Timber
import java.net.HttpURLConnection
import java.net.URL
import javax.inject.Inject

// ViewModel del laboratorio: estado unidireccional, sin datos personales, sin secretos.
// El token se pide al servidor local de Andy (solo debug, http://10.0.2.2:8787).
@HiltViewModel
class PlayerLabViewModel
    @Inject
    constructor(
        private val consentGate: ConsentGate,
        private val identitySession: IdentitySession,
        @IoDispatcher private val io: CoroutineDispatcher,
    ) : ViewModel() {
        private val local = MutableStateFlow(LocalState())

        val uiState: StateFlow<PlayerLabUiState> =
            combine(
                consentGate.granted,
                consentGate.counters,
                identitySession.state,
                local,
            ) { granted, counters, identity, l ->
                PlayerLabUiState(
                    consentGranted = granted,
                    mode = l.mode,
                    playbackId = l.playbackId,
                    token = l.token,
                    isLoadingToken = l.isLoadingToken,
                    tokenError = l.tokenError,
                    counters = counters,
                    identity = identity,
                )
            }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(STOP_TIMEOUT_MS), PlayerLabUiState())

        init {
            // Recupera la sesión guardada (tokens cifrados) al abrir la pantalla.
            viewModelScope.launch { identitySession.restore() }
        }

        // idpHint = "google" lleva directo al broker de Google en Keycloak (kc_idp_hint).
        fun onSignIn(
            idpHint: String?,
            startActivity: (Intent) -> Unit,
        ) {
            viewModelScope.launch { identitySession.signInIntent(idpHint)?.let(startActivity) }
        }

        fun onAuthResult(data: Intent?) {
            viewModelScope.launch { identitySession.onAuthorizationResult(data) }
        }

        // Fuerza una renovación: sirve para ver en el dispositivo la rotación del refresh token.
        fun onRefreshToken() {
            viewModelScope.launch { identitySession.freshAccessToken(forceRefresh = true) }
        }

        fun onSignOut(startActivity: (Intent) -> Unit) {
            viewModelScope.launch { identitySession.beginSignOut()?.let(startActivity) }
        }

        fun onConsentChange(granted: Boolean) {
            viewModelScope.launch {
                consentGate.setGranted(granted)
            }
        }

        fun onModeChange(mode: PlayerMode) {
            local.update { it.copy(mode = mode) }
        }

        fun onResetCounters() = consentGate.resetCounters()

        fun onLoadToken() {
            if (BuildConfig.PUBLIC_PLAYBACK_ID.isNotBlank()) {
                // Playback público: sin token ni servidor.
                local.update { it.copy(playbackId = BuildConfig.PUBLIC_PLAYBACK_ID, token = null, tokenError = false) }
                return
            }
            if (local.value.isLoadingToken) return
            local.update { it.copy(isLoadingToken = true, tokenError = false) }
            viewModelScope.launch {
                val result = runCatching { fetchToken() }
                result.onSuccess { (playbackId, token) ->
                    local.update { it.copy(playbackId = playbackId, token = token, isLoadingToken = false) }
                }.onFailure { e ->
                    // Sin cuerpo del error en logs: solo la clase, sin datos personales.
                    Timber.w("No se pudo pedir el token al servidor local: %s", e.javaClass.simpleName)
                    local.update { it.copy(isLoadingToken = false, tokenError = true) }
                }
            }
        }

        // GET {TOKEN_BASE_URL} -> { playbackId, token }. En local es 10.0.2.2:8787;
        // en nube (Appetize) se pasa con -PtokenUrl=<url pública del forward>.
        private suspend fun fetchToken(): Pair<String, String> =
            withContext(io) {
                val url = URL(BuildConfig.TOKEN_BASE_URL)
                val conn =
                    (url.openConnection() as HttpURLConnection).apply {
                        requestMethod = "GET"
                        connectTimeout = TOKEN_TIMEOUT_MS
                        readTimeout = TOKEN_TIMEOUT_MS
                    }
                try {
                    val code = conn.responseCode
                    check(code == HttpURLConnection.HTTP_OK) { "HTTP $code" }
                    val body = conn.inputStream.bufferedReader().use { it.readText() }
                    val json = JSONObject(body)
                    json.getString("playbackId") to json.getString("token")
                } finally {
                    conn.disconnect()
                }
            }

        private companion object {
            const val STOP_TIMEOUT_MS = 5_000L
            const val TOKEN_TIMEOUT_MS = 5_000
        }

        private data class LocalState(
            val mode: PlayerMode = PlayerMode.MUX_GATED,
            val playbackId: String? = null,
            val token: String? = null,
            val isLoadingToken: Boolean = false,
            val tokenError: Boolean = false,
        )
    }

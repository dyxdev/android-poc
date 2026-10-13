package com.venuenexa.pocmux.core.consent

import com.mux.stats.sdk.muxstats.INetworkRequest
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import org.json.JSONObject
import timber.log.Timber
import java.net.URL
import java.util.Hashtable

// API pública del módulo core:consent (la necesita feature:player-lab para
// MuxPlayer.Builder.addNetwork; el resto del módulo sigue internal).
// Puerta de consentimiento (opción e, ADR-AND-0032, decisión de Andy 2026-10-13):
// con consentimiento delega en MuxNetwork; sin consentimiento descarta la petición.
// El consentimiento se lee EN CADA ENVÍO, así que retirarlo corta los envíos al momento.
// Los eventos de antes del consentimiento no se reenvían después (no hay cola).
//
// Firmas verificadas contra código público main de muxinc/mux-stats-sdk-media3 y
// muxinc/stats-sdk-android (MuxNetwork.kt, MockNetworkRequest.java, FakeNetwork.kt),
// y contra muxinc/mux-player-android (MuxPlayer.kt: addNetwork + addExoPlayerBinding).
// PENDIENTE de confirmar contra la versión publicada 1.6.0 al compilar (ver docs/informe.md).
class ConsentGatingNetworkRequest(
    private val delegate: INetworkRequest,
    private val isGranted: () -> Boolean,
) : INetworkRequest {

    private val _counters = MutableStateFlow(NetworkCounters())
    val counters: StateFlow<NetworkCounters> = _counters.asStateFlow()

    fun resetCounters() {
        _counters.value = NetworkCounters()
    }

    override fun get(url: URL?) {
        gate(method = "get", host = url?.host) { delegate.get(url) }
    }

    override fun post(url: URL?, body: JSONObject?, headers: Hashtable<String, String>?) {
        gate(method = "post", host = url?.host) { delegate.post(url, body, headers) }
    }

    override fun postWithCompletion(
        domain: String?,
        envKey: String?,
        body: String?,
        headers: Hashtable<String, String>?,
        completion: INetworkRequest.IMuxNetworkRequestsCompletion?,
    ) {
        // Sin consentimiento: descartar y completar como fallo para no dejar callbacks colgados.
        // No se guarda el body: los eventos previos al consentimiento no se reenvían.
        if (!gate(method = "postWithCompletion", host = domain)) {
            completion?.onComplete(false)
            return
        }
        delegate.postWithCompletion(domain, envKey, body, headers, completion)
    }

    override fun postWithCompletion(
        domain: String?,
        envKey: String?,
        body: String?,
        headers: Hashtable<String, String>?,
        completion: INetworkRequest.IMuxNetworkRequestsCompletion2?,
    ) {
        if (!gate(method = "postWithCompletion", host = domain)) {
            completion?.onComplete(false, null)
            return
        }
        delegate.postWithCompletion(domain, envKey, body, headers, completion)
    }

    // Devuelve si se reenvió. Solo hosts y contadores en logs: sin cuerpos ni tokens.
    private inline fun gate(method: String, host: String?, forward: () -> Unit): Boolean {
        val granted = isGranted()
        _counters.value = _counters.value.record(method = method, host = host, wasForwarded = granted)
        if (granted) {
            forward()
            return true
        }
        Timber.d("Mux Data descartado (sin consentimiento): %s host=%s", method, host ?: "?")
        return false
    }
}

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
// Esta puerta no guarda cola: lo descartado no se reenvía desde aquí. Ojo: al descartar se
// completa el callback con false y el núcleo de Mux (BeaconBatchTracker, muxcore 8.12.0, lectura
// de bytecode) re-encola en memoria el lote fallido (hasta 3600 eventos) y lo reintenta, así que
// al conceder el consentimiento podrían salir eventos anteriores. Decisión pendiente: docs/informe.md §4.
//
// Firmas verificadas el 2026-10-14 contra los sources.jar publicados de Mux Player 1.6.0,
// Mux Data 1.13.2 y android 1.7.2, y contra muxcore 8.12.0 (docs/informe.md §2).
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

    override fun post(
        url: URL?,
        body: JSONObject?,
        headers: Hashtable<String, String>?,
    ) {
        gate(method = "post", host = url?.host) { delegate.post(url, body, headers) }
    }

    // IMuxNetworkRequestsCompletion está deprecada en el SDK, pero la interfaz obliga a implementarla.
    @Suppress("DEPRECATION")
    override fun postWithCompletion(
        domain: String?,
        envKey: String?,
        body: String?,
        headers: Hashtable<String, String>?,
        completion: INetworkRequest.IMuxNetworkRequestsCompletion?,
    ) {
        // Sin consentimiento: descartar y completar como fallo para no dejar callbacks colgados.
        // No se guarda el body: los eventos previos al consentimiento no se reenvían.
        val forwarded =
            gate(method = "postWithCompletion", host = domain) {
                delegate.postWithCompletion(domain, envKey, body, headers, completion)
            }
        if (!forwarded) {
            completion?.onComplete(false)
        }
    }

    override fun postWithCompletion(
        domain: String?,
        envKey: String?,
        body: String?,
        headers: Hashtable<String, String>?,
        completion: INetworkRequest.IMuxNetworkRequestsCompletion2?,
    ) {
        val forwarded =
            gate(method = "postWithCompletion", host = domain) {
                delegate.postWithCompletion(domain, envKey, body, headers, completion)
            }
        if (!forwarded) {
            completion?.onComplete(false, null)
        }
    }

    // Devuelve si se reenvió. Solo hosts y contadores en logs: sin cuerpos ni tokens.
    private inline fun gate(
        method: String,
        host: String?,
        forward: () -> Unit,
    ): Boolean {
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

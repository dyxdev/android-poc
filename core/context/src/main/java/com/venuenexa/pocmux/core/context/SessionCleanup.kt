package com.venuenexa.pocmux.core.context

import com.venuenexa.pocmux.core.consent.ConsentGate
import com.venuenexa.pocmux.core.data.SavedVenuesRepository
import com.venuenexa.pocmux.core.identity.IdentitySession
import com.venuenexa.pocmux.core.identity.IdentityStatus
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.map

// Limpia lo que es de la persona en cuanto la sesión pasa de iniciada a cerrada, sea cual sea la causa (botón de
// Cuenta, cierre del laboratorio o sesión caducada): guardados, contexto recordado y avisos vistos, cachés de
// imágenes (ADR-AND-0013) y consentimiento de analítica (decisión de Andy, 2026-10-16: el consentimiento es de la
// persona, no del dispositivo: se retira al cerrar sesión). Está en un solo sitio para que ningún camino de cierre
// se la salte. No actúa al arrancar (desconocida -> cerrada): ahí no había persona.
class SessionCleanup(
    private val identity: IdentitySession,
    private val savedVenues: SavedVenuesRepository,
    private val contextManager: ContextManager,
    private val consentGate: ConsentGate,
    private val clearImages: suspend () -> Unit,
) {
    suspend fun watch() {
        var previous = identity.state.value.status
        identity.state.map { it.status }.distinctUntilChanged().collect { status ->
            if (previous == IdentityStatus.SIGNED_IN && status == IdentityStatus.SIGNED_OUT) cleanUp()
            previous = status
        }
    }

    // Cada paso va aparte: que uno falle (disco, DataStore) no impide los demás.
    private suspend fun cleanUp() {
        savedVenues.clear()
        attempt { contextManager.reset() }
        attempt { consentGate.setGranted(false) }
        attempt { clearImages() }
    }

    private suspend fun attempt(step: suspend () -> Unit) {
        try {
            step()
        } catch (cancelled: CancellationException) {
            throw cancelled
        } catch (ignored: Exception) {
            // La limpieza es de mejor esfuerzo: lo que falle aquí no debe tumbar la app ni frenar el cierre.
        }
    }
}

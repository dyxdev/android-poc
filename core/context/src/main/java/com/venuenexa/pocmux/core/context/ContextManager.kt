package com.venuenexa.pocmux.core.context

import com.venuenexa.pocmux.core.data.UserPreferences
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.first
import javax.inject.Inject
import javax.inject.Singleton

// Contexto activo y aviso de primera vez. Cambiar de contexto no toca la sesión ni los tokens (RF-002):
// solo cambia qué parte de la app se enseña. Público para el grafo Hilt de :app.
@Singleton
class ContextManager
    @Inject
    constructor(
        private val capabilities: CapabilitiesSource,
        private val preferences: UserPreferences,
    ) {
        val state: Flow<ContextState> =
            combine(
                capabilities.available,
                preferences.lastContext,
                preferences.seenContexts,
            ) { available, last, seen ->
                val list = available.sortedBy { it.ordinal }
                ContextState(
                    available = list,
                    // El último usado, si sigue permitido; si no (revocado o sin sesión), el de cliente.
                    active = list.firstOrNull { it.name == last } ?: UserContext.GUEST,
                    hintPending = list.any { it != UserContext.GUEST && it.name !in seen },
                )
            }

        suspend fun select(context: UserContext) {
            if (context in capabilities.available.first()) preferences.setLastContext(context.name)
        }

        // Cierre de sesión: se olvida el último contexto y los avisos vistos (son de la persona, no del dispositivo).
        suspend fun reset() {
            preferences.clearContextState()
        }

        // Abrir la hoja de contextos o cerrar el globo cuenta como enterado de todos los contextos actuales.
        suspend fun acknowledgeHint() {
            preferences.markContextsSeen(capabilities.available.first().map { it.name }.toSet())
        }
    }

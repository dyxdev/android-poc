package com.venuenexa.pocmux.core.data

import kotlinx.coroutines.flow.Flow

// Preferencias pequeñas del usuario en el dispositivo. Los contextos se guardan por nombre (texto) para que este
// módulo no conozca el modelo de contextos.
interface UserPreferences {
    // Si ya se vio la bienvenida «Explorar / Iniciar sesión» (solo se muestra la primera vez).
    val welcomeSeen: Flow<Boolean>

    // Último contexto usado (ADR-AND-0026: se recuerda entre sesiones de la app).
    val lastContext: Flow<String?>

    // Contextos cuyo aviso ya se enseñó: el aviso vuelve a salir una vez por cada contexto nuevo.
    val seenContexts: Flow<Set<String>>

    suspend fun setWelcomeSeen()

    suspend fun setLastContext(value: String)

    suspend fun markContextsSeen(values: Set<String>)

    // Al cerrar sesión: el último contexto y los avisos vistos son de quien tenía la sesión, no del dispositivo.
    suspend fun clearContextState()

    // Herramienta de diagnóstico: vuelve a dejar la app como recién instalada.
    suspend fun resetOnboarding()
}

package com.venuenexa.pocmux.core.context

// Contextos de la app (ADR-AND-0026). Hoy cliente y propietario; la administración se añade con su feature.
enum class UserContext { GUEST, OWNER }

data class ContextState(
    val available: List<UserContext> = listOf(UserContext.GUEST),
    val active: UserContext = UserContext.GUEST,
    // Hay un contexto que el usuario aún no sabe que tiene: se enseña el aviso de la primera vez.
    val hintPending: Boolean = false,
) {
    val canSwitch: Boolean get() = available.size > 1
}

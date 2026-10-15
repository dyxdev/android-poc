package com.venuenexa.pocmux.core.identity

enum class IdentityStatus { UNKNOWN, SIGNED_OUT, SIGNED_IN }

// Errores que la UI sabe explicar. Sin texto libre del servidor: puede llevar datos del usuario.
enum class IdentityError { NOT_CONFIGURED, CANCELED, SESSION_EXPIRED, NETWORK, FAILED }

// Estado público de la sesión. Nunca lleva tokens, solo cuándo caduca el de acceso.
data class IdentityState(
    val status: IdentityStatus = IdentityStatus.UNKNOWN,
    val error: IdentityError? = null,
    val expiresAtMs: Long? = null,
)

class IdentityException(
    val error: IdentityError,
    cause: Throwable? = null,
) : Exception(error.name, cause)

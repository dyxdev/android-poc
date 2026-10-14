package com.venuenexa.pocmux.core.consent

// Solo hosts y contadores: sin cuerpos, sin tokens, sin datos personales (prompt §2).
data class NetworkCounters(
    val received: Int = 0,
    val forwarded: Int = 0,
    val dropped: Int = 0,
    val byMethodReceived: Map<String, Int> = emptyMap(),
    val byMethodForwarded: Map<String, Int> = emptyMap(),
    val byHost: Map<String, Int> = emptyMap(),
) {
    fun record(
        method: String,
        host: String?,
        wasForwarded: Boolean,
    ): NetworkCounters {
        val nextByMethodReceived = byMethodReceived + (method to ((byMethodReceived[method] ?: 0) + 1))
        val nextByHost = if (host != null) byHost + (host to ((byHost[host] ?: 0) + 1)) else byHost
        return if (wasForwarded) {
            copy(
                received = received + 1,
                forwarded = forwarded + 1,
                byMethodReceived = nextByMethodReceived,
                byMethodForwarded = byMethodForwarded + (method to ((byMethodForwarded[method] ?: 0) + 1)),
                byHost = nextByHost,
            )
        } else {
            copy(
                received = received + 1,
                dropped = dropped + 1,
                byMethodReceived = nextByMethodReceived,
                byHost = nextByHost,
            )
        }
    }
}

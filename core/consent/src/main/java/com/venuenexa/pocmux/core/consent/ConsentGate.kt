package com.venuenexa.pocmux.core.consent

import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.StateFlow

// API pública del módulo (el resto es internal).
interface ConsentGate {
    val granted: Flow<Boolean>

    suspend fun setGranted(value: Boolean)

    fun isGrantedNow(): Boolean

    val counters: StateFlow<NetworkCounters>

    fun resetCounters()
}

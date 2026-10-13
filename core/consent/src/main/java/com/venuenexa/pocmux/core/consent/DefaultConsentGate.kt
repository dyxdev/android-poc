package com.venuenexa.pocmux.core.consent

import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import java.util.concurrent.atomic.AtomicBoolean
import javax.inject.Inject
import javax.inject.Singleton

// Implementación de la única fuente de consentimiento + puerta de red.
// Lee DataStore una vez al arrancar (denegado por defecto) y guarda el valor
// en memoria para leerlo EN CADA ENVÍO sin bloquear el hilo de red.
@Singleton
class DefaultConsentGate @Inject constructor(
    private val repository: ConsentRepository,
    gatingFactory: GatingFactory,
    @AppScopeQualifier appScope: CoroutineScope,
) : ConsentGate {

    private val grantedNow = AtomicBoolean(false)
    private val gating: ConsentGatingNetworkRequest = gatingFactory.create { grantedNow.get() }

    override val granted: Flow<Boolean> = repository.granted

    override val counters: StateFlow<NetworkCounters> = gating.counters

    init {
        appScope.launch {
            repository.granted.collect { grantedNow.set(it) }
        }
    }

    override fun isGrantedNow(): Boolean = grantedNow.get()

    override suspend fun setGranted(value: Boolean) = repository.setGranted(value)

    override fun resetCounters() = gating.resetCounters()

    internal fun gatingNetwork(): ConsentGatingNetworkRequest = gating
}

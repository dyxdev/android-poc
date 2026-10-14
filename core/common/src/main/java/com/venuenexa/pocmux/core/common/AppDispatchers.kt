package com.venuenexa.pocmux.core.common

import kotlinx.coroutines.CoroutineDispatcher
import javax.inject.Inject
import javax.inject.Qualifier
import javax.inject.Singleton

// Dispatchers inyectados: nunca Dispatchers.* fuera de core:common (ADR-AND-0006).
@Qualifier
@Retention(AnnotationRetention.BINARY)
annotation class IoDispatcher

@Qualifier
@Retention(AnnotationRetention.BINARY)
annotation class DefaultDispatcher

@Qualifier
@Retention(AnnotationRetention.BINARY)
annotation class MainDispatcher

@Singleton
class AppDispatchers
    @Inject
    constructor(
        @IoDispatcher val io: CoroutineDispatcher,
        @DefaultDispatcher val default: CoroutineDispatcher,
        @MainDispatcher val main: CoroutineDispatcher,
    )

package com.venuenexa.pocmux.core.data

import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import javax.inject.Inject
import javax.inject.Singleton

// Público porque lo necesita el grafo Hilt generado en :app y las pruebas de los ViewModels.
@Singleton
class InMemorySavedVenuesRepository
    @Inject
    constructor() : SavedVenuesRepository {
        private val saved = MutableStateFlow<Set<String>>(emptySet())

        override val savedIds: StateFlow<Set<String>> = saved.asStateFlow()

        override fun toggle(venueId: String) = saved.update { if (venueId in it) it - venueId else it + venueId }

        override fun clear() = saved.update { emptySet() }
    }

package com.venuenexa.pocmux.core.data

import com.venuenexa.pocmux.core.model.Venue
import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.flowOf
import javax.inject.Inject
import javax.inject.Singleton

private const val DEMO_LATENCY_MS = 700L

// Venues de ejemplo con una latencia simulada, para que se vean los estados de carga. Público porque lo necesita
// el grafo Hilt generado en :app.
@Singleton
class DemoVenueRepository
    @Inject
    constructor() : VenueRepository {
        override fun venues(): Flow<List<Venue>> =
            flow {
                delay(DEMO_LATENCY_MS)
                emit(DemoVenues)
            }

        // Sin latencia: el detalle ya conoce el venue por la lista y la transición compartida no debe esperar.
        override fun venue(id: String): Flow<Venue?> = flowOf(DemoVenues.firstOrNull { it.id == id })
    }

@Module
@InstallIn(SingletonComponent::class)
abstract class DataModule {
    @Binds
    abstract fun bindVenueRepository(impl: DemoVenueRepository): VenueRepository

    @Binds
    abstract fun bindSavedVenuesRepository(impl: InMemorySavedVenuesRepository): SavedVenuesRepository

    @Binds
    abstract fun bindUserPreferences(impl: DataStoreUserPreferences): UserPreferences
}

package com.venuenexa.pocmux.core.consent

import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import com.venuenexa.pocmux.core.common.IoDispatcher
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.withContext
import javax.inject.Inject
import javax.inject.Singleton

// Única fuente del consentimiento de analítica (mismo que PostHog, INT-30).
// Denegado por defecto (condición de producto ADR-AND-0032).
// Público porque lo necesita el grafo Hilt generado en :app; la Key sigue internal.
@Singleton
class ConsentRepository @Inject constructor(
    private val dataStore: DataStore<Preferences>,
    @IoDispatcher private val io: CoroutineDispatcher,
) {
    val granted: Flow<Boolean> = dataStore.data.map { it[KEY_GRANTED] ?: false }

    suspend fun setGranted(value: Boolean) = withContext(io) {
        dataStore.edit { it[KEY_GRANTED] = value }
    }

    companion object {
        internal val KEY_GRANTED = booleanPreferencesKey("analytics_consent_granted")
    }
}

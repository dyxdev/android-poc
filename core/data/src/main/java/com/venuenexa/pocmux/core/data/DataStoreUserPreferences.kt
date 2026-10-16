package com.venuenexa.pocmux.core.data

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.emptyPreferences
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.core.stringSetPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.map
import java.io.IOException
import javax.inject.Inject
import javax.inject.Singleton

private val Context.userPreferencesStore: DataStore<Preferences> by preferencesDataStore(name = "user_prefs")

private val WelcomeSeenKey = booleanPreferencesKey("welcome_seen")
private val LastContextKey = stringPreferencesKey("last_context")
private val SeenContextsKey = stringSetPreferencesKey("seen_contexts")

// Público porque lo necesita el grafo Hilt generado en :app.
@Singleton
class DataStoreUserPreferences
    @Inject
    constructor(
        @ApplicationContext private val context: Context,
    ) : UserPreferences {
        // Un archivo ilegible se trata como vacío: lo peor es repetir la bienvenida o un aviso.
        private val data: Flow<Preferences> =
            context.userPreferencesStore.data.catch { error ->
                if (error is IOException) emit(emptyPreferences()) else throw error
            }

        override val welcomeSeen: Flow<Boolean> = data.map { it[WelcomeSeenKey] ?: false }
        override val lastContext: Flow<String?> = data.map { it[LastContextKey] }
        override val seenContexts: Flow<Set<String>> = data.map { it[SeenContextsKey].orEmpty() }

        override suspend fun setWelcomeSeen() {
            context.userPreferencesStore.edit { it[WelcomeSeenKey] = true }
        }

        override suspend fun setLastContext(value: String) {
            context.userPreferencesStore.edit { it[LastContextKey] = value }
        }

        override suspend fun markContextsSeen(values: Set<String>) {
            context.userPreferencesStore.edit { it[SeenContextsKey] = it[SeenContextsKey].orEmpty() + values }
        }

        override suspend fun clearContextState() {
            context.userPreferencesStore.edit {
                it.remove(LastContextKey)
                it.remove(SeenContextsKey)
            }
        }

        override suspend fun resetOnboarding() {
            context.userPreferencesStore.edit {
                it.remove(WelcomeSeenKey)
                it.remove(LastContextKey)
                it.remove(SeenContextsKey)
            }
        }
    }

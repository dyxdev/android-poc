package com.venuenexa.pocmux.core.identity

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.preferencesDataStore
import dagger.Binds
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

private val Context.identityDataStore: DataStore<Preferences> by preferencesDataStore(name = "identity")

// IdentityConfig lo aporta :app (BuildConfig); este módulo no conoce el realm.
@Module
@InstallIn(SingletonComponent::class)
object IdentityModule {
    @Provides
    @Singleton
    @IdentityDataStore
    fun provideIdentityDataStore(
        @ApplicationContext context: Context,
    ): DataStore<Preferences> = context.identityDataStore

    @Provides
    @Singleton
    fun provideIdentityManager(
        backend: AuthBackend,
        store: TokenStore,
    ): IdentityManager = IdentityManager(backend, store, System::currentTimeMillis)
}

@Module
@InstallIn(SingletonComponent::class)
abstract class IdentityBindings {
    @Binds
    @Singleton
    abstract fun bindAuthBackend(impl: AppAuthBackend): AuthBackend

    @Binds
    @Singleton
    abstract fun bindTokenStore(impl: EncryptedTokenStore): TokenStore

    @Binds
    abstract fun bindSecretCipher(impl: KeystoreCipher): SecretCipher

    @Binds
    abstract fun bindIdentitySession(impl: IdentityManager): IdentitySession
}

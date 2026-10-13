package com.venuenexa.pocmux.core.consent

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.preferencesDataStore
import com.mux.stats.sdk.muxstats.INetworkRequest
import com.mux.stats.sdk.muxstats.MuxDataSdk
import com.mux.stats.sdk.muxstats.MuxNetwork
import com.venuenexa.pocmux.core.common.DefaultDispatcher
import dagger.Binds
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.SupervisedJob
import javax.inject.Qualifier
import javax.inject.Singleton

private val Context.consentDataStore: DataStore<Preferences> by preferencesDataStore(name = "consent")

@Qualifier
@Retention(AnnotationRetention.BINARY)
annotation class AppScopeQualifier

@Module
@InstallIn(SingletonComponent::class)
object ConsentModule {

    @Provides
    @Singleton
    fun provideDataStore(@ApplicationContext context: Context): DataStore<Preferences> =
        context.consentDataStore

    @Provides
    @Singleton
    @AppScopeQualifier
    fun provideAppScope(@DefaultDispatcher dispatcher: CoroutineDispatcher): CoroutineScope =
        CoroutineScope(SupervisedJob() + dispatcher)

    // Delegado real: MuxNetwork pública del SDK (verificada en stats-sdk-android main).
    // El device solo aporta cabeceras de entorno; la puerta de consentimiento está delante.
    @Provides
    @Singleton
    fun provideMuxNetworkDelegate(
        @ApplicationContext context: Context,
        @AppScopeQualifier scope: CoroutineScope,
    ): INetworkRequest {
        val device = MuxDataSdk.AndroidDevice(
            ctx = context,
            playerVersion = "1.6.0",
            muxPluginName = "poc-android-mux",
            muxPluginVersion = "1.6.0",
            playerSoftware = "poc-android-mux",
        )
        return MuxNetwork(device, scope)
    }

    @Provides
    @Singleton
    fun provideGatingNetwork(gate: DefaultConsentGate): ConsentGatingNetworkRequest =
        gate.gatingNetwork()
}

@Module
@InstallIn(SingletonComponent::class)
abstract class ConsentGateBinding {
    @Binds
    @Singleton
    abstract fun bindConsentGate(impl: DefaultConsentGate): ConsentGate
}

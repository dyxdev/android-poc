package com.venuenexa.pocmux

import com.venuenexa.pocmux.core.identity.IdentityConfig
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

// El realm, el cliente y los ámbitos entran por BuildConfig (ver app/build.gradle.kts); :core:identity
// no conoce el entorno.
@Module
@InstallIn(SingletonComponent::class)
object IdentityConfigModule {
    @Provides
    @Singleton
    fun provideIdentityConfig(): IdentityConfig =
        IdentityConfig(
            issuer = BuildConfig.KEYCLOAK_ISSUER,
            clientId = BuildConfig.KEYCLOAK_CLIENT_ID,
            redirectUri = "${BuildConfig.REDIRECT_SCHEME}:/oauth2redirect",
            postLogoutRedirectUri = "${BuildConfig.REDIRECT_SCHEME}:/logout",
            scopes = BuildConfig.KEYCLOAK_SCOPES.split(" ").filter { it.isNotBlank() },
            // Solo debug: emisor http si su host es una IP (Keycloak en la red local). Release exige https.
            allowCleartextIpIssuer = BuildConfig.DEBUG,
        )
}

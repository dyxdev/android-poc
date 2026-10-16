package com.venuenexa.pocmux

import android.content.Context
import com.venuenexa.pocmux.core.consent.ConsentGate
import com.venuenexa.pocmux.core.context.ContextManager
import com.venuenexa.pocmux.core.context.SessionCleanup
import com.venuenexa.pocmux.core.data.SavedVenuesRepository
import com.venuenexa.pocmux.core.designsystem.clearImageCaches
import com.venuenexa.pocmux.core.identity.IdentitySession
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object SessionModule {
    @Provides
    @Singleton
    fun provideSessionCleanup(
        identity: IdentitySession,
        savedVenues: SavedVenuesRepository,
        contextManager: ContextManager,
        consentGate: ConsentGate,
        @ApplicationContext context: Context,
    ): SessionCleanup =
        SessionCleanup(identity, savedVenues, contextManager, consentGate) { clearImageCaches(context) }
}

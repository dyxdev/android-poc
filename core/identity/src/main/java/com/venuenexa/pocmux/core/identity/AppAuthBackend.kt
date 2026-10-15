package com.venuenexa.pocmux.core.identity

import android.content.ActivityNotFoundException
import android.content.Context
import android.content.Intent
import android.net.Uri
import com.venuenexa.pocmux.core.common.IoDispatcher
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.suspendCancellableCoroutine
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import kotlinx.coroutines.withContext
import net.openid.appauth.AuthorizationException
import net.openid.appauth.AuthorizationRequest
import net.openid.appauth.AuthorizationResponse
import net.openid.appauth.AuthorizationService
import net.openid.appauth.AuthorizationServiceConfiguration
import net.openid.appauth.EndSessionRequest
import net.openid.appauth.GrantTypeValues
import net.openid.appauth.ResponseTypeValues
import net.openid.appauth.TokenRequest
import java.io.IOException
import java.net.HttpURLConnection
import java.net.URL
import java.net.URLEncoder
import javax.inject.Inject
import javax.inject.Singleton
import kotlin.coroutines.resume
import kotlin.coroutines.resumeWithException

// Implementación con AppAuth for Android (ADR-AND-0008): cliente público, Authorization Code + PKCE
// (S256, lo genera AppAuth) y Custom Tabs; nunca WebView. Solo se probó la API contra el código
// publicado de AppAuth 0.11.1; el flujo real queda por probar contra un realm (docs/identidad-keycloak.md).
// Público porque lo necesita el grafo Hilt generado en :app.
@Singleton
class AppAuthBackend
    @Inject
    constructor(
        @ApplicationContext private val context: Context,
        private val config: IdentityConfig,
        @IoDispatcher private val io: CoroutineDispatcher,
    ) : AuthBackend {
        private val service by lazy { AuthorizationService(context) }
        private val configurationMutex = Mutex()
        private var configuration: AuthorizationServiceConfiguration? = null

        override suspend fun authorizationIntent(idpHint: String?): Intent {
            val builder =
                AuthorizationRequest.Builder(
                    serviceConfiguration(),
                    config.clientId,
                    ResponseTypeValues.CODE,
                    Uri.parse(config.redirectUri),
                ).setScopes(config.scopes)
            if (idpHint != null) {
                builder.setAdditionalParameters(mapOf(KC_IDP_HINT to idpHint))
            }
            return try {
                service.getAuthorizationRequestIntent(builder.build())
            } catch (e: ActivityNotFoundException) {
                // Sin navegador con Custom Tabs no hay login posible (nunca se cae a WebView).
                throw IdentityException(IdentityError.FAILED, e)
            }
        }

        override suspend fun exchange(data: Intent): Tokens {
            val response = AuthorizationResponse.fromIntent(data)
            if (response == null) {
                val exception = AuthorizationException.fromIntent(data)
                val canceled = exception == AuthorizationException.GeneralErrors.USER_CANCELED_AUTH_FLOW
                throw IdentityException(if (canceled) IdentityError.CANCELED else IdentityError.FAILED, exception)
            }
            return tokenRequest(response.createTokenExchangeRequest())
        }

        override suspend fun refresh(refreshToken: String): Tokens {
            val request =
                TokenRequest
                    .Builder(serviceConfiguration(), config.clientId)
                    .setGrantType(GrantTypeValues.REFRESH_TOKEN)
                    .setRefreshToken(refreshToken)
                    .build()
            return tokenRequest(request)
        }

        // Keycloak revoca con POST form a su endpoint de revocación; AppAuth no trae esa llamada.
        override suspend fun revoke(refreshToken: String) {
            val endpoint =
                serviceConfiguration()
                    .discoveryDoc
                    ?.docJson
                    ?.optString(REVOCATION_ENDPOINT)
                    .orEmpty()
            if (endpoint.isEmpty()) return
            val body =
                "client_id=${URLEncoder.encode(config.clientId, Charsets.UTF_8)}" +
                    "&token=${URLEncoder.encode(refreshToken, Charsets.UTF_8)}" +
                    "&token_type_hint=refresh_token"
            withContext(io) {
                try {
                    postForm(endpoint, body)
                } catch (e: IOException) {
                    throw IdentityException(IdentityError.NETWORK, e)
                } catch (e: IllegalStateException) {
                    throw IdentityException(IdentityError.FAILED, e)
                }
            }
        }

        private fun postForm(
            endpoint: String,
            body: String,
        ) {
            val connection = URL(endpoint).openConnection() as HttpURLConnection
            try {
                connection.requestMethod = "POST"
                connection.doOutput = true
                connection.connectTimeout = TIMEOUT_MS
                connection.readTimeout = TIMEOUT_MS
                connection.setRequestProperty("Content-Type", "application/x-www-form-urlencoded")
                connection.outputStream.use { it.write(body.toByteArray()) }
                check(connection.responseCode in HTTP_OK_RANGE) { "HTTP ${connection.responseCode}" }
            } finally {
                connection.disconnect()
            }
        }

        override suspend fun endSessionIntent(idTokenHint: String?): Intent? {
            val serviceConfiguration = serviceConfiguration()
            if (serviceConfiguration.endSessionEndpoint == null) return null
            val request =
                EndSessionRequest
                    .Builder(serviceConfiguration)
                    .setIdTokenHint(idTokenHint)
                    .setPostLogoutRedirectUri(Uri.parse(config.postLogoutRedirectUri))
                    .build()
            return service.getEndSessionRequestIntent(request)
        }

        // Descubrimiento OIDC una vez (en memoria); sin emisor configurado no hay nada que descubrir.
        private suspend fun serviceConfiguration(): AuthorizationServiceConfiguration {
            if (!config.isConfigured) throw IdentityException(IdentityError.NOT_CONFIGURED)
            return configurationMutex.withLock {
                configuration ?: fetchConfiguration().also { configuration = it }
            }
        }

        private suspend fun fetchConfiguration(): AuthorizationServiceConfiguration =
            suspendCancellableCoroutine { continuation ->
                AuthorizationServiceConfiguration.fetchFromIssuer(Uri.parse(config.issuer)) { fetched, exception ->
                    if (fetched != null) {
                        continuation.resume(fetched)
                    } else {
                        continuation.resumeWithException(IdentityException(IdentityError.NETWORK, exception))
                    }
                }
            }

        private suspend fun tokenRequest(request: TokenRequest): Tokens =
            suspendCancellableCoroutine { continuation ->
                service.performTokenRequest(request) { response, exception ->
                    val accessToken = response?.accessToken
                    if (response != null && accessToken != null) {
                        continuation.resume(
                            Tokens(
                                accessToken = accessToken,
                                refreshToken = response.refreshToken,
                                idToken = response.idToken,
                                expiresAtMs = response.accessTokenExpirationTime,
                            ),
                        )
                    } else {
                        continuation.resumeWithException(failure(exception))
                    }
                }
            }

        // invalid_grant = refresh token caducado, ya usado o revocado (otro dispositivo, cierre de cuenta).
        private fun failure(exception: AuthorizationException?): IdentityException {
            val error =
                when {
                    exception == null -> IdentityError.FAILED
                    isInvalidGrant(exception) -> IdentityError.SESSION_EXPIRED
                    exception == AuthorizationException.GeneralErrors.NETWORK_ERROR -> IdentityError.NETWORK
                    else -> IdentityError.FAILED
                }
            return IdentityException(error, exception)
        }

        private fun isInvalidGrant(exception: AuthorizationException): Boolean =
            exception.type == AuthorizationException.TYPE_OAUTH_TOKEN_ERROR && exception.error == INVALID_GRANT

        private companion object {
            const val KC_IDP_HINT = "kc_idp_hint"
            const val REVOCATION_ENDPOINT = "revocation_endpoint"
            const val INVALID_GRANT = "invalid_grant"
            const val TIMEOUT_MS = 10_000
            val HTTP_OK_RANGE = 200..299
        }
    }

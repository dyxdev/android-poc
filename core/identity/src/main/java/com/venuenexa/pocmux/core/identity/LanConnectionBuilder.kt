package com.venuenexa.pocmux.core.identity

import android.net.Uri
import net.openid.appauth.connectivity.ConnectionBuilder
import net.openid.appauth.connectivity.DefaultConnectionBuilder
import java.io.IOException
import java.net.HttpURLConnection
import java.net.URL

private const val LAN_TIMEOUT_MS = 15_000

// Conexiones de AppAuth: https como siempre; http solo si `IssuerPolicy` lo permite (debug + IP). Cualquier otra cosa
// sale como IOException, que AppAuth sí captura y convierte en un error normal en lugar de tumbar la app.
internal class LanConnectionBuilder(
    private val allowCleartextIp: Boolean,
) : ConnectionBuilder {
    override fun openConnection(uri: Uri): HttpURLConnection =
        when (IssuerPolicy.check(uri.toString(), allowCleartextIp)) {
            IssuerCheck.HTTPS -> DefaultConnectionBuilder.INSTANCE.openConnection(uri)
            IssuerCheck.CLEARTEXT_IP ->
                (URL(uri.toString()).openConnection() as HttpURLConnection).apply {
                    connectTimeout = LAN_TIMEOUT_MS
                    readTimeout = LAN_TIMEOUT_MS
                    instanceFollowRedirects = false
                }
            IssuerCheck.REJECTED -> throw IOException("Conexión no permitida: solo https (o http con IP en debug)")
        }
}

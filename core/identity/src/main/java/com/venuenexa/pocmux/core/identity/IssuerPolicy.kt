package com.venuenexa.pocmux.core.identity

import java.net.URI

// Qué emisores acepta la app. https siempre. http solo en debug y solo si el host es una IP (por ejemplo un Keycloak en
// la red local); un dominio con http se rechaza siempre. AppAuth lanza IllegalArgumentException con un emisor http dentro
// de una AsyncTask que no la captura, y eso tumba la app: por eso se comprueba antes de llamarlo.
internal enum class IssuerCheck { HTTPS, CLEARTEXT_IP, REJECTED }

internal object IssuerPolicy {
    private const val OCTET_MAX = 255
    private val IPV4 = Regex("""\d{1,3}(\.\d{1,3}){3}""")

    fun check(
        issuer: String,
        allowCleartextIp: Boolean,
    ): IssuerCheck {
        val uri = runCatching { URI(issuer) }.getOrNull() ?: return IssuerCheck.REJECTED
        val host = uri.host ?: return IssuerCheck.REJECTED
        return when (uri.scheme?.lowercase()) {
            "https" -> IssuerCheck.HTTPS
            "http" -> if (allowCleartextIp && isIpv4(host)) IssuerCheck.CLEARTEXT_IP else IssuerCheck.REJECTED
            else -> IssuerCheck.REJECTED
        }
    }

    private fun isIpv4(host: String): Boolean =
        IPV4.matches(host) && host.split(".").all { it.toInt() <= OCTET_MAX }
}

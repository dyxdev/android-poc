package com.venuenexa.pocmux.core.identity

import org.junit.Assert.assertEquals
import org.junit.Test

class IssuerPolicyTest {
    private fun check(
        issuer: String,
        allowCleartextIp: Boolean,
    ) = IssuerPolicy.check(issuer, allowCleartextIp)

    @Test
    fun `https siempre vale, con dominio o con IP`() {
        assertEquals(IssuerCheck.HTTPS, check("https://auth.example.com/realms/venuenexa", false))
        assertEquals(IssuerCheck.HTTPS, check("https://192.168.1.183:8443/realms/venuenexa", false))
    }

    @Test
    fun `http con IP solo vale en debug`() {
        assertEquals(IssuerCheck.CLEARTEXT_IP, check("http://192.168.1.183/realms/venuenexa", true))
        assertEquals(IssuerCheck.CLEARTEXT_IP, check("http://192.168.1.183:8080/realms/venuenexa", true))
        assertEquals(IssuerCheck.REJECTED, check("http://192.168.1.183/realms/venuenexa", false))
    }

    @Test
    fun `http con un dominio se rechaza incluso en debug`() {
        assertEquals(IssuerCheck.REJECTED, check("http://keycloak.local/realms/venuenexa", true))
        assertEquals(IssuerCheck.REJECTED, check("http://localhost:8080/realms/venuenexa", true))
        assertEquals(IssuerCheck.REJECTED, check("http://192.168.1.183.nip.io/realms/venuenexa", true))
    }

    @Test
    fun `una IP mal formada no cuenta como IP`() {
        assertEquals(IssuerCheck.REJECTED, check("http://999.1.1.1/realms/venuenexa", true))
        assertEquals(IssuerCheck.REJECTED, check("http://192.168.1/realms/venuenexa", true))
    }

    @Test
    fun `otros esquemas y textos raros se rechazan sin lanzar excepciones`() {
        assertEquals(IssuerCheck.REJECTED, check("ftp://192.168.1.183/realms/venuenexa", true))
        assertEquals(IssuerCheck.REJECTED, check("192.168.1.183/realms/venuenexa", true))
        assertEquals(IssuerCheck.REJECTED, check("http://", true))
        assertEquals(IssuerCheck.REJECTED, check("no es una url", true))
        assertEquals(IssuerCheck.REJECTED, check("", true))
    }
}

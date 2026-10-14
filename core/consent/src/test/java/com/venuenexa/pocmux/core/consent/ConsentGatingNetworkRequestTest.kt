package com.venuenexa.pocmux.core.consent

import com.mux.stats.sdk.muxstats.INetworkRequest
import org.json.JSONObject
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test
import java.net.URL
import java.util.Hashtable
import java.util.concurrent.atomic.AtomicBoolean

// Fake local al módulo (sin librerías de mocking, ADR-AND-0018).
private class FakeNetwork : INetworkRequest {
    var gets = 0
    var posts = 0
    var completions1 = 0
    var completions2 = 0
    var lastCompletion1Result: Boolean? = null
    var lastCompletion2Result: Boolean? = null

    override fun get(url: URL?) {
        gets++
    }

    override fun post(
        url: URL?,
        body: JSONObject?,
        headers: Hashtable<String, String>?,
    ) {
        posts++
    }

    override fun postWithCompletion(
        domain: String?,
        envKey: String?,
        body: String?,
        headers: Hashtable<String, String>?,
        completion: INetworkRequest.IMuxNetworkRequestsCompletion?,
    ) {
        completions1++
        completion?.onComplete(true)
        lastCompletion1Result = true
    }

    override fun postWithCompletion(
        domain: String?,
        envKey: String?,
        body: String?,
        headers: Hashtable<String, String>?,
        completion: INetworkRequest.IMuxNetworkRequestsCompletion2?,
    ) {
        completions2++
        completion?.onComplete(true, null)
        lastCompletion2Result = true
    }
}

class ConsentGatingNetworkRequestTest {
    private val url = URL("https://litix.io/android")

    @Test
    fun deniedByDefault_nothingReachesDelegate() {
        val fake = FakeNetwork()
        val gate = ConsentGatingNetworkRequest(fake) { false }

        gate.get(url)
        gate.post(url, JSONObject(), Hashtable())
        var c1: Boolean? = null
        gate.postWithCompletion(
            "litix.io",
            "env",
            "{}",
            null,
            object : INetworkRequest.IMuxNetworkRequestsCompletion {
                override fun onComplete(success: Boolean) {
                    c1 = success
                }
            },
        )
        var c2: Boolean? = null
        gate.postWithCompletion(
            "litix.io",
            "env",
            "{}",
            null,
            object : INetworkRequest.IMuxNetworkRequestsCompletion2 {
                override fun onComplete(
                    success: Boolean,
                    headers: Map<String, List<String>>?,
                ) {
                    c2 = success
                }
            },
        )

        assertEquals(0, fake.gets)
        assertEquals(0, fake.posts)
        assertEquals(0, fake.completions1)
        assertEquals(0, fake.completions2)
        // Al descartar se completa como fallo para no colgar callbacks.
        assertEquals(false, c1)
        assertEquals(false, c2)
        val counters = gate.counters.value
        assertEquals(4, counters.received)
        assertEquals(0, counters.forwarded)
        assertEquals(4, counters.dropped)
    }

    @Test
    fun granted_everythingReachesDelegate() {
        val fake = FakeNetwork()
        val gate = ConsentGatingNetworkRequest(fake) { true }

        gate.get(url)
        gate.post(url, JSONObject(), Hashtable())
        gate.postWithCompletion(
            "litix.io",
            "env",
            "{}",
            null,
            null as INetworkRequest.IMuxNetworkRequestsCompletion?,
        )
        gate.postWithCompletion(
            "litix.io",
            "env",
            "{}",
            null,
            null as INetworkRequest.IMuxNetworkRequestsCompletion2?,
        )

        assertEquals(1, fake.gets)
        assertEquals(1, fake.posts)
        assertEquals(1, fake.completions1)
        assertEquals(1, fake.completions2)
        val counters = gate.counters.value
        assertEquals(4, counters.received)
        assertEquals(4, counters.forwarded)
        assertEquals(0, counters.dropped)
        assertEquals(1, counters.byHost["litix.io"])
    }

    @Test
    fun midStreamChange_takesEffectOnNextSend() {
        val fake = FakeNetwork()
        val granted = AtomicBoolean(true)
        val gate = ConsentGatingNetworkRequest(fake) { granted.get() }

        gate.get(url)
        assertEquals(1, fake.gets)

        granted.set(false)
        gate.get(url)
        gate.get(url)
        assertEquals(1, fake.gets)

        granted.set(true)
        gate.get(url)
        assertEquals(2, fake.gets)

        val counters = gate.counters.value
        assertEquals(4, counters.received)
        assertEquals(2, counters.forwarded)
        assertEquals(2, counters.dropped)
    }

    @Test
    fun counters_doNotStoreBodiesOrTokens() {
        val fake = FakeNetwork()
        val gate = ConsentGatingNetworkRequest(fake) { false }
        gate.post(url, JSONObject().put("token", "secreto"), Hashtable())

        val counters = gate.counters.value
        assertTrue(counters.byHost.containsKey("litix.io"))
        assertFalse(counters.toString().contains("secreto"))
    }
}

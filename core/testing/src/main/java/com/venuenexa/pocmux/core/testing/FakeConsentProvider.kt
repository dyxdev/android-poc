package com.venuenexa.pocmux.core.testing

// Fakes compartidos para tests (ADR-AND-0018): nunca librerías de mocking.
class FakeConsentProvider(initial: Boolean = false) {
    @Volatile
    var granted: Boolean = initial

    fun isGranted(): Boolean = granted
}

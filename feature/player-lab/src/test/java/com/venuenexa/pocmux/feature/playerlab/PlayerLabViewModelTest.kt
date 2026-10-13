package com.venuenexa.pocmux.feature.playerlab

import com.venuenexa.pocmux.core.consent.ConsentGate
import com.venuenexa.pocmux.core.consent.NetworkCounters
import com.venuenexa.pocmux.core.testing.FakeConsentProvider
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Before
import org.junit.Test

// Fake del gate con el proveedor falso de core:testing (sin mocking, ADR-AND-0018).
private class FakeConsentGate(provider: FakeConsentProvider) : ConsentGate {
    private val _granted = MutableStateFlow(provider.granted)
    override val granted: Flow<Boolean> = _granted
    override val counters: StateFlow<NetworkCounters> = MutableStateFlow(NetworkCounters())
    var sets = 0

    override fun isGrantedNow(): Boolean = _granted.value

    override suspend fun setGranted(value: Boolean) {
        sets++
        _granted.value = value
    }

    override fun resetCounters() = Unit
}

@OptIn(ExperimentalCoroutinesApi::class)
class PlayerLabViewModelTest {

    private val dispatcher = StandardTestDispatcher()

    @Before
    fun setUp() {
        // viewModelScope usa Dispatchers.Main: hay que sustituirlo en tests JVM.
        Dispatchers.setMain(dispatcher)
    }

    @After
    fun tearDown() {
        Dispatchers.resetMain()
    }

    @Test
    fun consentDeniedByDefault_andTogglePersists() = runTest {
        val gate = FakeConsentGate(FakeConsentProvider(initial = false))
        val vm = PlayerLabViewModel(gate, dispatcher)

        assertEquals(false, vm.uiState.value.consentGranted)

        vm.onConsentChange(true)
        testScheduler.advanceUntilIdle()
        assertEquals(true, vm.uiState.value.consentGranted)
        assertEquals(1, gate.sets)
    }
}

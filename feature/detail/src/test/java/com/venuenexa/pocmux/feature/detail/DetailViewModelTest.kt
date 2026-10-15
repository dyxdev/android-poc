package com.venuenexa.pocmux.feature.detail

import com.venuenexa.pocmux.core.consent.ConsentGate
import com.venuenexa.pocmux.core.consent.NetworkCounters
import com.venuenexa.pocmux.core.data.InMemorySavedVenuesRepository
import com.venuenexa.pocmux.core.data.VenueRepository
import com.venuenexa.pocmux.core.model.Venue
import com.venuenexa.pocmux.core.model.VenueCategory
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.launch
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.TestScope
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test

private val Loft =
    Venue(
        id = "loft",
        name = "Loft Gràcia",
        city = "Barcelona",
        category = VenueCategory.SHOOTS,
        capacity = 60,
        pricePerDay = 620,
        rating = 4.8,
        reviewCount = 96,
        photos = listOf("https://example.test/1.jpg", "https://example.test/2.jpg"),
        tourPlaybackId = "tour",
        isAvailable = true,
        isFeatured = true,
    )

// Fakes sin mocking (ADR-AND-0018): el repositorio devuelve solo el loft.
private class FakeVenueRepository : VenueRepository {
    override fun venues(): Flow<List<Venue>> = flowOf(listOf(Loft))

    override fun venue(id: String): Flow<Venue?> = flowOf(Loft.takeIf { it.id == id })
}

private class FakeConsentGate(initial: Boolean) : ConsentGate {
    private val state = MutableStateFlow(initial)
    override val granted: Flow<Boolean> = state
    override val counters: StateFlow<NetworkCounters> = MutableStateFlow(NetworkCounters())

    override suspend fun setGranted(value: Boolean) {
        state.value = value
    }

    override fun isGrantedNow(): Boolean = state.value

    override fun resetCounters() = Unit
}

@OptIn(ExperimentalCoroutinesApi::class)
class DetailViewModelTest {
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

    // uiState usa stateIn(WhileSubscribed): sin colector el valor no se actualiza.
    private fun TestScope.viewModel(
        venueId: String,
        saved: InMemorySavedVenuesRepository = InMemorySavedVenuesRepository(),
        gate: FakeConsentGate = FakeConsentGate(initial = false),
    ): DetailViewModel {
        val vm = DetailViewModel(venueId, FakeVenueRepository(), saved, gate)
        backgroundScope.launch(dispatcher) { vm.uiState.collect {} }
        testScheduler.runCurrent()
        return vm
    }

    @Test
    fun knownVenue_showsContent_notSavedAndNoConsentByDefault() =
        runTest {
            val content = viewModel("loft").uiState.value as DetailUiState.Content
            assertEquals(Loft, content.venue)
            assertEquals(false, content.saved)
            assertEquals(false, content.analyticsConsent)
        }

    @Test
    fun unknownVenue_isNotFound() =
        runTest {
            assertEquals(DetailUiState.NotFound, viewModel("nope").uiState.value)
        }

    @Test
    fun toggleSaved_isSharedWithTheRepository() =
        runTest {
            val saved = InMemorySavedVenuesRepository()
            val vm = viewModel("loft", saved = saved)

            vm.onToggleSaved()
            testScheduler.runCurrent()
            assertEquals(setOf("loft"), saved.savedIds.value)
            assertEquals(true, (vm.uiState.value as DetailUiState.Content).saved)

            vm.onToggleSaved()
            testScheduler.runCurrent()
            assertTrue(saved.savedIds.value.isEmpty())
        }

    @Test
    fun consentChange_isReflectedInTheState() =
        runTest {
            val gate = FakeConsentGate(initial = false)
            val vm = viewModel("loft", gate = gate)

            gate.setGranted(true)
            testScheduler.runCurrent()
            assertEquals(true, (vm.uiState.value as DetailUiState.Content).analyticsConsent)
        }
}

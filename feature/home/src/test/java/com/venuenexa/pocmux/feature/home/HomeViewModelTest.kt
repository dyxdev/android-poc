package com.venuenexa.pocmux.feature.home

import com.venuenexa.pocmux.core.data.VenueRepository
import com.venuenexa.pocmux.core.model.Venue
import com.venuenexa.pocmux.core.model.VenueCategory
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.launch
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.TestScope
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test

private const val LATENCY_MS = 100L

private fun venue(
    id: String,
    name: String,
    city: String,
    category: VenueCategory,
    featured: Boolean = false,
) = Venue(
    id = id,
    name = name,
    city = city,
    category = category,
    capacity = 100,
    pricePerDay = 500,
    rating = 4.5,
    reviewCount = 10,
    photos = listOf("https://example.test/$id.jpg"),
    tourPlaybackId = null,
    isAvailable = true,
    isFeatured = featured,
)

private val Catalog =
    listOf(
        venue("garden", "Jardín de las Acacias", "Sevilla", VenueCategory.WEDDINGS, featured = true),
        venue("loft", "Loft Gràcia", "Barcelona", VenueCategory.SHOOTS, featured = true),
        venue("hall", "Salón Aurora", "Madrid", VenueCategory.WEDDINGS),
    )

// Fake del repositorio con latencia, para ver el estado de carga (sin mocking, ADR-AND-0018).
private class FakeVenueRepository : VenueRepository {
    override fun venues(): Flow<List<Venue>> =
        flow {
            delay(LATENCY_MS)
            emit(Catalog)
        }
}

@OptIn(ExperimentalCoroutinesApi::class)
class HomeViewModelTest {
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
    private fun TestScope.loadedViewModel(): HomeViewModel {
        val vm = HomeViewModel(FakeVenueRepository())
        backgroundScope.launch(dispatcher) { vm.uiState.collect {} }
        testScheduler.advanceTimeBy(LATENCY_MS + 1)
        testScheduler.runCurrent()
        return vm
    }

    @Test
    fun startsLoading_thenShowsCatalogWithFeatured() =
        runTest {
            val vm = HomeViewModel(FakeVenueRepository())
            backgroundScope.launch(dispatcher) { vm.uiState.collect {} }
            testScheduler.runCurrent()
            assertTrue(vm.uiState.value.isLoading)

            testScheduler.advanceTimeBy(LATENCY_MS + 1)
            testScheduler.runCurrent()
            val state = vm.uiState.value
            assertFalse(state.isLoading)
            assertEquals(3, state.venues.size)
            assertEquals(listOf("garden", "loft"), state.featured.map { it.id })
            assertFalse(state.isFiltering)
        }

    @Test
    fun query_matchesNameOrCity_ignoringCase() =
        runTest {
            val vm = loadedViewModel()

            vm.onQueryChange("  barcelona ")
            testScheduler.runCurrent()
            assertEquals(listOf("loft"), vm.uiState.value.venues.map { it.id })
            assertTrue(vm.uiState.value.isFiltering)

            vm.onQueryChange("AURORA")
            testScheduler.runCurrent()
            assertEquals(listOf("hall"), vm.uiState.value.venues.map { it.id })
        }

    @Test
    fun category_filtersAndPressingItAgainClearsIt() =
        runTest {
            val vm = loadedViewModel()

            vm.onCategorySelected(VenueCategory.WEDDINGS)
            testScheduler.runCurrent()
            assertEquals(listOf("garden", "hall"), vm.uiState.value.venues.map { it.id })

            vm.onCategorySelected(VenueCategory.WEDDINGS)
            testScheduler.runCurrent()
            assertEquals(null, vm.uiState.value.category)
            assertEquals(3, vm.uiState.value.venues.size)
        }

    @Test
    fun queryAndCategory_combine_andClearFiltersResetsBoth() =
        runTest {
            val vm = loadedViewModel()

            vm.onCategorySelected(VenueCategory.WEDDINGS)
            vm.onQueryChange("madrid")
            testScheduler.runCurrent()
            assertEquals(listOf("hall"), vm.uiState.value.venues.map { it.id })

            vm.onQueryChange("barcelona")
            testScheduler.runCurrent()
            assertTrue(vm.uiState.value.venues.isEmpty())

            vm.onClearFilters()
            testScheduler.runCurrent()
            assertFalse(vm.uiState.value.isFiltering)
            assertEquals(3, vm.uiState.value.venues.size)
        }

    @Test
    fun toggleSaved_addsAndRemoves() =
        runTest {
            val vm = loadedViewModel()

            vm.onToggleSaved("loft")
            testScheduler.runCurrent()
            assertEquals(setOf("loft"), vm.uiState.value.savedIds)

            vm.onToggleSaved("loft")
            testScheduler.runCurrent()
            assertTrue(vm.uiState.value.savedIds.isEmpty())
        }

    @Test
    fun intro_playsOnlyUntilMarkedAsPlayed() =
        runTest {
            val vm = loadedViewModel()
            assertTrue(vm.uiState.value.playIntro)

            vm.onIntroPlayed()
            testScheduler.runCurrent()
            assertFalse(vm.uiState.value.playIntro)
        }
}

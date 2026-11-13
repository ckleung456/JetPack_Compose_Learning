package com.example.feature.business.ui.viewmodel

import app.cash.turbine.test
import androidx.lifecycle.SavedStateHandle
import com.example.domain.Kind
import com.example.domain.Result
import com.example.domain.location.LocationError
import com.example.feature.business.FakeBusinessRepository
import com.example.feature.business.FakeLocationAddressProvider
import com.example.feature.business.MainDispatcherRule
import com.example.feature.business.R
import com.example.feature.business.business
import com.example.feature.business.model.domain.AddressDialogUi
import com.example.feature.business.model.domain.BusinessCategory
import com.example.feature.business.model.domain.BusinessFilter
import com.example.feature.business.model.domain.BusinessListAction
import com.example.feature.business.model.domain.BusinessListEvent
import com.example.feature.business.model.domain.BusinessListState
import com.example.feature.business.model.domain.BusinessSectionUi
import com.example.feature.business.usecase.BuildBusinessSectionsUseCase
import com.example.feature.business.usecase.GetCurrentAddressUseCase
import com.example.feature.business.usecase.SearchPizzaAndBeerUseCase
import com.example.presentation.model.UIState
import kotlinx.collections.immutable.ImmutableList
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.launch
import kotlinx.coroutines.test.TestScope
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import kotlinx.coroutines.test.advanceTimeBy
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Rule
import org.junit.Test
import java.io.IOException

@OptIn(ExperimentalCoroutinesApi::class)
class BusinessesViewModelTest {

    @get:Rule
    val mainRule = MainDispatcherRule()

    private val repository = FakeBusinessRepository(
        pizza = listOf(business(id = "p1", name = "Tony's"), business(id = "p2", name = "Apizza")),
        beer = listOf(business(id = "b1", name = "Brewery"))
    )
    private val locationProvider = FakeLocationAddressProvider()

    private fun viewModel(savedStateHandle: SavedStateHandle = SavedStateHandle()) = BusinessesViewModel(
        savedStateHandle = savedStateHandle,
        searchPizzaAndBeerUseCase = SearchPizzaAndBeerUseCase(repository),
        buildBusinessSectionsUseCase = BuildBusinessSectionsUseCase(),
        getCurrentAddressUseCase = GetCurrentAddressUseCase(locationProvider)
    )

    /** uiState only starts loading while subscribed, so keep a collector alive for the test. */
    private fun TestScope.subscribe(viewModel: BusinessesViewModel) {
        backgroundScope.launch(UnconfinedTestDispatcher(testScheduler)) { viewModel.uiState.collect {} }
    }

    private val BusinessesViewModel.state: BusinessListState get() = uiState.value

    @Suppress("UNCHECKED_CAST")
    private val BusinessesViewModel.sections: List<BusinessSectionUi>
        get() = (state.content as UIState.Success<ImmutableList<BusinessSectionUi>>).data

    private fun List<BusinessSectionUi>.names() = associate { section -> section.category to section.businesses.map { it.name } }

    @Test
    fun `loads sections for the default address on subscription`() = runTest {
        val viewModel = viewModel()
        subscribe(viewModel)

        assertEquals(listOf(BusinessListState.DEFAULT_ADDRESS), repository.requestedLocations)
        assertEquals(
            mapOf(BusinessCategory.PIZZA to listOf("Apizza", "Tony's"), BusinessCategory.BEER to listOf("Brewery")),
            viewModel.sections.names()
        )
    }

    @Test
    fun `shows an error when both searches fail`() = runTest {
        repository.pizzaError = IOException("pizza down")
        repository.beerError = IOException("beer down")
        val viewModel = viewModel()
        subscribe(viewModel)

        assertEquals(UIState.Error(message = "pizza down", kind = Kind.UNEXPECTED), viewModel.state.content)
    }

    @Test
    fun `partial failure shows remaining section and a snackbar`() = runTest {
        repository.beerError = IOException("beer down")
        val viewModel = viewModel()

        viewModel.events.test {
            subscribe(viewModel)
            assertEquals(BusinessListEvent.ShowSnackbar(messageId = R.string.partial_failure_beer), awaitItem())
        }
        assertEquals(listOf(BusinessCategory.PIZZA), viewModel.sections.map { it.category })
    }

    @Test
    fun `filter selection narrows sections`() = runTest {
        val viewModel = viewModel()
        subscribe(viewModel)

        viewModel.onAction(BusinessListAction.OnFilterSelect(BusinessFilter.BEER))

        assertEquals(BusinessFilter.BEER, viewModel.state.filter)
        assertEquals(mapOf(BusinessCategory.BEER to listOf("Brewery")), viewModel.sections.names())
    }

    @Test
    fun `search query is debounced before filtering`() = runTest {
        val savedStateHandle = SavedStateHandle()
        val viewModel = viewModel(savedStateHandle)
        subscribe(viewModel)

        viewModel.onAction(BusinessListAction.OnSearchQueryUpdate("tony"))

        assertEquals("tony", viewModel.state.searchQuery)
        assertEquals(2, viewModel.sections.size)

        advanceTimeBy(301)

        assertEquals(mapOf(BusinessCategory.PIZZA to listOf("Tony's")), viewModel.sections.names())
        assertEquals("tony", savedStateHandle["BusinessesViewModel.query"])
    }

    @Test
    fun `restores address and query from saved state`() = runTest {
        val viewModel = viewModel(
            SavedStateHandle(mapOf("BusinessesViewModel.address" to "Oakland, CA", "BusinessesViewModel.query" to "brew"))
        )
        subscribe(viewModel)

        assertEquals("Oakland, CA", viewModel.state.address)
        assertEquals(listOf("Oakland, CA"), repository.requestedLocations)
        assertEquals(mapOf(BusinessCategory.BEER to listOf("Brewery")), viewModel.sections.names())
    }

    @Test
    fun `entering an address reloads for that address`() = runTest {
        val viewModel = viewModel()
        subscribe(viewModel)

        viewModel.onAction(BusinessListAction.OnAddressClick)
        assertEquals(AddressDialogUi.ChooseSource, viewModel.state.addressDialogUi)

        viewModel.onAction(BusinessListAction.OnChooseEnterAddress)
        assertEquals(AddressDialogUi.EnterAddress(BusinessListState.DEFAULT_ADDRESS), viewModel.state.addressDialogUi)

        viewModel.onAction(BusinessListAction.OnAddressTextChange("  Berkeley, CA "))
        viewModel.onAction(BusinessListAction.OnAddressConfirm)

        assertEquals(AddressDialogUi.Hidden, viewModel.state.addressDialogUi)
        assertEquals("Berkeley, CA", viewModel.state.address)
        assertEquals(listOf(BusinessListState.DEFAULT_ADDRESS, "Berkeley, CA"), repository.requestedLocations)
    }

    @Test
    fun `blank address is not confirmed`() = runTest {
        val viewModel = viewModel()
        subscribe(viewModel)

        viewModel.onAction(BusinessListAction.OnChooseEnterAddress)
        viewModel.onAction(BusinessListAction.OnAddressTextChange("   "))
        viewModel.onAction(BusinessListAction.OnAddressConfirm)

        assertEquals(AddressDialogUi.EnterAddress("   "), viewModel.state.addressDialogUi)
        assertEquals(BusinessListState.DEFAULT_ADDRESS, viewModel.state.address)
    }

    @Test
    fun `dismissing the dialog hides it`() = runTest {
        val viewModel = viewModel()
        subscribe(viewModel)

        viewModel.onAction(BusinessListAction.OnAddressClick)
        viewModel.onAction(BusinessListAction.OnDialogDismiss)

        assertEquals(AddressDialogUi.Hidden, viewModel.state.addressDialogUi)
    }

    @Test
    fun `choosing device location asks for permission`() = runTest {
        val viewModel = viewModel()
        subscribe(viewModel)
        viewModel.onAction(BusinessListAction.OnAddressClick)

        viewModel.events.test {
            viewModel.onAction(BusinessListAction.OnChooseUseLocation)
            assertEquals(BusinessListEvent.RequestLocationPermission, awaitItem())
        }
        assertEquals(AddressDialogUi.Hidden, viewModel.state.addressDialogUi)
    }

    @Test
    fun `granted permission reloads with the device address`() = runTest {
        val viewModel = viewModel()
        subscribe(viewModel)

        viewModel.onAction(BusinessListAction.OnLocationPermissionResult(granted = true))

        assertEquals("500 Market St, San Francisco, CA", viewModel.state.address)
        assertFalse(viewModel.state.isLocatingAddress)
        assertEquals(
            listOf(BusinessListState.DEFAULT_ADDRESS, "500 Market St, San Francisco, CA"),
            repository.requestedLocations
        )
    }

    // Regression: GetCurrentAddressUseCase used to rethrow LocationError as a plain Exception, so the
    // ViewModel always fell back to UNAVAILABLE and the PERMISSION_DENIED branch was unreachable.
    @Test
    fun `provider permission error emits a permission denied event`() = runTest {
        locationProvider.result = Result.Error(LocationError.PERMISSION_DENIED)
        val viewModel = viewModel()
        subscribe(viewModel)

        viewModel.events.test {
            viewModel.onAction(BusinessListAction.OnLocationPermissionResult(granted = true))
            assertEquals(BusinessListEvent.ShowLocationPermissionDenied, awaitItem())
        }
        assertEquals(BusinessListState.DEFAULT_ADDRESS, viewModel.state.address)
    }

    @Test
    fun `other location errors show a localized snackbar, not the raw enum name`() = runTest {
        val expected = mapOf(
            LocationError.LOCATION_DISABLED to R.string.location_error_disabled,
            LocationError.UNAVAILABLE to R.string.location_error_unavailable,
            LocationError.GEOCODER_FAILED to R.string.location_error_geocoder
        )
        expected.forEach { (error, messageId) ->
            locationProvider.result = Result.Error(error)
            val viewModel = viewModel()
            subscribe(viewModel)

            viewModel.events.test {
                viewModel.onAction(BusinessListAction.OnLocationPermissionResult(granted = true))
                assertEquals(BusinessListEvent.ShowSnackbar(messageId = messageId), awaitItem())
            }
            assertEquals(BusinessListState.DEFAULT_ADDRESS, viewModel.state.address)
        }
    }

    @Test
    fun `resubscribing after the stop timeout does not reload or duplicate the pipeline`() = runTest {
        val viewModel = viewModel()
        val first = backgroundScope.launch(UnconfinedTestDispatcher(testScheduler)) { viewModel.uiState.collect {} }
        assertEquals(1, repository.requestedLocations.size)

        // Leave the screen long enough for WhileSubscribed(5_000) to stop upstream, then come back.
        first.cancel()
        advanceTimeBy(6_000)
        subscribe(viewModel)

        assertEquals(1, repository.requestedLocations.size)
        // Sections still update from a single pipeline after resubscribing.
        viewModel.onAction(BusinessListAction.OnFilterSelect(BusinessFilter.BEER))
        assertEquals(listOf(BusinessCategory.BEER), viewModel.sections.map { it.category })
    }

    @Test
    fun `failed location lookup stops the locating indicator`() = runTest {
        locationProvider.result = Result.Error(LocationError.LOCATION_DISABLED)
        val viewModel = viewModel()
        subscribe(viewModel)

        viewModel.onAction(BusinessListAction.OnLocationPermissionResult(granted = true))

        assertFalse(viewModel.state.isLocatingAddress)
    }

    @Test
    fun `denied permission emits a permission denied event`() = runTest {
        val viewModel = viewModel()
        subscribe(viewModel)

        viewModel.events.test {
            viewModel.onAction(BusinessListAction.OnLocationPermissionResult(granted = false))
            assertEquals(BusinessListEvent.ShowLocationPermissionDenied, awaitItem())
        }
        assertEquals(BusinessListState.DEFAULT_ADDRESS, viewModel.state.address)
    }

    @Test
    fun `selecting a business navigates to its detail`() = runTest {
        val viewModel = viewModel()

        viewModel.events.test {
            viewModel.onAction(BusinessListAction.OnBusinessSelected(id = "p1"))
            assertEquals(BusinessListEvent.NavigateToDetail(id = "p1"), awaitItem())
        }
    }

    @Test
    fun `failed refresh keeps existing content and shows a snackbar`() = runTest {
        val viewModel = viewModel()
        subscribe(viewModel)
        val loaded = viewModel.state.content
        repository.pizzaError = IOException("pizza down")
        repository.beerError = IOException("beer down")

        viewModel.events.test {
            viewModel.onAction(BusinessListAction.OnRefresh)
            assertEquals(BusinessListEvent.ShowSnackbar(message = "pizza down"), awaitItem())
        }
        assertEquals(loaded, viewModel.state.content)
        assertFalse(viewModel.state.isRefreshing)
    }

    @Test
    fun `retry after failure loads content`() = runTest {
        repository.pizzaError = IOException("pizza down")
        repository.beerError = IOException("beer down")
        val viewModel = viewModel()
        subscribe(viewModel)

        repository.pizzaError = null
        repository.beerError = null
        viewModel.onAction(BusinessListAction.OnRetry)

        assertEquals(2, viewModel.sections.size)
    }
}

package com.example.feature.country.ui.viewmodel

import androidx.lifecycle.SavedStateHandle
import app.cash.turbine.test
import com.example.domain.Kind
import com.example.feature.country.FakeCountryApis
import com.example.feature.country.MainDispatcherRule
import com.example.feature.country.country
import com.example.feature.country.model.domain.CountriesUIState
import com.example.feature.country.model.domain.CountryItem
import com.example.feature.country.module.network.CountryRepository
import com.example.feature.country.usecase.GetCountriesUseCase
import com.example.feature.country.usecase.SearchCountriesUseCase
import com.example.presentation.model.UIState
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import kotlinx.coroutines.test.TestScope
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import kotlinx.coroutines.test.advanceTimeBy
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Rule
import org.junit.Test

@OptIn(ExperimentalCoroutinesApi::class)
class CountriesViewModelTest {

    @get:Rule
    val mainRule = MainDispatcherRule()

    private val france = country(name = "France", code = "FR", capital = "Paris")
    private val japan = country(name = "Japan", code = "JP", capital = "Tokyo")
    private val apis = FakeCountryApis(countries = listOf(japan, france))
    private val savedStateHandle = SavedStateHandle()

    private fun viewModel() = CountriesViewModel(
        savedStateHandle = savedStateHandle,
        getCountriesUseCase = GetCountriesUseCase(CountryRepository(apis)),
        searchCountriesUseCase = SearchCountriesUseCase()
    )

    private fun TestScope.subscribe(viewModel: CountriesViewModel) {
        backgroundScope.launch(UnconfinedTestDispatcher(testScheduler)) { viewModel.uiState.collect {} }
    }

    /** The repository hops to Dispatchers.IO, so wait for the state rather than reading it immediately. */
    private suspend fun CountriesViewModel.awaitSuccess(predicate: (CountriesUIState) -> Boolean = { true }): CountriesUIState =
        (uiState.first { it is UIState.Success && predicate(it.data) } as UIState.Success).data

    private val allItems = listOf(
        CountryItem.Letter("F"), CountryItem.CountryInfo(france.toUi()),
        CountryItem.Letter("J"), CountryItem.CountryInfo(japan.toUi())
    )

    @Test
    fun `loads countries on subscription and saves them`() = runTest {
        val viewModel = viewModel()
        subscribe(viewModel)

        assertEquals(allItems, viewModel.awaitSuccess().countries)
        assertEquals(allItems, savedStateHandle.get<List<CountryItem>>("GetCountriesViewModel.COUNTRIES"))
    }

    @Test
    fun `shows an error when loading fails`() = runTest {
        apis.error = IllegalStateException("offline")
        val viewModel = viewModel()
        subscribe(viewModel)

        assertEquals(
            UIState.Error(message = "offline", kind = Kind.UNEXPECTED),
            viewModel.uiState.first { it is UIState.Error }
        )
    }

    @Test
    fun `restores saved countries without calling the api`() = runTest {
        savedStateHandle["GetCountriesViewModel.COUNTRIES"] = allItems
        val viewModel = viewModel()
        subscribe(viewModel)

        assertEquals(allItems, viewModel.awaitSuccess().countries)
        assertEquals(0, apis.calls)
    }

    @Test
    fun `search query is debounced then filters the list`() = runTest {
        val viewModel = viewModel()
        subscribe(viewModel)
        viewModel.awaitSuccess()

        viewModel.updateSearchQuery("tokyo")
        assertEquals("tokyo", viewModel.awaitSuccess().searchState.query)
        assertEquals(allItems, viewModel.awaitSuccess().countries)

        advanceTimeBy(301)

        assertEquals(listOf(CountryItem.CountryInfo(japan.toUi())), viewModel.awaitSuccess { it.countries.size == 1 }.countries)
    }

    @Test
    fun `clearing the search restores all countries`() = runTest {
        val viewModel = viewModel()
        subscribe(viewModel)
        viewModel.awaitSuccess()
        viewModel.updateSearchQuery("tokyo")
        advanceTimeBy(301)
        viewModel.awaitSuccess { it.countries.size == 1 }

        viewModel.clearSearchQuery()

        val state = viewModel.awaitSuccess { it.countries.size == allItems.size }
        assertEquals("", state.searchState.query)
    }

    @Test
    fun `triggerSearch toggles the search bar`() = runTest {
        val viewModel = viewModel()
        subscribe(viewModel)
        assertFalse(viewModel.awaitSuccess().searchState.isActive)

        viewModel.triggerSearch()
        assertTrue(viewModel.awaitSuccess().searchState.isActive)

        viewModel.triggerSearch()
        assertFalse(viewModel.awaitSuccess().searchState.isActive)
    }

    @Test
    fun `selecting a country emits it once`() = runTest {
        val viewModel = viewModel()

        viewModel.selectedCountry.test {
            viewModel.onSelectedCountry(france.toUi())
            assertEquals(france.toUi(), awaitItem())
        }
    }
}

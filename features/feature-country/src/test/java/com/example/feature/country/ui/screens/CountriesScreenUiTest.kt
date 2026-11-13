package com.example.feature.country.ui.screens

import androidx.compose.ui.test.SemanticsMatcher
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.hasSetTextAction
import androidx.compose.ui.test.hasText
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onNodeWithContentDescription
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.compose.ui.test.performTextInput
import androidx.lifecycle.SavedStateHandle
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.example.feature.country.FakeCountryApis
import com.example.feature.country.country
import com.example.feature.country.model.domain.CountryUi
import com.example.feature.country.module.network.CountryRepository
import com.example.feature.country.ui.viewmodel.CountriesViewModel
import com.example.feature.country.usecase.GetCountriesUseCase
import com.example.feature.country.usecase.SearchCountriesUseCase
import org.junit.Assert.assertEquals
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

/**
 * Renders [CountriesScreen] with Robolectric on top of the real [CountriesViewModel] and a fake API.
 * The repository hops to Dispatchers.IO, so assertions wait with `waitUntil`.
 */
@RunWith(AndroidJUnit4::class)
class CountriesScreenUiTest {

    @get:Rule
    val composeRule = createComposeRule()

    private val france = country(name = "France", code = "FR", capital = "Paris")
    private val japan = country(name = "Japan", code = "JP", capital = "Tokyo")
    private val apis = FakeCountryApis(countries = listOf(japan, france))
    private val selected = mutableListOf<CountryUi>()

    private fun setScreen() {
        val viewModel = CountriesViewModel(
            savedStateHandle = SavedStateHandle(),
            getCountriesUseCase = GetCountriesUseCase(CountryRepository(apis)),
            searchCountriesUseCase = SearchCountriesUseCase()
        )
        composeRule.setContent {
            CountriesScreen(viewModel = viewModel, onSelectedCountry = { selected += it })
        }
    }

    private fun waitFor(matcher: SemanticsMatcher, present: Boolean = true) =
        composeRule.waitUntil(timeoutMillis = 5_000) {
            composeRule.onAllNodes(matcher).fetchSemanticsNodes().isNotEmpty() == present
        }

    @Test
    fun showsCountriesGroupedByLetter() {
        setScreen()
        waitFor(hasText("France"))

        composeRule.onNodeWithText("Countries").assertIsDisplayed()
        composeRule.onNodeWithText("F").assertIsDisplayed()
        composeRule.onNodeWithText("France").assertIsDisplayed()
        composeRule.onNodeWithText("J").assertIsDisplayed()
        composeRule.onNodeWithText("Japan").assertIsDisplayed()
    }

    @Test
    fun searchFiltersTheList() {
        setScreen()
        waitFor(hasText("France"))

        composeRule.onNodeWithContentDescription("Search").performClick()
        waitFor(hasSetTextAction())
        composeRule.onNode(hasSetTextAction()).performTextInput("tokyo")

        waitFor(hasText("France"), present = false)
        composeRule.onNodeWithText("Japan").assertIsDisplayed()
    }

    @Test
    fun clickingCountryReportsSelection() {
        setScreen()
        waitFor(hasText("Japan"))

        composeRule.onNodeWithText("Japan").performClick()
        composeRule.waitUntil(timeoutMillis = 5_000) { selected.isNotEmpty() }

        assertEquals(listOf(japan.toUi()), selected)
    }

    @Test
    fun errorShowsRetryWhichReloads() {
        apis.error = IllegalStateException("offline")
        setScreen()
        waitFor(hasText("offline"))

        apis.error = null
        composeRule.onNodeWithText("Try Again").performClick()

        waitFor(hasText("France"))
        assertEquals(2, apis.calls)
    }
}

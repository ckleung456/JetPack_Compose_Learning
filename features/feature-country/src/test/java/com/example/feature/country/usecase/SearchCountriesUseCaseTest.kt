package com.example.feature.country.usecase

import com.example.domain.Kind
import com.example.domain.usecase.UseCaseOutputWithStatus
import com.example.feature.country.country
import com.example.feature.country.model.domain.CountriesUIState
import com.example.feature.country.model.domain.CountryItem
import com.example.feature.country.model.domain.SearchInput
import com.example.feature.country.model.domain.SearchState
import com.example.presentation.model.UIState
import kotlinx.collections.immutable.persistentListOf
import kotlinx.coroutines.flow.last
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Test

class SearchCountriesUseCaseTest {

    private val useCase = SearchCountriesUseCase()

    private val france = CountryItem.CountryInfo(country(name = "France", code = "FR", capital = "Paris").toUi())
    private val japan = CountryItem.CountryInfo(country(name = "Japan", code = "JP", capital = "Tokyo").toUi())
    private val allItems = listOf(CountryItem.Letter("F"), france, CountryItem.Letter("J"), japan)

    private val currentState = UIState.Success(
        CountriesUIState(countries = persistentListOf(france), searchState = SearchState(isActive = true, query = "old"))
    )

    private suspend fun search(query: String, state: UIState<CountriesUIState> = currentState) =
        (useCase(SearchInput(query = query, currentState = state, countries = allItems)).last()
                as UseCaseOutputWithStatus.Success).result

    @Test
    fun `blank query restores the full list`() = runTest {
        val result = search(query = " ") as UIState.Success

        assertEquals(allItems, result.data.countries)
        assertEquals(SearchState(isActive = true, query = " "), result.data.searchState)
    }

    @Test
    fun `query matches name code or capital ignoring case and drops headers`() = runTest {
        assertEquals(listOf(france), (search("fran") as UIState.Success).data.countries)
        assertEquals(listOf(japan), (search("jp") as UIState.Success).data.countries)
        assertEquals(listOf(japan), (search("TOKYO") as UIState.Success).data.countries)
        assertEquals(emptyList<CountryItem>(), (search("zzz") as UIState.Success).data.countries)
    }

    @Test
    fun `non success state is passed through untouched`() = runTest {
        val error = UIState.Error(message = "offline", kind = Kind.NETWORK)

        assertEquals(error, search(query = "fran", state = error))
    }
}

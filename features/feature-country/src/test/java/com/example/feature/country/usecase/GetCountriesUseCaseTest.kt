package com.example.feature.country.usecase

import com.example.domain.UnexpectedError
import com.example.domain.usecase.UseCaseOutputWithStatus
import com.example.feature.country.FakeCountryApis
import com.example.feature.country.country
import com.example.feature.country.model.domain.CountryItem
import com.example.feature.country.module.network.CountryRepository
import kotlinx.coroutines.flow.toList
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Test

class GetCountriesUseCaseTest {

    private val apis = FakeCountryApis()
    private val useCase = GetCountriesUseCase(CountryRepository(apis))

    @Test
    fun `groups countries alphabetically under letter headers`() = runTest {
        val canada = country(name = "Canada", code = "CA")
        val chile = country(name = "Chile", code = "CL")
        val albania = country(name = "albania", code = "AL")
        apis.countries = listOf(chile, albania, canada, country(name = null))

        val result = (useCase(Unit).toList().last() as UseCaseOutputWithStatus.Success).result

        assertEquals(
            listOf(
                CountryItem.Letter("C"),
                CountryItem.CountryInfo(canada.toUi()),
                CountryItem.CountryInfo(chile.toUi()),
                CountryItem.Letter("A"),
                CountryItem.CountryInfo(albania.toUi())
            ),
            result
        )
    }

    @Test
    fun `ui model gets a flag emoji from the code`() = runTest {
        apis.countries = listOf(country(name = "United States", code = "US"))

        val result = (useCase(Unit).toList().last() as UseCaseOutputWithStatus.Success).result

        assertEquals("🇺🇸", (result[1] as CountryItem.CountryInfo).country.flag)
    }

    @Test
    fun `api errors become failures`() = runTest {
        apis.error = IllegalStateException("offline")

        val outputs = useCase(Unit).toList()

        assertEquals(
            listOf(UseCaseOutputWithStatus.Progress(), UseCaseOutputWithStatus.Failed<List<CountryItem>>(UnexpectedError("offline"))),
            outputs
        )
    }
}

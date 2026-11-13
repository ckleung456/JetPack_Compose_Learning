package com.example.feature.country

import com.example.feature.country.model.domain.Country
import com.example.feature.country.model.domain.CountryLanguage
import com.example.feature.country.module.network.CountryApis
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.test.TestDispatcher
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.setMain
import org.junit.rules.TestWatcher
import org.junit.runner.Description

@OptIn(ExperimentalCoroutinesApi::class)
class MainDispatcherRule(
    val dispatcher: TestDispatcher = UnconfinedTestDispatcher()
) : TestWatcher() {
    override fun starting(description: Description) = Dispatchers.setMain(dispatcher)
    override fun finished(description: Description) = Dispatchers.resetMain()
}

fun country(name: String?, code: String? = null, capital: String? = null) = Country(
    capital = capital,
    code = code,
    currency = null,
    flag = null,
    language = CountryLanguage(code = "en", name = "English"),
    name = name,
    region = "NA"
)

class FakeCountryApis(var countries: List<Country> = emptyList()) : CountryApis {
    var error: Throwable? = null
    var calls = 0

    override fun fetchCountriesAPI(): Flow<List<Country>> {
        calls++
        val failure = error
        return flow {
            if (failure != null) throw failure
            emit(countries)
        }
    }
}

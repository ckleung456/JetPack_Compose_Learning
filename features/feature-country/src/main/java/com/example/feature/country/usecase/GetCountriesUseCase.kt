package com.example.feature.country.usecase

import com.example.core.usecase.FlowUseCase
import com.example.core.usecase.UseCaseOutputWithStatus
import com.example.feature.country.model.domain.Country
import com.example.feature.country.model.domain.CountryItem
import com.example.feature.country.module.network.CountryRepository
import dagger.hilt.android.scopes.ViewModelScoped
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject
import kotlin.collections.component1
import kotlin.collections.component2

@ViewModelScoped
class GetCountriesUseCase @Inject constructor(
    private val repository: CountryRepository
) : FlowUseCase<Unit, List<Country>, List<CountryItem>>() {
    override suspend fun flowWork(input: Unit): Flow<List<Country>> = repository.getCountries()

    override suspend fun onSucceedDataHandling(intermediate: List<Country>): UseCaseOutputWithStatus.Success<List<CountryItem>> {
        val grouped = intermediate
                .sortedBy { it.name }.groupBy { it.name?.first()?.uppercaseChar() }
        return UseCaseOutputWithStatus.Success(
            result = grouped.filter { it.key != null }.flatMap { (letter, countryList) ->
                listOf(
                    CountryItem.Letter(letter = letter.toString())
                ) + countryList.map { CountryItem.CountryInfo(country = it) }
            }
        )
    }
}
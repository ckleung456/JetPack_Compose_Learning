package com.example.myapplication.features.coutry.usecases

import com.example.core.usecase.FlowUseCase
import com.example.myapplication.features.coutry.model.country.CountryItem
import com.example.myapplication.features.coutry.module.network.CountryRepository
import dagger.hilt.android.scopes.ViewModelScoped
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flowOn
import kotlinx.coroutines.flow.map
import javax.inject.Inject

@ViewModelScoped
class GetCountriesUseCase @Inject constructor(
    private val repository: CountryRepository
) : FlowUseCase<Unit, List<CountryItem>>() {
    override suspend fun flowWork(input: Unit): Flow<List<CountryItem>> = repository
        .getCountries()
        .map { countries ->
            val grouped = countries
                .sortedBy { it.name }.groupBy { it.name?.first()?.uppercaseChar() }
            grouped.filter { it.key != null }.flatMap { (letter, countryList) ->
                listOf(
                    CountryItem.Letter(letter = letter.toString())
                ) + countryList.map { CountryItem.CountryInfo(country = it) }
            }
        }
        .flowOn(Dispatchers.IO)
}
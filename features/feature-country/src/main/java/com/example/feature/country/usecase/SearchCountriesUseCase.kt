package com.example.feature.country.usecase

import com.example.presentation.model.UIState
import com.example.shared.lib.FlowUseCase
import com.example.domain.usecase.UseCaseOutputWithStatus
import com.example.feature.country.model.domain.CountriesUIState
import com.example.feature.country.model.domain.CountryItem
import com.example.feature.country.model.domain.SearchInput
import dagger.hilt.android.scopes.ViewModelScoped
import kotlinx.collections.immutable.toPersistentList
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import javax.inject.Inject

@ViewModelScoped
class SearchCountriesUseCase @Inject constructor() : FlowUseCase<SearchInput, UIState<CountriesUIState>, UIState<CountriesUIState>>() {
    override suspend fun flowWork(input: SearchInput): Flow<UIState<CountriesUIState>> =
        flow {
            val currentState = input.currentState
            if (currentState is UIState.Success) {
                val query = input.query
                val searchState = currentState.data.searchState.copy(query = query)
                if (query.isBlank()) {
                    emit(
                        UIState.Success(
                            data = currentState.data.copy(
                                countries = input.countries.toPersistentList(),
                                searchState = searchState
                            )
                        )
                    )
                } else {
                    val filteredCountries = input.countries.filterIsInstance<CountryItem.CountryInfo>().filter { countryItem ->
                        val country = countryItem.country
                        country.name?.contains(query, ignoreCase = true) == true ||
                                country.code?.contains(query, ignoreCase = true) == true ||
                                country.capital?.contains(query, ignoreCase = true) == true
                    }.toPersistentList()
                    emit(
                        UIState.Success(
                            data = currentState.data.copy(
                                countries = filteredCountries,
                                searchState = searchState
                            )
                        )
                    )
                }
            } else {
                emit(currentState)
            }
        }


    override suspend fun onSucceedDataHandling(intermediate: UIState<CountriesUIState>): UseCaseOutputWithStatus.Success<UIState<CountriesUIState>> =
        UseCaseOutputWithStatus.Success(intermediate)
}

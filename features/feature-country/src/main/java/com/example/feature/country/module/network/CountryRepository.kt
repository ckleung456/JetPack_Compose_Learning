package com.example.feature.country.module.network

import com.example.feature.country.model.domain.Country
import dagger.hilt.android.scopes.ViewModelScoped
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject

@ViewModelScoped
class CountryRepository @Inject constructor(
    private val apis: CountryApis
) {
    fun getCountries(): Flow<List<Country>> = apis.fetchCountriesAPI()
}

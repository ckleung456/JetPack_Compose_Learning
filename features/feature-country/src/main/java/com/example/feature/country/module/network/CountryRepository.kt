package com.example.feature.country.module.network

import dagger.hilt.android.scopes.ViewModelScoped
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import javax.inject.Inject

@ViewModelScoped
class CountryRepository @Inject constructor(
    private val apis: CountryApis
) {
    suspend fun getCountries() = withContext(Dispatchers.IO) {
        apis.fetchCountriesAPI()
    }
}
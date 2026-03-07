package com.example.feature.country.module.network

import dagger.hilt.android.scopes.ViewModelScoped
import javax.inject.Inject

@ViewModelScoped
class CountryRepository @Inject constructor(
    private val apis: CountryApis
) {
    fun getCountries() = apis.fetchCountriesAPI()
}
package com.example.myapplication.features.coutry.module.network

import dagger.hilt.android.scopes.ViewModelScoped
import javax.inject.Inject

@ViewModelScoped
class CountryRepository @Inject constructor(
    private val apIs: CountryAPIs
) {
    fun getCountries() = apIs.fetchCountriesAPI()
}
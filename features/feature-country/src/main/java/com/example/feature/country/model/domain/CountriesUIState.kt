package com.example.feature.country.model.domain

data class CountriesUIState(
    val countries: List<CountryItem> = emptyList(),
    val searchState: SearchState = SearchState()
)

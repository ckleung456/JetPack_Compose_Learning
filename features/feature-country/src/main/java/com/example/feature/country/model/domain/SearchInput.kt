package com.example.feature.country.model.domain

import com.example.presentation.model.UIState

data class SearchInput(
    val query: String,
    val currentState: UIState<CountriesUIState>,
    val countries: List<CountryItem>
)

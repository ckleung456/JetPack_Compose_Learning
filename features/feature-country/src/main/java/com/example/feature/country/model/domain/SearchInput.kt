package com.example.feature.country.model.domain

import com.example.core.ui.model.UIState

data class SearchInput(
    val query: String,
    val currentState: UIState<CountriesUIState>,
    val countries: List<CountryItem>
)

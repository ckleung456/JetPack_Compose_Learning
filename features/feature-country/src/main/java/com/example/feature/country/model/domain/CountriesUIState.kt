package com.example.feature.country.model.domain

import androidx.compose.runtime.Immutable
import kotlinx.collections.immutable.ImmutableList
import kotlinx.collections.immutable.persistentListOf


@Immutable
data class CountriesUIState(
    val countries: ImmutableList<CountryItem> = persistentListOf(),
    val searchState: SearchState = SearchState()
)

package com.example.feature.country.ui.screens

import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.core.ui.EnhancedListItem
import com.example.core.ui.ErrorView
import com.example.core.ui.HeaderItem
import com.example.core.ui.UIStatefulContent
import com.example.core.utils.Utils.ObserveAsEvents
import com.example.feature.country.model.domain.Country
import com.example.feature.country.model.domain.CountryItem
import com.example.feature.country.ui.viewmodel.CountriesViewModel

@Composable
fun CountriesScreen(
    modifier: Modifier = Modifier,
    viewModel: CountriesViewModel = hiltViewModel(),
    onSelectedCountry: (Country) -> Unit
) {
    val state by viewModel.uiState.collectAsStateWithLifecycle()
    ObserveAsEvents(
        flow = viewModel.selectedCountry,
        key1 = "Country"
    ) { country ->
        country?.let {
            onSelectedCountry.invoke(it)
        }
    }

    UIStatefulContent(
        state = state,
        successContent = { countryItems ->
            CountriesListView(
                countries = countryItems,
                modifier = modifier
            ) {
                viewModel.onSelectedCountry(country = it)
            }
        },
        errorContent = { message, _ ->
            ErrorView(
                message = message
            ) {
                viewModel.getCountries()
            }
        }
    )
}

@Composable
private fun CountriesListView(
    modifier: Modifier = Modifier,
    countries: List<CountryItem>,
    onSelectedCountry: (Country) -> Unit
) {
    LazyColumn(
        modifier = modifier,
        contentPadding = PaddingValues(16.dp)
    ) {
        items(
            items = countries,
            key = { countryItem ->
                when (countryItem) {
                    is CountryItem.Letter -> countryItem.letter
                    is CountryItem.CountryInfo ->countryItem.country.code.orEmpty()
                }
            }
        ) { countryItem ->
            when(countryItem) {
                is CountryItem.Letter -> HeaderItem(
                    title = countryItem.letter
                )
                is CountryItem.CountryInfo -> {
                    val country = remember { countryItem.country }
                    EnhancedListItem(
                        title = country.name.orEmpty(),
                        description = country.code
                    ) {
                        onSelectedCountry.invoke(country)
                    }
                }
            }
        }
    }
}
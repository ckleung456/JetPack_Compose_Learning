package com.example.myapplication.features.coutry.ui.screens

import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.core.ui.EnhancedListItem
import com.example.core.ui.ErrorView
import com.example.core.ui.HeaderItem
import com.example.core.ui.UIStatefulContent
import com.example.myapplication.features.coutry.model.country.Country
import com.example.myapplication.features.coutry.model.country.CountryItem
import com.example.myapplication.features.coutry.ui.viewmodel.GetCountriesViewModel

@Composable
fun CountriesScreen(
    viewModel: GetCountriesViewModel = hiltViewModel(),
    modifier: Modifier = Modifier,
    onSelectedCountry: (Country) -> Unit
) {
    val state by viewModel.uiState.collectAsStateWithLifecycle()
    LaunchedEffect(Unit) {
        viewModel.selectedCountry.collect { country ->
            country?.let {
                onSelectedCountry.invoke(it)
            }
        }
    }

    UIStatefulContent(
        state = state,
        successContent = { countryItems ->
            CountriesListView(
                viewModel = viewModel,
                countries = countryItems,
                modifier = modifier
            )
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
fun CountriesListView(
    viewModel: GetCountriesViewModel,
    modifier: Modifier = Modifier,
    countries: List<CountryItem>
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
                is CountryItem.CountryInfo -> EnhancedListItem(
                    title = countryItem.country.name.orEmpty(),
                    description = countryItem.country.code
                ) {
                    viewModel.onSelectedCountry(country = countryItem.country)
                }
            }
        }
    }
}
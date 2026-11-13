package com.example.feature.country.ui.screens

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.pulltorefresh.PullToRefreshBox
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.presentation.AppTopBar
import com.example.presentation.EnhancedListItem
import com.example.presentation.ErrorView
import com.example.presentation.HeaderItem
import com.example.presentation.SearchBar
import com.example.presentation.UIStatefulContent
import com.example.feature.country.R
import com.example.feature.country.model.domain.CountryItem
import com.example.feature.country.model.domain.CountryUi
import com.example.feature.country.ui.viewmodel.CountriesViewModel
import com.example.shared.lib.ObserveAsEvents

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CountriesScreen(
    modifier: Modifier = Modifier,
    viewModel: CountriesViewModel = hiltViewModel(),
    onSelectedCountry: (CountryUi) -> Unit
) {
    val state by viewModel.uiState.collectAsStateWithLifecycle()
    var isRefreshing by remember { mutableStateOf(false) }
    val title = stringResource(R.string.title_countries)

    ObserveAsEvents(
        flow = viewModel.selectedCountry,
        key1 = "Country"
    ) { country ->
        country?.let {
            onSelectedCountry.invoke(it)
        }
    }

    Scaffold(
        modifier = modifier,
        topBar = {
            AppTopBar(
                title = title,
                actions = {
                    IconButton(onClick = { viewModel.triggerSearch() }) {
                        Icon(
                            imageVector = Icons.Default.Search,
                            contentDescription = stringResource(R.string.cd_search)
                        )
                    }
                }
            )
        }
    ) { innerPadding ->
        Box(modifier = modifier.padding(innerPadding)) {
            UIStatefulContent(
                state = state,
                loadingContent = {
                    isRefreshing = true
                },
                successContent = { successState ->
                    PullToRefreshBox(
                        isRefreshing = isRefreshing,
                        onRefresh = {
                            viewModel.getCountries()
                        }
                    ) {
                        isRefreshing = false
                        Column {
                            AnimatedVisibility(visible = successState.searchState.isActive) {
                                SearchBar(
                                    modifier = Modifier.fillMaxWidth().padding(8.dp),
                                    query = successState.searchState.query,
                                    onQueryChange = {
                                        viewModel.updateSearchQuery(it)
                                    },
                                    onClearClick = {
                                        viewModel.clearSearchQuery()
                                    },
                                    placeholder = stringResource(R.string.hint_search)
                                )
                            }

                            CountriesListView(
                                modifier = modifier,
                                countries = successState.countries
                            ) {
                                viewModel.onSelectedCountry(country = it)
                            }
                        }
                    }
                },
                errorContent = { message, _ ->
                    isRefreshing = false
                    ErrorView(
                        message = message
                    ) {
                        viewModel.getCountries()
                    }
                }
            )
        }
    }
}

@Composable
private fun CountriesListView(
    modifier: Modifier = Modifier,
    countries: List<CountryItem>,
    onSelectedCountry: (CountryUi) -> Unit
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
                    val country = remember(countryItem) { countryItem.country }
                    val onCountryClick = remember(countryItem) {
                        {
                            onSelectedCountry.invoke(country)
                        }
                    }
                    EnhancedListItem(
                        modifier = modifier,
                        title = country.name.orEmpty(),
                        description = country.code,
                        onClick = onCountryClick
                    )
                }
            }
        }
    }
}

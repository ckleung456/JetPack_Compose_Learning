package com.example.feature.country.ui.screens

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.pulltorefresh.PullToRefreshBox
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.core.ui.EnhancedListItem
import com.example.core.ui.ErrorView
import com.example.core.ui.HeaderItem
import com.example.core.ui.SearchBar
import com.example.core.ui.TopBarStateManager
import com.example.core.ui.UIStatefulContent
import com.example.core.ui.model.TopBarAction
import com.example.core.utils.Utils.ObserveAsEvents
import com.example.feature.country.R
import com.example.feature.country.model.domain.Country
import com.example.feature.country.model.domain.CountryItem
import com.example.feature.country.ui.viewmodel.CountriesViewModel

@Composable
fun CountriesScreen(
    modifier: Modifier = Modifier,
    viewModel: CountriesViewModel = hiltViewModel(),
    onSelectedCountry: (Country) -> Unit
) {
    val topBarStateManager = TopBarStateManager.LocalTopBarStateManager.current
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
        topBarStateManager.resetToDefault(title = title)
    }
    LaunchedEffect(Unit) {
        topBarStateManager.updateConfig(
            title = title,
            navigationIconEnabled = false,
            actions = listOf(
                TopBarAction(
                    icon = Icons.Default.Search,
                    contentDescription = "Search",
                    onClick = {
                        viewModel.triggerSearch()
                    }
                )
            )
        )
    }

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
                },
                modifier = modifier
            ) {
                isRefreshing = false
                Column(
                    modifier = modifier
                ) {
                    if (successState.searchState.isActive) {
                        Spacer(modifier = Modifier.height(8.dp))
                    }
                    AnimatedVisibility(visible = successState.searchState.isActive) {
                        SearchBar(
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
                        countries = successState.countries,
                        modifier = modifier
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
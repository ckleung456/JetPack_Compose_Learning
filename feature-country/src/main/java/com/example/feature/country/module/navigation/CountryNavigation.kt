package com.example.feature.country.module.navigation

import androidx.compose.runtime.Composable
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.navigation3.runtime.EntryProviderScope
import androidx.navigation3.runtime.NavBackStack
import androidx.navigation3.runtime.NavKey
import com.example.core.navigation.model.Route
import com.example.core.navigation.module.Navigator
import com.example.feature.country.model.domain.Country
import com.example.feature.country.ui.screens.CountriesScreen
import com.example.feature.country.ui.screens.CountryDetailScreen
import com.example.feature.country.ui.viewmodel.CountryDetailViewModel

private const val COUNTRY_DETAIL_KEY = "countryDetailKey"

@Composable
fun EntryProviderScope<NavKey>.CountryNavEntries(
    navigator: Navigator
) {
    entry<Route.Country.Countries> {
        CountriesScreen {
            navigator.navigate(
                Route.Country.CountryDetail(
                    detailArgument = mapOf(
                        COUNTRY_DETAIL_KEY to it
                    )
                )
            )
        }
    }
    entry<Route.Country.CountryDetail> {
        CountryDetailScreen(
            viewModel = hiltViewModel<CountryDetailViewModel, CountryDetailViewModel.Factory> { factory ->
                val country = it.detailArgument.getValue(COUNTRY_DETAIL_KEY) as? Country
                factory.create(
                    country = country
                )
            }
        )
    }
}

@Composable
fun EntryProviderScope<NavKey>.CountryNavEntriesWithoutBottomBar(
    backStack: NavBackStack<NavKey>
) {
    entry<Route.Country.Countries> {
        CountriesScreen {
            backStack.add(
                Route.Country.CountryDetail(
                    detailArgument = mapOf(
                        COUNTRY_DETAIL_KEY to it
                    )
                )
            )
        }
    }
    entry<Route.Country.CountryDetail> {
        CountryDetailScreen(
            viewModel = hiltViewModel<CountryDetailViewModel, CountryDetailViewModel.Factory> { factory ->
                val country = it.detailArgument.getValue(COUNTRY_DETAIL_KEY) as? Country
                factory.create(
                    country = country
                )
            }
        )
    }
}

fun countryScreensList() = listOf(
    Route.Country.Countries::class,
    Route.Country.CountryDetail::class
)
package com.example.myapplication.features.coutry.module.navigation

import androidx.compose.runtime.Composable
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.navigation3.runtime.EntryProviderScope
import androidx.navigation3.runtime.NavBackStack
import androidx.navigation3.runtime.NavKey
import com.example.core.navigation.module.Navigator
import com.example.myapplication.features.coutry.ui.screens.CountriesScreen
import com.example.myapplication.features.coutry.ui.screens.CountryScreen
import com.example.myapplication.features.coutry.ui.viewmodel.CountryDetailViewModel
import com.example.myapplication.module.navigation.Route

@Composable
fun EntryProviderScope<NavKey>.CountryNavEntries(
    navigator: Navigator
) {
    entry<Route.Country.Countries> {
        CountriesScreen {
            navigator.navigate(Route.Country.CountryDetail(country = it))
        }
    }
    entry<Route.Country.CountryDetail> {
        CountryScreen(
            viewModel = hiltViewModel<CountryDetailViewModel, CountryDetailViewModel.Factory> { factory ->
                factory.create(country = it.country)
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
            backStack.add(Route.Country.CountryDetail(country = it))
        }
    }
    entry<Route.Country.CountryDetail> {
        CountryScreen(
            viewModel = hiltViewModel<CountryDetailViewModel, CountryDetailViewModel.Factory> { factory ->
                factory.create(country = it.country)
            }
        )
    }
}
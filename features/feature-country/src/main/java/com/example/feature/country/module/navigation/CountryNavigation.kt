package com.example.feature.country.module.navigation

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Checklist
import androidx.compose.runtime.Composable
import androidx.compose.ui.res.stringResource
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.navigation3.runtime.EntryProviderScope
import androidx.navigation3.runtime.NavBackStack
import androidx.navigation3.runtime.NavKey
import com.example.core.navigation.model.BottomNavItem
import com.example.core.navigation.module.Navigator
import com.example.feature.country.R
import com.example.feature.country.model.domain.Country
import com.example.feature.country.model.domain.CountryRoute
import com.example.feature.country.ui.screens.CountriesScreen
import com.example.feature.country.ui.screens.CountryDetailScreen
import com.example.feature.country.ui.viewmodel.CountryDetailViewModel

private const val COUNTRY_DETAIL_KEY = "countryDetailKey"

@Composable
fun EntryProviderScope<NavKey>.CountryNavEntries(
    navigator: Navigator
) {
    entry<CountryRoute.Countries> {
        CountriesScreen {
            navigator.navigate(
                CountryRoute.CountryDetail(
                    detailArgument = mapOf(
                        COUNTRY_DETAIL_KEY to it
                    )
                )
            )
        }
    }
    entry<CountryRoute.CountryDetail> {
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
    entry<CountryRoute.Countries> {
        CountriesScreen {
            backStack.add(
                CountryRoute.CountryDetail(
                    detailArgument = mapOf(
                        COUNTRY_DETAIL_KEY to it
                    )
                )
            )
        }
    }
    entry<CountryRoute.CountryDetail> {
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
    CountryRoute.Countries::class,
    CountryRoute.CountryDetail::class
)

@Composable
fun countryRouteTopDestination(): Pair<NavKey, BottomNavItem> = Pair(
    CountryRoute.Countries, BottomNavItem(
        icon = Icons.Outlined.Checklist,
        title = stringResource(R.string.title_countries)
    )
)
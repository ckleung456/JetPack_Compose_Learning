package com.example.myapplication.features.coutry.navigation

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.navigation3.runtime.NavEntry
import androidx.navigation3.runtime.NavKey
import androidx.navigation3.runtime.entryProvider
import androidx.navigation3.runtime.rememberNavBackStack
import androidx.navigation3.ui.NavDisplay
import com.example.core.navigation.module.Navigator
import com.example.myapplication.features.coutry.ui.screens.CountriesScreen
import com.example.myapplication.features.coutry.ui.screens.CountryScreen
import com.example.myapplication.features.coutry.ui.viewmodel.CountryDetailViewModel
import com.example.myapplication.module.navigation.Route

@Composable
fun countryNavigation(
    navigator: Navigator,

) {

//    val countryBackStack = rememberNavBackStack(
//        Route.Country.Countries
//    )
//    NavDisplay(
//        backStack = countryBackStack,
//        modifier = modifier,
//        onBack = {
//            countryBackStack.removeLastOrNull()
//        },
//        entryProvider = entryProvider {
//            entry<Route.Country.Countries> {
//                CountriesScreen {
//                    countryBackStack.add(Route.Country.CountryDetail(country = it))
////                    navigator.navigate(Route.CountryRoute(country = it))
//                }
//            }
//            entry<Route.Country.CountryDetail> {
//                CountryScreen(
//                    viewModel = hiltViewModel<CountryDetailViewModel, CountryDetailViewModel.Factory> { factory ->
//                        factory.create(country = it.country)
//                    }
//                )
//            }
//        }
//    )
}
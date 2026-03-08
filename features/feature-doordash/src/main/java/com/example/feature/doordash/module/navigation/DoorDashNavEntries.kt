package com.example.feature.doordash.module.navigation

import androidx.compose.runtime.Composable
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.navigation3.runtime.EntryProviderScope
import androidx.navigation3.runtime.NavKey
import com.example.core.navigation.module.Navigator
import com.example.core.navigation.model.Route
import com.example.feature.doordash.ui.screen.RestaurantDetailScreen
import com.example.feature.doordash.ui.screen.RestaurantScreen
import com.example.feature.doordash.ui.viewmodel.RestaurantDetailViewModel


@Composable
fun EntryProviderScope<NavKey>.DoorDashNavEntries(
    navigator: Navigator
) {
    entry<Route.DoorDash.Restaurants> {
        RestaurantScreen {
            navigator.navigate(
                Route.DoorDash.RestaurantDetail(
                    restaurantId = it
                )
            )
        }
    }
    entry<Route.DoorDash.RestaurantDetail> {
        RestaurantDetailScreen(
            viewModel = hiltViewModel<RestaurantDetailViewModel, RestaurantDetailViewModel.Factory> { factory ->
                factory.create(
                    restaurantId = it.restaurantId
                )
            }
        )
    }
}

fun doordashScreensList() = listOf(
    Route.DoorDash.Restaurants::class,
    Route.DoorDash.RestaurantDetail::class
)
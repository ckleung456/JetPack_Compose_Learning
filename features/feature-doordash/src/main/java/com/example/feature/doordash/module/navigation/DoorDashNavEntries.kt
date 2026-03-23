package com.example.feature.doordash.module.navigation

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Fastfood
import androidx.compose.runtime.Composable
import androidx.compose.ui.res.stringResource
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.navigation3.runtime.EntryProviderScope
import androidx.navigation3.runtime.NavKey
import com.example.core.navigation.model.BottomNavItem
import com.example.core.navigation.module.Navigator
import com.example.feature.doordash.model.domain.DoordashRoute
import com.example.feature.doordash.ui.screen.RestaurantDetailScreen
import com.example.feature.doordash.ui.screen.RestaurantScreen
import com.example.feature.doordash.ui.viewmodel.RestaurantDetailViewModel


@Composable
fun EntryProviderScope<NavKey>.DoorDashNavEntries(
    navigator: Navigator
) {
    entry<DoordashRoute.Restaurants> {
        RestaurantScreen {
            navigator.navigate(
                DoordashRoute.RestaurantDetail(
                    restaurantId = it
                )
            )
        }
    }
    entry<DoordashRoute.RestaurantDetail> {
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
    DoordashRoute.Restaurants::class,
    DoordashRoute.RestaurantDetail::class
)

@Composable
fun doordashRouteTopDestination(): Pair<NavKey, BottomNavItem> = Pair(
    DoordashRoute.Restaurants,
    BottomNavItem(
        icon = Icons.Outlined.Fastfood,
        title = stringResource(com.example.core.R.string.title_doordash)
    )
)
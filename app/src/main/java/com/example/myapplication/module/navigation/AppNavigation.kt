package com.example.myapplication.module.navigation

import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.navigation3.runtime.NavKey
import androidx.navigation3.runtime.entryProvider
import androidx.navigation3.ui.NavDisplay
import com.example.core.navigation.model.BottomNavItem
import com.example.core.navigation.model.getSerializersConfig
import com.example.core.navigation.module.Navigator
import com.example.core.navigation.module.rememberNavigationState
import com.example.core.navigation.module.toEntries
import com.example.core.ui.AppNavigationBar
import com.example.feature.country.model.domain.CountryRoute
import com.example.feature.country.module.navigation.CountryNavEntries
import com.example.feature.country.module.navigation.countryRouteTopDestination
import com.example.feature.country.module.navigation.countryScreensList
import com.example.feature.doordash.module.navigation.DoorDashNavEntries
import com.example.feature.doordash.module.navigation.doordashRouteTopDestination
import com.example.feature.doordash.module.navigation.doordashScreensList

@Composable
fun AppFeatureNavigation(
    modifier: Modifier = Modifier
) {
    val topDestinations = listOf<Pair<NavKey, BottomNavItem>>(
        countryRouteTopDestination(),
        doordashRouteTopDestination()
    ).toMap()
    val navigationState = rememberNavigationState(
        startRoute = CountryRoute.Countries,
        topLevelRoutes = topDestinations.keys,
        serializableConfig = getSerializersConfig(
            screens = countryScreensList() +
                    doordashScreensList()
        )
    )
    val navigator = remember {
        Navigator(navigationState)
    }

    Scaffold(
        modifier = modifier,
        bottomBar = {
            AppNavigationBar(
                selectedKey = CountryRoute.Countries,
                items = topDestinations,
                onSelectKey = {
                    navigator.navigate(it)
                }
            )
        }
    ) { innerPadding ->
        NavDisplay(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding),
            onBack = {
                navigator.goBack()
            },
            entries = navigationState.toEntries(
                entryProvider {
                    CountryNavEntries(navigator = navigator)
                    DoorDashNavEntries(navigator = navigator)
                }
            )
        )
    }
}

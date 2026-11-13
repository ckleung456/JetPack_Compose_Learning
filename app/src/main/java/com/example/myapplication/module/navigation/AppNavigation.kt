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
import com.example.presentation.navigation.model.BottomNavItem
import com.example.presentation.navigation.model.getSerializersConfig
import com.example.presentation.navigation.module.Navigator
import com.example.presentation.navigation.module.rememberNavigationState
import com.example.presentation.navigation.module.toEntries
import com.example.presentation.AppNavigationBar
import com.example.feature.business.module.navigation.BusinessNavEntries
import com.example.feature.business.module.navigation.businessRouteTopDestination
import com.example.feature.business.module.navigation.businessScreensList
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
        doordashRouteTopDestination(),
        businessRouteTopDestination()
    ).toMap()
    val navigationState = rememberNavigationState(
        startRoute = CountryRoute.Countries,
        topLevelRoutes = topDestinations.keys,
        serializableConfig = getSerializersConfig(
            screens = countryScreensList() +
                    doordashScreensList() +
                    businessScreensList()
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
                    BusinessNavEntries(navigator = navigator)
                }
            )
        )
    }
}

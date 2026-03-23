package com.example.myapplication.module.navigation

import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBackIosNew
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.lifecycle.viewmodel.navigation3.rememberViewModelStoreNavEntryDecorator
import androidx.navigation3.runtime.NavKey
import androidx.navigation3.runtime.entryProvider
import androidx.navigation3.runtime.rememberNavBackStack
import androidx.navigation3.runtime.rememberSaveableStateHolderNavEntryDecorator
import androidx.navigation3.ui.NavDisplay
import com.example.core.navigation.model.BottomNavItem
import com.example.core.navigation.model.getSerializersConfig
import com.example.core.navigation.module.Navigator
import com.example.core.navigation.module.rememberNavigationState
import com.example.core.navigation.module.toEntries
import com.example.core.ui.AppNavigationBar
import com.example.feature.country.model.domain.CountryRoute
import com.example.feature.country.module.navigation.CountryNavEntries
import com.example.feature.country.module.navigation.CountryNavEntriesWithoutBottomBar
import com.example.feature.country.module.navigation.countryRouteTopDestination
import com.example.feature.country.module.navigation.countryScreensList
import com.example.feature.doordash.module.navigation.DoorDashNavEntries
import com.example.feature.doordash.module.navigation.doordashRouteTopDestination
import com.example.feature.doordash.module.navigation.doordashScreensList
import com.example.myapplication.R

@OptIn(ExperimentalMaterial3Api::class)
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
        },
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        text = stringResource(R.string.app_name)
                    )
                },
                navigationIcon = {
                    IconButton(onClick = { navigator.goBack() }) {
                        Icon(
                            imageVector = Icons.Default.ArrowBackIosNew,
                            contentDescription = "BACK",
                            tint = Color.White
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = Color.Blue,
                    titleContentColor = Color.White
                )
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

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AppWithNoBottomBar(
    modifier: Modifier = Modifier
) {
    val backStack = rememberNavBackStack(
        CountryRoute.Countries
    )
    Scaffold(
        modifier = modifier,
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        text = stringResource(R.string.app_name)
                    )
                },
                navigationIcon = {
                    IconButton(onClick = {
                        backStack.removeLastOrNull()
                    }) {
                        Icon(
                            imageVector = Icons.Default.ArrowBackIosNew,
                            contentDescription = "BACK",
                            tint = Color.White
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = Color.Blue,
                    titleContentColor = Color.White
                )
            )
        }
    ) { innerPadding ->
        NavDisplay(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding),
            backStack = backStack,
            entryDecorators = listOf(
                rememberSaveableStateHolderNavEntryDecorator(),
                rememberViewModelStoreNavEntryDecorator()
            ),
            entryProvider = entryProvider {
                CountryNavEntriesWithoutBottomBar(backStack = backStack)
            }
        )
    }
}
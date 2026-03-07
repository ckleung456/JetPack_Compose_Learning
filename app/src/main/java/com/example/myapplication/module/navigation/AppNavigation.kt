package com.example.myapplication.module.navigation

import androidx.compose.foundation.layout.Box
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
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.lifecycle.viewmodel.navigation3.rememberViewModelStoreNavEntryDecorator
import androidx.navigation3.runtime.entryProvider
import androidx.navigation3.runtime.rememberNavBackStack
import androidx.navigation3.runtime.rememberSaveableStateHolderNavEntryDecorator
import androidx.navigation3.ui.NavDisplay
import com.example.core.navigation.module.Navigator
import com.example.core.navigation.module.rememberNavigationState
import com.example.core.navigation.module.toEntries
import com.example.core.ui.AppNavigationBar
import com.example.myapplication.R
import com.example.myapplication.features.coutry.ui.screens.CountriesScreen
import com.example.myapplication.features.coutry.ui.screens.CountryScreen
import com.example.myapplication.features.coutry.ui.viewmodel.CountryDetailViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CountryFeatureNavigation(
    modifier: Modifier = Modifier
) {
    val navigationState = rememberNavigationState(
        startRoute = Route.Country.Countries,
        topLevelRoutes = topLevelDestinations().keys,
        serializableConfig = serializersConfig
    )
    val navigator = remember {
        Navigator(navigationState)
    }

    Scaffold(
        modifier = modifier,
        bottomBar = {
            AppNavigationBar(
                selectedKey = Route.Country.Countries,
                items = topLevelDestinations(),
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
                    entry<Route.DoorDash> {
                        Box(
                            modifier = Modifier
                                .fillMaxSize(),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(text = stringResource(R.string.title_doordash))
                        }
                    }
                }
            )
        )
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun appWithNoBottomBar(
    modifier: Modifier = Modifier
) {
    val backStack = rememberNavBackStack(
        Route.Country.Countries
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
//                entry<Route.CountriesRoute> {
//                    CountriesScreen {
//                        backStack.add(Route.CountryRoute(country = it))
//                    }
//                }
//                entry<Route.CountryRoute> {
//                    CountryScreen(
//                        viewModel = hiltViewModel<CountryDetailViewModel, CountryDetailViewModel.Factory> { factory ->
//                            factory.create(country = it.country)
//                        }
//                    )
//                }
            }
        )
    }
}
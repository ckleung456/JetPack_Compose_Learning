package com.example.feature.business.module.navigation

import android.util.Log
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Business
import androidx.compose.runtime.Composable
import androidx.compose.ui.res.stringResource
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.navigation3.runtime.EntryProviderScope
import androidx.navigation3.runtime.NavKey
import com.example.presentation.navigation.model.BottomNavItem
import com.example.presentation.navigation.module.Navigator
import com.example.feature.business.R
import com.example.feature.business.model.domain.BusinessRoute
import com.example.feature.business.ui.screens.BusinessDetailScreen
import com.example.feature.business.ui.screens.BusinessesScreen
import com.example.feature.business.ui.viewmodel.BusinessDetailViewModel


@Composable
fun EntryProviderScope<NavKey>.BusinessNavEntries(
    navigator: Navigator
) {
    entry<BusinessRoute.Businesses> {
        BusinessesScreen {
            Log.w("nav", "business selected: $it")
            navigator.navigate(
                BusinessRoute.BusinessDetail(
                    detailId = it
                )
            )
        }
    }
    entry<BusinessRoute.BusinessDetail> {
        BusinessDetailScreen(
            onBackClick = { navigator.goBack() },
            viewModel = hiltViewModel<BusinessDetailViewModel, BusinessDetailViewModel.Factory> { factory ->
                factory.create(businessId = it.detailId)
            }
        )
    }
}

fun businessScreensList() = listOf(
    BusinessRoute.Businesses::class,
    BusinessRoute.BusinessDetail::class
)

@Composable
fun businessRouteTopDestination(): Pair<NavKey, BottomNavItem> = Pair(
    BusinessRoute.Businesses, BottomNavItem(
        icon = Icons.Outlined.Business,
        title = stringResource(R.string.title_business)
    )
)
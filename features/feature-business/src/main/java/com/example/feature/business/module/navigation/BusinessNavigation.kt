package com.example.feature.business.module.navigation

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Business
import androidx.compose.runtime.Composable
import androidx.compose.ui.res.stringResource
import androidx.navigation3.runtime.EntryProviderScope
import androidx.navigation3.runtime.NavKey
import com.example.core.navigation.model.BottomNavItem
import com.example.core.navigation.module.Navigator
import com.example.feature.business.R
import com.example.feature.business.model.domain.BusinessRoute

private const val BUSINESS_DETAIL_KEY = "businessDetailKey"

@Composable
fun EntryProviderScope<NavKey>.BusinessNavEntries(
    navigator: Navigator,
) {
    entry<BusinessRoute.Businesses> {  }
    entry<BusinessRoute.BusinessDetail> {  }
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
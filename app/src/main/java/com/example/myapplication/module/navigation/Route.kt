package com.example.myapplication.module.navigation

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Checklist
import androidx.compose.material.icons.outlined.Fastfood
import androidx.compose.runtime.Composable
import androidx.compose.ui.res.stringResource
import androidx.navigation3.runtime.NavKey
import androidx.savedstate.serialization.SavedStateConfiguration
import com.example.core.navigation.model.BottomNavItem
import com.example.myapplication.R
import kotlinx.serialization.Serializable
import kotlinx.serialization.modules.SerializersModule
import kotlinx.serialization.modules.polymorphic

@Serializable
sealed interface Route : NavKey {

    @Serializable
    data object Country : Route {
        @Serializable
        data object Countries : Route

        @Serializable
        data class CountryDetail(
            val country: com.example.myapplication.features.coutry.model.country.Country
        ) : Route
    }

    @Serializable
    data object DoorDash : Route
}

val serializersConfig = SavedStateConfiguration {
    serializersModule = SerializersModule {
        polymorphic(NavKey::class) {
            subclass(Route.Country.Countries::class, Route.Country.Countries.serializer())
            subclass(Route.Country.CountryDetail::class, Route.Country.CountryDetail.serializer())
            subclass(Route.DoorDash::class, Route.DoorDash.serializer())
        }
    }
}

@Composable
fun topLevelDestinations() : Map<NavKey, BottomNavItem> = mapOf(
    Route.Country.Countries to BottomNavItem(
        icon = Icons.Outlined.Checklist,
        title = stringResource(R.string.title_countries)
    ),
    Route.DoorDash to BottomNavItem(
        icon = Icons.Outlined.Fastfood,
        title = stringResource(R.string.title_doordash)
    )
)
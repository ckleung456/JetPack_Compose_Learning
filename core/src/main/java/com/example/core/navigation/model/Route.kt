package com.example.core.navigation.model

import android.os.Parcelable
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Checklist
import androidx.compose.material.icons.outlined.Fastfood
import androidx.compose.runtime.Composable
import androidx.compose.ui.res.stringResource
import androidx.navigation3.runtime.NavKey
import androidx.savedstate.serialization.SavedStateConfiguration
import com.example.core.R
import kotlinx.serialization.InternalSerializationApi
import kotlinx.serialization.Serializable
import kotlinx.serialization.modules.SerializersModule
import kotlinx.serialization.modules.SerializersModuleBuilder
import kotlinx.serialization.modules.polymorphic
import kotlinx.serialization.serializer
import kotlin.reflect.KClass

@Serializable
sealed interface Route : NavKey {

    @Serializable
    data object Country : Route {
        @Serializable
        data object Countries : Route

        @Serializable
        data class CountryDetail(
            val detailArgument: Map<String, Parcelable>
        ) : Route
    }

    @Serializable
    data object DoorDash : Route {
        @Serializable
        data object Restaurants : Route

        @Serializable
        data class RestaurantDetail(
            val restaurantId: Long
        ) : Route
    }
}

@OptIn(InternalSerializationApi::class)
fun SerializersModuleBuilder.addPolymorphicScreens(
    screens: List<KClass<out NavKey>>
) {
    screens.forEach { screenClass ->
        // Add polymorphic mapping for each screen class
        polymorphic(NavKey::class) {
            @Suppress("UNCHECKED_CAST")
            val typeSafeClass = screenClass as KClass<NavKey>
            subclass(typeSafeClass, typeSafeClass.serializer())
        }
    }
}

fun getSerializersConfig(
    screens: List<KClass<out NavKey>> = emptyList(),
): SavedStateConfiguration {
    return SavedStateConfiguration {
        serializersModule = SerializersModule {
            addPolymorphicScreens(screens = screens)
        }
    }
}

@Composable
fun topLevelDestinations(
    moreDestinations: Map<NavKey, BottomNavItem> = emptyMap()
) : Map<NavKey, BottomNavItem> = mapOf(
    Route.Country.Countries to BottomNavItem(
        icon = Icons.Outlined.Checklist,
        title = stringResource(R.string.title_countries)
    ),
    Route.DoorDash.Restaurants to BottomNavItem(
        icon = Icons.Outlined.Fastfood,
        title = stringResource(R.string.title_doordash)
    )
) + moreDestinations
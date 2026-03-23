package com.example.feature.doordash.model.domain

import androidx.navigation3.runtime.NavKey
import kotlinx.serialization.Serializable

@Serializable
sealed interface DoordashRoute : NavKey {
    @Serializable
    data object Restaurants : DoordashRoute

    @Serializable
    data class RestaurantDetail(
        val restaurantId: Long
    ) : DoordashRoute
}
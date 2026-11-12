package com.example.feature.business.model.domain

import androidx.navigation3.runtime.NavKey
import kotlinx.serialization.Serializable

@Serializable
sealed interface BusinessRoute : NavKey {

    @Serializable
    data object Businesses: BusinessRoute

    @Serializable
    data class BusinessDetail(
        val detailId: () -> Unit
    ) : BusinessRoute
}
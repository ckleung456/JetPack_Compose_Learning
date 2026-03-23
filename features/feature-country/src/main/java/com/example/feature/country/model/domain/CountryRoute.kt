package com.example.feature.country.model.domain

import android.os.Parcelable
import androidx.navigation3.runtime.NavKey
import kotlinx.serialization.Serializable

@Serializable
sealed interface CountryRoute : NavKey {
    @Serializable
    data object Countries : CountryRoute

    @Serializable
    data class CountryDetail(
        val detailArgument: Map<String, Parcelable>
    ) : CountryRoute
}
package com.example.feature.business.model.domain

import androidx.compose.runtime.Immutable
import kotlinx.collections.immutable.ImmutableList
import kotlinx.collections.immutable.ImmutableSet
import kotlinx.collections.immutable.persistentListOf
import kotlinx.collections.immutable.persistentSetOf
import java.util.Locale

@Immutable
data class BusinessUi(
    val id: String,
    val name:String,
    val imageUrl: String?,
    val ratingText: String,
    val reviewCount: Int,
    val priceText: String?,
    val distanceText: String?,
    val address: String,
    val likeStatus: BusinessLikeStatus = BusinessLikeStatus.UNLIKED
)

@Immutable
data class BusinessSectionUi(
    val category: BusinessCategory,
    val businesses: ImmutableList<BusinessUi>
)

@Immutable
data class NearByBusinessesUi(
    val pizza: ImmutableList<BusinessUi> = persistentListOf(),
    val beer: ImmutableList<BusinessUi> = persistentListOf(),
    val failedCategories: ImmutableSet<BusinessCategory> = persistentSetOf()
)

enum class BusinessLikeStatus {
    LIKED, UNLIKED
}

internal const val METERS_PER_MILE = 1609.344

fun Business.toBusinessUi(): BusinessUi = BusinessUi(
    id = id,
    name = name,
    imageUrl = imageUrl,
    ratingText = String.format(Locale.getDefault(), "%.1f", rating),
    reviewCount = reviewCount,
    priceText = price,
    distanceText = distanceMeters?.let { String.format(Locale.getDefault(), "%.1f mi", it / METERS_PER_MILE) },
    address = displayAddress
)
package com.example.feature.business.model.domain

data class BuildBusinessSectionsInput(
    val businesses: NearByBusinessesUi,
    val filter: BusinessFilter,
    val searchQuery: String,
    val likedBusinessIds: Set<String> = emptySet()
)

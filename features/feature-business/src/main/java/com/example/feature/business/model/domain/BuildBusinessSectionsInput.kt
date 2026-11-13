package com.example.feature.business.model.domain

data class BuildBusinessSectionsInput(
    val businesses: NearyByBusinessesUi,
    val filter: BusinessFilter,
    val searchQuery: String
)

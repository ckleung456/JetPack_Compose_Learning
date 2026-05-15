package com.example.feature.country.model.domain

import androidx.compose.runtime.Immutable

@Immutable
data class SearchState(
    val isActive: Boolean = false,
    val query: String = ""
)

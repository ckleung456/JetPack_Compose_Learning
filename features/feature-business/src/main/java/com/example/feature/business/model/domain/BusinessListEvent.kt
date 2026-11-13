package com.example.feature.business.model.domain

import androidx.annotation.StringRes

sealed interface BusinessListEvent {
    data class ShowSnackbar(val message: String? = null, @StringRes val messageId: Int? = null): BusinessListEvent
    data class NavigateToDetail(val id: String): BusinessListEvent
    data object ShowLocationPermissionDenied: BusinessListEvent
    data object RequestLocationPermission: BusinessListEvent
}
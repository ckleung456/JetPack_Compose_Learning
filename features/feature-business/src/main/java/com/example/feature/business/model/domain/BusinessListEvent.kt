package com.example.feature.business.model.domain

sealed interface BusinessListEvent {
    data class ShowSnackbar(val message: String? = null, val messageId: Int? = 0): BusinessListEvent
    data class NavigateToDetail(val id: String): BusinessListEvent
    data object ShowLocationPermissionDenied: BusinessListEvent
    data object RequestLocationPermission: BusinessListEvent
}
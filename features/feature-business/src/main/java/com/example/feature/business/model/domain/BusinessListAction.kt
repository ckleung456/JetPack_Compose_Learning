package com.example.feature.business.model.domain

sealed interface BusinessListAction {
    data class ShowSnackbar(val message: String? = null, val messageId: Int? = 0): BusinessListAction
    data class OnBusinessSelected(val id: String): BusinessListAction
    data class OnBusinessLikeClick(val businessUi: BusinessUi): BusinessListAction

    data class OnFilterSelect(val filter: BusinessFilter): BusinessListAction
    data object OnRefresh: BusinessListAction
    data object OnRetry: BusinessListAction

    data class OnSearchQueryUpdate(val query: String): BusinessListAction
    // location actions
    data class OnLocationPermissionResult(val granted: Boolean): BusinessListAction
    data object OnAddressClick: BusinessListAction
    data object OnChooseEnterAddress: BusinessListAction
    data class OnAddressTextChange(val address: String): BusinessListAction
    data object OnAddressConfirm: BusinessListAction
    data object OnChooseUseLocation: BusinessListAction
    // dialog
    data object OnDialogDismiss: BusinessListAction
}